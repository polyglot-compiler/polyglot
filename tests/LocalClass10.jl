class C {
	static void main() {
		int Type;
		class Type extends Exception {}
		try {
			throw new Type();
		} catch (Type t) {
		}
	}
}
