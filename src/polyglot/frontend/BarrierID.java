package jltools.frontend;

/**
 * A <code>BarrierID</code> is a <code>PassID</code> for a
 * <code>BarrierPass</code>.  All jobs must complete the first pass before any
 * job starts the second pass.
 */
public class BarrierID extends PassID {
    PassID first;
    PassID second;

    public BarrierID(PassID first, PassID second) {
	super(first.toString() + "->" + second.toString());
	this.first = first;
	this.second = second;
    }

    PassID first() { return first; }
    PassID second() { return second; }
}
