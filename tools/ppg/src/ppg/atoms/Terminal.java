package jltools.util.jlgen.atoms;

import jltools.util.jlgen.parse.*;
import jltools.util.jlgen.util.*;
public class Terminal extends GrammarSymbol
{
	public Terminal(String name, String label)
	{
		this.name = name;
		this.label = label;
	}

	public Terminal(String name)
	{
		this.name = name;
		label = null;
	}

	public boolean equals(Object o) {
		if (o instanceof Terminal) {
			return name.equals( ((Terminal)o).getName() );	
		}
		return false;
	}
}
