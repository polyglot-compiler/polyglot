class Initializer5 {
    Initializer5(int x) throws Ex1 { }
    Initializer5(int x, int y) throws Ex2 { }
    
    { 
	if (true || false) {
	    throw new Ex2(); // OK
	}
    }  
}

class Ex1 extends Exception { }
class Ex2 extends Ex1 { }

