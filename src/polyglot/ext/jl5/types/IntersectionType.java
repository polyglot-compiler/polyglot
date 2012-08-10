package polyglot.ext.jl5.types;

import java.util.List;

import polyglot.types.ReferenceType;

public interface IntersectionType extends JL5ClassType {
    List<ReferenceType> bounds();

    void setBounds(List<ReferenceType> newBounds);

    void boundOf(TypeVariable tv);

    TypeVariable boundOf();

    public static final Kind INTERSECTION = new Kind("intersection");

}
