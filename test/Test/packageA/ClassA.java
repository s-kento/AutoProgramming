package packageA;

import packageB.ClassB;

public class ClassA {
	public void methodA(String name){
		System.out.println("Hello, "+name);
		ClassB cb = new ClassB();
		cb.methodB();
	}
}
