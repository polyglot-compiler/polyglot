package parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java_cup.Main;
import java_cup.assoc;
import java_cup.lalr_item;
import java_cup.lalr_state;
import java_cup.lalr_transition;
import java_cup.non_terminal;
import java_cup.production;
import java_cup.symbol;
import java_cup.symbol_part;
import java_cup.terminal;
import java_cup.terminal_set;

/**
 * A representation of a parser item.
 * CUP's lalr_item class does not include a corresponding parser state; this
 * class encapsulates both state and item into one object.
 *
 */
public class StateItem {
    protected final lalr_state state;
    protected final lalr_item item;

    protected StateItem(lalr_state state, lalr_item item) {
        this.state = state;
        this.item = item;
    }

    /**
     * Compute a set of StateItems that can make a transition on the given
     * symbol to this StateItem such that the resulting possible lookahead
     * symbols are as given.
     * @param sym The symbol to make a transition on.
     * @param lookahead The expected possible lookahead symbols
     * @param guide If not null, restricts the possible parser states to this
     *          set; otherwise, explore all possible parser states that can
     *          make the desired transition.
     * @return A set of StateItems that result from making a reverse transition
     *          from this StateItem on the given symbol and lookahead set.
     */
    protected List<StateItem> reverseTransition(symbol sym,
            Set<symbol> lookahead, Set<lalr_state> guide) {
        List<StateItem> result = new LinkedList<>();
        result.add(null);
        List<StateItem> init = new LinkedList<>();
        init.add(this);
        SearchState ss = new SearchState(init, lookahead);
        StateItem si = ss.sis.get(0);
        if (si.item.dot_pos() > 0) {
            Set<StateItem> prevs = revTrans.get(si).get(sym);
            if (prevs == null) return result;
            // There are StateItems that can make a transition on sym
            // to the current StateItem.  Now, check if the lookahead
            // is compatible.
            for (StateItem prev : prevs) {
                if (guide != null && !guide.contains(prev.state)) continue;
                if (ss.lookahead != null
                        && !intersect(prev.item.lookahead(), ss.lookahead))
                    continue;
                result.add(prev);
            }
            return result;
        }
        // Consider items in the same state that might use this
        // production.
        for (SearchState candidate : ss.reverseProduction(false)) {
            List<StateItem> seq = new LinkedList<>(candidate.sis);
            seq.remove(seq.size() - 1);
            // XXX
            result.add(seq.get(0));
        }
        return result;
    }

    /**
     * Compute a set of sequences of StateItems that can make production steps
     * to this StateItem such that the resulting possible lookahead symbols are
     * as given.
     * @param lookahead The expected possible lookahead symbols.
     * @return A set of sequences of StateItems that result from making reverse
     *          production steps from this StateItem on the given lookahead set.
     */
    protected List<List<StateItem>> reverseProduction(Set<symbol> lookahead) {
        List<List<StateItem>> result = new LinkedList<>();
        List<StateItem> init = new LinkedList<>();
        init.add(this);
        for (SearchState ss : new SearchState(init,
                                              lookahead).reverseProduction(false)) {
            List<StateItem> seq = new LinkedList<>(ss.sis);
            seq.remove(seq.size() - 1);
            result.add(seq);
        }
        return result;
    }

    @Override
    public String toString() {
        return state.index() + " " + item;
    }

    /**
     * A search state for previous item consists of a list of StateItems
     * indicating a sequence of transitions and production steps,
     * along with the required lookahead, which could be null.
     *
     */
    protected static class SearchState {
        protected List<StateItem> sis;
        protected Set<symbol> lookahead;

        protected SearchState(List<StateItem> sis, Set<symbol> lookahead) {
            this.sis = sis;
            this.lookahead = lookahead;
        }

        /**
         * Compute a set of SearchState that can make a production steps to this
         * SearchState.
         * @param uniqueItems True if the sequence of StateItems should not be
         *              repeated; false otherwise.
         * @return A set of SearchState that result from making a reverse
         *              production step from this SearchState.
         */
        protected List<SearchState> reverseProduction(boolean uniqueItems) {
            List<SearchState> result = new LinkedList<>();
            StateItem si = sis.get(0);
            Map<non_terminal, Set<lalr_item>> revProd = revProds.get(si.state);
            if (revProd != null) {
                production prod = si.item.the_production();
                symbol lhs = prod.lhs().the_symbol();
                Set<lalr_item> prevs = revProd.get(lhs);
                if (prevs == null) return result;
                // A production step was made to the current lalr_item.
                // Check that the next symbol in the parent lalr_item is
                // compatible with the lookahead.
                for (lalr_item prev : prevs) {
                    production prevProd = prev.the_production();
                    if (!StateItem.productionAllowed(prevProd, prod)) continue;
                    StateItem prevsi = lookup(si.state, prev);
                    // Avoid reusing the same item if desired.
                    if (uniqueItems && sis.contains(prevsi)) continue;
                    int prevLen = prevProd.rhs_length();
                    int prevPos = prev.dot_pos() + 1;
                    terminal_set prevLookahead = prev.lookahead();
                    Set<symbol> nextLookahead;
                    if (prevPos == prevLen) { // reduce item
                        // Check that some lookaheads can be preserved.
                        if (!intersect(prevLookahead, lookahead)) continue;
                        nextLookahead = new HashSet<>(symbolSet(prevLookahead));
                        nextLookahead.retainAll(lookahead);
                    }
                    else { // shift item
                        if (lookahead != null) {
                            // Check that lookahead is compatible with the first
                            // possible symbols in the rest of the production.
                            // Alternatively, if the rest of the production is
                            // nullable, the lookahead must be compatible with
                            // the lookahead of the corresponding item.
                            boolean applicable = false;
                            boolean nullable = true;
                            for (int pos = prevPos; !applicable && nullable
                                    && pos < prevLen; pos++) {
                                symbol nextSym = rhs(prevProd, pos);
                                if (nextSym instanceof terminal) {
                                    applicable = intersect((terminal) nextSym,
                                                           lookahead);
                                    nullable = false;
                                }
                                else if (nextSym instanceof non_terminal) {
                                    non_terminal nt = (non_terminal) nextSym;
                                    applicable = intersect(nt.first_set(),
                                                           lookahead);
                                    if (!applicable) nullable = nt.nullable();
                                }
                            }
                            if (!applicable && !nullable) continue;
                        }
                        nextLookahead = symbolSet(prevLookahead);
                    }
                    List<StateItem> seq = new LinkedList<>(sis);
                    seq.add(0, prevsi);
                    result.add(new SearchState(seq, nextLookahead));
                }
            }
            return result;
        }
    }

    /**
     * Determine if the given terminal is in the given symbol set or can begin
     * a nonterminal in the given symbol set.
     * @param t A terminal
     * @param syms A symbol set
     * @return true if {@code t} is in {@code syms} or can begin a nonterminal
     *          in {@code syms}; false otherwise
     */
    protected static boolean intersect(terminal t, Set<symbol> syms) {
        if (syms == null) return true;
        for (symbol sym : syms) {
            if (sym instanceof terminal && t.equals(sym)) return true;
            if (sym instanceof non_terminal
                    && ((non_terminal) sym).first_set().contains(t))
                return true;
        }
        return false;
    }

    /**
     * Determine if any symbol in the given terminal is in the given symbol set
     * or can begin a nonterminal in the given symbol set.
     * @param ts A terminal set
     * @param syms A symbol set
     * @return true if some terminal in {@code ts} is in {@code syms} or can
     *          begin a nonterminal in {@code syms}; false otherwise
     */
    protected static boolean intersect(terminal_set ts, Set<symbol> syms) {
        if (syms == null) return true;
        for (symbol sym : syms) {
            if (sym instanceof terminal && ts.contains((terminal) sym))
                return true;
            if (sym instanceof non_terminal
                    && ts.intersects(((non_terminal) sym).first_set()))
                return true;
        }
        return false;
    }

    /**
     * Clear parser state maps.
     */
    public static void clear() {
        stateItms = null;
        trans = null;
        if (revTrans != null) revTrans.clear();
        prods = null;
        if (revProds != null) revProds.clear();
        symbolSets.clear();
    }

    /**
     * Report various statistics on parser state maps.
     */
    public static void report() {
        init();
        int stateItmSize = 0;
        for (Map<lalr_item, StateItem> stateItm : stateItms.values())
            stateItmSize += stateItm.size();
        System.out.println("items:\n" + stateItmSize);
        int prodSize = 0;
        for (Set<lalr_item> prod : prods.values())
            prodSize += prod.size();
        System.out.println("productions:\n" + prodSize);
        int revTranSize = 0;
        for (Map<symbol, Set<StateItem>> revTran : revTrans.values())
            for (Set<StateItem> rev : revTran.values())
                revTranSize += rev.size();
        System.out.println("reverse transitions:\n" + revTranSize);
        int revProdSize = 0;
        for (Map<non_terminal, Set<lalr_item>> revProd : revProds.values())
            for (Set<lalr_item> rev : revProd.values())
                revProdSize += rev.size();
        System.out.println("reverse productions:\n" + revProdSize);
    }

    /** Map: state -> item -> StateItem */
    protected static Map<lalr_state, Map<lalr_item, StateItem>> stateItms;

    /**
     * Initialize stateItms map.
     */
    protected static void initStateItms() {
        stateItms = new HashMap<>();
        for (lalr_state state : lalr_state.all_states()) {
            Map<lalr_item, StateItem> itms = new HashMap<>();
            for (lalr_item item : state.items()) {
                StateItem si = new StateItem(state, item);
                itms.put(item, si);
            }
            stateItms.put(state, itms);
        }
    }

    /**
     * Lookup method for stateItms map
     * @param state
     * @param item
     * @return
     */
    protected static StateItem lookup(lalr_state state, lalr_item item) {
        Map<lalr_item, StateItem> itms = stateItms.get(state);
        return itms == null ? null : itms.get(item);
    }

    /** Transition map: StateItem -> symbol -> StateItem */
    protected static Map<StateItem, Map<symbol, StateItem>> trans;
    /** Reverse transition map: StateItem -> symbol -> Set of StateItems */
    protected static Map<StateItem, Map<symbol, Set<StateItem>>> revTrans;

    /**
     * Initialize trans and revTrans maps.
     */
    protected static void initTrans() {
        trans = new HashMap<>();
        revTrans = new HashMap<>();
        for (lalr_state src : lalr_state.all_states()) {
            Map<symbol, lalr_state> transitions = new HashMap<>();
            for (lalr_transition t = src.transitions(); t != null; t =
                    t.next()) {
                symbol sym = t.on_symbol();
                lalr_state dst = t.to_state();
                transitions.put(sym, dst);
            }
            for (lalr_item srcItm : src.items()) {
                if (srcItm.dot_at_end()) continue;
                production prod = srcItm.the_production();
                int expectedDotPos = srcItm.dot_pos() + 1;
                symbol sym = srcItm.symbol_after_dot();
                lalr_state dst = transitions.get(sym);
                for (lalr_item dstItm : dst.items()) {
                    if (prod != dstItm.the_production()) continue;
                    if (expectedDotPos != dstItm.dot_pos()) continue;
                    // We have found the target item after transition on sym
                    // from the source item.
                    StateItem srcSI = lookup(src, srcItm);
                    StateItem dstSI = lookup(dst, dstItm);
                    Map<symbol, StateItem> tran = trans.get(srcSI);
                    if (tran == null) {
                        tran = new HashMap<>();
                        trans.put(srcSI, tran);
                    }
                    tran.put(sym, dstSI);
                    Map<symbol, Set<StateItem>> revTran = revTrans.get(dstSI);
                    if (revTran == null) {
                        revTran = new HashMap<>();
                        revTrans.put(dstSI, revTran);
                    }
                    Set<StateItem> srcs = revTran.get(sym);
                    if (srcs == null) {
                        srcs = new HashSet<>();
                        revTran.put(sym, srcs);
                    }
                    srcs.add(srcSI);
                    break;
                }
            }
        }
    }

    /** Production map within the same state: StateItem -> Set of items */
    protected static Map<StateItem, Set<lalr_item>> prods;
    /** Reverse production map: state -> nonterminal -> Set of items */
    protected static Map<lalr_state, Map<non_terminal, Set<lalr_item>>> revProds;

    /**
     * Initialize prods and revProds maps.
     */
    protected static void initProds() {
        prods = new HashMap<>();
        revProds = new HashMap<>();
        for (lalr_state state : lalr_state.all_states()) {
            // closureMap records all items with dot at the beginning of the
            // right-hand side in this state.  In other words, the items
            // recorded are the productions added to this state by taking
            // closure.
            Map<non_terminal, Set<lalr_item>> closureMap = new HashMap<>();
            for (lalr_item item : state.items()) {
                if (item.dot_pos() == 0) {
                    production prod = item.the_production();
                    non_terminal lhs = (non_terminal) prod.lhs().the_symbol();
                    Set<lalr_item> itms = closureMap.get(lhs);
                    if (itms == null) {
                        itms = new HashSet<>();
                        closureMap.put(lhs, itms);
                    }
                    itms.add(item);
                }
            }
            Map<non_terminal, Set<lalr_item>> revProd = new HashMap<>();
            // Now, if the symbol after dot in any item within this state is in
            // the closure map, add the item to the lookup map.
            for (lalr_item item : state.items()) {
                // Avoid reduce items, which cannot make a production step.
                if (item.dot_at_end()) continue;
                symbol sym = item.symbol_after_dot();
                // If next symbol is a terminal, a production step is not
                // possible.
                if (sym instanceof terminal) continue;
                if (closureMap.containsKey(sym)) {
                    non_terminal nt = (non_terminal) sym;
                    StateItem si = lookup(state, item);
                    Set<lalr_item> prod = prods.get(si);
                    if (prod == null) {
                        prod = new HashSet<>();
                        prods.put(si, prod);
                    }
                    prod.addAll(closureMap.get(sym));
                    Set<lalr_item> revItms = revProd.get(nt);
                    if (revItms == null) {
                        revItms = new HashSet<>();
                        revProd.put(nt, revItms);
                    }
                    revItms.add(item);
                }
            }
            revProds.put(state, revProd);
        }
    }

    /**
     * Initialize all maps, if not already.
     */
    protected static void init() {
        long start = System.nanoTime();
        if (stateItms == null) initStateItms();
        if (trans == null) initTrans();
        if (prods == null) initProds();
        if (Main.report_cex_stats) {
            if (Main.report_cex_stats_to_out)
                System.out.println("init:\n" + (System.nanoTime() - start));
            else System.err.println("init: " + (System.nanoTime() - start));
        }
    }

    /** Cache of symbol-set representations of terminal sets */
    protected static Map<terminal_set, Set<symbol>> symbolSets =
            new HashMap<>();

    /**
     * Return a symbol-set representation of the given terminal set.
     * @param ts
     * @return
     */
    protected static Set<symbol> symbolSet(terminal_set ts) {
        Set<symbol> result = symbolSets.get(ts);
        if (result == null) {
            result = new HashSet<>();
            symbolSets.put(ts, result);
            for (terminal t : terminal.all()) {
                if (ts.contains(t)) result.add(t);
            }
        }
        return result;
    }

    /**
     * Return the symbol at the desired position of the given production.
     * @param prod A production
     * @param pos A desired position
     * @return
     */
    protected static symbol rhs(production prod, int pos) {
        symbol_part sp = (symbol_part) prod.rhs(pos);
        return sp.the_symbol();
    }

    /**
     * Determine, using precedence and associativity, whether the next
     * production is allowed from the current production.
     * @param prod The current production.
     * @param nextProd The next production.
     * @return true if {@code nextProd} is possible after {@code prod}; false
     *          otherwise.
     */
    protected static boolean productionAllowed(production prod,
            production nextProd) {
        int prodPred = prod.precedence_num();
        int nextProdPred = nextProd.precedence_num();
        if (prodPred >= 0 && nextProdPred >= 0) {
            // Do not expand if lower precedence.
            if (prodPred > nextProdPred) return false;
            if (prodPred == nextProdPred) {
                int prodAssoc = prod.precedence_side();
                // Do not expand if same precedence, but left-associative.
                if (prodAssoc == assoc.left) return false;
            }
        }
        return true;
    }
}
