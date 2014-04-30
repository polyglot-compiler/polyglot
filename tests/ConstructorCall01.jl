class T8851q3 {
    class Inner {}
}
class Sub3 extends T8851q3.Inner {
    Sub3() {
        // this will not execute, but must compile
        ((T8851q3) null).super();
    }
}
