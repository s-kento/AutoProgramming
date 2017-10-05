package experiment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.ParseException;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import search.MethodInfo;
import search.Search;
import transformation.Controller;
import transformation.Transformation;

public class TestCaseChecker {
	public static void main(String[] args) throws ClassNotFoundException, SQLException, ParseException,
			DecoderException, IOException, InterruptedException {
		Search search = new Search();
		List<MethodInfo> methods = search.execute(args);
		String targetAbsClassName;
		File dstsrcDir;
		File dstclassDir;
		String targetJavaFileName;
		String targetClassName;
		String projectJarFileName = "work\\commons-math\\target\\commons-math4-4.0-SNAPSHOT.jar";
		String dependencies = "work\\commons-numbers-core-1.0-SNAPSHOT.jar;work\\commons-numbers-gamma-1.0-SNAPSHOT.jar;"
				+ "work\\commons-numbers-angle-1.0-SNAPSHOT.jar;work\\commons-numbers-arrays-1.0-SNAPSHOT.jar;"
				+ "work\\commons-rng-client-api-1.0.jar;work\\commons-rng-simple-1.0.jar;work\\commons-rng-sampling-1.1-SNAPSHOT.jar;"
				+ "work\\jmh-core-1.13.jar;work\\jmh-generator-annprocess-1.13.jar;work\\junit-4.11.jar;"
				+ "work\\commons-numbers-fraction-1.0-SNAPSHOT.jar;work\\commons-rng-core-1.0.jar;work\\jopt-simple-4.6.jar;"
				+ "work\\commons-math3-3.2.jar;work\\hamcrest-core-1.3.jar;work\\commons-numbers-combinatorics-1.0-SNAPSHOT.jar";
		Main.initialize();
		File file = new File("TestCaseInfo.txt");
		PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
		pw.println("FilePath, MethodName, startLine, testClassName, sameSignatureNumber, hasTestCase");
		int count = 0;
		for (MethodInfo targetMethod : methods) {
			count++;
			System.out.println(methods.size() + "個のメソッド中" + count + "目を実行");
			targetAbsClassName = targetMethod.getClassName();
			dstsrcDir = new File("work\\commons-math\\src\\main\\java\\" + Main.toDirectoryName(targetAbsClassName));
			dstclassDir = new File("work\\commons-math\\target\\classes\\" + Main.toDirectoryName(targetAbsClassName));
			targetJavaFileName = new File(targetMethod.getFilePath()).getName();
			targetClassName = FilenameUtils.removeExtension(targetJavaFileName);
			TestCaseInfo testcase = new TestCaseInfo(targetMethod);
			String[] argument = { "-r", targetMethod.getReturnType(), "-p", targetMethod.getParameterType(), "-m",
					targetMethod.getMethodName(), "-P", "commons-math" };
			List<MethodInfo> evolvedMethods = search.execute(argument);
			testcase.setSigNumber(evolvedMethods.size() - 1);
			if (!Main.existsTestFile(getClassName(targetMethod), Main.toDirectoryName(targetMethod.getClassName()))
					|| testcase.getSigNumber() == 0) {
				writeTestCaseInfo(testcase, pw);
				continue;
			} else {// テストケースが存在，かつ同じシグネチャのメソッドが1つ以上存在すれば
				Transformation trans = new Transformation();
				Controller ctr = new Controller();
				for (MethodInfo evMethod : evolvedMethods) {
					if (targetMethod.equals(evMethod))
						continue;
					String replacedCode = trans.replaceCode(targetMethod, evMethod);
					File targetJavaFile = new File("work\\" + targetJavaFileName);
					FileWriter filewriter = new FileWriter(targetJavaFile);
					filewriter.write(replacedCode);
					filewriter.close();
					int r = ctr.compile(projectJarFileName + ";" + dependencies, targetJavaFileName);
					if (r != 0) {
						Main.deleteFile(targetJavaFile.getAbsolutePath());
						continue;
					} else {// コンパイル成功
						File[] files = new File(".\\work").listFiles();
						List<String> targetClassFileNames = new ArrayList<>();
						for (File f : files) {
							if (f.getPath().endsWith(".class")) {
								targetClassFileNames.add(f.getName());
							}
						}
						taihi(dstsrcDir, dstclassDir, targetJavaFileName, targetClassFileNames);
						FileUtils.moveFileToDirectory(targetJavaFile, dstsrcDir, false);
						for (String targetClassFileName : targetClassFileNames) {
							FileUtils.moveFileToDirectory(new File("work\\" + targetClassFileName), dstclassDir, false);
						}

						TestCaseRunnerThread th = new TestCaseRunnerThread(targetClassName,
								Main.toPackageName(targetAbsClassName), testcase);
						th.start();
						th.join(60000);
						if (th.isAlive()) {
							th.stop();
						}

						untaihi(dstsrcDir, dstclassDir, targetJavaFileName, targetClassFileNames);
					}
					break;
				}
				writeTestCaseInfo(testcase, pw);
			}
		}
	}

	/**
	 * MethodInfoからクラス名を取得(パッケージ名なし)
	 *
	 * @param method
	 *            MethodInfo
	 * @return className クラス名
	 */
	public static String getClassName(MethodInfo method) {
		String javaFileName = new File(method.getFilePath()).getName();
		String className = FilenameUtils.removeExtension(javaFileName);
		return className;
	}

	/**
	 * MethodInfoからパッケージ名を取得
	 *
	 * @param method
	 *            MethodInfo
	 * @return packageName パッケージ名
	 */
	public static String getPackageName(MethodInfo method) {
		String absClassName = method.getClassName();
		String packageName = Main.toPackageName(absClassName);
		return packageName;
	}

	/**
	 * MethodInfoからファイル名を取得
	 *
	 * @param method
	 *            MethodInfo
	 * @return fileName
	 */
	public static String getFileName(MethodInfo method) {
		String fileName = new File(method.getFilePath()).getName();
		return fileName;
	}

	public static void writeTestCaseInfo(List<TestCaseInfo> testcases) throws IOException {
		File file = new File("TestCaseInfo.txt");
		PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
		pw.println("FilePath, MethodName, startLine, testClassName, sameSignatureNumber, hasTestCase");
		for (TestCaseInfo testcase : testcases) {
			MethodInfo method = testcase.getMethod();
			pw.println(method.getFilePath() + ", " + method.getMethodName() + ", " + method.getStartLine() + ", "
					+ getClassName(method) + "Test, " + testcase.getSigNumber() + ", " + testcase.existsTestCase());
		}
	}

	public static void writeTestCaseInfo(TestCaseInfo testcase, PrintWriter pw) throws IOException {
		MethodInfo method = testcase.getMethod();
		pw.println(method.getFilePath() + ", " + method.getMethodName() + ", " + method.getStartLine() + ", "
				+ getClassName(method) + "Test, " + testcase.getSigNumber() + ", " + testcase.existsTestCase());
		System.out.println(testcase.existsTestCase());
	}

	public static void taihi(File dstsrcDir, File dstclassDir, String targetJavaFileName,
			List<String> targetClassFileNames) {
		File original = new File(dstsrcDir.getAbsolutePath() + "\\" + targetJavaFileName);
		File taihi = new File(dstsrcDir.getAbsolutePath() + "\\" + targetJavaFileName + ".taihi");
		original.renameTo(taihi);
		for (String targetClassFileName : targetClassFileNames) {
			original = new File(dstclassDir.getAbsolutePath() + "\\" + targetClassFileName);
			taihi = new File(dstclassDir.getAbsolutePath() + "\\" + targetClassFileName + ".taihi");
			original.renameTo(taihi);
		}
	}

	public static void untaihi(File dstsrcDir, File dstclassDir, String targetJavaFileName,
			List<String> targetClassFileNames) {
		File original = new File(dstsrcDir.getAbsolutePath() + "\\" + targetJavaFileName);
		File taihi = new File(dstsrcDir.getAbsolutePath() + "\\" + targetJavaFileName + ".taihi");
		Main.deleteFile(original.getAbsolutePath());
		taihi.renameTo(original);

		for (String targetClassFileName : targetClassFileNames) {
			original = new File(dstclassDir.getAbsolutePath() + "\\" + targetClassFileName);
			taihi = new File(dstclassDir.getAbsolutePath() + "\\" + targetClassFileName + ".taihi");
			Main.deleteFile(original.getAbsolutePath());
			taihi.renameTo(original);
		}

	}
}
