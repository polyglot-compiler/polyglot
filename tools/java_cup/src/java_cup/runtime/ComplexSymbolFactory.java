package java_cup.runtime;

/**
 * Default Implementation for SymbolFactory, creates
 * plain old Symbols
 *
 * @version last updated 27-03-2006
 * @author Michael Petter
 */

/* *************************************************
  class DefaultSymbolFactory
  interface for creating new symbols  
 ***************************************************/
public class ComplexSymbolFactory implements SymbolFactory {
    public static class Location {
        private String unit = "unknown";
        private int line, column, offset = 0;

        public Location(String unit, int line, int column, int offset) {
            this(unit, line, column);
            this.offset = offset;
        }

        public Location(String unit, int line, int column) {
            this.unit = unit;
            this.line = line;
            this.column = column;
        }

        public Location(int line, int column, int offset) {
            this(line, column);
            this.offset = offset;
        }

        public Location(int line, int column) {
            this.line = line;
            this.column = column;
        }

        public int getColumn() {
            return column;
        }

        public int getLine() {
            return line;
        }

        public String getUnit() {
            return unit;
        }

        @Override
        public String toString() {
            return getUnit() + ":" + getLine() + "/" + getColumn() + "("
                    + offset + ")";
        }

        public int getOffset() {
            return offset;
        }
    }

    /**
     * ComplexSymbol with detailed Location Informations and a Name
     */
    public static class ComplexSymbol extends Symbol {
        protected String name;
        public Location xleft, xright;

        public ComplexSymbol(String name, int id) {
            super(id);
            this.name = name;
        }

        public ComplexSymbol(String name, int id, Object value) {
            super(id, value);
            this.name = name;
        }

        @Override
        public String toString() {
            if (xleft == null || xright == null) return "Symbol: " + name;
            return "Symbol: " + name + " (" + xleft + " - " + xright + ")";
        }

        public ComplexSymbol(String name, int id, int state) {
            super(id, state);
            this.name = name;
        }

        public ComplexSymbol(String name, int id, Symbol left, Symbol right) {
            super(id, left, right);
            this.name = name;
            if (left != null) this.xleft = ((ComplexSymbol) left).xleft;
            if (right != null) this.xright = ((ComplexSymbol) right).xright;
        }

        public ComplexSymbol(String name, int id, Location left, Location right) {
            super(id, left.offset, right.offset);
            this.name = name;
            this.xleft = left;
            this.xright = right;
        }

        public ComplexSymbol(String name, int id, Symbol left, Symbol right,
                Object value) {
            super(id, left.left, right.right, value);
            this.name = name;
            if (left != null) this.xleft = ((ComplexSymbol) left).xleft;
            if (right != null) this.xright = ((ComplexSymbol) right).xright;
        }

        public ComplexSymbol(String name, int id, Location left,
                Location right, Object value) {
            super(id, left.offset, right.offset, value);
            this.name = name;
            this.xleft = left;
            this.xright = right;
        }

        public Location getLeft() {
            return xleft;
        }

        public Location getRight() {
            return xright;
        }
    }

    // Factory methods
    public Symbol newSymbol(String name, int id, Location left, Location right,
            Object value) {
        return new ComplexSymbol(name, id, left, right, value);
    }

    public Symbol newSymbol(String name, int id, Location left, Location right) {
        return new ComplexSymbol(name, id, left, right);
    }

    @Override
    public Symbol newSymbol(String name, int id, Symbol left, Symbol right,
            Object value) {
        return new ComplexSymbol(name, id, left, right, value);
    }

    @Override
    public Symbol newSymbol(String name, int id, Symbol left, Symbol right) {
        return new ComplexSymbol(name, id, left, right);
    }

    @Override
    public Symbol newSymbol(String name, int id) {
        return new ComplexSymbol(name, id);
    }

    @Override
    public Symbol newSymbol(String name, int id, Object value) {
        return new ComplexSymbol(name, id, value);
    }

    @Override
    public Symbol startSymbol(String name, int id, int state) {
        return new ComplexSymbol(name, id, state);
    }
}
