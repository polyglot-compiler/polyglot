package jltools.main;

import jltools.ast.Node;
import jltools.frontend.Compiler;
import jltools.types.TypeSystem;
import jltools.util.*;
import java.io.*;
import java.util.*;

public class Main
{
    private static final String MAIN_OPT_SOURCE_PATH =
      "Source Path (Coll'n of File";
    
    private static final String MAIN_OPT_OUTPUT_DIRECTORY =
      "Output Directory (File)";
    
    private static final String MAIN_OPT_SOURCE_EXT =
      "Source Extension (String)";
    
    private static final String MAIN_OPT_OUTPUT_EXT =
      "Output Extension (String)";
    
    private static final String MAIN_OPT_STDOUT = "Output to stdout (Boolean)";
    
    private static final String MAIN_OPT_POST_COMPILER =
      "Name of Post Compiler (String)";
    
    private static final String MAIN_OPT_DUMP = "Dump AST (Boolean)";
    
    private static final String MAIN_OPT_SCRAMBLE = "Scramble AST (Boolean)";
    
    private static final String MAIN_OPT_SCRAMBLE_SEED =
      "Scramble Random Seed (Long)";
    
    private static final String MAIN_OPT_EXT_OP =
      "Use ObjectPrimitive Ext (Boolean)";
    
    private static final String MAIN_OPT_THREADS =
      "Use multiple threads (Boolean)";
    
    private static final int MAX_THREADS = 2;
    
    private static Map options;
    
    private static Set source;
    
    private static TypeSystem ts;
    
    private static boolean hasErrors = false;
    
    public static final void main(String[] args) {
        options = new HashMap();
        source = new TreeSet();
        MainTargetFactory tf;
        parseCommandLine(args, options, source);
        tf =
          new MainTargetFactory((String)options.get(MAIN_OPT_SOURCE_EXT),
                                (Collection)options.get(MAIN_OPT_SOURCE_PATH),
                                (File)options.get(MAIN_OPT_OUTPUT_DIRECTORY),
                                (String)options.get(MAIN_OPT_OUTPUT_EXT),
                                (Boolean)options.get(MAIN_OPT_STDOUT));
        if (((Boolean)options.get(MAIN_OPT_EXT_OP)).booleanValue()) {
            ts = new jltools.ext.op.ObjectPrimitiveTypeSystem();
        } else {
            ts = new jltools.types.StandardTypeSystem();
        }
        Compiler.initialize(options, ts, tf);
        int totalThreads;
        if (((Boolean)options.get(MAIN_OPT_THREADS)).booleanValue()) {
            totalThreads = Math.min(source.size(), MAX_THREADS);
        } else {
            totalThreads = 1;
        }
        TargetSet tl = new TargetSet(source, totalThreads);
        Compiler compiler = new Compiler();
        if (totalThreads > 1) {
            for (int i = 0; i < totalThreads; i++) {
                WorkThread wt = new WorkThread(tl, compiler);
                Thread thread = new Thread(wt);
                thread.start();
            }
        } else {
            Iterator iter;
            String targetName = null;
            try {
                Compiler.verbose(Main.class,
                                 "read all files from the command-line.");
                iter = source.iterator();
                while (iter.hasNext()) {
                    targetName = (String)iter.next();
                    if (!compiler.readFile(targetName)) { hasErrors = true; }
                }
                Compiler.verbose(Main.class, "done reading, now translating...");
                iter =
                  source.iterator();
                while (iter.hasNext()) {
                    targetName =
                      (String)
                        iter.next();
                    if (!compiler.compileFile(targetName)) {
                        hasErrors =
                          true;
                    }
                }
            }
                catch(FileNotFoundException fnfe) {
                    System.err.println(Main.class.getName() +
                                         ": cannot find source file -- " +
                                         targetName);
                    System.exit(1);
                }
                catch(IOException e) {
                    System.err.println(Main.class.getName() +
                                         ": caught IOException while compiling -- " +
                                         targetName +
                                         ": " +
                                         e.getMessage());
                    System.exit(1);
                }
                catch(ErrorLimitError ele) {
                    System.exit(1);
                }
            Collection completed =
              new LinkedList();
            try {
                if (!compiler.cleanup(completed)) {
                    System.exit(1);
                }
            }
                catch(IOException e) {
                    System.err.println("Caught IOException while compiling: " +
                                         e.getMessage());
                    System.exit(1);
                }
            if (hasErrors) {
                System.exit(1);
            }
            if (options.get(MAIN_OPT_POST_COMPILER) !=
                  null &&
                  !((Boolean)
                      options.get(MAIN_OPT_STDOUT)).booleanValue()) {
                Runtime runtime =
                  Runtime.getRuntime();
                Process proc;
                MainTargetFactory.MainTarget t;
                String command;
                iter =
                  completed.iterator();
                while (iter.hasNext()) {
                    t =
                      (MainTargetFactory.MainTarget)
                        iter.next();
                    if (t.outputFileName ==
                          null) {
                        continue; 
                    }
                    command =
                      (String)
                        options.get(MAIN_OPT_POST_COMPILER) +
                        " -classpath " +
                        (options.get(MAIN_OPT_OUTPUT_DIRECTORY) !=
                           null ? options.get(MAIN_OPT_OUTPUT_DIRECTORY) +
                                    File.pathSeparator +
                                    "." +
                                    File.pathSeparator : "") +
                        System.getProperty("java.class.path") +
                        " " +
                        t.outputFileName;
                    Compiler.verbose(Main.class,
                                     "executing " +
                                       command);
                    try {
                        proc =
                          runtime.exec(command);
                        InputStreamReader err =
                          new InputStreamReader(proc.getErrorStream());
                        char[] c =
                          new char[72];
                        int len;
                        while ((len =
                                  err.read(c)) >
                                 0) {
                            System.err.print(String.valueOf(c,
                                                            0,
                                                            len));
                        }
                        proc.waitFor();
                        if (proc.exitValue() >
                              0) {
                            System.exit(proc.exitValue());
                        }
                    }
                        catch(Exception e) {
                            System.err.println("Caught Exception while running compiler: " +
                                                 e.getMessage());
                            System.exit(1);
                        }
                }
            }
        }
    }
    
    private static void setHasErrors(boolean b) {
        hasErrors =
          b;
    }
    
    private static class TargetSet
    {
        private static final int READ =
          0;
        
        private static final int COMPILE =
          1;
        
        private Set source;
        
        private int totalThreads;
        
        private int phase =
          READ;
        
        private Iterator sourceIter;
        
        private Object countLock =
          new Object();
        
        private int threadsInNext =
          0;
        
        private Object sourceLock =
          new Object();
        
        TargetSet(Set source,
                  int totalThreads) {
            this.source =
              source;
            this.totalThreads =
              totalThreads;
            sourceIter =
              source.iterator();
            Compiler.verbose(Main.class,
                             "read all files from the command-line.");
        }
        
        String nextTarget() {
            String result;
            synchronized (countLock)  {
                threadsInNext++;
            }
            synchronized (sourceLock)  {
                if (!sourceIter.hasNext()) {
                    if (threadsInNext ==
                          totalThreads &&
                          phase ==
                            READ) {
                        Compiler.verbose(Main.class,
                                         "done reading, now translating...");
                        phase =
                          COMPILE;
                        sourceIter =
                          source.iterator();
                        sourceLock.notifyAll();
                        result =
                          null;
                    } else
                        if (phase ==
                              COMPILE) {
                            result =
                              null;
                        } else {
                            try {
                                sourceLock.wait();
                            }
                                catch(InterruptedException e) {
                                    setHasErrors(true);
                                }
                            result =
                              null;
                        }
                } else {
                    result =
                      (String)
                        sourceIter.next();
                }
            }
            synchronized (countLock)  {
                threadsInNext--;
            }
            return result;
        }
        
        public static final java.lang.String jlc$CompilerVersion =
          "1.0.0";
        
        public static final long jlc$SourceLastModified =
          945373925000L;
        
        public static final java.lang.String jlc$ClassType =
          "\037\uff8b\010\000\000\000\000\000\000\000\uff95U]h\034U\024\uffbe\uff99\uffec&\uffcd&\uffdb&1\uff98\uffa6! j)y\uffe8.\uffe8S)\uff88k\uff9b\uffe0\uffc6\uffc9\uff8f&\010Fb{w\ufff6f3\uffc9\uffcc\uffbd\uffe3\uffbdw6\033\uff91\uff82\010\"\uffd4\uffbf\uff87\ufffa\uffa0\uffa2\017\"\uffadH\ufff1\uffa1h\uffdf\uffd4B\021A|Q\004\037\033\uff8b\ufff8\uffa4\uff8fZ\005\013\uff9esggvv\uff97mu\037.\uffcc\uffb9\uffe7;\177\uffdfw\uffee^\ufffe\uff9dd\uff95$3\uffdb\uff9e\026\uffc2S\005\uffbd\0270UX\uffa1R\uffb1\uffea)\uff8f*\uffb5\006\uff86\uffe5p\ufff4\uffa3\ufff7\ufffe\uff9a\ufffa\uffc4\"\uffa4!\uffc9t\uffbbo\uffe2U\ufff6\003\uffef\uff99\uffbf?\ufffc\uffe7\uffc4\uffa5\ufff9\001\uff8b\uffe4\uffd7\uffc9\uffb0\uffabJ\\\ufff0=_\uff84j\uff9d\014\uffba\uffaa\uffcc9\uff9369\uffe4\010\uffae\uffa9\uffcb]^3`M\uffa6\uffecf\uffcc\uffa2\uff89YLb\uff9e\uffb4\uffc9\uffc0\uffa6\uffcb\uffbc*8\035N\uff9cB\uffedzE\uffbc\uffae\uffda\uffae\uffd2\uffe0\uff93\uffdd\ufff4h\015\\\uffa6;\uffe2\uff94\034\uff87)5\uff8f\uff97\uffe0u`3\ufff4\uffbc%\uffea3M\uffc6\uffedmZ\uffa7E\uff8f\ufff2ZqUK\uffa8\004\uffeeG\\\uffac\uffcf\uffe4f\uffeayr\uff8e\ufff4\uffdbd\uffc8\uffd8\020\uff84\uff86\uffacMr.\uffd7LnR'q\031\uff84\uff80[\uffa2\032\177\016\007\uffd4\uffd9\uffa15\uff96\uffc2\014\uffa9-!u\uff9b!\014\uff98\uffc4\0164\uff99\uffe8\uffa8\uffd9\uffb4\015c\uff9e\uffec1\uffe6\uffdb\uffaf_<.\ufffe\ufffc\uffec\uffdb\uff88\uff8c\ufff1v/txw\uffc8\uffdeX\ufffe\uffe0\uffb9\uff97\uffd1! Du1V\ufff2+n-\004R\uffd0\ufffb\uffd6#\ufffb_<\ufffa\uffe0\037\uffb7,\uffd2g\uff93\014\uff8fkl\uffe0y@\uff93\uffb1\030\uffea\003a\uff85E8 \uffde\uffbd\uffb1\021\uff89($D\ufffcv\uffe5\uffe4\uffe8\033\uffc7\uffd5U\uff8b\ufff4\uffaf\uffc3\uffe0|?\uffd4\uffb4\uffe21\030,\ufff5<\uffb1\uffcb\uffaag\uffb4\uffe9x,5|\uffd3\026\uffce\uffbe\002S\003\032\uffcex\020H\uff93\uffd1\uffc8\uffc5\020m8\uff86N\uffea]\uff8d\uffcc\uffa32\uffca\\i\uffca\035\ufff6\ufffd\uffc77\ufffbn<\ufffc\uffe2u\013\007\uff9cg\uffdc\ufff1\uff84\uff82\uff80X\035\ufff62\020\uffab\004?2\uffe9VAb\uffe2i\uffea\uff85\035\uffb2X\uffael3\007\uffc5\uff95\uffd1q\uff88F\000\uffcdO\uffa0K\uffd4\uffb9\uffed\ufff2\uff9d\uffa8\ufff5\uff91\uffd9\uffd5\uff8d\uff85\uffb3\uffaf\uffde\uffdf\uff8f3\uffdf\uffcd\020\002\024#d\014\uff8f!@MuP\uffd0\022\uffe6\uffa7\027OO/\uffed\036\ufffb\034\010(\uff93L\uffc5\uffd5\012Y#G4\uffc9<5W:\015\uffd8\uff83&#\026U\uffb0\005(\ufff5\ufffc/o}\ufff7\uffe6\0037\000\uffb0@\uffb2u\uffac\034\uff840\uffdarZ\012\ufffd\012\uff93\uffaf\\~{f\ufff8\uffc2\ufffe\ufff9\uffa6\014\uffa2_\uffb7\030V\uffa4\uffeb\uffbb\uffda\uffad3\034\uffd4CW\uff8e\ufffe\uffbc\uffff\uffd3\uff85\uffc7\uffa3Z\uff80\uff8fj\uffa4\003@\016\uffa4\ufffb\uffc1s2*r\ufff0\uffd4\ufff2\uffe2J\uffd9\uff9e3\uffb6#\uffcd4}\uffe6k\uffa6\027\uffcc\uffd2`\027\uffa1tX`l9M\ufff2\uffad\uffa9\uffae2\uffdd\0034\uffa2\uff85\uffa6\uffde\uffda\uff96d\uffb4\uffaa\uff82\uffbb\uffe6\uffc8\006[T\uffb1\uffbb\ufffb\uffe5\uffa2Z\uffca\uffb0\uffd7I=\uffe3\uffadz\uffd0N\uffb5\uff90=\uffd0C\uff8e\010\uffb9\uffb6\uff85\uffb3\uff93\uff80S\\D*\uffea\001\uffcd\uffeb\uffa8\uff952_b\015\ufffd\uff9f\0135\uffb9\uffd0|\uffa2\uff91\uffdcB\uffd6\uffe9\uffaeU-\uffacQYc:\036h\uff9e\uffd4\015\uff9b\uffe6c\uffc2\uffa8\024\uffa4\uffd1\uffbc\uffc2s\uffae\uffed&\uffc6Hr_\uffbbd\026\uffcd{g\uff9e\ufffc\uffe6\uffeee^\uffbbV\uffcf}\ufff5\uffe5\uffaf\026nV\ufff7\uffeeY69\uffc8\032\uffc0\uffb6v\005Gc\ufffcV\uffb6mdN2\035J\uff9e,,\uff88\ufffap\uffaf\uffc4O\ufffcx\ufff6RH\uffec\uffdb\026\uffc6\uffceC\uff97\uffa1\uffcf\uffb8N\uff87\uffee|\uffcaR\uffadY\uffb0\005G;^W\001\uff9d\uffc8\uffd0\001\uff9a\uffd3}\035\uffd2\uffe7\036\ufffbz\ufff6f\015w\010\ufff1\013m\uffc3\uff92\uffe4\uff9e\uff96JJR\uffd2=|\012\032/\ufffd0\ufff3\uffce7\ufff4\ufffd~\uffb3C\uffca}\uff81\uff99\ufff5\uffb3\uffa2\uffd4\uff88<\uffd6\"\uffba\uffa1\uffc9\uffec\035h+<\uff9b*l\uffa3\uffa5\uff874]O\"Yi\uffca&\uffcd\uffa2\uffc7)\uffa2U\\\uffb8#\014\uff94\uffc5A\uff80Q\uffda\uffff\uff9d\uffa5]\ufff0\uffd1\uffbfi#\uff80\uffc5H\uffda0Z\ufffd\027'6\003\uffcej\010\000\000";
        
        public static final java.lang.String jlc$CompilerVersion =
          "1.0.0";
        
        public static final long jlc$SourceLastModified =
          945380246000L;
        
        public static final java.lang.String jlc$ClassType =
          "\037\uff8b\010\000\000\000\000\000\000\000\uff95U]h\\E\024\uff9e\uffdc\uffec&\uffddM\uffb6Mbh\uff9a\uffc6\uff80hK\uffc9CwA\uff9fJA\\\uffd3\004\uffb7\uffdelB\023Z\uff8c\uffa4\uffed\uffec\uffdd\uffc9f\uff92{g\uffae3s7\033\uff91\uff82\010R\uffa8\177\017\ufff5AE\037DZ\uff91\uffeaC\uffa9\uffbe\ufff9\003\"\uff82\ufff8\uffa2\010\uffbe\010\uff8dE|\uffd2Gm\013\026<3w\uffef\uffdd\uffbb\uff9bn\uffab\ufffb0p\uffcf\uff9c\uff9f\uffef\uff9c\uffef\uff9b\uffb3W\ufffeDi)\uffd0\uffe4\uffba\uffab8we^m\ufff9D\uffe6\027\uffb0\uff90\uffa4:\uffedb)\uff97\uffc00\037\014}\ufff0\uffce\uffad\ufff1\uff8f,\uff84\032\002M\uffb4\ufffb\uffc6^%\uffcfw\uff9f\uffb9\ufffd\ufffe?G.\uffcf\ufff6Y(\uffb7\uff8c\006\uffa8,2\uffce\uffb6<\036\uffc8e\uffd4Oe\uff891\"l\uffb4\uffc7\uffe1La\uffca(\uffab\uff99`\uff85\uffc6\uffedf\uffce\uff82\uffc9Y\uff88s\036\uffb5Q\uffdf*%n\025\uff9c\ufff6\uffc5N\uff81\uffa2nA_Wm*\025\ufff8\uffa4W]\\\003\uff97\uff89\uff8e<E\uffc7!R\uffce\uffeaK\ufff0\uffda\uffb5\032\uffb8n\031{D\uffa1\021{\035\uffd7q\uffc1\uffc5\uffacVXT\002\uff90\uffc0\ufffd \uffd5\ufff8Lm\"\uff9fC\uffe7P\uffaf\uff8d2\uffc6\uffa6\uff83\uffb4!m\uffa3,e\uff8a\uff88U\uffec\uffc4.\ufffd\uff90p\uff8dW\uffa3\uffcf\001\037;\033\uffb8F\0221\031\uffb9\uffc6\uff85j3\004>\021\uffba\003\uff85F;0\uff9b\uffb6a\uffccc]\uffc6|\uffe7\uffd5K\uff87\ufff9\uffdf\uffd7\uffbe\013\uffc9\030i\ufff7\uffd2\016og\uffec\uff95\ufff9\ufff7N\uffbf\uffa4\035|\uff84\uffe4\016\uffc6\uff8a^\uff85\uffd6\002 E{\uffdf||\ufffb\ufff3'\016\ufffcu\uffd3B=6J\uffb1\010cC\uff9f\uffbb\024\032\uff8eB= ,?\007\007\uffe4\uffdb\033\0315\021\ufff9\uff98\uff88?\uffae\036\035z\uffed\uffb0\ufffc\uffccB\uffbd\uffcb08\uffcf\013\024\uffae\uffb8\004\006\uff8b]\uff97o\uff92\uffea\031e:\036N\014\uffdf\uffb4\uffa5g_\uff81\uffa9\001\015g\\H\uffa4\uffd0P\uffe8b\uff886\034C'\ufff5\035\uff8d\uffccje\uff94\uff98T\uff989\uffe4\uff87\017o\ufff4\\\177\uffec\uff85\uffaf-=\uffe0\034a\uff8e\uffcb%$\uffd4\uffe8t/}\uff91J\ufff4G*\uffd9*H\uff8c\uff9f\uffc4n\uffd0!\uff8b\ufff9\uffca:q\uffb4\uffb8R*J\uffd1\ufff0\uffa1\ufff9Q\uffed\022vnS\uffb6\021\uffb6>8\uffb5\uffb8r\ufffc\uffec\ufff9\uff87{\ufff5\uffcc7S\010\uffa1AShX\037\031\uff88\032\uffef\uffa0\uffa0%\uffccO.\035\uff9b(o\036\ufffa\024\010(\uffa1T\uff85*\uffa9YC\ufffb\025J\uff9d\uff98)\036\uff83\uffd8\uffdd\uffa6\uffa2\006\uff95\uffb79(\ufff5\uffc2oo|\uffff\ufffa#\uffd7!\uffe08J\uffd75r\020\uffc2P\uffcb\uffa9\034x\025\"^\uffbe\ufff2\uffe6\uffe4\uffc0\uffc5\uffed\013M\031\uff84\uffbf\uff9dbX\020\uffd4\uffa3\uff8a\uffd6\uff89\036\uffd4\uffa3W\017\ufffe\uffba\ufffd\ufff3\uffc5\uffa7B,\uffc0G5\uffd4\001D\ufff6%\ufffb\uffd1\uffe7X\010\uffb2\177z~n\uffa1d\uffcf\030\uffdb\ufffef\uff99\036\ufff35\uffd9-\uffccR`\uffe7\uff81p\uff88olY\uff85r\uffad\uffa9.\022\uffd5%hPq\uff85\uffdd\uffa55ApU\ufffa\ufff7\uffad\uff91\ufff6\uffd7\uffb0$\ufff7\ufff7\uffcb\uff86XJ\ufff0\uffaec<#-<\uffda\uff8e\025\027]\uffa23\016\017\uff98\uffb2\uffb9\uffb3\021\007'\uffb8\010U\uffd4%4\uffa7\uffc2VJ\uffacL\032\uffea?\0035\uffb5\uffb4\ufff9\uffc8\uffdd\ufffc\uffc6\025z`\uffddu\016Ls\uffcf\uffa7.\021'\uff89\uff90\uff94\uffb3\uffbb\uff81\0137_\uff97${u\uff92\uffc5\uffb0 \uff96j\uff8eW)l\uffe2j\uffcc\ufff3/\017~\\>\037C\uffee\uffef\uff92%g\uffa0D\uffbb\uffcb\uffa0\uff9ei\uffc4\016\000gb\uffc7\uff82\uffc9/aQ#*\uff92A\016\uffd5\uff8d\006\uffcd\uffc7\uffa8y[ \uffe8\uffe6\uff95>O\uffb4\uffddD1\002=\uffd4.\ufff49\uffb3\uffa5\uffcd\037Usc\uffa4^\ufff9\uffaa\uff9e\ufffd\ufff2\uff8b\uffdf-\uffbd\017vn\014\uffcbF\uffbbI\0034\uffaa`|\uffda\030m\ufff8\uffb6=\uff92\025D\005\uff82\uffc5k\006\uff9e\uffe2\uffben\uff85\uff9f\ufffe\uffe9\uffec\uffe5\000\uffd9w,\uff9d;\007]\006\036a*\uff99\uffbas\001'Z\uffb3\uffe0\uffed\036\uffec\ufff8O\uffe0\uffd0\uff89\010\034\020g\uffb2\uffaf=\uffea\uffdc\uff93\uffdfL\uffdd\uffa8\uffe9\uff97\uffaf\uffe3O\uffb5\015K\uff80<bm\027\uff85\uffc0[z\uff815^\ufffcq\ufff2\uffado\ufff1\uffbb\uffbd\uffe6\uffe5K\ufffa<1K\uffc3\012K\uffeb\uffc8C-y6\024\uff9a\uffba\007m\ufff9g\023\uffc0VZ\uff92H\uffd2uZ\uff93\uff95\uffa4l\uffcc\uffac\uffa7\uffa8D\uffb8@N\uffdd3\014\uffde\003\uff83g\023\uff96\ufffd?UB\021\ufffa\ufff0tc\uffc8\uffe65\ufffd\013\uffce\uff89z\013\014\011\000\000";
        
        public static final java.lang.String jlc$CompilerVersion =
          "1.0.0";
        
        public static final long jlc$SourceLastModified =
          945380731000L;
        
        public static final java.lang.String jlc$ClassType =
          "\037\uff8b\010\000\000\000\000\000\000\000\uff9dU]h\\E\024\uff9e\uffdc\uffec&\uffddM\uffb6\ufff914Mc@\uffb4\uffa5\uffe4\uffa1\033\uffd0\uffa7R\020\uffd74\uffc1\uffad7?\uff98P0\022\uffdb\uffd9\uffbb\uff93\uffcd$\ufff7\uffce\\g\uffe6n6\"\005\021\uffa4P\uffff\036\uffea\uff83\uff8a>\uff88\uffb4\"\uffd5\uff87b}\ufff3\017\021A|Q\004_\uff84\uffc6\">)\ufff8\uffa2U\uffb0\uffe0\uff99\uffb9{\uffef\uffde\uffdd\uffed&\uffd6\ufffb0p\uffcf\uff9c\uff9f\uffef\uff9c\uffef\uff9c3W~Ci)\uffd0\uffc4\uff86\uffab8we^m\ufffbD\uffe6\027\uffb1\uff90\uffa4<\uffedb)\uff97A\uffb0\020\014\uffbe\ufff3\uffc6_c\uffefY\010\uffd5\004\032o\uffd6\uff8d\uffb5\uff8a\uff9e\uffef>\ufffe\ufff7\uffdb\uffff\034\uffbf<\uffdbc\uffa1\uffdc\012\uffea\uffa3\uffb2\uffc08\uffdb\ufff6x WP/\uff95E\uffc6\uff88\uffb0\uffd1\uff80\uffc3\uff99\uffc2\uff94QV1\uffc6\012\uff8d\uffd9u\uff9fS\uffc6\uffe7T\uffec\ufff3\uff84\uff8dz\uffd6(q\uffcb\uffa0t0V\012\024u\uffa7\ufff4u\uffd9\uffa6R\uff81Nz\uffcd\uffc5\025P\031o\ufff1Sp\034\"\uffe5\uffac\uffbe\004\uffad}k\uff81\uffeb\uffcec\uff8f(4lo\uffe0*\uff9er1\uffabL-)\001H\uffe0\uffbe\uff9fj|&6\uff91O\uffa1s\uffa8\uffdbF\031#\uffd3FZ\uff90\uffb6Q\uff962E\uffc4\032vb\uff95^p\uffb8\uffce\uffcb\uffd1o\uff9f\uff8f\uff9dM\\!\011\uff9b\uff8c\\\uffe7B5\011\002\uff9f\010\uff9d\uff81B#-\uff98M\uffdaP\uffe6\uffd1\016e\uffbe\ufff5\uffe2\uffa5c\ufffc\uffcf\017\uffbf\016\uffc9\030n\uffd6\uffd2\012\uffafg\uffec\uffd5\uff85\uffb7\uff9e|N+\ufff8\010\uffc96\uffc6\012^\uff89V\002 Ek\uffdf|p\uffe7\uffe3\uff87\016\uffffq\uffd3B]6J\uffb1\010cM\uff9f\ufffb\024\032\uff8aL= ,?\007\007\ufff8;\020\0115\021\ufff9\uff98\uff88_\uffaf\uff9e\030|\uffe9\uff98\ufffc\uffc8B\uffdd+P8\uffcf\013\024.\uffb9\004\012\uff8b]\uff97o\uff91\ufff2\031e2\036J\024\uffdf\uffa4\uffa5k_\uff82\uffaa\001\015g\\p\uffa4\uffd0`\uffa8b\uff886\034C&\uffd5\uffb6Dfug\024\uff99T\uff989\uffe4\uffdbwot]\177\uffe0\uff99/,]\uffe0\034a\uff8e\uffcb%8\uffd4\uffe8t.=Q\uff97\uffe8\uff9fT2Uh1~\032\uffbbAK[,\uff946\uff88\uffa3\uff9b+\uffa5\"\0275\037\uff92\037\uffd1*a\uffe66e\uff9ba\uffea\ufffd\uff93K\uffab\uffa7\uffce\uff9e\uffbf\uffb7[\uffd7|+\uff85\020\0320\uff81\uff86\ufff4\uff91\001\uffab\uffb1\026\012\032\uff8d\ufff9\uffc1\uffa5\uff93\uffe3\ufff3[G\uffaf\001\001E\uff94*Q%5k\uffe8\uff90B\uffa9\uffc7f\012'\uffc1v\uffbf\uff89\uffa8A\uffe5m\016\uff9dz\uffe1\uffe7W\uffbey\ufff9\uffbe\uffeb`p\012\uffa5\uffab\03294\uffc2`Ci>\ufff0JD<\177\uffe5\uffd5\uff89\uffbe\uff8b;\027\uffeam\020~\uffed\uffcd\uffb0(\uffa8G\025\uffad\022]\uffa8\ufffb\uffaf\036\ufff9i\uffe7\uff87\uff8b\uff8f\uff84X\uff80\uff8fr\uffd8\007`\uffd9\uff93\uffccG\uff9f\uffa3!\uffc8\uffde\uffe9\uff85\uffb9\uffc5\uffa2=cd\uff87\uffeaa\uffba\uffcc\uffdfD'3K\uff81\uff9c\007\uffc2!\uffbe\uff91e\025\uffca5\uffaa\uffbaDT\007\uffa3~\uffc5\025v\uff97\uffd7\005\uffc1e\uffe9\uffef\031#\uffed\uffafcI\ufff6\uffd6\uffcb\uff86X\uff8a0\uffd71\uff9e\uffe1\006\036-\uffc7\uff8a\uff8b\016\uffd6\031\uff87\007L\uffd9\uffdc\uffd9\uff8c\uff8d\023\\\uff84]\uffd4\uffc14\uffa7\uffc2T\uff8al\uff9e\uffd4\uffd4\177\006jbi\ufff1\ufff1\uffdb\uffe9\uff8d)t\uffd7\uff86\uffeb\034\uff9e\uffe6\uff9eO]\"N\023!)g\uffb7\003\027n\uffbe\016N\016h'Ka@,\uffd5\034/S\uffd8\uffc4\uffe5\uff98\uffe7\037\uffef~\177\ufffe|\014\uffb9\uffb7\uff83\uff97\uff9c\uff81\022\uffed.\uff83z\uffe6NP\uffeff\uffb0'\uffc2\uff89\uffdc\uffe7\uffbf\uffff/\uff84\uffb5X\001\0126\uffde\uffb6\002\ufff3\uffcbXT\uff88\uff8a\0325\uff87\uffaafJ\uffcc\uffcf\uff88\uff99~\030\uffb9\ufffa\uff95>W\uff9bn\"\033\uff81\uffeei\036\uffc59\ufff3\uff8e\uff98\uffa7\uffb4\uffbe\uffd3R/|V\uffcd~\ufffa\uffc9/\uff96\uffdeX\uffed;\uffcd\uffb2\uffd1~R\uff83)RP*-\uff8c\uffde\uffa0\uffa6M\uff97\025D\005\uff82\uffc5\uff8b\020\uff96\uffc5\uffc1N\uff81\037\ufffd\ufffe\uffec\uffe5\000\uffd9\uffb7,\uffed;\007Y\006\036a*\uffe9\uffba\ufff5\uff89H\uffa4f\uffc1v9\uffd2\ufff2jq\uffc8D\004\016\uff8cO2\uffaf\001u\uffee\uffe1/'oT\ufff4n\uffd2\ufff6\uffa5\uffa6b\011h\uff85x\ufffa\012B\uffe0m\uffbdbk\uffcf~7\ufff1\uffdaW\ufff8\uffcdn\uffb3\uff9b$}\uff9a\uff98\uffb5f\uff85\uffa1\uffb5\uffe5\uffd1\uffc6\000\uffd5\024\uff9a\uffdc\uff85\uffb6\ufffc\023\011`\uffab\uff8d\uff96H\uffd2E5YI\uffcaF\uffcd\002\uff8dB\uff84+\uffae\uffb4\uffab\031L,\uff83\uffc1\016\uffc3\uffdeI\uff94\uffb0\011}X.1d3\uffef\uffff\002\000\ufff7.\035\uffae\011\000\000";
        
    }
    
    private static class WorkThread implements Runnable
    {
        TargetSet tl;
        
        Compiler compiler;
        
        WorkThread(TargetSet tl,
                   Compiler compiler) {
            this.tl =
              tl;
            this.compiler =
              compiler;
        }
        
        public void run() {
            String targetName =
              null;
            try {
                while ((targetName =
                          tl.nextTarget()) !=
                         null) {
                    if (!compiler.readFile(targetName)) {
                        setHasErrors(true);
                    }
                }
                while ((targetName =
                          tl.nextTarget()) !=
                         null) {
                    if (!compiler.compileFile(targetName)) {
                        setHasErrors(true);
                    }
                }
            }
                catch(FileNotFoundException fnfe) {
                    System.err.println(Main.class.getName() +
                                         ": cannot find source file -- " +
                                         targetName);
                    setHasErrors(true);
                }
                catch(IOException e) {
                    System.err.println(Main.class.getName() +
                                         ": caught IOException while compiling -- " +
                                         targetName +
                                         ": " +
                                         e.getMessage());
                    setHasErrors(true);
                }
                catch(ErrorLimitError ele) {
                    setHasErrors(true);
                }
        }
        
        public static final java.lang.String jlc$CompilerVersion =
          "1.0.0";
        
        public static final long jlc$SourceLastModified =
          945373925000L;
        
        public static final java.lang.String jlc$ClassType =
          "\037\uff8b\010\000\000\000\000\000\000\000\uff95TOhTG\030\uffff\ufff2v\023\uff93\uffd5\uffd4$\0065\uff8db)JQq\027\ufff4$\uff82\uff98ZB\uffa3O#&\uffb44\uff92\uffea\uffec\uffdb\uffd9\uffcd$\ufff3\uffe6=g\uffe6m^J\021J/=\uffb4=\ufff5\uffd2\026=\uff94\uffa2R\uff8a\007Qo\uffdaB)\uff82\uffb7\uff96B\uff8fZ\017=\uffb5G\uffb5\005\005\uffe7\uff9b\uffb7\uffef\uffed\uffdb]\024\uffbc\014\ufffbf~\uffdf\uff9f\uffdf\ufff7\ufffb}\ufffb\uffe3\uffbf\uffd0\uffaf$l_\uffe6:\010\uffb8*\uffeb\uffb5\uff90\uffaa\ufff2)\"\025\uffad\035\uffe5D\uffa9ys1\033\uff8d|\uffff\uffed\177\023?8\000\uffb1\uff84\uffc9Nl\uff86\uff9a\ufff1C\ufffe\uffc1\uffff\uffdf==xez\uffc0\uff81\uffe1\005X\uffcf\uffd4\uff94\010\uffc4\uff9a\037Dj\001\uffd615#\004\uff95.l\ufff4\002\uffa1\011\023L4l\uffb0\uff86\011\uffb7\uff95\uffb3bsV\uffb2\uff9c\uff87\\\030\uffa83\uffcak\006\uffb45\003E\uff9a\ufff1\012>\uffd7\\\uffa6\uffb4\uffc1\ufff4\uffd79i\030\uffc8dW\uff9e)\uffcf\uffa3JM\uffe3\uffa3A\015\uffd6#\uffceO\022\uff9fj\030s\uff97I\uff93T8\021\uff8d\uffca\uff9c\uff96\uffa6\023\ufff3\uffbe\uff81a\177\uffb66U\uffe7\uffe1\002\024\\\030\uffb2w\030\uff84\027\ufffd.\uff94\uff98\uffd0T\uffd6\uff89\uff97A\uffd6\uff99\uff84KA-\ufffd\\\037\022o\uff854h.fH-\005Rw\\D!\uff95\uffc8@\uffc3xW\uffcf\uff96\uffb6\031\ufff3\uff96\027\uff8c\ufff9\uffd9\027\uff97\ufff7\005\uff8fo\uffdcK\uffc4\030\uffebD!\uffe0\uff9b!wq\ufff6\uffd2\uff87\uff9f\" \004P=\uff8aM\ufff9U\uffd6\uff88\uff8c(\uff88~r\ufff8\uffc1\uffed#;\037=q\uffa0\uffcf\uff85\uffa2H{\uff8c\ufff1\034\uffd40\uff9a\uff86\ufffaF\uffb0\ufff2\011s\uff98|\uff9b\uffd3K\024\uffa2\uff9c\011\ufff1\uffcf\ufff5C#_\uffeeS\uffb7\034(,\uff98\uffc1\ufff9~\uffa4I\uff95S3X\uffc2y\uffb0Jkg\uffb5e<\uff9a\033\uffbe\uffa5\uff85\uffb3\uffaf\uff9a\uffa9\031\031\uffcer\uff93H\uffc3H\002\uffb1B[\uff8d\015\uff93f\017\uff91it\uffc6\uff8cP\uff9a\010\uff8f\ufffev\ufff5a\uffdf\ufffd\003\037\uffff\uffe2\uffe0\uff80\uff87\uffa9\ufff0x\uffa0LB\uffec\016\uffb9\014\uffa4.\uffc1\uff8fb\uff9e\uffaa\uffb1X\ufff0\036\uffe1Q\uff97-f\uffab\uffcb\uffd4Cs\025u\uff9a\"\016\015\ufff9q\uff84$\uffcc]&V\022\uffea\033v\uffcf-\036;\ufff7\uffd9\uff9b\005\uff9c\ufff9j\021\000\034[h\024\uff8f!\0235\uffd1%A\uffdb\uff98\uffd7.\uffbf3yr\ufff5\uffad\uff9bF\uff80\031(V\uff99V\uffa8\032\uff80\006G\ufff3\uffd0f)\uffe96\ufff3L\uff87\ufff2<\uff91\015\uffaa\uffe7\uffa8\uffceW\uffc2sK\022>\uffe8\005~\uffc88\uff95Y\uff92\uffac\uff89\uffba4\033HE\uffad|\uffb4\005\uff89\uffb3@\003\uffdb\uffd6[\uffeb\ufffd@\uffae\uffcc/IJ\022\uff9b\017C\uffd3\ufffa\uffc3~\uff8c[\uffbe\uff86w\uffeb\011\uffcf\uff9d\uffb9\uff97\uffbe\uffb4\ufffc\uff98\uff9d\034\016\uffb7|:\022\002\uffbd\021\uffa7\uffe9$\uffbc\uffd19\uffa1\023v\uffa9\uffec\uffffJK\uffe0\uffe2\uffe7?7K?\uffdd\ufff9\uffdbA\ufff9z\005v\\x\uff8d\uffc6\036\0155\013\004^\uffa6\013\uffd9!{IR\035I\uff91\uffb9\uffc2\uffac\uffd0\uffd6\027\025>\ufffe\uffc7\uffb9+\021\uffb8\uffcf\034\uffcc=l\uffa6\035\ufff9T\uffe8|\uffea\uffee}\uffc9\uffb1v\uff8c\uffe8\uffbb\uffbaV80Ld\uffe4\uffe9@\uffe6ym\uffd4\027\uffde\ufffeu\ufff7\uffc3\006\uffee+\uffc6\uffef\uffed\uff98\uffa3\uff84Mm\uffc3MII\uffd6\uffd0o\ufff1'\uffbfo\uffff\ufffa.\uffb9X\uffb0\uffa6Q\uffec#jM\uffe3$\uffa51\ufff2u<v\uffc4\032\ufff6\uffbcL\uffcd\ufff2\uff99\\S\uff8bm\013\uffe5U\uffdc\uff8f\uffea\uffb64n\ufffb\uffab\ufff7o\uffe5\uff94d>\uffd3\uffacI\uff91\uffdc\ufffe\uffeb\uffbb\ufffez\ufff0\uffe7W\uffef&\uffae6\uff9b]K&\uff94\ufff9a\uffefK\uffebh(\uffc8H\uffbcB?}\ufff8\uffe3`\034j(uy\uffd5\030o\uffa4m\uffbcd\uffab\uff9f\003r?dL\ufff6\006\000\000";
        
        public static final java.lang.String jlc$CompilerVersion =
          "1.0.0";
        
        public static final long jlc$SourceLastModified =
          945380246000L;
        
        public static final java.lang.String jlc$ClassType =
          "\037\uff8b\010\000\000\000\000\000\000\000\uff95T]h\034U\024>\uff99l\ufffe6\uffdd6\uff89\uffa1mL\uff83\uffa2)R\uffb1\uffbb\uffa0\uffbeH@L+\uffa1\uffa9\uff93\uffa4\uff98P1\022\uffdb\uffbb\uffb37\uff9bIf\uffee\uff8c\ufff7\uffde\uffd9lD\012\"H\uffa1\ufffe=\uffd4\007\025}\020iEZ\037\uff8a\ufffa\uffd6*\uff88\010\uffe2\uff8b\"\ufff8\"4\026\ufff1I\037\uffb5\012\026<\uffe7\uffce\uffce\uffec\uffec\uffae)\ufff8r\uff99\uffb9\ufff7\uff9c\uffef\uff9e\uffef|\uffdf\uffb9\uff97~\uff87\036%ab\uffdd\uffd3A\uffe0\uffa9\uffa2\uffde\012\uffb9*\uff9e`R\ufff1\uffcaQ\uff8f)\uffb5\uff84\033\013\uffd1\uffd0\007\uffef\ufffc5\ufff6\uff91\005P\uff970\uffde\032\uff9bF\uffcd\ufffa\uffa1\ufff7\ufff4\uffdf\uffef\uffff\ufff3\uffc8\uffc5\uff99^\013\012\uffcb0\uffe8\uffaai\021\uff88-?\uff88\uffd42\ufff4\uffb9jV\010.m\uffd8\uffe3\004B3W\uffb8\uffa2j\uff925\uff8c\uffd9\015\uffcc\uff92\uffc1,\uffa5\uff98S6\ufff4\uffae\uffba\uffdc\uffab`\uffd0\ufffe4(\uffd2\uffaeW\uffa2\uffe3\uff8a\uffed*\uff8d1=\uffab\036\uffabb\uffc8x\033\uffce\uffb4\uffe3p\uffa5f\uffe8\020\uffa3\ufffaW#\uffcf\uff9bg>\uffd70b\uffaf\uffb3\032+yLTK\uff8bZb%x\uffbe\uffcb\uffa5\ufffa\uffcc\uffdd\\=\007g\uffa0\uffdb\uff86\001\uffb3GI\uffb4\uffd1cC\uffde\025\uff9a\uffcbU\uffe6\uffa4!}\010\uffb8\026T\uff92\uffdf\uffc1\uff909\033\uffac\uffca39\003j-\uff90\uffbae#\012\uffb9$\006\032F\uffdbj6\uffb4\uffb1\uffcd\ufffbvh\ufff3\uffadW/\034\016\ufffe\ufffc\uffe4\uff9bX\uff8c\uff91\uffd6(\012x{\uffc0^Yx\uffef\uffd9\uff97( \004P\035\uff8aM\ufffbe\uffb7\032\uffa1(\024}\ufff3\uffd1\uffed\uffab\uff8fM\ufffeq\uffd3\uff82.\033r\"\uffa9\uffb1Nk\uffbf\uff86\uffe1$\uffd5G\uffc1\uff8as\uffb8 \uffde\uffded\uff93\uff84(\uffa6B\ufffcvej\uffe8\uffb5\uffc3\uffea3\013\uffba\uff97\uffb1q\uffbe\037iV\ufff686\uff96y^\uffb0\uffc9+\uffa7\uffb4a<\uff9ci\uffbe\uffa1E\uffbd/c\uffd7P\uff86S\036\002i\030\uff8aC\uff8c\uffd0FcdR\uffeb 2C\uffce\uff98\025J3\uffe1\ufff0\uffef>\uffbc\uffd1u\ufffd\uffa1\027\uffbe\uffb4\uffa8\uffc1\005.\034/P\010H\uffd5\021\uff97\uffde\uffc4%\ufff4\uff93\uffcbRE\uff8b\005'\uff99\027\uffb5\uffd9b\uffa1\uffbc\uffce\0352WN'\020\ufff5\020\uffc9\uff8fRH\uffcc\uffdcv\uffc5FL}\uffd7\uffa1\uffc5\uff95\uffe3\uffa7\uffcf\uffde\uffd3M=\uffdf\uffcc\001\uffe0TQ\uffca0-\003\uff985\uffd6&A\uffd3\uff98\037_x||~\ufff3\uffbeOQ\uff80Y\uffc8\uff95]\uffadH5\000\015\uff96\ufff6B\uff83\uff92\uffd7M\uffe6\uffa9\016\uffc5%&\uffab\\/r\uff9d\uffbd\uff89\uffd6}qz\uffbf\023\ufff8\uffa1\uffebq\uff99\uff82\uffa4E\uffacJ\uff9c@.*\uffc5\uffa3\uff8d\uff90\uffff\uff82\030\uffd3p\uffc7\uffba\uffe7L&1'1\uffcc\015D\uff8a6d\032A\uffbd*\uffc6#\uffb4\003\uffc8^\002Y\014\"\uffe9p\uff9b)=\027T\\\034\uffe9\0126ew\023\uffc1\016p\004\uffcf\ufffd\ufff2\uffc6\uffb7\uffaf\uffdf{\035;q\034zj$\011:<s\uffcd|\uffe4\uff97\uffb9|\ufff9\uffd2\uff9b\023\uff83\uffe7\uffb7\uffcf5\ufffc\015?\035\uffb8<\177\uffb6\uffd3\uffe5'\uffa4\uffeb\uffbb\uffda\uffadqr\uffc0\uff83W\016\ufffe\uffbc\ufffd\uffe3\ufff9cq\uff93\uffd1h\uff95\uffd8\uffe0\uff98\uffde\uffb7C\uffd9\005\uffc3=\uff99\uffba\uff90N&\uffebi\000\ufff2?\uffd0)\uffc9S\uff81\uffdcXZ\uff93\uff9c\uffc5\uffafA\001j\uffe6\026\ufff33jl\uff81%7\uff8eh}8s\uffd2\uff95\ufff4u\uffa4I\ufff8\uffc9H\010\032\uffa1z\002'\uffe1\uffeeV\uff96s\uffe6\uffed1\uffcfoc\016r\uffaf|Q\uffcb\177~\uffedW\uff8b\\\uffde9\007\uff96\015\uffbby\uffdd\uffe1\uffa1F-i3y\uffb7Z\uffa6#/\uffb9\uff8e\uffa4H\uff87\007u\uffd8\uffbf\uffd3\uffc5O\ufffcp\ufffab\004\ufff6-\uff8b\uffb0\013h\uffca\uffc8\uffe7Bg\uffa1\uffdb\uff9f\uff95\014k\013\uff85;\uffd8\ufff6\uffd2\005\uffc8DF\uff8e\016d\uff96\uffd7\036}\uffe6\uffc8W\uff87nTIv\uffca\uff9fj\uffe9\uffa3D\uffaf\uffa6s9-%\uffdb\uffa2\uffb1\uffac\uffbf\ufff8\ufffd\uffc4[_\uffb3w\uffbb\uff8d\uffec\uffca}\uff9e\033\uffc7X\ufff1\uffd5\uff94y'-w\uffd55\uffdc\177;5\uff8b\uffcfd\uff8aZiZ%\uffab\uffe2\021R\uffb7\uffa1qs\014\uffcd\uffd7\003\uffa9\uffbcS\uffb7M\uffd3\uffd0-#\ufff1?\uffe0\uffbb\uffe8\uffe3X=\uffd4\uff90o\uffb3^\uffeb|\uffc6o\uffd9\uffbf\uffc0\024{\uff81\uffec\007\000\000";
        
        public static final java.lang.String jlc$CompilerVersion =
          "1.0.0";
        
        public static final long jlc$SourceLastModified =
          945380731000L;
        
        public static final java.lang.String jlc$ClassType =
          "\037\uff8b\010\000\000\000\000\000\000\000\uff95U]h\034U\024>\uff99l\ufffe6\uffdd6\uff89\uffa1mL\uff83bS$bwA\uff9f\uffa4\uffa0\uffc6\uffcab\uffea$)n\uffa8\030\uff89\uffed\uffec\uffec\uffcdf\uff92\uff99;\uffe3\uffbdw6\033\uff91\uff82\010R\uffa8\177\017\ufff5AE\037DZ\uff91\uffeaCQ\uffdf\uffac\uff8a\uff88 \uffbe(\uff82/Bc\021\uff9f\024|\uffd1*X\ufff0\uff9c;;\uffb3\uffb3\uffbbn /\uff97\uff9d{\uffcf\ufff9\uffee9\uffdf\ufff7\uff9d\uffbb\uff97\177\uff87>)`j\uffddU\uffbe\uffef\uffca\uffbc\uffda\012\uff98\uffcc\uff9f\uffb4\uff84d\uff95\uffe3\uffae%\uffe5\022n,\uff86#\uffef\uffbe\ufff9\ufff7\uffc4\ufffb\006@]\uffc0dkl\0225\uffe7\005\uffee\023\uffff\uffbc\ufff3\uffef}\uff97\uff8a\ufffd\006\uffe4\uff96a\uffd8\uff91\uffb3\uffdc\uffe7[\uff9e\037\uffcae\030p\uffe4\034\uffe7L\uff98\uffb0\uffcf\ufff6\uffb9\uffb2\034\uffee\ufff0\uffaaNV0a60\013\032\uffb3\uff90`\0363\uffa1\177\uffd5an\005\uff83\016&A\uffa1r\uffdc\002\035WLG*\uff8c\uffe9[u\uffad*\uff86L\uffb6\uffe1\uffcc\uffda6\uff93\uffb2H\uff87\0305\uffb8\032\uffba\uffee\uff82\uffe51\005c\uffe6\uffbaU\uffb3\012\uffae\uffc5\uffab\uff85\uff92\022X\011\uff9e\uffefq\uffa8>}7\uff93O\uffc3Y\uffe85aH\uffefQ\022m\ufff4\uff99\uff90u\uffb8bb\uffd5\uffb2\uff93\uff90\001\004\\\ufff3+\ufff1\uffe7p`\uffd9\033V\uff95\uffa5r\uff86\uffe4\uff9a/T\uffcbF\0300A\035(\030o\uffabY\uffb7\uff8d4\037\uffe8B\ufff3\uffcd\uff97.\036\ufff5\uffff\ufffa\uffe8\uff9bH\uff8c\uffb1\uffd6(\012xc\uffc8\\Y|\ufffb\uffa9\uffe7) \000\uff90\035\uff8a\uffcdze\uffa7\032\uffa2(\024}\uffe3\ufffe\uffedO\037\uff9c\ufffe\ufff3\uff86\001=&dx\\c\uff9d\uffd6A\005\uffa3q\uffaa\uff87\uff82\uffe5\uffe7qA\uffbc\ufffd\ufff1&\011\uff91O\uff84\ufff8\uffed\uffca\uffb1\uff91\uff97\uff8f\uffcaO\014\uffe8]F\uffe2</TV\uffd9eH\uffac\uffe5\uffba\ufffe&\uffab\uff9cV\uffba\uffe3\uffd1\024\ufff9\uffba-\uffe2\uffbe\uff8c\uffac\uffa1\014\uffa7]\004R0\022\uff85h\uffa1\uffb5\uffc6\uffd8I\uffad\uffa3\uff91\"9c\uff8eKeq\uff9b}\ufff7\uffde\ufff5\uff9ek\ufff7>\ufffb\uffa5A\004\uffe7\030\uffb7]_\" UG\uffbd\ufff4\uffc7.\uffa1\uff8fL\uffbaU\uffb4\uff98\177\uffcar\uffc36[,\uff96\uffd7\uff99M\uffe6\uffca\uffa8\030\uffa2\036`\ufff3\uffe3\024\022un:|#j}\uffcfLi\uffe5\uffc4\uff99sw\ufff4\022\uffe7\uff9b\031\000\030\uffd4\027\uff8d\uffd22\uff84Y\023m\0224\uff8d\ufff9\uffe1\uffc5\uff87'\0276\uffef\ufffc\030\005\uff98\uff83L\uffd9Q\uff92T\003P`(7\uffd0(Y\uffd5\uffec<\uffd1!\uffbfd\uff89*S%\uffa6\uffd27\uffd1z J\037\uffb4}/p\\&\022\uff90\uffa4\uff88U\uff81\023\uffc8x%\177\uffbc\021\ufff2\177\020\023\012nYw\uffed\uffe98\uffe6\024\uff869>O\uffd0F4\021\uffc4U>\032\uffa1. \ufffb\011\uffa4\uffe4\uff87\uffc2f\uffa6%\uffd5\uffbc_qp\uffa4+H\uffca\uffde&\uff82\uffe9\uffe3\010\uff9e\uffff\uffe5\uffd5o_9|\015\uff998\001}5\uff92\004\035\uff9e\uffbaf!\ufff4\uffcaL\uffbcp\ufff9\uffb5\uffa9\uffe1\013\uffdb\uffe7\033\ufffe\uff86\uff9f\016}\uffb0p\uffae\uffd3\uffe5'\uff85\uffe39\uffca\uffa91r\uffc0=W\uff8e\ufffc\uffbc\ufffd\uffe3\uff85G\"\uff92\uffd1h\uff95\uffc8\uffe0\uff98>\uffd0\uffa5\uffec\uff9c\uffee=\uff9e\uffba\uff80N\uffa6wC\uffd3N\011\uffdd(\uffa1\uff90\031ji*\ufff7\uffc5\037\ufffa\uffeb\uffee]VXO\002P\uffa1C\uff9d\uffa6y\uffdc\027\033Kk\uff82Y\uffd1e9\uffa8i\036\ufff4\uffc7\uffb86.\uff92\uffda8\uffa2\ufff5\uff81\uffd4IO\uffac\ufffcXS\uff92\uffc7B\uffcei\uffc8\uffeb1\uff9c\uff80\uffdb[u\uff98\uffd7\uffaf\uffa3\ufffe\uff83hLj\uffe6\uffc5\uffcfk\uffd9\uffcf\uffae\ufffej\uffd0\034vN\uffaaa\uffc2^V\uffb7Y\uffa0\uff90F\uffda\uff8c_\uffd6\uff96\ufff9\uffcd\012\uffa6B\uffc1\uff93\ufff1F\uffa7\034\uffecv\ufff1\uffa3?\uff9c\uffb9\024\uff82y\uffd3 \uffec\034\uff8eM\uffe81\uffae\uffd2\uffd0\uffed\017_\uffaak\003\uffadu\uffa4\uffed-\ufff6\uffb1\023\021\uffda\uffca\027\uffe9\uffbe\ufff6\uffa9\uffb3\017}5s\uffbdJ\uffc6\uffa4\ufffcb\013\uff8f\002m\uff92\uffbc\034\uffb3BX[\ufff4p\uffd4\uff9f\ufffb~\uffea\ufff5\uffaf\uffad\uffb7z\uffb51\uffa5\ufff3\014\uffd3\uff9e6\uffa2\uffab)\ufff3VZn\uffab+\uffb8k'5\ufff3O\uffa6\uff8aZiZ%\uffad\uffe2<\uffa9\uffdb\uffd0\uffb8\ufff9P$6\uff8b\uffe4-\uffee\uff98\uffa6\uffa0W\uff84|\027\ufff0=\ufff4\uffa3T\017\024d\uffdb\uffac\uffd7\ufffa\uff82D\uffaf\uffed\177s\uffc1\uff8f1\uff8e\010\000\000";
        
    }
    
    static java_cup.runtime.lr_parser getParser(jltools.lex.Lexer lexer,
                                                ErrorQueue eq) {
        if (((Boolean)
               options.get(MAIN_OPT_EXT_OP)).booleanValue()) {
            return new jltools.ext.op.Grm(lexer,
                                          ts,
                                          eq);
        } else {
            return new jltools.parse.Grm(lexer,
                                         ts,
                                         eq);
        }
    }
    
    static Iterator getNodeVisitors(int stage) {
        List l =
          new LinkedList();
        if (((Boolean)
               options.get(MAIN_OPT_SCRAMBLE)).booleanValue() &&
              stage ==
                Compiler.DISAMBIGUATED) {
            if (((Boolean)
                   options.get(MAIN_OPT_DUMP)).booleanValue()) {
                CodeWriter cw =
                  new CodeWriter(new UnicodeWriter(new PrintWriter(System.out)),
                                 ((Integer)
                                    options.get(Compiler.OPT_OUTPUT_WIDTH)).intValue());
                l.add(new jltools.visit.DumpAst(cw));
            }
            jltools.visit.NodeScrambler ns;
            Long seed =
              (Long)
                options.get(MAIN_OPT_SCRAMBLE_SEED);
            if (seed ==
                  null) {
                ns =
                  new jltools.visit.NodeScrambler();
            } else {
                ns =
                  new jltools.visit.NodeScrambler(seed.longValue());
            }
            l.add(ns.fp);
            l.add(ns);
        }
        if (((Boolean)
               options.get(MAIN_OPT_EXT_OP)).booleanValue() &&
              stage ==
                Compiler.CHECKED) {
            if (((Boolean)
                   options.get(MAIN_OPT_DUMP)).booleanValue()) {
                CodeWriter cw =
                  new CodeWriter(new UnicodeWriter(new PrintWriter(System.out)),
                                 ((Integer)
                                    options.get(Compiler.OPT_OUTPUT_WIDTH)).intValue());
                l.add(new jltools.visit.DumpAst(cw));
            }
            l.add(new jltools.ext.op.ObjectPrimitiveCastRewriter(ts));
        }
        if (((Boolean)
               options.get(MAIN_OPT_DUMP)).booleanValue()) {
            CodeWriter cw =
              new CodeWriter(new UnicodeWriter(new PrintWriter(System.out)),
                             ((Integer)
                                options.get(Compiler.OPT_OUTPUT_WIDTH)).intValue());
            l.add(new jltools.visit.DumpAst(cw));
        }
        return l.iterator();
    }
    
    static final void parseCommandLine(String[] args,
                                       Map options,
                                       Set source) {
        if (args.length <
              1) {
            usage();
            System.exit(1);
        }
        boolean hasError =
          false;
        Collection sourcePath =
          new LinkedList();
        sourcePath.add(new File("."));
        options.put(MAIN_OPT_SOURCE_PATH,
                    sourcePath);
        options.put(MAIN_OPT_DUMP,
                    new Boolean(false));
        options.put(MAIN_OPT_STDOUT,
                    new Boolean(false));
        options.put(MAIN_OPT_SCRAMBLE,
                    new Boolean(false));
        options.put(MAIN_OPT_EXT_OP,
                    new Boolean(false));
        options.put(MAIN_OPT_THREADS,
                    new Boolean(false));
        options.put(Compiler.OPT_OUTPUT_WIDTH,
                    new Integer(80));
        options.put(Compiler.OPT_VERBOSE,
                    new Boolean(false));
        options.put(Compiler.OPT_FQCN,
                    new Boolean(false));
        options.put(Compiler.OPT_SERIALIZE,
                    new Boolean(true));
        for (int i =
               0;
             i <
               args.length;
             ) {
            if (args[i].equals("-h")) {
                usage();
                System.exit(0);
            } else
                if (args[i].equals("-version")) {
                    System.out.println("jltools Compiler version " +
                                         (Compiler.VERSION_MAJOR) +
                                         "." +
                                         (Compiler.VERSION_MINOR) +
                                         "." +
                                         (Compiler.VERSION_PATCHLEVEL));
                    System.exit(0);
                } else
                    if (args[i].equals("-d")) {
                        i++;
                        options.put(MAIN_OPT_OUTPUT_DIRECTORY,
                                    new File(args[i]));
                        i++;
                    } else
                        if (args[i].equals("-S")) {
                            i++;
                            StringTokenizer st =
                              new StringTokenizer(args[i],
                                                  File.pathSeparator);
                            while (st.hasMoreTokens()) {
                                sourcePath.add(new File(st.nextToken()));
                            }
                            i++;
                        } else
                            if (args[i].equals("-fqcn")) {
                                i++;
                                options.put(Compiler.OPT_FQCN,
                                            new Boolean(true));
                            } else
                                if (args[i].equals("-post")) {
                                    i++;
                                    options.put(MAIN_OPT_POST_COMPILER,
                                                args[i]);
                                    i++;
                                } else
                                    if (args[i].equals("-stdout")) {
                                        i++;
                                        options.put(MAIN_OPT_STDOUT,
                                                    new Boolean(true));
                                    } else
                                        if (args[i].equals("-sx")) {
                                            i++;
                                            options.put(MAIN_OPT_SOURCE_EXT,
                                                        args[i]);
                                            i++;
                                        } else
                                            if (args[i].equals("-ox")) {
                                                i++;
                                                options.put(MAIN_OPT_OUTPUT_EXT,
                                                            args[i]);
                                                i++;
                                            } else
                                                if (args[i].equals("-dump")) {
                                                    i++;
                                                    options.put(MAIN_OPT_DUMP,
                                                                new Boolean(true));
                                                } else
                                                    if (args[i].equals("-scramble")) {
                                                        i++;
                                                        options.put(MAIN_OPT_SCRAMBLE,
                                                                    new Boolean(true));
                                                        try {
                                                            long l =
                                                              Long.parseLong(args[i]);
                                                            options.put(MAIN_OPT_SCRAMBLE_SEED,
                                                                        new Long(l));
                                                            i++;
                                                        }
                                                            catch(NumberFormatException e) {
                                                                
                                                            }
                                                    } else
                                                        if (args[i].equals("-noserial")) {
                                                            i++;
                                                            options.put(Compiler.OPT_SERIALIZE,
                                                                        new Boolean(false));
                                                        } else
                                                            if (args[i].equals("-threads")) {
                                                                i++;
                                                                options.put(MAIN_OPT_THREADS,
                                                                            new Boolean(true));
                                                            } else
                                                                if (args[i].equals("-op")) {
                                                                    i++;
                                                                    options.put(MAIN_OPT_EXT_OP,
                                                                                new Boolean(true));
                                                                } else
                                                                    if (args[i].equals("-v") ||
                                                                          args[i].equals("-verbose")) {
                                                                        i++;
                                                                        options.put(Compiler.OPT_VERBOSE,
                                                                                    new Boolean(true));
                                                                    } else
                                                                        if (args[i].startsWith("-")) {
                                                                            System.err.println(Main.class.getName() +
                                                                                                 ": illegal option -- " +
                                                                                                 args[i]);
                                                                            i++;
                                                                            hasError =
                                                                              true;
                                                                        } else {
                                                                            if (hasError) {
                                                                                usage();
                                                                                System.exit(1);
                                                                            }
                                                                            if (options.get(MAIN_OPT_SOURCE_EXT) ==
                                                                                  null &&
                                                                                  args[i].indexOf('.') !=
                                                                                    -1) {
                                                                                options.put(MAIN_OPT_SOURCE_EXT,
                                                                                            args[i].substring(args[i].lastIndexOf('.')));
                                                                            }
                                                                            source.add(args[i]);
                                                                            sourcePath.add(new File(args[i]).getParentFile());
                                                                            i++;
                                                                        }
        }
        if (hasError) {
            usage();
            System.exit(1);
        }
        if (source.size() <
              1) {
            System.err.println(Main.class.getName() +
                                 ": must specify at least one source file");
            usage();
            System.exit(1);
        }
        if (options.get(MAIN_OPT_SOURCE_EXT) ==
              null) {
            options.put(MAIN_OPT_SOURCE_EXT,
                        ".java");
        }
        if (options.get(MAIN_OPT_OUTPUT_EXT) ==
              null) {
            options.put(MAIN_OPT_OUTPUT_EXT,
                        ".java");
        }
    }
    
    private static void usage() {
        System.err.println("usage: " +
                             Main.class.getName() +
                             " [options] " +
                             "File.jl ...\n");
        System.err.println("where [options] includes:");
        System.err.println(" -d <directory>          output directory");
        System.err.println(" -S <path list>          source path");
        System.err.println(" -fqcn                   use fully-qualified class names");
        System.err.println(" -sx <ext>               set source extension");
        System.err.println(" -ox <ext>               set output extension");
        System.err.println(" -dump                   dump the ast");
        System.err.println(" -scramble [seed]        scramble the ast");
        System.err.println(" -noserial               disable class serialization");
        System.err.println(" -op                     use op extension");
        System.err.println(" -post <compiler>        run javac-like compiler after translation");
        System.err.println(" -v -verbose             print verbose debugging info");
        System.err.println(" -version                print version info");
        System.err.println(" -h                      print this message");
        System.err.println();
    }
    
    public static final java.lang.String jlc$CompilerVersion =
      "1.0.0";
    
    public static final long jlc$SourceLastModified =
      945373925000L;
    
    public static final java.lang.String jlc$ClassType =
      "\037\uff8b\010\000\000\000\000\000\000\000\uff95VMl\033E\024\036o\034'q\uff92\uffc6\ufff9Q\uff934D \uffa0B9\uffd4\uff91\uffe0TU\uffaapmGqY\uffc7\uffc6vB\033\024\uffdc\uffb5=q6\uffd9?ff\035\007\uffa1J\010\uffa9B*\177\uff87r\000\004\007\uff84Z\uff84*\016\025p\uffe3GB\010\011q\001!qL\uff88\020'8BA\uffa2\022o\uffc6\uffde\ufff5\uffda\uff8e\uffdd8\uff87\uff89w\ufff7{\uffef}\uffef\uffbdo\uffde\uffcc\uffed?Q?%h~[c\uffa6\uffa9\uffd10\uffdb\uffb30\015\uffa7\025Bq)\uffaa)\uff94\uffe6\uffe0E\uffca\016}\ufff8\uffee?\uffb3\037K\010U\011\uff9ak\uffc6\uffba\uffa8\uff84ni\uff97\uffff\ufffd\uffe0\uffbf\uffb3\uffb7\uff96\002\022\032]G\uffc3*\uff8d\030\uffa6\uffb1\uffa7\uff9b6]G\003*M\030\006&2\032+\uff9a\006STC5\uffca\uffc2\uff98\uffa1Y\uffb9\uffeesQ\ufff8\\t}\uff9e\uff93Q`S\uffc5Z\011@3.\uffc8f\uffaa\uffb6\uffc8?\uff97d\uff952\uffc0\ufff4ojJ\031 s-~\"\uffc5\"\uffa6t\uff89\177\004\uffd4\uffe0\uffa6\uffadi+\uff8a\uff8e\031\uff9a\uff90\uffb7\uff95\uff8a\uffb2\uffa8)Fy1\uffcb\0100\uff81\uffef#*\uffe7'bc\ufffa<\uffba\uff8a\ufffad4$\uffdeq#\ufffe\uffa2_FA\uffd5`\uff98l*E\0272\000\016\uffb7\uffcc\uff92\ufff38l)\uffc5\035\uffa5\uff8c=6Ct\uffcb$\uffac\uffe9\uff85ma\uffc23`h\uffaa\uff85\uffb3H\033\uffca<\uffdd\uffa1\uffcc\ufff7^\uffbby\uffc6\ufffc\ufffb\uffd3\uffefk\uffcd\uff98hFq\uffc0;C\ufff2F\uffea\ufffd\uffe7^\uffe6\000\013!\013\uffda{\uffd2A\ufff1\uffca\uff85\uffdd\uffca\ufffdq\uffe7\\\uffe8\ufff53\ufff4s\011\ufff5\uffadC\uffa6\uffban3\uffa5\uffa0a\uffa8\uff84\uffa2i\uffe6..\uffe5\uff99\uffa08\uffee\uffa9\uff96\uffe0\uffc1\uff8bU\uff804\uffa1ny\015\0341\024\uffaaADgDS t\uffa5M+K\uffbc\uff95\011\uff832\uffc5(\uffe2\037?:\ufff4\uffed?\ufff1\uffe27\022\uffaf\uffc8(6\uff8a\uff9aI\uffc1!g\uffc7\uffcb\024p\uffda\uffca\037\ufffc2\ufff2\033\uff8d\ufffa\005\uffcc5E\uffb3[\ufffa\uff98*l\uffe3\"W\uff83\uff9f9.\uffaa<\ufff9)\016\uffa9e.\uffab\uffc6N-\ufff5\uff91\uff85\uffec\uffc6\uffc5+\uffaf<\uffdc\uffc7\uff8b\uffb4\uffebG\010\uff85D\uffa0\023|\031\002\uffab\uffd9f\uffe6\036%}r36\uffb7\uffb2\ufffb\uffd8g\022\ufff2%\uff90\uffbf\uffa02\uffca\uffcb\uff8cNA+\uff93\uff91\uffc4J>\uff95\uffce\uffe5\uffb3\uffa9\uffd5L4\uff9eOGr\uffcb\uff9cBK\031\"zA-\uffdb\uffb0+x\uffaew\uffcf\037|\ufff1\uffe4\uffa3\177\uffdd\005w\uffde$\uffab|\035\uff84\uffc2\012\ufff6<\uffc1pM\uffa8^\uff9a|\uff9d\uffaa\uffc5\uff9euc\uffa7Vs\uffe9\uffd5\\>\uff96\uffc8\uffc4\uffa3\uffb9T\uffe6\uffb2\uffc5Q3\035\uffcc&[)\uffc7/\uffe5\uff8eiP\uff8fs?\uff83\uffb1F\uff84\\\014l\uffba\uff82O\uffba\uffe0t*\uff9b\uffcbGS\uffc9tB\uff8eg\uffba\uffda\uff8c\uffba6\uffb1\uffd5d\uffba+t\uffbc\uffc1%\uff9a\uff89$/\uffc8\ufff1c\uffb2q\uffe0\ufff9l<\036;f\uffbaP\030\ufff8\uffdf\025\034r\uffc1\uffb9\uffe5L<\022\uffcbvE\017'#\uff97\034 \uff88\uffeaDC\031\uffb2\011\003\uffec\ufffaoo\ufffe\ufff0\uffc6#\ufffb\uffa0\uffa3\uff8b\uffa8\uffbf\uffc2\ufff7\007\uffcc\007\uff8f|Vl\uffbd\uff80\uffc9\uffb5\uffdbo\uffcd\017\uffdf8\uffb8^\uff9f\016\uffe2Oj\uff97h\uff9a\uffa8\uffba\uffca\uffd4\012\uffe6\022}\ufffc\uffce\uffe9_\017~\uffb9\uffb1\\S<\uffec\ufffaRM\uff9d`\0328\uff8ak\uff90\uffa1\001\uffd3b\uffaaiPK\uffbc\uff9c\uff86.5vaR\uffb1:X\005\uffa8i\uff93\">\uffca(\uff8bY\007#\uff895\uffa2\uffcc\uffb4\uff8f\uffc3\uffec\036eX\uffef`;\uffb4\uffa5\uffd08!&\uffa9\uffbb8\uffcf\uffabQu\021>\uff90\uff8c\uffe3Q\uff87\023\013\uffa8\uffab\uff86\ufff8\032D\025Q\002\ufff10.\006\uff88\uffe4\uff90\uff98k3\011\uffe7\024R\uffc6\uffcc\uffc9\0010\017\uffb4c\uff9e1\uffc9Nn\uff8b`\uffa5T\uffb5\uffea1\ufff8\uffba\uffe6\011\uff81\uffaaNp\uff82\036jN5)\016!q\016\uffd7\uffe7\uffab\uffff\uffd5\uffaf+\uffc1\uffaf\uffbe\ufffc]\uffe2\uffd3\uffb3}\uffbeJ2:\uff81\uffabPl\uffde'\ufffe\uffd29\uffc0\uff9a\uffa6n\uff90`f\023\uffc3\035\uffca \uffa9\uff99N\uff81\uff9f\ufffa\ufff9\uffca-\033\uffc9\ufff7$\uffee{\0242\uffb6ul0\uffaf\uffeb\uffd6\ufff1\uffe6Im@\ufffc\uffce7%N\uffd0dC\000\021B\uff94=>\uffba\uffab/\ufffd4\uffff\ufff6w\uffca{}B\uff8dT}\001\013!\ufffb\uff84\033\037m;9\uff85!gqxv\uffa1\uffbc?ym\011\ufff8\uff81aI\uffd5)0*(\uffb4\uff9e\uff99#i\uff9f\uffd8\uff83U\uff86\ufffc\uffbc7\015\uffc9x\uffdb\uff81y3\uffbc-\uffe1j\uff99u%\uffe4;\"\031\uffc7\uffcc\uffe1\uffd9\uff90\033C#\024\uffb3eG\uff89=D\014\ufff2\037z\uff97`R\uffb32]1k\uffb8\032\uff96q\025\023\uffe7\uffcbt\uffd3\015A\020y\uffda\uffc60B`\uff8f\uff80r\uffc5\uff85\uff90\ufff4\uffc0l\uffd0q|\uff8aw0_\uffb4\uffad0\uffb1\015\uffa6\uffea8\uffac\uff91\uffbc%\uffdc\ufff5P\uffa4\000\ufff0\030\003\036+f\011\uffaf\uffa9Te\uffbd\uffd5\uffc9e3\uffd1\uffd0S\002\uffaeq\uff8a\uffe3\uffe7h\026}\uffc2O\uff9fx.\uffbb\uffca\uffe0K\uff94/\uffcb@*$R\uff89\uff9a\uffba\uffae\030p\uffb10p\017\uffacf\uffee\uffd7=\uffc7\uff82\uffa1~\uff9b\uffc2\uff95\uffb2we\020t\uffba\uffe5\032\0113\uff99\021\uffbb\010\uff89{g\uffc5\030\uffbbz\uffe1\uffdb\uff85\uffc32?\025\uff8e\uffc3\uffe7\uffc1\ufff6\ufff1\ufff5\uffac\uffc7\ufff5F\017LEQ\ufff5\uffaa\005\ufffb\uffcd\uff9d\uffae\uffd3M\ufff7\uff9e\uffda\uffc5\uffee\177lv\uffa2\uffbf\uffaa\014\000\000";
    
    public static final java.lang.String jlc$CompilerVersion =
      "1.0.0";
    
    public static final long jlc$SourceLastModified =
      945380246000L;
    
    public static final java.lang.String jlc$ClassType =
      "\037\uff8b\010\000\000\000\000\000\000\000\uff95VMl\033E\024\036;\uff8e\ufff3\uffd74\uffbf\uffcaO\uffd3\012\004\uffadP\016u$8U\uff91*\\\uffc7Q\\\uffd6\uffb1\uff89\uff9d\uffd0\026\025wlO\uff9cM\ufff6\uff8f\uff99\uffd9d\uff83P%\uff84TU*\177\uff87r\000\004\007\uff84Z\uff84Z\016\025p\uffe3GB\010\011q\001!qAJ\uff89\020'8BA\uffa2\022of\uffbd\uffeb\uffb5\035\uffa7q\016\023\uffef\uffee\ufff7\uffde\ufffb\uffde{\uffdf\uffbc\uff99[\177\uffa2NF\uffd1\uffb1u\uff8d\uff9b\uffa6\uffc6b|\uffdb\",\uff96\uffc5\uff94\uff91rB\uffc3\uff8c\uffe5\uffe1E\uffc6\036\ufffc\uffe0\uff9d\177&?\012#\uffe4P4U\uff8f\ufff5Q)\uffdd\uffd2\uffce\uffff\ufffb\ufffe\177\uffa7n\uffceG\uffc3\uffa8\uffff\002\uffeaSY\uffdc0\uff8dm\uffdd\uffb4\uffd9\005\uffd4\uffa5\uffb2\uff94a\020\uffaa\uffa0\uff81\uff92ip\uffac\032\uffaaQ\uff91\uffc6\034M*U\uff9f3\uffd2\uffe7\uff8c\uffefsVA\uffd1U\uff95he\000M\ufff8 \uff9b\uffab\uffda\uff8c\ufff8\\VT\uffc6\001\uffd3\uffb9\uffaa\uffe1\012@\uffa6\032\ufffc\uffc4K%\uffc2\uffd8\uffbc\ufff8\010\uffa8\uffeeU[\uffd3\026\uffb1N8\032V\uffd6\ufff1&\uff9e\uffd1\uffb0Q\uff99\uffc9q\012L\uffe0\ufffb!U\ufff0\uff93\uffb1\011{\036]F\035\012\uffea\uff91\uffef\uff84\uff91x\uffd1\uffa9\uffa0^\uffd5\uffe0\uff84\uffae\uffe2\uff92\017\uffe9\002\uff87kf\uffd9{\uffec\uffb3pi\003WH\uffc0\uffa6\uff87\uffad\uff99\uff94\uffd7\uffbd\uffb0-BE\006\034\uff8d6p\uff96iC\uff99\uffc7[\uff94\ufff9\ufffe\uffab7N\uff9a\177\177\ufff2\uff9d\uffdb\uff8c\uffe1z\uff94\000\uffbc\uffdd\uffa3\\\uffcc\uffbc\ufff7\uffdc\uffcb\002`!dA{\uffc7<\uff94\uffa8\\\uffcc\uffaf\uffdc\037wf\007_;\uffc9>\013\uffa3\uff8e\013\uff90\uffa9\uffae\uffdb\034\0275\002\uff95\uffc0\uff9afn\uff91r\uff81K\uff8aC\uff81jI\036\uffa2XEH\023\uffeaV\uffd0\uffc0\021G\uff83.DvF6\005Bo6ie^\uffb42e0\uff8e\uff8d\022\ufff9\uffe1\uffc3\uffdd\uffd0\uffce\023/~\035\026\025\uffe9'FI3\0318\024\uffecD\uff99\uffa2^[\uffc5CDA\021\uffa3V\uffbf\uffa8\uffb9\uff825\uffbb\uffa1\uff8f\uff99\uffe2:)\0115D\uffb8\uffe7\uffc2\021\uffc9\uff8f\012\uff88\uff9b\uffb9\uffa2\032\033n\uffea\uff87\uffa6s\027\uffcf^\uffba\ufffaH\uff87(\uffd2V\004!4\"\003\035\026K\017XM\uffd63\017(\uffe9\uffe3\033sS\uff8b[\uff8f}\032F\uffa1\024\uff8a\024U\uffceD\uff99\uffd1\021he:\uff9eZ,d\uffb2\ufff9B.\uffb3\uffbc\uff94H\026\uffb2\ufff1\ufffc\uff82\uffa0\uffd0P\uff86\uffb8^T+6\uffec\012\uff91\uffeb\uffbd\uffd3w?\177\ufff2\ufff8_\ufff7\uffc0]0IG\uffac\uffddPX\uffc9^$\030s\uff85\032\uffa4)\uffd6Q7\ufff6\uffa4\037;\uffb3\uff9c\uffcf.\uffe7\013s\uffa9\uffa5d\"\uff9fY:o\011\uffd4D\013\uffb3\uff91F\uffca\uffc9s\ufff9\003\032T\uffe3<\uffc8`\uffa0\026!?\0076\ufffb\uff82\uffc7|p6\uff93\uffcb\027\022\uff99t6\uffa5$\uff97\ufff6\uffb5\uffe9\ufff7m\uffe6\uff96\uffd3\uffd9}\uffa1C5.\uff89\uffa5x\ufffa\uff8c\uff92< \033\017^\uffc8%\uff93s\007L\027\012\003\uffff\ufff7\005\017\ufffa\uffe0\ufffc\uffc2R2>\uff97\uffdb\027\uffdd\uff97\uff8e\uff9f\ufff3\uff80 \uffaa\uffc35e(&\014\uffb0k\uffbf\uffbd\ufff1\ufffd\uffeb\uff8f\uffee\uff80\uff8e\uffce\uffa2\uffceM\uffb1?`>\004\uffe4\uffb3h\uffebEB\uffaf\uffdcz\ufff3X\uffdf\ufff5\uffbb\uffd7\uffaa\uffd3A\ufffe\uff85\uff9b%\uff9a\uffa5\uffaa\uffaeru\uff93\010\uff89>~\uffe7\uffc4\uffafw\177\uffbe\uffbe\uffe0*\036v}\uffd9U'\uff98F\ufff7\uffe2\uffda\uffcbQ\uff97iq\uffd54\uff98%_\uff8eC\uff97j\uffbb0\uff8d\uffad\026VQf\uffda\uffb4D\ufff62\uffca\021\uffde\uffc2(\uffcckQ&\uff9a\uffc7an\uff9bq\uffa2\uffb7\uffb0\uffedY\uffc3,I\uffa9I\uffab.N\uffcbj\uffec\uff81\uff9d\004\uffe5\uffafk\uffa5\uffe3\011S\uffb7T\uff8d\uffd0\025B\031\uffe4\uffd7\uffba_`0&\014r2#\0053\uff9e6\uffcb*\uff9ce\uffeeA1\013\uff90_\uff8e\uffde^\uffbc\uffea\uff87\uffedj\uffe1\uffa5_\uff86\ufff5\uffa6\uffbf\033\uffd0\ufff1\001!\020\uffb5\uff97\uffb3\016g*\024W5\uffe4\uffd7^\uffb4)\uff9b$\037\uff86\uffe4\uff88\013{e\uff9aj2\uff89\uffe51\uffad\020\uffeeU\0310G\uff9b1\uffcf\uff98t#\uffbfF\011.;V5\uff86Xq \004r\uffbc\uffe0\024=\\\uffdf\uff8c\uffb4<&\uffe5M\uffa1z\002D^\ufff9j\uffb3\ufff7\uffcb/~\017\uff8b\ufff9\uffde|\002\uff84\025t\uff988 \007\uffa1$\ufff1\uffd2;b\uffeb\uffce\uff85^J\uffb8M\015\uffff\uffd8\000\uffd1O\uffb4\012\ufffc\uffd4O\uff97n\uffdaH\uffb9\037\026\uffbe\ufffb!c['\006\017\uffban\034\uffc0\uff81\uffd4\uffdc\006\uffad\uffd7%NA\025\uffbeD\uffe3\uff94\uffe2mq\uffb88/\ufffdx\uffec\uffado\ufff1\uffbb\035r\uffbf0\ufff5\005\"\uffb7ZH\uffba\011\uffb1\uffa6\uffb3]\032\012\026\uffbb\uffa7\uffa6+;#W\uffe6\uff81\037\030\uff96U\uff9d\001\uffa3\"f\uffd5\uffcc\uffbcM\027rE\uffc0QD\ufff4\uffa6\uffa6\uff98`;\uffe4\uffde\016\uffb6D\uffca\uffc9W[h\uff8fd<3\uff8f\uffa7\uffbf! \uffd2!F\ufff8\uff82\uffb7W\uffda\uff88\uffd8+~8\ufffb\004\013\uffd7+\uffd3\027\uffb3F\uff9c\uff98B\034B\uffbd/\uffe3uw\030I\uffe4i\uff9b\uffc0\uff90\uff83]\014\uffca\uff95WV\uffda\006\uffb3n\uffcf\ufff1\021\uffd1\uffc1B\uffc9\uffb6b\uffd46\uffb8\uffaa\uff93\uff98F\013\uff96t\uffd7F\uff91\uffa2\uffc0c\000x,\uff9ae\uffb2\uffa22\uff95\uffb7W'\uff9f\uffcdpMO)\uffb8hb\uffcf\uffcf\uffde,:\uffa4\uff9f\016\ufff9L}e\uff88%!\uff96\005 5(S\uff81\uffb9\uffa5c\003\uffae>\006i\uff83\uffd5\uffc4\uff83\uffba\uffe7Yp\uffd4i3\uffb8\ufff4\uffb6\uffaf\014\uff8aN4\\t\uffe1\uffd4\uffe0\uffd4.A\uffe2\uffc1Y1\uffc0/\uff9f\ufff9fz\uffb7\"\uffce\uffad\uff83\ufff0y\uffa8y|=\033p}\uffb1\015\uffa6\uffb2\uffa8\uff8ec\uffc1~\ufff3\uffa7\uffebx\uffdd\uffcd\uffcc\uffbdz\ufffe\017qU\uff90\ufff0L\015\000\000";
    
    public static final java.lang.String jlc$CompilerVersion =
      "1.0.0";
    
    public static final long jlc$SourceLastModified =
      945380731000L;
    
    public static final java.lang.String jlc$ClassType =
      "\037\uff8b\010\000\000\000\000\000\000\000\uffadVKl[E\024\035;\uff8e\ufff3o>\uff8d\ufff2i\032\uff81\uffa0\025\uffca\uffa2\uff8e\004\uffab*R\uff85\uffeb8\uff8a\uffcbslb\uffa7\uffb4E\uffc1\035\uffdb\023\uffe7%\uffef\uffc7\uffcc\uffbc\uffc4\uffe1S\011E\uffaa*\uff95\uffdf\uffa2,\000\uffc1\002\uffa1\026\uffa1\uffc2\uffa2\002v\ufffc\uff84\020\022b\003Bb\uff83\uff94\020!V \uffb1\uff81\uff82D%\uffee\uff8c\ufffd\uff9e\uff9f\uffed\uffd8\uff8d\021YL\ufffc\uffde;\ufff7\uffdes\uffef=sgn\ufffe\uff86\uffda\031E\uff93k\0327M\uff8d\uff85\ufff8\uff96EX(\uff89)#\ufff9\uff88\uff86\031K\uffc3\uff8b\uff84=\ufff0\ufff6\uffeb\177\uff8d\uffbf\uffebG\uffa8H\uffd1D5\uffd6E\uffc5tK;\uffff\ufff7[\uffff\uff9c\uffbc1\027\ufff4\uffa3\uffbe\013\uffa8Gea\uffc34\uffb6t\uffd3f\027P\uff87\uffcab\uff86A\uffa8\uff82\ufffas\uffa6\uffc1\uffb1j\uffa8FA\032s4\uffae\uff94}NK\uff9f\uffd3\uffae\uffcf\031\005\005WT\uffa2\uffe5\0014\uffe6\uff82l\uffaej\uffd3\uffe2s^Q\031\007L\ufffb\uff8a\uff86\013\000\uff99\uffa8\ufff1\023\uffce\uffe5\010cs\uffe2#\uffa0:WlM[\uffc0:\uffe1hHY\uffc3\033xZ\uffc3Fa:\uffc5)0\uff81\uffef\uffbd\uffaa\uffe0'c\023\ufff6$\uffba\uff84\uffda\024\uffd4%\uffdf\011#\ufff1\uffa2]A\uffdd\uffaa\uffc1\011]\uffc19\027\uffd2\001\016W\uffcd\uffbc\ufff3\uffd8c\uffe1\uffdc:.\020\uff8fM\027[5)\uffafza[\uff84\uff8a\0148\032\uffae\uffe1,\uffd3\uff862\uff8f6(\ufff3\uff9d\027\uffae\uff9f0\uffff\ufffc\uffe0\uffebR3\uff86\uffaaQ\002\ufff0Z\uff97\uffb2\uff9cx\ufff3\uff89m\001\uffb0\020\uffb2\uffa0\uffbd#\016JT.\uffe4V\uffee\uffd7[3\003/\uff9e`\037\ufff9Q\uffdb\005\uffc8T\uffd7m\uff8e\uffb3\032\uff81J`M37I>\uffc3%\uffc5AO\uffb5$\017Q\uffac,\uffa4\011u\uffcbh\uffe0\uff88\uffa3\uff81\022DvF6\005Bo\uffd4ieN\uffb42f0\uff8e\uff8d\034\ufff9\ufff6\uff9d=\uffdf\uffceC\uffcf|\uffe1\027\025\uffe9#FN3\0318\024\uffecD\uff99\uff82N[\uffc5C@A\001\uffa3R\uffbf\uffa0y\026kvM\037\023\uffd95\uff92\023j\010p\uffc7EQ$?, \uffa5\uffcc\025\uffd5X/\uffa5\uffde;\uff95Z>s\ufff1\uffca}m\uffa2H\uff9b\001\uff84\uffd0\uff88\014tH,]`5^\uffcd\uffdc\uffa3\uffa4\ufff7\uffaf\uffcfN,l>\ufff0\uffa1\037\ufff9b(\uff90U9\023eFG\uffa0\uff95\ufff1pl!\uff93H\uffa63\uffa9\uffc4\uffd2b$\uff9aI\uff86\uffd3\ufff3\uff82BM\031\uffc2zV-\uffd8\uffb0+D\uffae\uffb7O\uffed~\ufffc\ufff0\uffb1?n\uff83;o\uff92E\uffb1vBa%{\uff91`\uffa8$T/M\uffb1\016\uff97b\uff8f\uffbb\uffb1\023K\uffe9\uffe4R:3\033[\uff8cF\uffd2\uff89\uffc5\ufff3\uff96@\uff8d50;\\K9z.}@\uff83r\uff9c\uffbb\031\ufff4W\"\uffa4g\uffc1\uffa6)x\uffc4\005'\023\uffa9t&\uff92\uff88'cJt\uffb1\uffa9M\uff9fk3\uffbb\024O6\uff85\016V\uffb8D\026\uffc3\ufff1\uffd3J\ufff4\uff80l\034x&\025\uff8d\uffce\0360](\014\ufffco\012\036p\uffc1\uffe9\ufff9\uffc5hx6\uffd5\024\uffdd\023\017\uff9fs\uff80 \uffaaC\025e(&\014\uffb0\uffab?\uffbf\ufffc\uffcdK\ufff7\uffef\uff80\uff8e\uffce\uffa0\ufff6\015\uffb1?`>x\uffe4\uffb3`\uffebYB/\uffdf|e\uffb2\uffe7\uffda\uffee\uffd5\ufff2t\uff90\177\ufffez\uff89&\uffa9\uffaa\uffab\\\uffdd B\uffa2\017\uffde:\ufffe\uffd3\uffee\017\uffd7\uffe6K\uff8a\uff87]\uff9f/\uffa9\023L\uff83\ufffbq\uffed\uffe6\uffa8\uffc3\uffb4\uffb8j\032\uffcc\uff92/G\uffa1K\uff95]\030\uffc7V\003\uffab 3m\uff9a#\ufffb\031\uffa5\010o`\uffe4\uffe7\uff95(c\ufff5\uffe30\uffb5\uffc58\uffd1\033\uffd8v\uffadb\026\uffa5\uffd4\uffa4e\027\uffa7d5\ufff6\uffc1\uff8e\uff83\ufff2\uffd7\uffb4\uffdc\uffb1\uff88\uffa9[\uffaaF\uffe8YB\031\uffe4\uffd7\uffb8_`0\"\014R2#\0053\0367\ufff3*\uff9ce\uffa5\uff83b\006 ?\036}o\uffe1\uff8a\033\uffb6\uffa3\uff81\uff97>\031\uffd6\uff99\ufffeM\003\ufffe\uffff\014'\ufffb>\uffff\ufffd?1,\uffba\000\037l;\uffa7+:\uff9c\ufffa\uffd0~\uffd5\uff90_\uffbb\uffd1\uff86\uff94\uff91|\030\uff94C\uffd8\uffef4r\uffa2\uffce$\uff94\uffc6\uffb4@\uffb8\uffa3\003\uffc0\034\uffad\uffc7<f\uffd2\ufff5\ufff4*%8_\uffb4\uffca1\uffc4\uffaayB\uffa0\uffa2\023\uff9c\uffa2{\uffab\uffe5\022\uff97\007\uffb9\uffbc\uffcb\uff94\uffcf\uffa8\uffc0\ufff3\uff9fmt\177\ufffa\uffc9/~q\002\uffd5\uff9fQ~\005\035\"E\020\uffac\uffd0\uffbax\uffe9\\\002\uffaaN\uffaenJ\uffb8M\015\ufff7`\uff83m9\uffd6(\ufff0#\uffdf_\uffbca#\uffe5\uff8e_\ufff8\uffee\uff83\uff8cm\uff9d\030\uffdc\uffeb\uffba\ufff6\uff88\ufff0\uffa4Vj\uff90]\uff958\005U\uffb8\uff9b(L)\uffde\022\uffc7_\ufff1\uffb9\uffef&_\ufffd\012\uffbf\uffd1&w4S\uff9f\"r\030\ufff8\uffa4\033\037\uffab\uffbb}HC\uffc1b\uffef\uffe4Ta\uffe7\ufff0\uffe59\uffe0\007\uff86yUg\uffc0(\uff8bY93g,\ufff8J\"\uffe0( zSQ\uff8c\uffb7\035O\uff8bfx[\"\uffe5\uffe4\uffaa\uffcd\uffb7O2\uff8e\uff99\uffc3\uffd3\uffdd\uffb2\020\uffa9\uff97\021>\uffef\uffec\uffe6\026\"v\uff8b\037\uffdbM\uff82\ufff9\uffab\uff95\uffe9\uff8aY#\uffc5\uff90B\uff8a\uff84:_F\uffabnY\uff92\uffc8\uffa36\uff811\014s\006\uff94+/\uffd5\uffb4\005f\uff9d\uff8e\uffe3#\uffa2\uff83\uff99\uff9cm\uff85\uffa8mpU'!\uff8df,\uffe9\uffae\uff85\"\005\uff81G?\ufff0X0\ufff3\uffe4\uffac\uffcaT\uffdeZ\uff9d\\6C\025=\uffc5\uffe0*\uff8c\035?\ufffb\uffb3h\uff93~\uffda\uffe4\ufff3\uffb3\uffae2\uffc4\022\021\uffcb<\uff90\032\uff90\uffa9\uffc0\uffdc\uffd2\uffb1\001\uff973\uff83\uffb4\uffc0j\uffecn\uffdds,8j\uffb7\031\\\uffcb[W\006E\uffc7k\uffae\uffe2p\uffaeqj\uffe7 q\uffef\uffac\uffe8\uffe7\uff97N\1779\uffb5W\020'\uffebA\ufff8\uffdcS?\uffbe\036\ufff7\uffb8^n\uff81\uffa9,\uffeav\uffd1\uff82\ufffd\uffe6N\uffd7\uffd1\uffaa\uffbbc\uffe9r\ufffc/V\uffd2\uff92;\uffee\015\000\000";
    
}
