interface T92i6 extends Cloneable {
    class Inner {
        Object bar(T92i6 i) {
            try {
                // Because this call is nested in the interface, it would have
                // full access to protected i.clone() if that existed.
                return i.clone();
            } catch (CloneNotSupportedException e) {
                return null;
            }
        }
    }
}
