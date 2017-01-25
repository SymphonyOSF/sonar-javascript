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

