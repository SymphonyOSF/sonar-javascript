class A1 {
  constructor(x) {
    // no super()            // OK            
  }
}

class B1 extends A1 {
  constructor() {
    super();                 // OK
    super.x = 1;
  }
}

var B1b = class extends A1 {
  constructor() {
    super();                 // OK
    super.x = 1;
  }
}

class A2 {
  constructor() {
    super();                 // Noncompliant {{super() can only be invoked in a derived class constructor.}}
//  ^^^^^
    this.f = function() {
      super();               // Noncompliant {{super() can only be invoked in a derived class constructor.}}
    }
    this.g = (function() {
      super();               // Noncompliant {{super() can only be invoked in a derived class constructor.}}
    })(1)
  }
  bar() {
    super();                 // Noncompliant {{super() can only be invoked in a derived class constructor.}}
  }
}

class B2 extends A2 {
  constructor() {
    super();                 // OK
    this.f = function() {
      super();               // Noncompliant {{super() can only be invoked in a derived class constructor.}}
    }
  }
}

var B2b = class extends A2 {
  constructor() {
    super();                 // OK
    this.f = function() {
      super();               // Noncompliant {{super() can only be invoked in a derived class constructor.}}
    }
  }
}

class A3 extends Unknown {
  constructor() {
    super(x);                // OK
  }
  bar() {
    super(x);                // Noncompliant {{super() can only be invoked in a derived class constructor.}}
  }
}

class A4 extends Unknown {
  constructor() {
    super(x);                // OK
  }
}

class A5 extends null {
  constructor() {             
    super();                 // NOK, but no solution: super() -> TypeError, whereas no super() -> ReferenceError 
  }
}

class A6 extends null {
  constructor() {
    // no super()            // NOK, but no solution: super() -> TypeError, whereas no super() -> ReferenceError
  }
}

function foo() {
  super();                   // Noncompliant {{super() can only be invoked in a derived class constructor.}}
}

function constructor() {
  super();                   // Noncompliant {{super() can only be invoked in a derived class constructor.}}
}

//-------------------------------------------------------------------------------------------------

class A10 {
}

class B10 extends A10 {
  constructor() {            // Noncompliant {{super() must be invoked in any derived class constructor.}}
//^^^^^^^^^^^
    // no super()            
  }
}

class B11 extends Unknown {
  constructor() {            // Noncompliant {{super() must be invoked in any derived class constructor.}}
    // no super()
  }
}

//-------------------------------------------------------------------------------------------------

class A20 {
  constructor(x) {
    this.x = x;
    this.foo();
  }
  foo() {}
}

class B20a {
  constructor(x, y) {
    bar();
    super(x);                // OK
    this.y = y;
    super.x = x + y;
    this.foo();
    super.foo();
  }
}

class B20b {
  constructor(x, y) {
    this.y = y;
    super(x);                // Noncompliant {{super() must be invoked before "this|super" can be used.}}
//  ^^^^^
  }
}

class B20c {
  constructor(x, y) {
    super.foo();
    super(x);                // Noncompliant {{super() must be invoked before "this|super" can be used.}}
  }
}

//-------------------------------------------------------------------------------------------------

class A30 {
  constructor() {}
}

class B30 extends A30 {
  constructor(x) {
    super();                 // OK
    super(x);                // Noncompliant {{super() must be invoked with 0 arguments.}}
    super(x, y);             // Noncompliant {{super() must be invoked with 0 arguments.}}
  }
}

class A31 {
  constructor(x) {}
}

class B31 extends A31 {
  constructor() {
    super(x, y);             // Noncompliant {{AAAsuper() must be invoked with 1 argument.}}
  }
}

class A32 {
  constructor(x, y, z) {}
}

class B32 extends A32 {
  constructor() {
    super(x, y);             // Noncompliant {{AAAsuper() must be invoked with 3 arguments.}}
  }
}
