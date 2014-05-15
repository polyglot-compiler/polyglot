/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2012 Polyglot project group, Cornell University
 * Copyright (c) 2006-2012 IBM Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program and the accompanying materials are made available under
 * the terms of the Lesser GNU Public License v2.0 which accompanies this
 * distribution.
 * 
 * The development of the Polyglot project has been supported by a
 * number of funding sources, including DARPA Contract F30602-99-1-0533,
 * monitored by USAF Rome Laboratory, ONR Grants N00014-01-1-0968 and
 * N00014-09-1-0652, NSF Grants CNS-0208642, CNS-0430161, CCF-0133302,
 * and CCF-1054172, AFRL Contract FA8650-10-C-7022, an Alfred P. Sloan 
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.visit;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.tools.JavaFileObject;

import polyglot.ast.Import;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.PackageNode;
import polyglot.ast.SourceCollection;
import polyglot.ast.SourceFile;
import polyglot.ast.TopLevelDecl;
import polyglot.frontend.Job;
import polyglot.frontend.TargetFactory;
import polyglot.types.Context;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.Copy;
import polyglot.util.ErrorInfo;
import polyglot.util.InternalCompilerError;

/**
 * A Translator generates output code from the processed AST. Output is sent to
 * one or more java file in the directory {@code Options.output_directory}.
 * Each SourceFile in the AST is output to exactly one java file. The name of
 * that file is determined as follows:
 * <ul>
 * <li>If the SourceFile has a declaration of a public top-level class "C", file
 * name is "C.java". It is an error for there to be more than one top-level
 * public declaration.
 * <li>If the SourceFile has no public declarations, the file name is the input
 * file name (e.g., "X.jl") with the suffix replaced with ".java" (thus,
 * "X.java").
 * </ul>
 * 
 * To use:
 * 
 * <pre>
 * new Translator(job, ts, nf, tf).translate(ast);
 * </pre>
 * 
 * The {@code ast} must be either a SourceFile or a SourceCollection.
 */
public class Translator extends PrettyPrinter implements Copy<Translator> {
    protected Job job;
    protected NodeFactory nf;
    protected TargetFactory tf;
    protected TypeSystem ts;

    /**
     * The current typing context, or null if type information is unavailable in
     * this subtree of the AST.
     */
    protected Context context;

    /**
     * Create a Translator. The output of the visitor is a collection of files
     * whose names are added to the collection {@code outputFiles}.
     */
    public Translator(Job job, TypeSystem ts, NodeFactory nf, TargetFactory tf) {
        super(nf.lang());
        this.job = job;
        this.nf = nf;
        this.tf = tf;
        this.ts = ts;
        this.context = ts.createContext();
    }

    /**
     * Return the job associated with this Translator.
     */
    public Job job() {
        return job;
    }

    /** Copy the translator. */
    @Override
    public Translator copy() {
        try {
            return (Translator) super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new InternalCompilerError("Java clone() weirdness.");
        }
    }

    /** Get the extension's type system. */
    public TypeSystem typeSystem() {
        return ts;
    }

    /** Get the extension's node factory. */
    public NodeFactory nodeFactory() {
        return nf;
    }

    /** Get the current typing context, or null. */
    public Context context() {
        return this.context;
    }

    /**
     * Create a new {@code Translator} identical to {@code this} but
     * with new context {@code c}
     */
    public Translator context(Context c) {
        if (c == this.context) {
            return this;
        }
        Translator tr = copy();
        tr.context = c;
        return tr;
    }

    /**
     * Print an AST node using the given code writer. This method should not be
     * called directly to translate a source file AST; use
     * {@code translate(Node)} instead. This method should only be called
     * by nodes to print their children.
     */
    @Override
    public void print(Node parent, Node child, CodeWriter w) {
        Translator tr = this;

        if (context != null) {
            if (child.isDisambiguated() && child.isTypeChecked()) {
                if (parent == null) {
                    Context c = lang().enterScope(child, context);
                    tr = this.context(c);
                }
                else if (parent.isDisambiguated() && parent.isTypeChecked()) {
                    Context c = lang().enterChildScope(parent, child, context);
                    tr = this.context(c);
                }
                else {
                    tr = this.context(null);
                }
            }
            else {
                tr = this.context(null);
            }
        }

        lang().translate(child, w, tr);

        if (context != null) {
            if (child.isDisambiguated() && child.isTypeChecked()) {
                lang().addDecls(child, context);
            }
        }
    }

    /** Translate the entire AST. */
    public boolean translate(Node ast) {
        if (ast instanceof SourceFile) {
            SourceFile sfn = (SourceFile) ast;
            return translateSource(sfn);
        }
        else if (ast instanceof SourceCollection) {
            SourceCollection sc = (SourceCollection) ast;

            boolean okay = true;

            for (SourceFile sfn : sc.sources()) {
                okay &= translateSource(sfn);
            }

            return okay;
        }
        else {
            throw new InternalCompilerError("AST root must be a SourceFile; "
                    + "found a " + ast.getClass().getName());
        }
    }

    /** Translate a single SourceFile node */
    protected boolean translateSource(SourceFile sf) {
        TargetFactory tf = this.tf;
        int outputWidth = job.compiler().outputWidth();
        Collection<JavaFileObject> outputFiles = job.compiler().outputFiles();

        PackageNode pkgNode = sf.package_();
        String pkg = pkgNode != null ? pkgNode.package_().fullName() : "";

        // Find the public declarations in the file. We'll use these to
        // derive the names of the target files. There will be one
        // target file per public declaration. If there are no public
        // declarations, we'll use the source file name to derive the
        // target file name.
        for (Map.Entry<String, List<TopLevelDecl>> fileEntry : filenames(sf).entrySet()) {
            String filename = fileEntry.getKey();
            List<TopLevelDecl> decls = fileEntry.getValue();

            JavaFileObject of;
            if (filename == null) {
                // Use the source name to derive a default output file name.
                of = tf.outputFileObject(pkg, sf.source());
            }
            else of = tf.outputFileObject(pkg, filename, sf.source());

            String opfPath = of.getName();
            if (!opfPath.endsWith("$")) outputFiles.add(of);
            try (CodeWriter w = tf.outputCodeWriter(of, outputWidth)) {
                writeHeader(sf, w);

                for (Iterator<TopLevelDecl> i = decls.iterator(); i.hasNext();) {
                    TopLevelDecl decl = i.next();
                    translateTopLevelDecl(w, sf, decl);

                    if (i.hasNext()) w.newline(0);
                }
            }
            catch (IOException e) {
                job.compiler()
                   .errorQueue()
                   .enqueue(ErrorInfo.IO_ERROR,
                            "I/O error while translating: " + e.getMessage());
                return false;
            }
        }
        return true;
    }

    /**
     * Translate a top-level declaration {@code decl} of source file
     * {@code source}.
     * 
     * @param w
     * @param source
     * @param decl
     */
    protected void translateTopLevelDecl(CodeWriter w, SourceFile source,
            TopLevelDecl decl) {
        Translator tr;
        if (source.isDisambiguated() && source.isTypeChecked()) {
            Context c = lang().enterScope(source, context);
            tr = this.context(c);
        }
        else {
            tr = this.context(null);
        }
        lang().translate(decl, w, tr);
    }

    /** Write the package and import declarations for a source file. */
    protected void writeHeader(SourceFile sfn, CodeWriter w) {
        if (sfn.package_() != null) {
            w.write("package ");
            lang().translate(sfn.package_(), w, this);
            w.write(";");
            w.newline(0);
            w.newline(0);
        }

        boolean newline = false;

        for (Import imp : sfn.imports()) {
            lang().translate(imp, w, this);
            newline = true;
        }

        if (newline) {
            w.newline(0);
        }
    }

    /**
     * Determine the list of file names that top-level declarations in the
     * given source file will reside.
     * @param sf
     * @return
     */
    protected Map<String, List<TopLevelDecl>> filenames(SourceFile sf) {
        Map<String, List<TopLevelDecl>> filenameMap = new LinkedHashMap<>();
        List<TopLevelDecl> files = new LinkedList<>();

        for (TopLevelDecl decl : sf.decls()) {
            files.add(decl);
            if (decl.flags().isPublic()) {
                if (!filenameMap.isEmpty()) files = new LinkedList<>();
                filenameMap.put(decl.name(), files);
            }
        }
        if (filenameMap.isEmpty()) filenameMap.put(null, files);
        return filenameMap;
    }

    @Override
    public String toString() {
        return "Translator";
    }
}
