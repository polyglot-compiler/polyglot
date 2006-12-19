/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.visit;

import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.frontend.Job;
import polyglot.frontend.TargetFactory;
import polyglot.types.Context;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;

/**
 * TypedTranslator extends Translator for type-directed code generation. The
 * base Translator uses types only to generate more readable code. If an
 * ambiguous or untyped AST node is encountered, code generation continues. In
 * contrast, with TypedTranslator, encountering an ambiguous or untyped node is
 * considered internal compiler error. TypedTranslator should be used when the
 * output AST is expected to be (or required to be) type-checked before code
 * generation.
 */
public class TypedTranslator extends Translator {

    public TypedTranslator(Job job, TypeSystem ts, NodeFactory nf, TargetFactory tf) {
        super(job, ts, nf, tf);
    }
    
    public void print(Node parent, Node child, CodeWriter w) {
        if (context == null) {
            throw new InternalCompilerError("Null context found during type-directed code generation.", child.position());
        }
        
        if (parent != null && 
                (! parent.isDisambiguated() || ! parent.isTypeChecked())) {
            throw new InternalCompilerError("Untyped AST node found during type-directed code generation.", parent.position());
        }
        
        if (child != null && 
                (! child.isDisambiguated() || ! child.isTypeChecked())) {
            throw new InternalCompilerError("Untyped AST node found during type-directed code generation.", child.position());
        }
        
        super.print(parent, child, w);
    }
}
