interface I {}
class C {
	void foo(I i) {
		i.equals(null);
		i.getClass();
		i.hashCode();
		i.notify();
		i.notifyAll();
		i.toString();
	}
}
