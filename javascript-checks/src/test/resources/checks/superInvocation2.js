class A1 {
  constructor(x) {
    // no super()            // OK            
  }
}

class B1a extends A1 {
  constructor() {
    super();                 // OK
    super.a = 1;
  }
}

var B1b = class extends A1 {
  constructor() {
    super();                 // OK
    super.a = 1;
  }
} 
