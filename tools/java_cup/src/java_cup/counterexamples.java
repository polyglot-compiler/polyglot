package java_cup;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class counterexamples {

    private static class Path {
        Path(LinkedList<Step> t, StateItem si) {
            steps = t;
            last = si;
        }

        /** steps is a linked list of lalr_transition _or_ production.
         * The latter are found in the path when a "push" occurs to work
         * on a new production. */
        LinkedList<Step> steps;
        /** last is the last state and item reached on the path. */
        StateItem last;
    }

    private static class Produce extends Step {
        private production prod;

        public Produce(StateItem st, production pr) {
            super(st);
            prod = pr;
        }

        @Override
        public void appendToReport(StringBuilder example_s,
                PrintStream derivation_s, boolean first) {
            /* production: don't add anything to the example */
            if (!first) derivation_s.print(" ");
            derivation_s.print("[" + prod.lhs().the_symbol().name() + "::=");
        }

        @Override
        public String toString() {
            return "Produce " + prod;
        }
    }

    private static class Reduce extends Step {
        private production prod;

        public Reduce(StateItem st, production pr) {
            super(st);
            prod = pr;
        }

        @Override
        public void appendToReport(StringBuilder example_s,
                PrintStream derivation_s, boolean first) {
            /* production: don't add anything to the example */
            if (!first) derivation_s.print(" ");
            derivation_s.print("]");
        }

        @Override
        public String toString() {
            return "Reduce " + prod;
        }
    }

    private static class StateItem {
        lalr_state state;
        lalr_item item;

        StateItem(lalr_state s, lalr_item i) {
            state = s;
            item = i;
        }

        @Override
        public boolean equals(Object o) {
            StateItem si2 = (StateItem) o;
            return state.equals(si2.state) && item.equals(si2.item);
        }

        @Override
        public int hashCode() {
            return state.hashCode() + item.hashCode();
        }
    }

    /** A Path represents a path through the DFA, with edges between different
     *  items in the same state represented explicitly.
     */

    private static abstract class Step {
        StateItem source;

        public Step(StateItem src) {
            source = src;
        }

        StateItem sourceState() {
            return source;
        }

        abstract void appendToReport(StringBuilder example_s,
                PrintStream derivation_s, boolean first);

        @Override
        public String toString() {
            StringBuilder ex = new StringBuilder();
            ByteArrayOutputStream ds = new ByteArrayOutputStream();
            appendToReport(ex, new PrintStream(ds), true);
            return ex.toString();
        }
    }

    // A 'transition' step: either a shift (on a terminal) or a goto (on a nonterminal).
    private static class TransStep extends Step {
        public TransStep(StateItem from, lalr_transition tr) {
            super(from);
            trans = tr;
        }

        lalr_transition trans;

        @Override
        public void appendToReport(StringBuilder example_s,
                PrintStream derivation_s, boolean first) {
            String name = trans.on_symbol().name();
            if (!first) {
                derivation_s.print(" ");
                example_s.append(" ");
            }
            example_s.append(name);
            derivation_s.print(name);
        }

        @Override
        public String toString() {
            return "Shift " + super.toString();
        }
    }

    /**   A machine history is a pair of paths stored in an array of length 2. The first
         *    element is the full history to this point, the second element is the current path
         *    after applying all the actions in the history, including reduce actions.
         *    Our goal is to get to a history where the the current path is empty.
         *    
         *    Return: machine history that get from path p to such a final path, or null if
         *    somehow such a history cannot be found. The extended part of the history uses reduces and
         *    state transitions, but no productions, since 'shifting' whatever nonterminal the
         *    production produces can be done directly during the search.
         * @throws internal_error 
         */
    static Path complete_path(Path p) {
        System.out.println("Completing path " + p);
        LinkedList<Path[]> active = new LinkedList<Path[]>();
        HashSet<StateItem> visited = new HashSet<>();
        active.add(new Path[] { p, p });
        while (!active.isEmpty()) {
            Path[] h = active.removeFirst();

            assert h[1].last.state == h[0].last.state
                    && h[1].last.item == h[0].last.item;

            Completed c = complete_from_state(h, visited, active);
            if (c != null) return c.history;
        }

        throw new Error("Cannot complete the parse in error diagnostic.");
    }

    static class Completed extends Exception {
        Path history;
    }

    static Completed complete_from_state(Path[] h, Set<StateItem> visited,
            List<Path[]> active) {
        StateItem si = h[1].last;
        if (visited.contains(si)) {
            System.out.println("Dropping " + si + " on second encounter.");
            return null;
        }

        lalr_state s = si.state;
        lalr_item i = si.item;

        System.out.println("Looking at " + s);
        System.out.println("item is " + i);

        /* try taking transitions */
        for (lalr_transition tr = s.transitions(); tr != null; tr = tr.next()) {
            if (tr.on_symbol().equals(i.symbol_after_dot())) {
                System.out.println("Found transition symbol " + tr.on_symbol());
                lalr_item i2 = i.shift();

                LinkedList<Step> ns0 = new LinkedList<>(h[0].steps), ns1 =
                        new LinkedList<>(h[1].steps);
                Step trs = new TransStep(si, tr);
                ns0.add(trs);
                ns1.add(trs);

                active.add(new Path[] {
                        new Path(ns0, new StateItem(tr.to_state(), i2)),
                        new Path(ns1, new StateItem(tr.to_state(), i2)) });
                System.out.println("Added " + i2 + ", path now has "
                        + ns1.size() + " steps.");
                System.out.println(ns1);
            }
        }
        // Try doing reduces
        if (i.dot_at_end()) {
            System.out.println("Trying to reduce " + i);

            LinkedList<Step> ns0 = new LinkedList<>(h[0].steps);

            production pr = i.the_production();
            ns0.add(new Reduce(si, pr));
            int rhslen = pr.rhs_length();
            int nsteps = h[1].steps.size();

            for (int j = 0; j < nsteps; j++) {
                Step step = h[1].steps.get(j);
                int k = j - (nsteps - rhslen - 1);
                System.out.println("Step " + k + ". " + step);
                assert k != 0 || step instanceof Produce;
                assert k <= 0 || step instanceof TransStep;

            }
            if (nsteps == rhslen) {

                Completed c = new Completed();
                c.history = h[0];
                return c;
            }
            StateItem si1 = h[1].steps.get(nsteps - rhslen - 1).source;

            System.out.println("In state  " + si1.state + " with item "
                    + si1.item);

            LinkedList<Step> ns1 = new LinkedList<>();
            for (int j = 0; j < nsteps - rhslen - 1; j++) {
                ns1.add(h[1].steps.get(j));
            }

            symbol lhs = pr.lhs().the_symbol();

            System.out.println("doing a goto on " + lhs + " to "
                    + si1.item.shift());
            boolean found_transition = false;
            for (lalr_transition tr = si1.state.transitions(); tr != null; tr =
                    tr.next()) {
                if (tr.on_symbol().equals(lhs)) {
                    assert !found_transition;
                    System.out.println("Found the right transition: " + tr);
                    found_transition = true;
                    System.out.println("Before: " + ns1);
                    ns1.add(new TransStep(si1, tr));

                    System.out.println("After: " + ns1);
                    StateItem si1new =
                            new StateItem(tr.to_state(), si1.item.shift());
                    Path p1 = new Path(ns1, si1new);
                    Path p0 = new Path(ns0, si1new);

                    active.add(new Path[] { p0, p1 });
                }
            }
//                   
//                }
//                ns0new.add(new TransStep(si0, ))

            //                for (production prod : nt.productions()) {
            //                    terminal_set new_lookaheads =
            //                            i.calc_lookahead(i.lookahead());
            //                    lalr_item i2 =
            //                            new lalr_item(prod,
            //                                          new terminal_set(new_lookaheads));
            //                    LinkedList<Step> newt = new LinkedList<>(p1.steps);
            //                    newt.add(new Produce(s, prod));
            //                    Path p2 = new Path(newt, new StateItem(s, i2));
            //                    active.add(p2);
            //                }
            //}

        }
        return null;
    }

    /**
     * Report on example_s a textual version of the shortest
     * path from the start state and start item to the current state
     * and item itm. Report on derivation_s a more detailed
     * textual description including derivation information.
     * This output is useful for diagnosing conflicts in the grammar.
     */
    protected static void report_shortest_path(lalr_state state, lalr_item itm,
            StringBuilder example_s, PrintStream derivation_s)
            throws internal_error {
        Path p = shortest_path(state, itm);
        Path p2 = complete_path(p);
        boolean first = true;
        for (Step s : p2.steps) {
            s.appendToReport(example_s, derivation_s, first);
            first = false;
        }
    }

    /**
     * Find the shortest way to get from the start state and start item
     * to the current state and the item "itm". The steps that change
     * the item being transitioned on (corresponding to items formed
     * through closure computation) are represented explicitly in this
     * path.
     */
    protected static Path shortest_path(lalr_state target_state,
            lalr_item target_itm) throws internal_error {
        HashSet<StateItem> visited = new HashSet<>();
        LinkedList<Path> active = new LinkedList<>(); // work queue
        StateItem start =
                new StateItem(lalr_state.startState(), lalr_state.startItem());

        // This is a breadth-first search over the state graph, building
        // up paths as we go.
        Path p = new Path(new LinkedList<Step>(), start);
        active.add(p);
        while (!active.isEmpty()) {
            Path p1 = active.removeFirst();
            StateItem si = p1.last;
            if (visited.contains(si)) continue; /* saw it already */
            visited.add(si);
            lalr_state s = si.state;
            lalr_item i = si.item;
            if (target_state.equals(s) && target_itm.equals(i)) {
                p = p1;
                return p; // done!
            }
            /* try taking transitions */
            for (lalr_transition tr = s.transitions(); tr != null; tr =
                    tr.next()) {
                if (tr.on_symbol().equals(i.symbol_after_dot())) {
                    lalr_item i2 = i.shift();
                    LinkedList<Step> newt = new LinkedList<Step>(p1.steps);
                    newt.add(new TransStep(si, tr));
                    Path p2 = new Path(newt, new StateItem(tr.to_state(), i2));
                    active.add(p2);
                }
            }
            /* try changing the production (one step of closure) */
            non_terminal nt = i.dot_before_nt();
            if (nt != null) {
                for (production prod : nt.productions()) {
                    terminal_set new_lookaheads =
                            i.calc_lookahead(i.lookahead());
                    lalr_item i2 =
                            new lalr_item(prod,
                                          new terminal_set(new_lookaheads));
                    LinkedList<Step> newt = new LinkedList<>(p1.steps);
                    newt.add(new Produce(si, prod));
                    Path p2 = new Path(newt, new StateItem(s, i2));
                    active.add(p2);
                }
            }
        }
        return null;
    }

}
