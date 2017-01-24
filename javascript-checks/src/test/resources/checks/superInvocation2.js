class A1 {
  constructor(x) {
    // no super()            // OK            
  }
}

class B1 extends A1 {
  constructor() {
    super();                 // OK
    super.a = 1;
  }
}
