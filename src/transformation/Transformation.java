package transformation;

public class Transformation {
	public void execute(String[] args) {
		Controller cr = new Controller();

		long start = System.currentTimeMillis();
		cr.extractJavaFile("MachineLearning.jar", "IdentifybyName.java");
		long end = System.currentTimeMillis();
		System.out.println((end - start) + "ms");

		start = System.currentTimeMillis();
		cr.compile("MachineLearning.jar", "IdentifybyName.java");
		end = System.currentTimeMillis();
		System.out.println((end - start) + "ms");

		start = System.currentTimeMillis();
		System.out.println(cr.run("commons-io-2.5.jar", "ByteOrderMark.java", "org.apache.commons.io", "ByteOrderMark",
				"length", "null", null));
		end = System.currentTimeMillis();
		System.out.println((end - start) + "ms");
	}
}
