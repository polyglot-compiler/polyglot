class T15213t6 {
	interface I1 {
		void m();
	}
	interface I2 {
		int m();
	}
	boolean m(I1 i1, I2 i2) {
		return i1 == i2;
	}
}
