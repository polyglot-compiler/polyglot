package ppg.atoms;

import java.util.*;

import ppg.parse.*;
import ppg.util.*;

public class Production implements Unparse {
    private Nonterminal lhs;
    private Vector<Vector<GrammarPart>> rhs;
    private static String HEADER = "ppg [nterm]: ";

    public Production(Nonterminal lhs, Vector<Vector<GrammarPart>> rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public Nonterminal getLHS() {
        return lhs;
    }

    public void setLHS(Nonterminal nt) {
        lhs = nt;
    }

    public Vector<Vector<GrammarPart>> getRHS() {
        return rhs;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object clone() {
        return new Production((Nonterminal) lhs.clone(),
                              (Vector<Vector<GrammarPart>>) rhs.clone());
    }

    public void drop(Production prod) {
        //assertSameLHS(prod, "drop");
        Vector<Vector<GrammarPart>> toDrop = prod.getRHS();
        // this is O(n^2)
        for (int i = 0; i < toDrop.size(); i++) {
            Vector<GrammarPart> target = toDrop.elementAt(i);
            for (int j = 0; j < rhs.size(); j++) {
                Vector<GrammarPart> source = rhs.elementAt(j);
                if (isSameProduction(target, source)) {
                    rhs.removeElementAt(j);
                    break;
                }
                // production match not found
                if (j == rhs.size() - 1) {
                    System.err.println(HEADER
                            + "no match found for production:");
                    System.err.print(prod.getLHS() + " ::= ");
                    for (int k = 0; k < target.size(); k++) {
                        System.err.print(target.elementAt(k) + " ");
                    }
                    System.exit(1);
                }
            }
        }
    }

    public static boolean isSameProduction(Vector<GrammarPart> u,
            Vector<GrammarPart> v) {
        int uIdx = 0, vIdx = 0;
        GrammarPart ug = null, vg = null;

        while (uIdx < u.size() && vIdx < v.size()) {
            ug = u.elementAt(uIdx);
            if (ug instanceof SemanticAction) {
                uIdx++;
                continue;
            }

            vg = v.elementAt(vIdx);
            if (vg instanceof SemanticAction) {
                vIdx++;
                continue;
            }

            if (!ug.equals(vg))
                return false;
            else {
                uIdx++;
                vIdx++;
            }
        }

        if (uIdx == u.size() && vIdx == v.size()) {
            // got through all the way, they are the same
            return true;
        }
        else {
            // one of the lists was not seen all the way, 
            // must check that only semantic actions are left
            if (uIdx < u.size()) {
                for (; uIdx < u.size(); uIdx++) {
                    ug = u.elementAt(uIdx);
                    if (!(ug instanceof SemanticAction)) return false;
                }
                return true;
            }
            else { // vIdx < v.size()
                for (; vIdx < v.size(); vIdx++) {
                    vg = v.elementAt(vIdx);
                    if (!(vg instanceof SemanticAction)) return false;
                }
                return true;
            }
        }
    }

    public void union(Production prod) {
        Vector<Vector<GrammarPart>> additional = prod.getRHS();
        union(additional);
    }

    public void union(Vector<Vector<GrammarPart>> prodList) {
        for (int i = 0; i < prodList.size(); i++) {
            Vector<GrammarPart> toAdd = prodList.elementAt(i);
            for (int j = 0; j < rhs.size(); j++) {
                Vector<GrammarPart> current = rhs.elementAt(i);
                if (isSameProduction(toAdd, current)) break;
                if (j == rhs.size() - 1) rhs.addElement(toAdd);
            }
        }
    }

    public void add(Production prod) {
        //assertSameLHS(prod, "add");
        Vector<Vector<GrammarPart>> additional = prod.getRHS();
        for (int i = 0; i < additional.size(); i++) {
            rhs.addElement(additional.elementAt(i));
        }
    }

    public void addToRHS(Vector<GrammarPart> rhsPart) {
        rhs.addElement(rhsPart);
    }

    @Override
    public void unparse(CodeWriter cw) {
        cw.begin(0);
        cw.write(lhs.toString() + " ::=");
        cw.allowBreak(3);
        for (int i = 0; i < rhs.size(); i++) {
            Vector<GrammarPart> rhs_part = rhs.elementAt(i);
            for (int j = 0; j < rhs_part.size(); j++) {
                cw.write(" ");
                rhs_part.elementAt(j).unparse(cw);
            }
            if (i < rhs.size() - 1) {
                cw.allowBreak(0);
                cw.write(" | ");
            }
        }
        cw.write(";");
        cw.newline();
        cw.newline();

        cw.end();
    }

    @Override
    public String toString() {
        String result = lhs.toString();
        result += " ::=";
        for (int i = 0; i < rhs.size(); i++) {
            Vector<GrammarPart> rhs_part = rhs.elementAt(i);
            for (int j = 0; j < rhs_part.size(); j++) {
                result += " " + rhs_part.elementAt(j).toString();
            }
            if (i < rhs.size() - 1) result += " | ";
        }
        return result + ";";
    }
}
