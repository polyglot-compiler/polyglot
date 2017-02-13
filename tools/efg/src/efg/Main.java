package efg;

public class Main {
  public static void main(String[] args) {
    polyglot.main.Main main = new polyglot.main.Main();

    try {
      main.start(args, new efg.ExtensionInfo());
    } catch (polyglot.main.Main.TerminationException e) {
      System.err.println(e.getMessage());
      System.exit(1);
    }
  }
}
