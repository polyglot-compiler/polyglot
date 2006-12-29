// This tests whether the implement-as-less-public error is caught in
// interface overrides.

interface Override0 {
  void method();
}

class Sub implements Override0 {
  void method() {} 
}
