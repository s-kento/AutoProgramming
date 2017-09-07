package transformation;

import java.util.List;

import search.MethodInfo;
import search.Search;

public class Transformation {
	public static void main(String[] args)
			throws Exception {
		Transformation tr = new Transformation();
		tr.execute(args);
	}

	public void execute(String[] args)
			throws Exception {
		Search search = new Search();
		List<MethodInfo> methods = search.execute(args);
		/*
		 * ここらへんで，GenProgに渡す適当な引数を生成する(テストケース生成も含めて)：未実装
		 */
		GenProg gen = new GenProg();
		String[] arguments = { "-location", "./commons-text-1.0", "-mode", "jgenprog", "-scope", "global", "-failing",
				"org.apache.commons.text.StrBuilderTest", "-srcjavafolder", "/src/main/java/", "-srctestfolder",
				"/src/test/", "-binjavafolder", "/target/classes", "-bintestfolder", "/target/test-classes",
				"-flthreshold", "0.5", "-seed", "4", "-maxtime", "100", "-stopfirst", "true", "-dependencies",
				"./commons-text-1.0/lib/hamcrest-all-1.3.jar:./commons-text-1.0/lib/hamcrest-core-1.3.jar;./commons-text-1.0/lib/junit-4.12.jar;./commons-text-1.0/lib/commons-lang3.jar" };
		gen.execute(arguments);
	}

	/*
	 * public void execute(String[] args) { Controller cr = new Controller();
	 *
	 * long start = System.currentTimeMillis();
	 * cr.extractJavaFile("MachineLearning.jar", "IdentifybyName.java"); long
	 * end = System.currentTimeMillis(); System.out.println((end - start) +
	 * "ms");
	 *
	 * start = System.currentTimeMillis(); cr.compile("MachineLearning.jar",
	 * "IdentifybyName.java"); end = System.currentTimeMillis();
	 * System.out.println((end - start) + "ms");
	 *
	 * start = System.currentTimeMillis();
	 * System.out.println(cr.run("commons-io-2.5.jar", "ByteOrderMark.java",
	 * "org.apache.commons.io", "ByteOrderMark", "length", "null", null)); end =
	 * System.currentTimeMillis(); System.out.println((end - start) + "ms"); }
	 */
}
