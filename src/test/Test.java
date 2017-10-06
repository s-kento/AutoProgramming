package test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.apache.commons.cli.ParseException;
import org.apache.commons.codec.DecoderException;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import search.MethodInfo;
import search.Search;
import transformation.Transformation;

public class Test {
	public static void main(String[] args) throws URISyntaxException, ClassNotFoundException, IOException, SQLException,
			ParseException, DecoderException, InterruptedException {
		test10(args);

	}

	/*
	 * jarにまとめたプロジェクトからメソッドを実行する リフレクションの利用
	 */
	public static void test1() {
		File file = new File("C:\\pleiades\\workspace\\AutoProgramming\\test\\Test.jar");
		URLClassLoader load;
		try {
			load = URLClassLoader.newInstance(new URL[] { file.toURI().toURL() });
			Class cl;
			cl = load.loadClass("packageA.ClassA"); // クラス名がこのクラスと同名だとまずい
			String name = "Kento";
			Method method;
			method = cl.getMethod("methodA", new Class[] { String.class });
			method.invoke(cl.newInstance(), name);
		} catch (MalformedURLException | ClassNotFoundException | SecurityException | NoSuchMethodException
				| IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| InstantiationException e) {
			e.printStackTrace();
		}
	}

	/*
	 * 書き換えたソースファイルをコンパイルする JavaCompilerの利用
	 */
	public static void test2() {
		File src = new File("test\\ByteOrderMark.java");
		String[] args = { "-classpath", "C:\\pleiades\\workspace\\AutoProgramming\\test\\commons-io.2.5.jar",
				src.getAbsolutePath() };
		JavaCompiler c = ToolProvider.getSystemJavaCompiler();
		int r = c.run(null, null, null, args);
		if (r != 0)
			throw new RuntimeException("コンパイル失敗:" + r);
	}

	/*
	 * 生成したクラスファイルが動くかどうかテストする
	 *
	 */
	public static void test3() {
		File file1 = new File("C:\\pleiades\\workspace\\AutoProgramming\\test\\");
		File file2 = new File("C:\\pleiades\\workspace\\AutoProgramming\\test\\Test.jar");
		URLClassLoader load;
		try {
			load = URLClassLoader.newInstance(new URL[] { file1.toURI().toURL(), file2.toURI().toURL() });
			Class cl;
			cl = load.loadClass("packageA.ClassA");
			String name = "Kento";
			Method method = cl.getMethod("methodA", new Class[] { String.class });
			method.invoke(cl.newInstance(), name);
		} catch (NoSuchMethodException | SecurityException | ClassNotFoundException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException | InstantiationException
				| MalformedURLException e) {
			e.printStackTrace();
		}
	}

	/*
	 * jarファイルから，指定したjavaファイルを読み込む
	 */
	public static void test4() {
		File file = new File("test\\commons-io-2.5.jar");
		try (JarFile jarFile = new JarFile(file)) {
			printFile(jarFile, "ByteOrderMark.java");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * BLOBクラスのてすと
	 */
	public static void test5() {
		String str = "hogehoge";
		try {
			byte[] bytes = str.getBytes("UTF-8");
			InputStream in = new ByteArrayInputStream(bytes);
			int size = in.available();
			char[] theChars = new char[size];
			byte[] b = new byte[size];

			in.read(b, 0, size);
			for (int i = 0; i < size;)
				theChars[i] = (char) (b[i++] & 0xff);

			System.out.println(new String(theChars));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void printFile(ZipFile zipFile, String name) throws IOException {
		File file = new File("test\\" + name);
		PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));

		ZipEntry ze = null;
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while (entries.hasMoreElements()) {
			ze = entries.nextElement();
			if (new File(ze.getName()).getName().equals(name)) {
				break;
			}
		}
		// テキストファイルとして読み込む（JDK1.7 [2014-04-16]）
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(ze)))) {
			for (;;) {
				String text = reader.readLine();
				if (text == null) {
					break;
				}
				System.out.println(text);
				pw.println(text);
			}
		}
		pw.close();
	}

	public static void test6() throws ClassNotFoundException, MalformedURLException {
		File testJarFile = new File(
				"C:\\pleiades\\workspace\\AutoProgramming\\work\\commons-math\\target\\test-classes\\");
		File srcJarFile = new File(
				"C:\\pleiades\\workspace\\AutoProgramming\\work\\commons-math\\target\\commons-math4-4.0-SNAPSHOT.jar");
		String[] dependences = { "work\\commons-math3-3.2.jar", "work\\commons-numbers-angle-1.0-SNAPSHOT.jar",
				"work\\commons-numbers-arrays-1.0-SNAPSHOT.jar", "work\\commons-numbers-combinatorics-1.0-SNAPSHOT.jar",
				"work\\commons-numbers-core-1.0-SNAPSHOT.jar", "work\\commons-numbers-fraction-1.0-SNAPSHOT.jar",
				"work\\commons-numbers-gamma-1.0-SNAPSHOT.jar", "work\\commons-rng-client-api-1.0.jar",
				"work\\commons-rng-core-1.0.jar", "work\\commons-rng-sampling-1.1-SNAPSHOT.jar",
				"work\\commons-rng-simple-1.0.jar", "work\\hamcrest-core-1.3.jar", "work\\jmh-core-1.13.jar",
				"work\\jmh-generator-annprocess-1.13.jar", "work\\jopt-simple-4.6.jar", "work\\junit-4.11.jar" };
		File[] files = Arrays.stream(dependences).map(File::new).toArray((e) -> new File[e]);
		URL[] deps = new URL[files.length + 2];
		deps[0] = testJarFile.toURI().toURL();
		deps[1] = srcJarFile.toURI().toURL();
		URLClassLoader load;
		for (int i = 2; i < files.length + 2; i++) {
			deps[i] = files[i - 2].toURI().toURL();
		}
		load = URLClassLoader.newInstance(deps);
		Class cl = load.loadClass("org.apache.commons.math4.analysis.differentiation.DerivativeStructureTest");
		JUnitCore junit = new JUnitCore();
		// junit.main(cl.getName());
		Result result = junit.runClasses(cl);
		// Result result = junit.run(Computer.serial(), cl);
		for (Failure failure : result.getFailures()) {
			System.out.println(failure.toString());
		}
		System.out.println(result.wasSuccessful());
	}

	/*
	 * 外部プロセスで，maven testを実行
	 */
	public static void test7() throws IOException {

		ProcessBuilder pb = new ProcessBuilder(
				"C:\\pleiades\\workspace\\AutoProgramming\\apache-maven-3.5.0\\bin\\mvn.cmd", "test",
				"-Dtest=DerivativeStructureTest");
		// ProcessBuilder pb = new ProcessBuilder("maven","test");
		pb.redirectErrorStream(true);
		pb.directory(new File("work\\commons-math"));
		Process process = pb.start();
		InputStream is = process.getInputStream(); // 標準出力
		printInputStream(is);
	}

	public static void printInputStream(InputStream is) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(is, "ms932"));
		try {
			for (;;) {
				String line = br.readLine();
				if (line == null)
					break;
				System.out.println(line);
			}
		} finally {
			br.close();
		}
	}

	public static void test8()
			throws ClassNotFoundException, SQLException, ParseException, DecoderException, IOException {
		Search search = new Search();
		String[] args = { "-r", "java.lang.String", "-p", "int", "-m", "a" };
		List<MethodInfo> methods = search.execute(args);
		Transformation trans = new Transformation();
		System.out.println(methods.get(0).getSourceCode());
		System.out.println(methods.get(2).getSourceCode());
		System.out.println(trans.replaceCode(methods.get(0), methods.get(2)));

	}

	public static void test9() throws InterruptedException {
		TestThread t = new TestThread();
		t.start();
		t.join(5000);
		t.stop();
		System.out.println("スレッド破棄");
	}

	public static void test10(String args[])
			throws ClassNotFoundException, SQLException, ParseException, DecoderException, IOException {
		Search search = new Search();
		List<MethodInfo> methods = search.execute(args);
		MethodInfo targetMethod = methods.get(0);
		String[] argument = { "-r", targetMethod.getReturnType(), "-p", targetMethod.getParameterType(), "-m",
				targetMethod.getMethodName(), "-P", "commons-math" };
		List<MethodInfo> evolvedMethods = search.execute(argument);
		for (MethodInfo targetEvolvedMethod : evolvedMethods) {
			if (targetMethod.getClassName().equals(targetEvolvedMethod.getClassName())
					|| targetMethod.getMethodName().equals(targetEvolvedMethod.getMethodName())) {
				continue;
			}
			Transformation trans = new Transformation();
			System.out.println(
					"target: " + targetMethod.getClassName()+"."+targetMethod.getMethodName() + ", evolved: "+targetEvolvedMethod.getClassName()+"." + targetEvolvedMethod.getMethodName());
			System.out.println(trans.replaceCode3(targetMethod, targetEvolvedMethod));
			break;
		}
	}
}
