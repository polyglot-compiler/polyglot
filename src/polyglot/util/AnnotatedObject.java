/*
 * AnnotatedObject.java
 */

package jltools.util;


import java.util.ArrayList;
import java.util.Iterator;

/**
 * AnnotatedObject
 *
 * Overview: AnnotatedObject is any object to which generic
 *   annotations may be attached.  Each annotation is tagged by an integer.
 *   Duplicate tags are not allowed.  Annotations may not be null.
 *
 * Notes:
 *   The *Annotation methods methods should not be called directly, but
 *   instead should be called from 'Annotate'.
 **/
public abstract class AnnotatedObject {


  private static final class AnnotationTuple {
    int tag;
    Object val;
    public AnnotationTuple(int t, Object v) {
      tag = t;
      val = v;
    }
  }
  // All members of annotations are AnnotationTuple.  No tag appears more
  // than once.
  private ArrayList annotations;

  /**
   * Contstructor.
   **/
  public AnnotatedObject() {
    annotations = new ArrayList();
  }

  /**
   * void setAnnotation(int tag, Object val)
   *
   * Requires: val != null
   * Modifies: This.
   * Effects: Causes this to have an annotation tagged with <tag>
   *   whose value is <val>.
   **/
  public final void setAnnotation(int tag, Object val) {
    if (val==null) 
      throw new NullPointerException("Cannot have a null annotation");
    
    AnnotationTuple tup = getTupleForTag(tag);
    
    if (tup != null)
      tup.val = val;
    else
      annotations.add(new AnnotationTuple(tag, val));
  }

  /**
   * Object getAnnotation(int tag)
   *
   * Effects: Gets the value of the annotation tagged by <tag>.  Returns
   *   null if no such such annotation exists.
   **/
  public final Object getAnnotation(int tag) {
    AnnotationTuple tup = getTupleForTag(tag);
    
    if (tup != null)
      return tup.val;
    else
      return null;
  }

  /**
   * void removeAnnotation(int tag)
   *
   * Effects: Causes this to contain no annotation tagged by tag.
   **/
  public final void removeAnnotation(int tag) {
    for (Iterator elts = annotations.iterator(); elts.hasNext(); ) {
      if (((AnnotationTuple) elts.next()).tag == tag) {
	elts.remove();
	return;
      }
    }
  }

  /**
   * void copyAnnotationsFrom(AnnotatedObject o)
   *
   * Effects: Copies all annotations from <o>.
   **/
  public final void copyAnnotationsFrom(AnnotatedObject o) {
    ArrayList new_annotations = new ArrayList();
    for (Iterator elts = o.annotations.iterator(); elts.hasNext(); ) {
      AnnotationTuple tup = (AnnotationTuple) elts.next();
      new_annotations.add(new AnnotationTuple(tup.tag, tup.val));      
    }
    annotations = new_annotations;
  }

  private AnnotationTuple getTupleForTag(int tag) {
    // See if we have an AnnotationTuple with the right tag.
    for (Iterator elts = annotations.iterator(); elts.hasNext(); ) {
      AnnotationTuple tup = (AnnotationTuple) elts.next();
      if (tup.tag == tag)
	return tup;
    }
    return null;
  }
}


