package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.util.*;
import jltools.types.*;
import jltools.visit.*;
import java.util.*;

/**
 * Overview: An immutable representation of a try block, one or more
 * catch blocks, and an optional finally block.
 */
public class Try_c extends Stmt_c implements Try
{
    protected Block tryBlock;
    protected List catchBlocks;
    protected Block finallyBlock;

    public Try_c(Ext ext, Position pos, Block tryBlock, List catchBlocks, Block finallyBlock) {
	super(ext, pos);
	this.tryBlock = tryBlock;
	this.catchBlocks = TypedList.copyAndCheck(catchBlocks, Catch.class, true);
	this.finallyBlock = finallyBlock;
    }

    public Block tryBlock() {
	return this.tryBlock;
    }

    public Try tryBlock(Block tryBlock) {
	Try_c n = (Try_c) copy();
	n.tryBlock = tryBlock;
	return n;
    }

    public List catchBlocks() {
	return Collections.unmodifiableList(this.catchBlocks);
    }

    public Try catchBlocks(List catchBlocks) {
	Try_c n = (Try_c) copy();
	n.catchBlocks = TypedList.copyAndCheck(catchBlocks, Catch.class, true);
	return n;
    }

    public Block finallyBlock() {
	return this.finallyBlock;
    }

    public Try finallyBlock(Block finallyBlock) {
	Try_c n = (Try_c) copy();
	n.finallyBlock = finallyBlock;
	return n;
    }

    protected Try_c reconstruct(Block tryBlock, List catchBlocks, Block finallyBlock) {
	if (tryBlock != this.tryBlock || ! CollectionUtil.equals(catchBlocks, this.catchBlocks) || finallyBlock != this.finallyBlock) {
	    Try_c n = (Try_c) copy();
	    n.tryBlock = tryBlock;
	    n.catchBlocks = TypedList.copyAndCheck(catchBlocks, Catch.class, true);
	    n.finallyBlock = finallyBlock;
	    return n;
	}

	return this;
    }

    public Node visitChildren(NodeVisitor v) {
	Block tryBlock = (Block) this.tryBlock.visit(v);

	List catchBlocks = new ArrayList(this.catchBlocks.size());
	for (Iterator i = this.catchBlocks.iterator(); i.hasNext(); ) {
	    Catch n = (Catch) i.next();
	    n = (Catch) n.visit(v);
	    catchBlocks.add(n);
	}

	Block finallyBlock = null;

	if (this.finallyBlock != null) {
	    finallyBlock = (Block) this.finallyBlock.visit(v);
	}

	return reconstruct(tryBlock, catchBlocks, finallyBlock);
    }

    /**
     * Performs exceptionChecking. This is a special method that is called
     * via the exceptionChecker's override method (i.e, doesn't follow the
     * standard model for visitation.  
     *
     * @param ec The ExceptionChecker that was run against the 
     * child node. It contains the exceptions that can be thrown by the try
     * block.
     */
    public Node exceptionCheckOverride_(ExceptionChecker ec)
	throws SemanticException
    {
	TypeSystem ts = ec.typeSystem();

	// Visit the try block.
	Block tryBlock = (Block) this.tryBlock.visit(ec);

	// First, get exceptions from the try block.
	SubtypeSet thrown = ec.throwsSet(); 
        SubtypeSet caught = new SubtypeSet();

	// Add the unchecked exceptions.
	thrown.addAll(ts.uncheckedExceptions());

	// Walk through our catch blocks, making sure that they each can 
	// catch something.
	for (Iterator i = this.catchBlocks.iterator(); i.hasNext(); ) {
	    Catch cb = (Catch) i.next();
	    Type catchType = cb.catchType();

	    // Check if the catch type is a supertype or a subtype of an
	    // exception thrown in the try block.

	    boolean match = false;

	    for (Iterator j = thrown.iterator(); j.hasNext(); ) {
		Type ex = (Type) j.next();

		if (ex.isSubtype(catchType) || catchType.isSubtype(ex)) {
		    match = true;
		    break;
		}
	    }

	    if (! match) {
		throw new SemanticException("The exception \"" +
		    catchType + "\" is not thrown in the try block.",
		    cb.position()); 
	    }

	    // Check if the exception has already been caught.
	    if (caught.contains(catchType)) {
		throw new SemanticException("The exception \"" +
		    catchType + "\" has been caught by an earlier catch block.",
		    cb.position()); 
	    }

	    caught.add(catchType);
	}

	// Remove exceptions which have been caught.
	thrown.removeAll(caught);

	// "thrown" now contains any exceptions which were not caught.
	// We now visit the catch blocks and finallyBlock to get the
	// exceptions they throw.

	List catchBlocks = new ArrayList(this.catchBlocks.size());

	for (Iterator i = this.catchBlocks.iterator(); i.hasNext(); ) {
	    Catch cb = (Catch) i.next();

	    ExceptionChecker ec2 = ec.alloc();

	    cb = (Catch) cb.visit(ec2);
	    catchBlocks.add(cb);

	    thrown.addAll(ec2.throwsSet());
	    ec.release(ec2);
	}

	Block finallyBlock = null;

	if (this.finallyBlock != null) {
	    thrown.clear();

	    ExceptionChecker ec2 = ec.alloc();

	    finallyBlock = (Block) this.finallyBlock.visit(ec);

	    thrown.addAll(ec2.throwsSet());
	    ec.release(ec2);
	}

	return reconstruct(tryBlock, catchBlocks, finallyBlock);
    }

    public String toString() {
	return "try { ... } " + (catchBlocks.isEmpty() ? "" : "catch ...") +
	                        (finallyBlock != null ? "finally { ... }" : "");
    }

    public void translate_(CodeWriter w, Translator tr) {
	w.write("try ");
	translateSubstmt(tryBlock, w, tr);

	for (Iterator it = catchBlocks.iterator(); it.hasNext(); ) {
	    Catch cb = (Catch) it.next();
	    w.newline(4);
	    translateBlock(cb, w, tr);
	}

	if (finallyBlock != null) {
	    w.newline(4);
	    w.write ("finally ");
	    translateSubstmt(finallyBlock, w, tr);
	}
    }
}
