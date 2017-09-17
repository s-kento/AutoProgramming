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
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Blob;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.junit.runner.Computer;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class Test {
	public static void main(String[] args) throws URISyntaxException, ClassNotFoundException, MalformedURLException {
		//test1();
		test6();

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
		File file = new File("C:\\pleiades\\workspace\\AutoProgramming\\commons-text-1.0\\target\\commons-text-1.0-tests.jar");
		URLClassLoader load;
		load = URLClassLoader.newInstance(new URL[] { file.toURI().toURL() });
		Class cl = load.loadClass("org.apache.commons.text.StrBuilderAppendInsertTest");
		JUnitCore junit = new JUnitCore();
		Result result = junit.run(Computer.serial(),cl);
		for (Failure failure : result.getFailures()) {
			System.out.println(failure.toString());
		}
		System.out.println(result.wasSuccessful());
	}

}
