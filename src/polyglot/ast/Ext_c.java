package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;

/**
 * <code>Ext</code> is the super type of all node extension objects.
 * It defines default implementations of the methods which implement compiler
 * passes, dispatching to the node to perform the actual work of the pass.
 */
public class Ext_c implements Ext {
    Node n;

    public Ext_c() {
    }

    public void init(Node n) {
	this.n = n;
    }

    protected Node node() {
	return n;
    }

    public Object copy() {
	try {
	    return clone();
	}
	catch (CloneNotSupportedException e) {
	    throw new InternalCompilerError("Java clone() wierdness.");
	}
    }

    public Node buildTypesOverride(TypeBuilder tb) throws SemanticException {
	return n.buildTypesOverride_(tb);
    }

    public Node buildTypes(TypeBuilder tb) throws SemanticException {
	return n.buildTypes_(tb);
    }

    public Node addMembersOverride(AddMemberVisitor tc) throws SemanticException {
	return n.addMembersOverride_(tc);
    }

    public Node addMembers(AddMemberVisitor tc) throws SemanticException {
	return n.addMembers_(tc);
    }

    public Node disambiguateOverride(AmbiguityRemover ar) throws SemanticException {
	return n.disambiguateOverride_(ar);
    }

    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
	return n.disambiguate_(ar);
    }

    public Node foldConstantsOverride(ConstantFolder cf) {
	return n.foldConstantsOverride_(cf);
    }

    public Node foldConstants(ConstantFolder cf) {
	return n.foldConstants_(cf);
    }

    public Node typeCheckOverride(TypeChecker tc) throws SemanticException {
	return n.typeCheckOverride_(tc);
    }

    public Node typeCheck(TypeChecker tc) throws SemanticException {
	return n.typeCheck_(tc);
    }

    public Node exceptionCheckOverride(ExceptionChecker ec) throws SemanticException {
	return n.exceptionCheckOverride_(ec);
    }

    public Node exceptionCheck(ExceptionChecker ec) throws SemanticException {
	return n.exceptionCheck_(ec);
    }

    public void translate(CodeWriter w, Translator tr) {
	n.translate_(w, tr);
    }

    public String toString() {
        return StringUtil.getShortNameComponent(getClass().getName());
    }
}
