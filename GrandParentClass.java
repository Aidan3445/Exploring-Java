package src;

public class GrandParentClass {
  public static void main(String[] args) {
    A c = new C("NAME ");
    System.out.println(c.method2());
  }
}

abstract class A {
  String name;

  A(String name) {
    this.name = name + this.method();
  }
  String method() {
    return "A";
  }

  int method2() {
    return 1;
  }
}

class B extends A {
  B(String name) {
    super(name);
    this.name += this.method();
  }
  @Override
  String method() {
    return super.method() + "B";
  }

  @Override
  int method2() {
    return 2;
  }
}

class C extends B {
  C(String name) {
    super(name);
    this.name += this.method();
  }
  @Override
  String method() {
    return super.method() + "C";
  }
}
