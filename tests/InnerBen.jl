// test case from Ben Hindman (b@cs.washington.edu).
// Polyglot 1.3.4 generates bad code.  2.x seems to work.
public class Inner {
  class A {}

  public static void main(String[] args) {
    Inner i = new Inner();
    Inner.A ia = i.new A();
  }
}

