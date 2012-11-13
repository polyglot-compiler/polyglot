public class Test {
    public I test() {
        return new I() {
            class Aux {
                public Aux copy() {
                    return new Aux();
                }
            }
        };
    }
}

interface I {
} 
