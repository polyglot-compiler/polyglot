class T846i2 {
	private int m() { return 1; }
	class Inner extends T846i2 {
		int j = this.m(); // m not inherited
	}
}
