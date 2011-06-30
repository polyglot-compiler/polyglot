package polyglot.types.reflect;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject.Kind;

import polyglot.frontend.AbstractExtensionInfo;
import polyglot.frontend.ExtensionInfo;
import polyglot.main.Report;
import polyglot.util.CollectionUtil;
import polyglot.util.InternalCompilerError;

public class ClassFileLoader_c implements ClassFileLoader {
	/**
	 * A cache for package lookups.
	 */
	protected Map<String,Boolean> packageCache;

	/**
	 * The list of locations to search for class files, in order.
	 */
	protected List<Location> locations;
	
	/**
	 * A cache of names that are not found.
	 */
	protected Set<String> nocache;

	protected ExtensionInfo extInfo;
	
	protected static final int BUF_SIZE = 1024 * 8;
	protected final static Set<Kind> ALL_KINDS = new HashSet<Kind>();
	{
		ALL_KINDS.add(Kind.CLASS);
		ALL_KINDS.add(Kind.SOURCE);
		ALL_KINDS.add(Kind.HTML);
		ALL_KINDS.add(Kind.OTHER);
	}
	
	protected final static Collection<String> report_topics = CollectionUtil
	.list(Report.types, Report.resolver, Report.loader);

	public ClassFileLoader_c(ExtensionInfo extInfo, List<Location> locations) {
		this.extInfo = extInfo;
		this.locations = new ArrayList<Location>(locations);
		this.packageCache = new HashMap<String,Boolean>();
		this.nocache = new HashSet<String>();
	}

	public void addLocation(Location loc) {
		List<Location> newLocs = new ArrayList<Location>(locations.size()+1);
		newLocs.add(loc);
		newLocs.addAll(locations);
		locations = newLocs;
	}
	
	public boolean packageExists(String name) {
		Boolean exists = packageCache.get(name);
		if (exists != null)
			return exists;
		else {
			StandardJavaFileManager fm = extInfo.fileManager();
			exists = false;
			for(Location l : locations) {
				Iterable<JavaFileObject> contents;
				try {
					contents = fm.list(l, name, ALL_KINDS, false);
					
				} catch (IOException e) {
					throw new InternalCompilerError("Error while checking for package "
							+ name, e);
				}
				exists = contents.iterator().hasNext();
				if(exists)
					break;
			}			
			packageCache.put(name, exists);
			return exists;
		}
	}

	/**
	 * Load a class file for class <code>name</code>.
	 */
	public ClassFile loadFile(String name) {
		if (nocache.contains(name)) {
			return null;
		}

		try {
			StandardJavaFileManager fm = extInfo.fileManager();

			JavaFileObject jfo = null;
			for(Location l : locations) {
				Iterable<JavaFileObject> contents;
				try {
					jfo = fm.getJavaFileForInput(l, name, Kind.CLASS);
				} catch (IOException e) {
					throw new InternalCompilerError("Error while checking for class file "
							+ name, e);
				}
				if(jfo != null) {
					if (Report.should_report(report_topics, 4)) {
						Report.report(4, "Class " + name + " found in "
								+ l + " at " + jfo.toUri());
					}
					break;
				}
				else {
					if (Report.should_report(report_topics, 4)) {
						Report.report(4, "Class " + name
								+ " not found in " + l);
					}
				}
			}

			if (jfo != null) {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				InputStream is = jfo.openInputStream();
				byte buf[] = new byte[BUF_SIZE];
				int len;
				while ((len = is.read(buf, 0, BUF_SIZE)) != -1)
					bos.write(buf, 0, len);

				ClassFile clazz = extInfo.createClassFile(jfo, bos
						.toByteArray());

				return clazz;
			}
		} catch (ClassFormatError e) {
			if (Report.should_report(report_topics, 4))
				Report.report(4, "Class " + name + " format error");
		} catch (IOException e) {
			if (Report.should_report(report_topics, 4))
				Report.report(4, "Error loading class " + name);
		}

		nocache.add(name);

		return null;
	}

}
