package polyglot.types;

import java.util.*;

import polyglot.main.Report;
import polyglot.util.CollectionUtil;
import polyglot.util.StringUtil;
import polyglot.util.InternalCompilerError;

/**
 * Loads member classes using a TopLevelResolver that can only handle
 * top-level classes.
 */
public class MemberClassResolver implements TopLevelResolver
{
  TypeSystem ts;
  TopLevelResolver inner;
  boolean allowRawClasses;
  Set nocache;

  protected final static Collection report_topics = CollectionUtil.list(
    Report.types, Report.resolver, Report.loader, "mcr");

  /**
   * Create a member class resolver.
   * @param ts The type system
   * @param inner The resolver for top-level classes
   */
  public MemberClassResolver(TypeSystem ts, TopLevelResolver inner, boolean allowRawClasses) {
    this.ts = ts;
    this.inner = inner;
    this.allowRawClasses = allowRawClasses;
    this.nocache = new HashSet();
  }

  public boolean packageExists(String name) {
    return inner.packageExists(name);
  }

  /**
   * Find a type by name.
   */
  public Named find(String name) throws SemanticException {
    if (Report.should_report(report_topics, 3))
      Report.report(3, "MemberCR.find(" + name + ")");

    if (nocache.contains(name)) {
        throw new NoClassException(name);
    }

    Named n = ts.systemResolver().check(name);

    if (n != null) {
        return n;
    }

    SemanticException error = null;

    // First, just try the long name.
    try {
        if (Report.should_report(report_topics, 2))
            Report.report(2, "MCR: loading " + name + " from " + inner);
        return inner.find(name);
    }
    catch (SemanticException e) {
        if (Report.should_report(report_topics, 2))
            Report.report(2, "MCR: " + e.getMessage());
        if (StringUtil.isNameShort(name)) {
            throw e;
        }
        error = e;
    }

    boolean install = true;

    // Now try the prefix of the name and look for a member class
    // within it named with the suffix.
    String prefix = StringUtil.getPackageComponent(name);
    String suffix = StringUtil.getShortNameComponent(name);

    // Try the full name of the prefix first, then the raw class name,
    // so that encoded type information and source files are preferred
    // to the raw class file.
    try {
        if (Report.should_report(report_topics, 2))
            Report.report(2, "MCR: loading prefix " + prefix);

        n = find(prefix);

        // This may be called during deserialization; n's
        // member classes might not be initialized yet.
        if (n instanceof ParsedTypeObject) {
            if (true || ((ParsedTypeObject) n).initializer() != null &&
                ((ParsedTypeObject) n).initializer().isTypeObjectInitialized()) {
                return findMember(n, suffix);
            }
            else {
                install = false;
            }
        }
    }
    catch (SemanticException e) {
    }

    if (install) {
        nocache.add(name);
    }

    throw error;
  }

  protected Named findMember(Named container, String name) throws SemanticException {
      if (container instanceof ClassType) {
          ClassType ct = (ClassType) container;

          if (Report.should_report(report_topics, 2))
              Report.report(2, "MCR: found prefix " + ct);

          // Uncomment if we should search superclasses
          // return ct.resolver().find(name);
          Named n = ct.memberClassNamed(name);

          if (n != null) {
              if (Report.should_report(report_topics, 2))
                  Report.report(2, "MCR: found member of " + ct + ": " + n);
              return n;
          }
      }

      throw new NoClassException(container.fullName() + "." + name);
  }
}
