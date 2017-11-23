package experiment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import search.MethodInfo;
import search.SQLite;
import search.Search;
import transformation.Controller;
import transformation.Transformation;

public class MethodGenerator {


	public static void main(String[] args) throws Exception {
		MethodGenerator mg = new MethodGenerator();
		mg.execute(args);
	}

	public void execute(String[] args) throws Exception {
		Properties properties = new Properties();
		Logger logger = null;
		String suffixOfTestClass=null;
		loadProperty(properties,"experiment.properties");
		initialize(properties);
		setLogger(logger,"ExperimentLog");

		/* 進化させるメソッドを取得 */
		final int startId = Integer.parseInt(properties.getProperty("startId"));
		Search search = new Search();
		List<MethodInfo> methods = search.execute(args);// idの昇順で並んでいる
		splitList(methods, startId);
		for (MethodInfo targetMethod : methods) {
			if (!isCoverage100(targetMethod) || !isBranchCoverage100(targetMethod))
				continue;
			logger.info("メソッドid " + targetMethod.getId() + "の進化開始");
			String targetAbsClassName = targetMethod.getClassName();
			File dstsrcDir = new File(properties.getProperty("targetsrcDir") + toDirectoryName(targetAbsClassName));
			File dstclassDir = new File(properties.getProperty("targetclassDir") + toDirectoryName(targetAbsClassName));
			String targetJavaFileName = new File(targetMethod.getFilePath()).getName();
			String targetClassName = FilenameUtils.removeExtension(targetJavaFileName);
			String projectJarFileName = properties.getProperty("projectJarFileName");
			String dependencies = properties.getProperty("dependencies");
			String[] targetArgs = { "-r", targetMethod.getReturnType(), "-p", targetMethod.getParameterType(), "-m",
					targetMethod.getMethodName(), "-P", properties.getProperty("targetProject") };
			List<MethodInfo> evolvedMethods = search.execute(targetArgs);
			logger.info("進化対象のメソッド数：" + evolvedMethods.size());

			/* メソッドの書き換え，コンパイル */
			Transformation trans = new Transformation();
			Controller ctr = new Controller();
			for (MethodInfo evMethod : evolvedMethods) {
				if (targetMethod.equals(evMethod) || evMethod.getStatementNumber() <= 1
						|| !isSameClass(targetMethod, evMethod))
					continue;
				/*
				 * if (targetMethod.equals(evMethod) || !isCoverage100(evMethod)
				 * || !isBranchCoverage100(evMethod)) continue;
				 */
				TestCaseInfo testcase = new TestCaseInfo(targetMethod);
				System.out.println("target: " + targetMethod.getClassName() + ", " + targetMethod.getMethodName());
				System.out.println("evoleved: " + evMethod.getClassName() + ", " + evMethod.getMethodName());
				String replacedCode = trans.replaceCode(targetMethod, evMethod);
				File targetJavaFile = new File(properties.getProperty("workingDir") + targetJavaFileName);
				FileWriter filewriter = new FileWriter(targetJavaFile);
				filewriter.write(replacedCode);
				filewriter.close();
				int r;
				System.out.println(r = ctr.compile(projectJarFileName + ";" + dependencies, targetJavaFileName));
				if (r != 0)
					continue;
				else {// コンパイル成功
					logger.info("メソッドid " + evMethod.getId() + "コンパイル成功");
					File[] files = new File(properties.getProperty("workingDir")).listFiles();
					List<String> targetClassFileNames = new ArrayList<>();
					for (File file : files) {
						if (file.getPath().endsWith(".class")) {
							targetClassFileNames.add(file.getName());
						}
					}
					taihi(dstsrcDir, dstclassDir, targetJavaFileName, targetClassFileNames);
					FileUtils.moveFileToDirectory(targetJavaFile, dstsrcDir, false);
					for (String targetClassFileName : targetClassFileNames) {
						FileUtils.moveFileToDirectory(
								new File(properties.getProperty("workingDir") + targetClassFileName), dstclassDir,
								false);
					}
					Thread th = new Thread(new TestCaseRunnerThread(targetClassName,
							toPackageName(targetAbsClassName), testcase,properties,suffixOfTestClass));
					th.start();
					th.join(60000);
					if (th.isAlive()) {
						th.stop();
					}
					if (testcase.existsTestCase()) {
						logger.info("テストケース成功．GenProg起動");
						String[] arguments = { "-location", properties.getProperty("location"), "-mode", "jgenprog",
								"-scope", properties.getProperty("scope"), "-failing",
								targetAbsClassName + suffixOfTestClass,
								"-srcjavafolder",
								"/src/main/java/", "-srctestfolder", "/src/test/", "-binjavafolder", "/target/classes",
								"-bintestfolder", "/target/test-classes", "-flthreshold", "0.5", "-seed",
								properties.getProperty("seed"), "-maxtime", properties.getProperty("maxtime"),
								"-stopfirst", "true", "-dependencies", dependencies, "-out",
								"./outputMutation_" + targetMethod.getId() };
						trans.execute(arguments, targetMethod);
						logger.info("GenProg終了");
					}
					untaihi(dstsrcDir, dstclassDir, targetJavaFileName, targetClassFileNames);
				}
				if (isSuccess(targetMethod, properties)) {
					logger.info("メソッドid " + targetMethod.getId() + "の自動生成が成功");
					break;
				}
			}
		}
	}

	/**
	 * 初期化処理 プロジェクトをオリジナルの状態に戻し，workディレクトリ内のjavaファイルとclassファイルを削除する
	 *
	 * @throws IOException
	 */
	public void initialize(Properties properties) throws IOException {
		System.out.println("initializing working directory...");
		File dir = new File(properties.getProperty("targetDir"));
		if (dir.exists())
			FileUtils.deleteDirectory(dir);
		FileUtils.copyDirectory(new File(properties.getProperty("originalDir")),
				new File(properties.getProperty("targetDir")));
		File[] files = new File(properties.getProperty("workingDir")).listFiles();
		for (File file : files) {
			if (file.getPath().endsWith(".java") || file.getPath().endsWith(".class"))
				file.delete();
		}
		System.out.println("done initializing");
	}

	/**
	 * 絶対クラス名から，パッケージのパスを得る org.apche.commons.text.StrBuilder ->
	 * org\apache\commons\text
	 *
	 * @param absClassName
	 *            絶対クラス名
	 * @return directoryName パス
	 */
	public String toDirectoryName(String absClassName) {
		int index = absClassName.lastIndexOf(".");
		String packageName = absClassName.substring(0, index);
		String regex = "\\.";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(packageName);
		String directoryName = m.replaceAll("\\\\");
		return directoryName;
	}

	/**
	 * 絶対クラス名からパッケージ名を得る org.apche.commons.text.StrBuilder ->
	 * org.apche.commons.text
	 *
	 * @param absClassName
	 * @return packageName
	 */
	public String toPackageName(String absClassName) {
		int index = absClassName.lastIndexOf(".");
		String packageName = absClassName.substring(0, index);
		return packageName;
	}

	/**
	 * 指定したファイルを削除する
	 *
	 * @param filePath
	 */
	public void deleteFile(String filePath) {
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
	public boolean existsTestFile(String className, String packagePath, Properties properties) {
		boolean exists = false;
		File file = new File(properties.getProperty("targettestDir") + packagePath + "\\" + className + "Test.java");
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
	public boolean testFailed(String className, String packageName,Properties properties, String suffixOfTestClass)
			throws IOException, InterruptedException, ClassNotFoundException {
		boolean failed = false;
		String[] dependencies = properties.getProperty("dependencies").split(";", -1);
		List<String> classFileNameList = new ArrayList<>();
		classFileNameList.addAll(Arrays.asList(dependencies));
		classFileNameList.add(properties.getProperty("targetclassDir"));
		classFileNameList.add(properties.getProperty("targettestclassDir"));
		String[] classFileName = (String[]) classFileNameList.toArray(new String[0]);
		File[] classFiles = Arrays.stream(classFileName).map(File::new).toArray((e) -> new File[e]);
		URL[] classFilesURL = new URL[classFiles.length];
		for (int i = 0; i < classFiles.length; i++) {
			classFilesURL[i] = classFiles[i].toURI().toURL();
		}
		URLClassLoader load;
		load = URLClassLoader.newInstance(classFilesURL);
		Class cl;
		if (existsTestFile(className, toDirectoryName(packageName + "." + className),properties)) {
			suffixOfTestClass = "Test";
			cl = load.loadClass(packageName + "." + className + suffixOfTestClass);
		} else {
			suffixOfTestClass = "TestCase";
			cl = load.loadClass(packageName + "." + className + suffixOfTestClass);//commons-ioの仕様
		}
		JUnitCore junit = new JUnitCore();
		Result result = junit.runClasses(cl);
		// Result result = junit.run(Computer.serial(), cl);
		List<Failure> failures = result.getFailures();
		for (Failure failure : failures)
			System.out.println(failure.toString());
		if (failures.size() > 0)
			failed = true;
		return failed;
	}

	public void printInputStream(InputStream is) throws IOException {
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

	/**
	 * メソッドの自動生成が成功したかどうか
	 *
	 * @param targetMethod
	 * @return success
	 */
	public boolean isSuccess(MethodInfo targetMethod, Properties properties) {
		boolean success = false;
		File[] output = new File("outputMutation_" + targetMethod.getId() + "\\AstorMain-"
				+ properties.getProperty("targetProject") + "\\src").listFiles();
		if (null != output) {
			if (output.length > 1)
				success = true;
		}
		return success;
	}

	/**
	 * メソッドの命令カバレッジが100%かどうか
	 *
	 * @param targetMethod
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public boolean isCoverage100(MethodInfo targetMethod) throws ClassNotFoundException, SQLException {
		SQLite sqLite = new SQLite(null, "coverages");
		final double coverage;
		coverage = sqLite.getCoverage(String.valueOf(targetMethod.getId()));
		if (coverage == 1)
			return true;
		else
			return false;
	}

	/**
	 * メソッドのブランチカバレッジが100%かどうか
	 *
	 * @param targetMethod
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public boolean isBranchCoverage100(MethodInfo targetMethod) throws ClassNotFoundException, SQLException {
		SQLite sqLite = new SQLite(null, "coverages");
		final double branchCoverage;
		branchCoverage = sqLite.getBranchCoverage(String.valueOf(targetMethod.getId()));
		if (branchCoverage == 1)
			return true;
		else
			return false;
	}

	/**
	 * startIdよりも若いIDのメソッドは切り捨てる methodがIDで昇順になっていることが条件
	 *
	 * @param methods
	 * @param startId
	 */
	public void splitList(List<MethodInfo> methods, int startId) {
		Iterator<MethodInfo> it = methods.iterator();
		while (it.hasNext()) {
			MethodInfo method = it.next();
			if (method.getId() == startId)
				break;
			else
				it.remove();
		}
	}

	/**
	 * プロパティファイルを読み込む
	 *
	 * @param propertyFileName
	 * @throws IOException
	 */
	public void loadProperty(Properties properties, String propertyFileName) throws IOException {
		final InputStream pinput = new FileInputStream(new File(propertyFileName));
		properties.load(pinput);
		pinput.close();
	}

	/**
	 * ログファイルの作成
	 *
	 * @param logFileName
	 * @throws SecurityException
	 * @throws IOException
	 */
	public Logger setLogger(Logger logger,String logFileName) throws SecurityException, IOException {
		logger = Logger.getLogger(logFileName);
		FileHandler fh = new FileHandler(logFileName + ".log", true);
		fh.setFormatter(new java.util.logging.SimpleFormatter());
		logger.addHandler(fh);
		return logger;
	}

	/**
	 * 二つのメソッドが同一クラスかどうか判定する
	 *
	 * @param method1
	 * @param method2
	 * @return
	 */
	public boolean isSameClass(MethodInfo method1, MethodInfo method2) {
		if (method1.getProjectName().equals(method2.getProjectName())
				&& method1.getClassName().equals(method2.getClassName()))
			return true;
		else
			return false;
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
		MethodGenerator mg = new MethodGenerator();
		String packageName = mg.toPackageName(absClassName);
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

	public void taihi(File dstsrcDir, File dstclassDir, String targetJavaFileName,
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

	public void untaihi(File dstsrcDir, File dstclassDir, String targetJavaFileName,
			List<String> targetClassFileNames) {
		File original = new File(dstsrcDir.getAbsolutePath() + "\\" + targetJavaFileName);
		File taihi = new File(dstsrcDir.getAbsolutePath() + "\\" + targetJavaFileName + ".taihi");
		deleteFile(original.getAbsolutePath());
		taihi.renameTo(original);

		for (String targetClassFileName : targetClassFileNames) {
			original = new File(dstclassDir.getAbsolutePath() + "\\" + targetClassFileName);
			taihi = new File(dstclassDir.getAbsolutePath() + "\\" + targetClassFileName + ".taihi");
			deleteFile(original.getAbsolutePath());
			taihi.renameTo(original);
		}

	}
}
