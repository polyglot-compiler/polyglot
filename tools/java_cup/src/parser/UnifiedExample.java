package parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import java_cup.lalr_item;
import java_cup.lalr_state;
import java_cup.non_terminal;
import java_cup.production;
import java_cup.symbol;
import java_cup.symbol_part;
import java_cup.terminal;

public class UnifiedExample {

    protected static final int PRODUCTION_COST = 50;
    protected static final int REDUCE_COST = 1;
    protected static final int SHIFT_COST = 1;
    protected static final int UNSHIFT_COST = 1;
    protected static final int DUPLICATE_PRODUCTION_COST = 0;

    protected static final long ASSURANCE_LIMIT = 2 * 1000000000L;
    protected static final long TIME_LIMIT = 10 * 1000000000L;

    protected lalr_state conflict;
    protected lalr_item itm1;
    protected lalr_item itm2;
    protected terminal nextSym;
    protected boolean isShiftReduce;
    protected List<StateItem> shortestConflictPath;
    protected Set<lalr_state> scpSet;
    protected Set<lalr_state> rppSet;

    public UnifiedExample(lalr_state conflict, lalr_item itm1, lalr_item itm2,
            terminal nextSym) {
        this.conflict = conflict;
        this.nextSym = nextSym;
        if (itm1.dot_at_end()) {
            if (itm2.dot_at_end()) {
                // reduce/reduce conflict
                this.itm1 = itm1;
                this.itm2 = itm2;
                isShiftReduce = false;
            }
            else {
                // shift/reduce conflict
                this.itm1 = itm1;
                this.itm2 = itm2;
                isShiftReduce = true;
            }
        }
        else if (itm2.dot_at_end()) {
            // shift/reduce conflict
            this.itm1 = itm2;
            this.itm2 = itm1;
            isShiftReduce = true;
        }
        else throw new Error("Expected at least one reduce item.");
        shortestConflictPath = findShortestPathFromStart();
        scpSet = new HashSet<>(shortestConflictPath.size());
        production reduceProd = this.itm1.the_production();
        rppSet = new HashSet<>(reduceProd.rhs_length());
        boolean reduceProdReached = false;
        for (StateItem si : shortestConflictPath) {
            scpSet.add(si.state);
            reduceProdReached =
                    reduceProdReached || si.item.the_production() == reduceProd;
            if (reduceProdReached) rppSet.add(si.state);
        }
    }

    protected List<StateItem> findShortestPathFromStart() {
        StateItem.init();
        long start = System.nanoTime();
        lalr_state startState = lalr_state.startState();
        lalr_item startItem = lalr_state.startItem();
        StateItem source = StateItem.lookup(startState, startItem);
        StateItem target = StateItem.lookup(conflict, itm1);
        Queue<List<StateItemWithLookahead>> queue = new LinkedList<>();
        Set<StateItemWithLookahead> visited = new HashSet<>();
        {
            List<StateItemWithLookahead> init = new LinkedList<>();
            init.add(new StateItemWithLookahead(source,
                                                StateItem.symbolSet(source.item.lookahead())));
            queue.add(init);
        }
        while (!queue.isEmpty()) {
            List<StateItemWithLookahead> path = queue.remove();
            StateItemWithLookahead last = path.get(path.size() - 1);
            if (visited.contains(last)) continue;
            visited.add(last);
            if (target.equals(last.si) && last.lookahead.contains(nextSym)) {
                // Done
//                System.err.println(path);
                System.err.println("reachable: " + (System.nanoTime() - start));
                List<StateItem> shortestConflictPath =
                        new ArrayList<>(path.size());
                for (StateItemWithLookahead sil : path)
                    shortestConflictPath.add(sil.si);
                return shortestConflictPath;
            }
            if (StateItem.trans.containsKey(last.si)) {
                for (Map.Entry<symbol, StateItem> trans : StateItem.trans.get(last.si)
                                                                         .entrySet()) {
                    StateItem nextSI = trans.getValue();
                    StateItemWithLookahead next =
                            new StateItemWithLookahead(nextSI, last.lookahead);
                    List<StateItemWithLookahead> nextPath =
                            new LinkedList<>(path);
                    nextPath.add(next);
                    queue.add(nextPath);
                }
            }
            if (StateItem.prods.containsKey(last.si)) {
                production prod = last.si.item.the_production();
                int len = prod.rhs_length();
                int pos = last.si.item.dot_pos() + 1;
                Set<symbol> lookahead;
                if (pos == len)
                    lookahead = last.lookahead;
                else {
                    symbol sym = rhs(prod, pos);
                    if (sym instanceof terminal)
                        lookahead = StateItem.symbolSet(sym);
                    else lookahead =
                            StateItem.symbolSet(((non_terminal) sym).first_set());
                }
                for (lalr_item itm : StateItem.prods.get(last.si)) {
                    StateItem nextSI = StateItem.lookup(last.si.state, itm);
                    StateItemWithLookahead next =
                            new StateItemWithLookahead(nextSI, lookahead);
                    List<StateItemWithLookahead> nextPath =
                            new LinkedList<>(path);
                    nextPath.add(next);
                    queue.add(nextPath);
                }
            }
        }
        throw new Error("Cannot find shortest path to conflict state.");
    }

    protected class StateItemWithLookahead {
        protected StateItem si;
        protected Set<symbol> lookahead;

        protected StateItemWithLookahead(StateItem si, Set<symbol> lookahead) {
            this.si = si;
            this.lookahead = lookahead;
        }

        @Override
        public String toString() {
            return si.toString() + lookahead.toString();
        }

        @Override
        public int hashCode() {
            return si.hashCode() * 31 + lookahead.hashCode();
        }

        protected boolean equals(StateItemWithLookahead sil) {
            return si.equals(sil.si) && lookahead.equals(sil.lookahead);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof StateItemWithLookahead)) return false;
            return equals((StateItemWithLookahead) o);
        }
    }

    public void find() {
        StateItem.init();
        findExample();
    }

    protected void findExample() {
        SearchState initial =
                new SearchState(StateItem.lookup(conflict, itm1),
                                StateItem.lookup(conflict, itm2));
        long start;
        Map<Integer, FixedComplexitySearchState> fcssMap = new HashMap<>();
        PriorityQueue<FixedComplexitySearchState> pq = new PriorityQueue<>();
        Map<List<StateItem>, Set<List<StateItem>>> visited = new HashMap<>();
        add(pq, fcssMap, visited, initial);
        start = System.nanoTime();
        boolean assurancePrinted = false;
        SearchState stage3result = null;
        while (!pq.isEmpty()) {
            FixedComplexitySearchState fcss = pq.remove();
            for (SearchState ss : fcss.sss) {
                int stage = 2;
                StateItem si1src = ss.states1.get(0);
                StateItem si2src = ss.states2.get(0);
                visited(visited, ss);
                if (ss.reduceDepth < 0 && ss.shiftDepth < 0) {
                    // Stage 3
                    if (!scpSet.contains(si1src.state)
                            || !scpSet.contains(si2src.state)) {
                        // The current head state is not on the shortest path.
                        // Ignore this search state.
                        continue;
                    }
                    stage = 3;
                    if (si1src.item.the_production().lhs().the_symbol() == si2src.item.the_production()
                            .lhs()
                            .the_symbol()
                            && hasCommonPrefix(si1src.item, si2src.item)) {
                        if (ss.derivs1.size() == 1
                                && ss.derivs2.size() == 1
                                && ss.derivs1.get(0).sym == ss.derivs2.get(0).sym) {
                            System.err.println(ss.derivs1.get(0).prettyPrint());
                            System.err.println(ss.derivs1);
                            System.err.println(ss.derivs2);
                            System.err.println(ss.states1);
                            System.err.println(ss.states2);
                            System.err.println(ss.complexity);
                            return;
                        }
                        if (stage3result == null) stage3result = ss;
                        stage = 4;
                    }
                }
                if (!assurancePrinted
                        && System.nanoTime() - start > ASSURANCE_LIMIT
                        && stage3result != null) {
                    System.err.println("Productions leading up to the conflict stage found.  Finding a possible unified example...");
                    assurancePrinted = true;
                }
                if (System.nanoTime() - start > TIME_LIMIT
                        && stage3result != null) {
                    System.err.println("time limit exceeded: "
                            + (System.nanoTime() - start));
                    System.err.println(stage3result.derivs1);
                    System.err.println(stage3result.derivs2);
                    System.err.println(stage3result.states1);
                    System.err.println(stage3result.states2);
                    System.err.println(stage3result.complexity);
                    return;
                }
                StateItem si1 = ss.states1.get(ss.states1.size() - 1);
                StateItem si2 = ss.states2.get(ss.states2.size() - 1);
                boolean si1reduce = si1.item.dot_at_end();
                boolean si2reduce = si2.item.dot_at_end();
                symbol si1sym = si1reduce ? null : si1.item.symbol_after_dot();
                symbol si2sym = si2reduce ? null : si2.item.symbol_after_dot();
                if (!si1reduce && !si2reduce) {
                    // Both paths are not reduce items.
                    // Two actions are possible:
                    // - Make a transition on the next symbol of the items,
                    //   if they are the same.
                    // - Take a production step, avoiding duplicates as necessary.
                    if (si1sym == si2sym) {
                        StateItem si1last =
                                StateItem.trans.get(si1).get(si1sym);
                        StateItem si2last =
                                StateItem.trans.get(si2).get(si2sym);
                        List<Derivation> derivs1 = new LinkedList<>();
                        List<Derivation> derivs2 = new LinkedList<>();
                        List<StateItem> states1 = new LinkedList<>();
                        List<StateItem> states2 = new LinkedList<>();
                        derivs1.add(new Derivation(si1sym));
                        states1.add(si1last);
                        derivs2.add(new Derivation(si2sym));
                        states2.add(si2last);
                        // Check for subsequent nullable nonterminals
                        production prod1 = si1.item.the_production();
                        for (int pos = si1.item.dot_pos() + 1, len1 =
                                prod1.rhs_length(); pos < len1; pos++) {
                            symbol sp = rhs(prod1, pos);
                            if (!sp.is_non_term()) break;
                            non_terminal nt = (non_terminal) sp;
                            if (!nt.nullable()) break;
                            si1last = StateItem.trans.get(si1last).get(nt);
                            derivs1.add(new Derivation(nt,
                                                       Collections.<Derivation> emptyList()));
                            states1.add(si1last);
                        }
                        production prod2 = si2.item.the_production();
                        for (int pos = si1.item.dot_pos() + 1, len =
                                prod2.rhs_length(); pos < len; pos++) {
                            symbol sp = rhs(prod2, pos);
                            if (!sp.is_non_term()) break;
                            non_terminal nt = (non_terminal) sp;
                            if (!nt.nullable()) break;
                            si2last = StateItem.trans.get(si2last).get(nt);
                            derivs2.add(new Derivation(nt,
                                                       Collections.<Derivation> emptyList()));
                            states2.add(si2last);
                        }
                        for (int i = 1, size1 = derivs1.size(); i <= size1; i++) {
                            List<Derivation> subderivs1 =
                                    new ArrayList<>(derivs1.subList(0, i));
                            List<StateItem> substates1 =
                                    new ArrayList<>(states1.subList(0, i));
                            for (int j = 1, size2 = derivs2.size(); j <= size2; j++) {
                                List<Derivation> subderivs2 =
                                        new ArrayList<>(derivs2.subList(0, j));
                                List<StateItem> substates2 =
                                        new ArrayList<>(states2.subList(0, j));
                                SearchState copy = ss.copy();
                                copy.derivs1.addAll(subderivs1);
                                copy.states1.addAll(substates1);
                                copy.derivs2.addAll(subderivs2);
                                copy.states2.addAll(substates2);
                                copy.complexity += 2 * SHIFT_COST;
                                add(pq, fcssMap, visited, copy);
                            }
                        }
                    }
                    if (si1sym instanceof non_terminal
                            && StateItem.prods.containsKey(si1))
                        for (lalr_item itm1 : StateItem.prods.get(si1)) {
                            // Take production step only if lhs is not nullable and
                            // if first rhs symbol is compatible with the other path
                            boolean applicable =
                                    !itm1.dot_at_end()
                                            && compatible(itm1.symbol_after_dot(),
                                                          si2sym);
                            if (!applicable) continue;
                            SearchState copy = ss.copy();
                            StateItem next = StateItem.lookup(si1.state, itm1);
                            // TODO
                            if (copy.states1.contains(next))
                                copy.complexity += DUPLICATE_PRODUCTION_COST;
                            copy.states1.add(next);
                            copy.complexity += PRODUCTION_COST;
                            add(pq, fcssMap, visited, copy);
                        }
                    if (si2sym instanceof non_terminal
                            && StateItem.prods.containsKey(si2))
                        for (lalr_item itm2 : StateItem.prods.get(si2)) {
                            // Take production step only if lhs is not nullable and
                            // if first rhs symbol is compatible with the other path
                            boolean applicable =
                                    !itm2.dot_at_end()
                                            && compatible(itm2.symbol_after_dot(),
                                                          si1sym);
                            if (!applicable) continue;
                            SearchState copy = ss.copy();
                            StateItem next = StateItem.lookup(si2.state, itm2);
                            // TODO
                            if (copy.states2.contains(next))
                                copy.complexity += DUPLICATE_PRODUCTION_COST;
                            copy.states2.add(next);
                            if (ss.shiftDepth >= 0) copy.shiftDepth++;
                            copy.complexity += PRODUCTION_COST;
                            add(pq, fcssMap, visited, copy);
                        }
                }
                else {
                    // One of the paths requires a reduction.
                    production prod1 = si1.item.the_production();
                    int len1 = prod1.rhs_length();
                    production prod2 = si2.item.the_production();
                    int len2 = prod2.rhs_length();
                    int size1 = ss.states1.size();
                    int size2 = ss.states2.size();
                    boolean ready1 = si1reduce && size1 > len1;
                    boolean ready2 = si2reduce && size2 > len2;
                    // If there is a path ready for reduction
                    // without being prepended further, reduce.
                    if (ready1) {
                        List<SearchState> reduced1 = ss.reduce1(si2sym);
                        if (ready2) {
                            reduced1.add(ss);
                            for (SearchState red1 : reduced1) {
                                for (SearchState candidate : red1.reduce2(si1sym))
                                    add(pq, fcssMap, visited, candidate);
                                if (si1reduce && red1 != ss)
                                    add(pq, fcssMap, visited, red1);
                            }
                        }
                        else {
                            for (SearchState candidate : reduced1)
                                add(pq, fcssMap, visited, candidate);
                        }
                    }
                    else if (ready2) {
                        List<SearchState> reduced2 = ss.reduce2(si1sym);
                        for (SearchState candidate : reduced2)
                            add(pq, fcssMap, visited, candidate);
                    }
                    // Otherwise, prepend both paths and continue.
                    else {
                        symbol sym;
                        if (si1reduce && !ready1)
                            sym = rhs(prod1, len1 - size1);
                        else sym = rhs(prod2, len2 - size2);
                        for (SearchState prepended : ss.prepend(sym,
                                                                null,
                                                                null,
                                                                ss.reduceDepth >= 0
                                                                ? rppSet
                                                                        : scpSet
                                                                        /*: ss.shiftDepth < 0
                        ? scpSet
                        : null*/)) {
                            add(pq, fcssMap, visited, prepended);
                        }
                    }
                }
            }
            fcssMap.remove(fcss.complexity);
        }
    }

    protected void add(PriorityQueue<FixedComplexitySearchState> pq,
            Map<Integer, FixedComplexitySearchState> fcssMap,
            Map<List<StateItem>, Set<List<StateItem>>> visited, SearchState ss) {
        Set<List<StateItem>> visited1 = visited.get(ss.states1);
        if (visited1 != null && visited1.contains(ss.states2)) return;
        FixedComplexitySearchState fcss = fcssMap.get(ss.complexity);
        if (fcss == null) {
            fcss = new FixedComplexitySearchState(ss.complexity);
            fcssMap.put(ss.complexity, fcss);
            pq.add(fcss);
        }
        fcss.add(ss);
    }

    protected void visited(Map<List<StateItem>, Set<List<StateItem>>> visited,
            SearchState ss) {
        Set<List<StateItem>> visited1 = visited.get(ss.states1);
        if (visited1 == null) {
            visited1 = new HashSet<>();
            visited.put(ss.states1, visited1);
        }
        visited1.add(ss.states2);
    }

    protected static class FixedComplexitySearchState implements
            Comparable<FixedComplexitySearchState> {
        protected int complexity;
        protected Set<SearchState> sss;

        protected FixedComplexitySearchState(int complexity) {
            this.complexity = complexity;
            sss = new HashSet<>();
        }

        protected void add(SearchState ss) {
            sss.add(ss);
        }

        @Override
        public int compareTo(FixedComplexitySearchState o) {
            return complexity - o.complexity;
        }
    }

    /**
     * A search state consists of
     * - a pair of the following items:
     *   - a list of derivations, simulating the parser's symbol stack
     *   - a list of StateItems, simulating the parser's state stack but
     *     including explicit production steps
     *   Each item in the pair corresponds to a possible parse that involves
     *   each of the conflict LALR items.
     * - a complexity of the partial parse trees, determined by the number of
     *   states and production steps the parse has to encounter
     * - a shift depth, indicating the number of unreduced production steps
     *   that has been made from the original shift item.  This helps keep
     *   track of when the shift conflict item is reduced.
     * @author Chinawat
     *
     */
    protected class SearchState implements Comparable<SearchState> {

        protected List<Derivation> derivs1, derivs2;
        protected List<StateItem> states1, states2;
        protected int complexity, reduceDepth, shiftDepth;

        protected SearchState(StateItem si1, StateItem si2) {
            derivs1 = new LinkedList<>();
            derivs2 = new LinkedList<>();
            states1 = new LinkedList<>();
            states2 = new LinkedList<>();
            states1.add(si1);
            states2.add(si2);
            complexity = 0;
            reduceDepth = 0;
            shiftDepth = 0;
        }

        private SearchState(List<Derivation> derivs1, List<Derivation> derivs2,
                List<StateItem> states1, List<StateItem> states2,
                int complexity, int reduceDepth, int shiftDepth) {
            this.derivs1 = new LinkedList<>(derivs1);
            this.derivs2 = new LinkedList<>(derivs2);
            this.states1 = new LinkedList<>(states1);
            this.states2 = new LinkedList<>(states2);
            this.complexity = complexity;
            this.reduceDepth = reduceDepth;
            this.shiftDepth = shiftDepth;
        }

        protected SearchState copy() {
            return new SearchState(derivs1,
                                   derivs2,
                                   states1,
                                   states2,
                                   complexity,
                                   reduceDepth,
                                   shiftDepth);
        }

        protected List<SearchState> prepend(symbol sym, symbol nextSym1,
                                            symbol nextSym2) {
            return prepend(sym, nextSym1, nextSym2, null);
        }

        protected List<SearchState> prepend(symbol sym, symbol nextSym1,
                symbol nextSym2, Set<lalr_state> guide) {
            List<SearchState> result = new LinkedList<>();
            SearchState ss = this;
            StateItem si1src = ss.states1.get(0);
            StateItem si2src = ss.states2.get(0);
            Set<symbol> si1lookahead =
                    nextSym1 == null
                    ? StateItem.symbolSet(si1src.item.lookahead())
                            : symbolSet(nextSym1);
                    Set<symbol> si2lookahead =
                            nextSym2 == null
                            ? StateItem.symbolSet(si2src.item.lookahead())
                                    : symbolSet(nextSym2);
                            List<List<StateItem>> prev1 =
                                    si1src.reverseTransition(sym, si1lookahead, guide);
                            List<List<StateItem>> prev2 =
                                    si2src.reverseTransition(sym, si2lookahead, guide);
                            for (List<StateItem> psis1 : prev1) {
                                StateItem psi1 = psis1.isEmpty() ? si1src : psis1.get(0);
                                for (List<StateItem> psis2 : prev2) {
                                    StateItem psi2 = psis2.isEmpty() ? si2src : psis2.get(0);
                                    if (psi1 == si1src && psi2 == si2src) continue;
                                    if (psi1.state != psi2.state) continue;
                                    SearchState copy = ss.copy();
                                    copy.states1.addAll(0, psis1);
                                    copy.states2.addAll(0, psis2);
                                    if (!psis1.isEmpty()
                                            && copy.states1.get(0).item.dot_pos() + 1 == copy.states1.get(1).item.dot_pos()) {
                                        if (!psis2.isEmpty()
                                                && copy.states2.get(0).item.dot_pos() + 1 == copy.states2.get(1).item.dot_pos()) {
                                            Derivation deriv = new Derivation(sym);
                                            copy.derivs1.add(0, deriv);
                                            copy.derivs2.add(0, deriv);
                                        }
                                        else continue;
                                    }
                                    else if (!psis2.isEmpty()
                                            && copy.states2.get(0).item.dot_pos() + 1 == copy.states2.get(1).item.dot_pos()) {
                                        continue;
                                    }
                                    int prependSize = psis1.size() + psis2.size();
                                    int productionSteps =
                                            productionSteps(psis1, si1src)
                                            + productionSteps(psis2, si2src);
                                    copy.complexity +=
                                            UNSHIFT_COST * (prependSize - productionSteps)
                                            + PRODUCTION_COST * productionSteps;
                                    result.add(copy);
                                }
                            }
            return result;
        }

        protected List<SearchState> reduce1(symbol nextSym) {
            List<StateItem> states = states1;
            List<Derivation> derivs = derivs1;
            int sSize = states.size();
            lalr_item item = states.get(sSize - 1).item;
            if (!item.dot_at_end())
                throw new Error("Cannot reduce item without dot at end.");
            List<SearchState> result = new LinkedList<>();
            Set<symbol> symbolSet =
                    nextSym == null
                    ? StateItem.symbolSet(item.lookahead())
                            : symbolSet(nextSym);
                    if (!StateItem.intersect(item.lookahead(), symbolSet))
                        return result;
                    production prod = item.the_production();
                    symbol lhs = prod.lhs().the_symbol();
                    int len = prod.rhs_length();
                    int dSize = derivs.size();
                    Derivation deriv =
                            new Derivation(lhs, new LinkedList<>(derivs.subList(dSize
                            - len, dSize)));
                    if (reduceDepth == 0) {
                        deriv.deriv.add(itm1.dot_pos(), Derivation.dot);
                        reduceDepth--;
                    }
                    derivs = new LinkedList<>(derivs.subList(0, dSize - len));
                    derivs.add(deriv);
                    if (sSize == len + 1) {
                        // The head StateItem is a production item, so we need to prepend
                        // with possible source StateItems.
                        List<List<StateItem>> prev =
                                states.get(0).reverseProduction(symbolSet);
                        for (List<StateItem> psis : prev) {
                            SearchState copy = copy();
                            copy.derivs1 = derivs;
                            copy.states1 =
                                    new LinkedList<>(states1.subList(0, sSize - len - 1));
                            copy.states1.addAll(0, psis);
                            copy.states1.add(StateItem.trans.get(copy.states1.get(copy.states1.size() - 1))
                                                    .get(lhs));
                            int statesSize = copy.states1.size();
                            int productionSteps =
                                    productionSteps(copy.states1, states.get(0));
                            copy.complexity +=
                                    UNSHIFT_COST * (statesSize - productionSteps)
                                    + PRODUCTION_COST * productionSteps;
                            result.add(copy);
                        }
                    }
                    else {
                        SearchState copy = copy();
                        copy.derivs1 = derivs;
                        copy.states1 =
                                new LinkedList<>(states1.subList(0, sSize - len - 1));
                        copy.states1.add(StateItem.trans.get(copy.states1.get(copy.states1.size() - 1))
                                                .get(lhs));
                        copy.complexity += REDUCE_COST;
                        result.add(copy);
                    }
                    return result;
        }

        protected List<SearchState> reduce2(symbol nextSym) {
            List<StateItem> states = states2;
            List<Derivation> derivs = derivs2;
            int sSize = states.size();
            lalr_item item = states.get(sSize - 1).item;
            if (!item.dot_at_end())
                throw new Error("Cannot reduce item without dot at end.");
            List<SearchState> result = new LinkedList<>();
            Set<symbol> symbolSet =
                    nextSym == null
                    ? StateItem.symbolSet(item.lookahead())
                            : symbolSet(nextSym);
                    if (!StateItem.intersect(item.lookahead(), symbolSet))
                        return result;
                    production prod = item.the_production();
                    symbol lhs = prod.lhs().the_symbol();
                    int len = prod.rhs_length();
                    int dSize = derivs.size();
                    Derivation deriv =
                            new Derivation(lhs, new LinkedList<>(derivs.subList(dSize
                            - len, dSize)));
                    if (shiftDepth == 0)
                        deriv.deriv.add(itm2.dot_pos(), Derivation.dot);
                    derivs = new LinkedList<>(derivs.subList(0, dSize - len));
                    derivs.add(deriv);
                    if (sSize == len + 1) {
                        // The head StateItem is a production item, so we need to prepend
                        // with possible source StateItems.
                        List<List<StateItem>> prev =
                                states.get(0).reverseProduction(symbolSet);
                        for (List<StateItem> psis : prev) {
                            SearchState copy = copy();
                            copy.derivs2 = derivs;
                            copy.states2 =
                                    new LinkedList<>(states.subList(0, sSize - len - 1));
                            copy.states2.addAll(0, psis);
                            copy.states2.add(StateItem.trans.get(copy.states2.get(copy.states2.size() - 1))
                                                    .get(lhs));
                            int statesSize = copy.states2.size();
                            int productionSteps =
                                    productionSteps(copy.states2, states.get(0));
                            copy.complexity +=
                                    SHIFT_COST * (statesSize - productionSteps)
                                    + PRODUCTION_COST * productionSteps;
                            if (copy.shiftDepth >= 0) copy.shiftDepth--;
                            result.add(copy);
                        }
                    }
                    else {
                        SearchState copy = copy();
                        copy.derivs2 = derivs;
                        copy.states2 =
                                new LinkedList<>(states.subList(0, sSize - len - 1));
                        copy.states2.add(StateItem.trans.get(copy.states2.get(copy.states2.size() - 1))
                                                .get(lhs));
                        copy.complexity += REDUCE_COST;
                        if (copy.shiftDepth >= 0) copy.shiftDepth--;
                        result.add(copy);
                    }
                    return result;
        }

        @Override
        public int compareTo(SearchState o) {
            int diff = complexity - o.complexity;
            if (diff != 0) return diff;
            return reductionStreak(states1) + reductionStreak(states2)
                    - (reductionStreak(o.states1) + reductionStreak(o.states2));
        }

        @Override
        public int hashCode() {
            return states1.hashCode() * 31 + states2.hashCode();
        }

        protected boolean equals(SearchState ss) {
            return states1.equals(ss.states1) && states2.equals(ss.states2);
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof SearchState) return equals((SearchState) o);
            return false;
        }
    }

    protected static boolean samePrefix(lalr_item itm1, lalr_item itm2) {
        if (itm1.dot_pos() != itm2.dot_pos()) return false;
        production prod1 = itm1.the_production();
        production prod2 = itm2.the_production();
        for (int i = 0, len = itm1.dot_pos(); i < len; i++)
            if (rhs(prod1, i) != rhs(prod2, i)) return false;
        return true;
    }

    protected static int productionSteps(List<StateItem> sis, StateItem last) {
        int count = 0;
        lalr_state lastState = last.state;
        for (ListIterator<StateItem> itr = sis.listIterator(sis.size()); itr.hasPrevious();) {
            StateItem si = itr.previous();
            lalr_state state = si.state;
            if (state == lastState) count++;
            lastState = state;
        }
        return count;
    }

    protected static boolean compatible(symbol sym1, symbol sym2) {
        if (sym1 instanceof terminal) {
            if (sym2 instanceof terminal) return sym1 == sym2;
            non_terminal nt2 = (non_terminal) sym2;
            return nt2.first_set().contains((terminal) sym1);
        }
        else {
            non_terminal nt1 = (non_terminal) sym1;
            if (sym2 instanceof terminal)
                return nt1.first_set().contains((terminal) sym2);
            non_terminal nt2 = (non_terminal) sym2;
            return nt1 == nt2 || nt1.first_set().intersects(nt2.first_set());
        }
    }

    protected static int reductionStreak(List<StateItem> sis) {
        int count = 0;
        StateItem last = null;
        for (ListIterator<StateItem> itr = sis.listIterator(sis.size()); itr.hasPrevious();) {
            StateItem si = itr.previous();
            if (last == null) {
                last = si;
                continue;
            }
            if (si.state != last.state) break;
            count++;
        }
        return count;
    }

    protected static boolean hasCommonPrefix(lalr_item itm1, lalr_item itm2) {
        if (itm1.dot_pos() != itm2.dot_pos()) return false;
        int dotPos = itm1.dot_pos();
        production prod1 = itm1.the_production();
        production prod2 = itm2.the_production();
        for (int i = 0; i < dotPos; i++)
            if (rhs(prod1, i) != rhs(prod2, i)) return false;
        return true;
    }

    protected static Set<symbol> symbolSet(symbol sym) {
        Set<symbol> result = new HashSet<>();
        result.add(sym);
        return result;
    }

    protected static symbol rhs(production prod, int pos) {
        symbol_part sp = (symbol_part) prod.rhs(pos);
        return sp.the_symbol();
    }

    /**
     * A derivation consists of
     * - a symbol
     * - a list of derivations that derives the symbol
     * @author Chinawat
     *
     */
    protected static class Derivation {
        public static final Derivation dot = new Derivation(new symbol("(*)") {
            @Override
            public boolean is_non_term() {
                throw new UnsupportedOperationException();
            }
        });

        protected final symbol sym;
        protected final List<Derivation> deriv;

        protected Derivation(symbol sym) {
            this(sym, null);
        }

        protected Derivation(symbol sym, List<Derivation> deriv) {
            this.sym = sym;
            this.deriv = deriv;
        }

        protected int size() {
            int size = 1;
            if (deriv != null) {
                for (Derivation d : deriv)
                    size += d.size();
            }
            return size;
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer(sym.name());
            if (deriv != null) {
                sb.append(" ::= [");
                boolean tail = false;
                for (Derivation d : deriv) {
                    if (tail)
                        sb.append(" ");
                    else tail = true;
                    sb.append(d);
                }
                sb.append("]");
            }
            return sb.toString();
        }

        public String prettyPrint() {
            if (deriv == null) return sym.name();

            StringBuffer sb = new StringBuffer();
            for (Derivation d : deriv) {
                if (sb.length() != 0) sb.append(" ");
                sb.append(d.prettyPrint());
            }
            return sb.toString();
        }

        protected boolean equals(Derivation d) {
            return sym == d.sym;
        }

        @Override
        public int hashCode() {
            return sym.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Derivation) return equals((Derivation) o);
            return false;
        }
    }
}
