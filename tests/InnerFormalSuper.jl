class Outer {
  	class Inner{}
}

public class InnerFormalSuper extends Outer.Inner {
  	InnerFormalSuper(Outer a) {
      a.super();
    }
}

