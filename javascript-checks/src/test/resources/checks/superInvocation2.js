class B20b extends A20 {
  constructor(x, y) {
    this.x = x + y;
    super.x = x + y;
    super(x);                // Noncompliant [[secondary=-1]] {{super() must be invoked before "this" or "super" can be used.}}
//  ^^^^^
  }
}
