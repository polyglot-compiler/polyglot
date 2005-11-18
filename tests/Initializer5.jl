class Initializer5 {
    Initializer5(int x) throws Ex1, Ex2, Ex3 { }
    Initializer5(int x, int y) throws Ex2, Ex3, Ex4 { }
    
    { 
	if (true || false) {
	    throw new Ex2(); // OK
	}
	if (true || false) {
	    throw new Ex3(); // OK
	}
    }  
}

class Ex1 extends Exception { }
class Ex2 extends Exception { }
class Ex3 extends Exception { }
class Ex4 extends Exception { }
