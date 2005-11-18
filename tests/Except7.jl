class Except7 {
    void m() {
	if (true || false) {
	    throw new Ex1();
	}
        try {
	    throw new Ex1();
        } 
	catch (Ex1 e) { }
    }
}
class Ex1 extends Exception { }
