class T6551n6 {
    interface C {}
    void foo() {
        new Object() {
            class C {}
            // C refers to the innermost class C, not the outer interface
            class Sub extends C {}
        };
    }
}
