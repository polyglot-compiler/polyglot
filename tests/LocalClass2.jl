class LocalClass2 {
    void m(int i) {
        switch (i) {
            case 0:
                class Local {}
                break;
            case 1:
		// javac incorrectly rejects this: Local is in scope here.
                new Local();
        }
    }
}
