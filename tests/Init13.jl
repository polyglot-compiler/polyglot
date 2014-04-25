class T1525r7 {
	public static void main(String[] args) {
		boolean b, r = args.length > 0;
		if ((r && (b = r)) ? r : r && (b = r))
			r = b;
		System.out.print("OK");
	}
}
