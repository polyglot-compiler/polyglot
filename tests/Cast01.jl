class T1516a15 {
	interface I { int m(); }
	interface J { void m(); }
	I[] i = (I[]) new J[0];
}
