class Inner14 {
  void m() {
    Inner14 o1 = new Inner14() {
      void m() {
	Inner14 aoeu1 = Inner14.this;
	Inner14 o2 = new Inner14() {
	  Inner14 aoeu2 = Inner14.this;
	};
      }
    };
  }
}

