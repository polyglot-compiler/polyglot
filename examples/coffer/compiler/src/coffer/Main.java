/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer;

/**
 * Main is the main program of the compiler extension.
 * It simply invokes Polyglot's main, passing in the extension's
 * ExtensionInfo.
 */
public class Main {
    public static void main(String[] args) {
        polyglot.main.Main polyglotMain = new polyglot.main.Main();

        try {
            polyglotMain.start(args, new coffer.ExtensionInfo());
        }
        catch (polyglot.main.Main.TerminationException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
