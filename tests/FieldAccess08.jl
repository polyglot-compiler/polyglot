class T83i2 {
	private int i;
	class Inner extends T83i2 {
		int j = this.i; // i not inherited
	}
}
