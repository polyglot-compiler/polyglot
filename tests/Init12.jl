class C {
  static public final boolean FOO = true;
  final int x;
  final int y;
  final int z;
  final int w;
  final int u;
  final int v;
  {
      // check the use of constant expressions
      boolean b = FOO && ((x=0)==0);
      b = !FOO || ((y=0)==0);
      int c = FOO ? z = 0: 99;
      int d = !FOO ? 99 : (w = 0);

      if (FOO) {
	  u = 0;
      }
      else {
	  // dead code
      }
      if (!FOO) {
	  // dead code
      }
      else {
	  v = 0;
      }
      int a = u + v;
  }

}
