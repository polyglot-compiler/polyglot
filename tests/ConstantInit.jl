/**
 * This test case checks that numeric conversion is done correctly for
 * non-literal compile-time constant expressions.
 */
class ConstantInit {
    final static int X = 0;
    byte m() {
	byte b = X; // OK
        return b;
    }

    byte n() {
        return X;
    }
}
