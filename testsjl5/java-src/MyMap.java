class MyMap<T, U> {
    MyMap(T t) {
	this.field = t;
    }
    <E> MyMap(T t, E e) {
	this.field = t;
    }
  void put(T key, U value) { }
  U get(T key) { return null; }
  <E> E foo(E x, T y) { return x; }
  T field;
}
