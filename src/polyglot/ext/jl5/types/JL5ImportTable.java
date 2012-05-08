package polyglot.ext.jl5.types;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import polyglot.types.*;
import polyglot.util.Position;
import polyglot.util.StringUtil;

public class JL5ImportTable extends ImportTable {

	protected ArrayList<String> singleStaticImports;
	protected ArrayList<String> staticOnDemandImports;

	public int id = counter++;
	private static int counter = 0;

	public JL5ImportTable(TypeSystem ts, polyglot.types.Package pkg, String src) {
		super(ts, pkg, src);
		this.singleStaticImports = new ArrayList();
		this.staticOnDemandImports = new ArrayList();
	}

	public JL5ImportTable(TypeSystem ts, polyglot.types.Package pkg) {
		this(ts, pkg, null);
	}

	public void addSingleStaticImport(String member, Position pos) {
		singleStaticImports.add(member);
	}

	public void addStaticOnDemandImport(String className, Position pos) {
		staticOnDemandImports.add(className);
	}

	public List singleStaticImports() {
		return singleStaticImports;
	}

	public List staticOnDemandImports() {
		return staticOnDemandImports;
	}

	public Named find(String name) throws SemanticException {
		Named result = null;
		// may be member in static import
		for (Iterator it = singleStaticImports.iterator(); it.hasNext();) {
			String next = (String) it.next();
			String id = StringUtil.getShortNameComponent(next);
			if (name.equals(id)) {
				String className = StringUtil.getPackageComponent(next);
				Named nt = ts.forName(className);
				if (nt instanceof Type) {
					Type t = (Type) nt;
					try {
						result = ts.findMemberClass(t.toClass(), name);
					} catch (SemanticException e) {
					}
					if (result != null
							&& ((ClassType) result).flags().isStatic())
						return result;
				}
			}
		}

		for (Iterator it = staticOnDemandImports.iterator(); it.hasNext();) {
			String next = (String) it.next();
			Named nt = ts.forName(next);

			if (nt instanceof Type) {
				Type t = (Type) nt;
				try {
					result = ts.findMemberClass(t.toClass(), name);
				} catch (SemanticException e) {
				}
				if (result != null && ((ClassType) result).flags().isStatic())
					return result;
			}
		}

		return super.find(name);
	}

	public ReferenceType findTypeContainingMethodOrField(String name)
			throws SemanticException {
		for (Iterator it = singleStaticImports.iterator(); it.hasNext();) {
			String next = (String) it.next();
			String id = StringUtil.getShortNameComponent(next);
			if (name.equals(id)) {
				// it's a match
				String className = StringUtil.getPackageComponent(next);
				Named nt = ts.forName(className);
				if (nt instanceof ReferenceType) {
					ReferenceType t = (ReferenceType) nt;
					if (!t.methodsNamed(name).isEmpty()
							|| t.fieldNamed(name) != null) {
						return t;
					}
				}
			}
		}

		for (Iterator it = staticOnDemandImports.iterator(); it.hasNext();) {
			String next = (String) it.next();
			Named nt = ts.forName(next);

			if (nt instanceof ReferenceType) {
				ReferenceType t = (ReferenceType) nt;
				if (!t.methodsNamed(name).isEmpty()
						|| t.fieldNamed(name) != null) {
					return t;
				}
			}
		}
		return null;
	}

}
