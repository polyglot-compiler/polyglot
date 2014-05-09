class E1 extends Exception {}
class E2 extends E1 {}
class E3 extends Exception {}
class E4 extends E3 {}
abstract class T151222a12a {
public abstract void m() throws E1, E4;
}
interface T151222a12b {
void m() throws E2, E3;
}
abstract class T151222a12c extends T151222a12a implements T151222a12b {
{
  try {
      m();
      // whether a.m() or b.m() is chosen, it cannot throw E1 or E3
      // directly; but can throw something assignable to E1 or E3
  } catch (E2 e2) {
  } catch (E4 e4) {
  }
}
}
