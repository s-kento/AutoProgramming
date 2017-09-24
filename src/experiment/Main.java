package experiment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.ParseException;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import search.MethodInfo;
import search.Search;
import transformation.Controller;
import transformation.Transformation;

public class Main {
	public static void main(String[] args) throws Exception {
		// initialize();
		execute(args);

	}

	public static void execute(String[] args) throws Exception {
		/* 進化させるメソッドを取得 */
		Search search = new Search();
		List<MethodInfo> methods = search.execute(args);
		MethodInfo targetMethod = methods.get(0);
		String targetAbsClassName = targetMethod.getClassName();
		File dstsrcDir = new File("work\\commons-math\\src\\main\\java\\" + toDirectoryName(targetAbsClassName));
		File dstclassDir = new File("work\\commons-math\\target\\classes\\" + toDirectoryName(targetAbsClassName));
		String targetJavaFileName = new File(targetMethod.getFilePath()).getName();
		String targetClassName = FilenameUtils.removeExtension(targetJavaFileName);
		String projectJarFileName = "work\\commons-math\\target\\commons-math4-4.0-SNAPSHOT.jar";
		String dependencies = "work\\commons-numbers-core-1.0-SNAPSHOT.jar;work\\commons-numbers-gamma-1.0-SNAPSHOT.jar;"
				+ "work\\commons-numbers-angle-1.0-SNAPSHOT.jar;work\\commons-numbers-arrays-1.0-SNAPSHOT.jar;"
				+ "work\\commons-rng-client-api-1.0.jar;work\\commons-rng-simple-1.0.jar;work\\commons-rng-sampling-1.1-SNAPSHOT.jar;"
				+ "work\\jmh-core-1.13.jar;work\\jmh-generator-annprocess-1.13.jar;work\\junit-4.11.jar;"
				+ "work\\commons-numbers-fraction-1.0-SNAPSHOT.jar;work\\commons-rng-core-1.0.jar;work\\jopt-simple-4.6.jar;"
				+ "work\\commons-math3-3.2.jar;work\\hamcrest-core-1.3.jar;work\\commons-numbers-combinatorics-1.0-SNAPSHOT.jar";
		String[] targetArgs = { "-r", targetMethod.getReturnType(), "-p", targetMethod.getParameterType(), "-m",
				targetMethod.getMethodName(), "-P", "commons-math" };
		List<MethodInfo> evolvedMethods = search.execute(targetArgs);

		/* メソッドの書き換え，コンパイル */
		Transformation trans = new Transformation();
		Controller ctr = new Controller();
		for (MethodInfo evMethod : evolvedMethods) {
			if (targetMethod.equals(evMethod))
				continue;
			System.out.println("target: " + targetMethod.getClassName() + ", " + targetMethod.getMethodName());
			System.out.println("evoleved: " + evMethod.getClassName() + ", " + evMethod.getMethodName());
			String replacedCode = trans.replaceCode(targetMethod, evMethod);
			File targetJavaFile = new File("work\\" + targetJavaFileName);
			FileWriter filewriter = new FileWriter(targetJavaFile);
			filewriter.write(replacedCode);
			filewriter.close();
			int r;
			System.out.println(r = ctr.compile(projectJarFileName + ";" + dependencies, targetJavaFileName));
			if (r != 0)
				continue;
			else {// コンパイル成功
				deleteFile(dstsrcDir + "\\" + targetJavaFileName);
				FileUtils.moveFileToDirectory(targetJavaFile, dstsrcDir, false);
				File[] files = new File(".\\work").listFiles();
				for (File file : files) {
					if (file.getPath().endsWith(".class")) {
						deleteFile(dstclassDir + "\\" + file.getName());
						FileUtils.moveFileToDirectory(file, dstclassDir, false);
					}
				}
				if (testFailed(targetClassName, toPackageName(targetAbsClassName))) {
					String[] arguments = { "-location", "./work/commons-math", "-mode", "jgenprog", "-scope", "global",
							"-failing", targetAbsClassName + "Test", "-srcjavafolder", "/src/main/java/",
							"-srctestfolder", "/src/test/", "-binjavafolder", "/target/classes", "-bintestfolder",
							"/target/test-classes", "-flthreshold", "0.5", "-seed", "4", "-maxtime", "100",
							"-stopfirst", "true", "-dependencies", dependencies };
					trans.execute(arguments);
				}
			}
			break;
		}
	}

	/**
	 * 初期化処理 プロジェクトをオリジナルの状態に戻し，workディレクトリ内のjavaファイルとclassファイルを削除する
	 *
	 * @throws IOException
	 */
	public static void initialize() throws IOException {
		System.out.println("initializing working directory...");
		File dir = new File("work\\commons-math");
		if (dir.exists())
			FileUtils.deleteDirectory(dir);
		FileUtils.copyDirectory(new File("D:\\new_workspace\\commons-math"), new File("work\\commons-math"));
		File[] files = new File(".\\work").listFiles();
		for (File file : files) {
			if (file.getPath().endsWith(".java") || file.getPath().endsWith(".class"))
				file.delete();
		}
		System.out.println("done initializing");
	}

	/**
	 * 絶対クラス名から，パッケージのパスを得る
	 *
	 * @param absClassName
	 *            絶対クラス名
	 * @return directoryName パス
	 */
	public static String toDirectoryName(String absClassName) {
		int index = absClassName.lastIndexOf(".");
		String packageName = absClassName.substring(0, index);
		String regex = "\\.";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(packageName);
		String directoryName = m.replaceAll("\\\\");
		return directoryName;
	}

	public static String toPackageName(String absClassName) {
		int index = absClassName.lastIndexOf(".");
		String packageName = absClassName.substring(0, index);
		return packageName;
	}

	/**
	 * 指定したファイルを削除する
	 *
	 * @param filePath
	 */
	public static void deleteFile(String filePath) {
		File file = new File(filePath);
		if (file.exists())
			file.delete();
	}

	/**
	 * テストクラスが存在するかどうか
	 *
	 * @param className
	 * @param packagePath
	 * @return exists
	 */
	public static boolean existsTestFile(String className, String packagePath) {
		boolean exists = false;
		File file = new File("work\\commons-math\\src\\test\\java\\" + packagePath + "\\" + className + "Test.java");
		if (file.exists())
			exists = true;
		return exists;
	}

	/**
	 * テストケースが失敗したかどうか
	 *
	 * @param className
	 * @param packageName
	 * @return failed
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ClassNotFoundException
	 */
	public static boolean testFailed(String className, String packageName)
			throws IOException, InterruptedException, ClassNotFoundException {
		boolean failed = false;
		String[] classFileName = { "work\\commons-math\\target\\test-classes\\",
				"work\\commons-math\\target\\classes", "work\\commons-math3-3.2.jar",
				"work\\commons-numbers-angle-1.0-SNAPSHOT.jar", "work\\commons-numbers-arrays-1.0-SNAPSHOT.jar",
				"work\\commons-numbers-combinatorics-1.0-SNAPSHOT.jar", "work\\commons-numbers-core-1.0-SNAPSHOT.jar",
				"work\\commons-numbers-fraction-1.0-SNAPSHOT.jar", "work\\commons-numbers-gamma-1.0-SNAPSHOT.jar",
				"work\\commons-rng-client-api-1.0.jar", "work\\commons-rng-core-1.0.jar",
				"work\\commons-rng-sampling-1.1-SNAPSHOT.jar", "work\\commons-rng-simple-1.0.jar",
				"work\\hamcrest-core-1.3.jar", "work\\jmh-core-1.13.jar", "work\\jmh-generator-annprocess-1.13.jar",
				"work\\jopt-simple-4.6.jar", "work\\junit-4.11.jar" };
		File[] classFiles = Arrays.stream(classFileName).map(File::new).toArray((e) -> new File[e]);
		URL[] classFilesURL = new URL[classFiles.length];
		for (int i = 0; i < classFiles.length; i++) {
			classFilesURL[i]=classFiles[i].toURI().toURL();
		}
		URLClassLoader load;
		load = URLClassLoader.newInstance(classFilesURL);
		Class cl = load.loadClass(packageName + "." + className+"Test");
		JUnitCore junit = new JUnitCore();
		Result result = junit.runClasses(cl);
		// Result result = junit.run(Computer.serial(), cl);
		List<Failure> failures = result.getFailures();
		for(Failure failure:failures)
			System.out.println(failure.toString());
		if (failures.size() > 0)
			failed = true;
		return failed;
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

}
