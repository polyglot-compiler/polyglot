package jltools.ast;

import jltools.util.CodeWriter;
import jltools.util.Copy;
import jltools.visit.TypeBuilder;
import jltools.visit.TypeAmbiguityRemover;
import jltools.visit.AmbiguityRemover;
import jltools.visit.ConstantFolder;
import jltools.visit.TypeChecker;
import jltools.visit.ExceptionChecker;
import jltools.visit.Translator;
import jltools.types.SemanticException;
import jltools.types.TypeSystem;
import jltools.types.Context;

public interface Ext extends Copy
{
    Object copy();
    void init(Node n);

    Node buildTypesOverride(TypeBuilder tb) throws SemanticException;
    Node buildTypes(TypeBuilder tb) throws SemanticException;

    Node disambiguateTypesOverride(TypeAmbiguityRemover sc) throws SemanticException;
    Node disambiguateTypes(TypeAmbiguityRemover sc) throws SemanticException;

    Node disambiguateOverride(AmbiguityRemover ar) throws SemanticException;
    Node disambiguate(AmbiguityRemover ar) throws SemanticException;

    Node foldConstantsOverride(ConstantFolder cf);
    Node foldConstants(ConstantFolder cf);

    Node typeCheckOverride(TypeChecker tc) throws SemanticException;
    Node typeCheck(TypeChecker tc) throws SemanticException;

    Node exceptionCheckOverride(ExceptionChecker ec) throws SemanticException;
    Node exceptionCheck(ExceptionChecker ec) throws SemanticException;

    void translate(CodeWriter w, Translator tr);

    Node reconstructTypes(NodeFactory nf, TypeSystem ts, Context c)
	throws SemanticException;
}
