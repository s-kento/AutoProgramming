package experiment;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import search.MethodInfo;
import search.Search;
import transformation.Controller;
import transformation.Nopol;
import transformation.Transformation;

public class MethodGeneratorWithNopol {


	public static void main(String[] args) throws Exception {
		Properties properties = new Properties();
		Logger logger=null;
		MethodGenerator mg = new MethodGenerator();
		mg.loadProperty(properties,"experiment_nopol.properties");
		mg.initialize(properties);
		logger=mg.setLogger(logger,"ExperimentLog_nopol");

		final int startId = Integer.parseInt(properties.getProperty("startId"));
		Search search = new Search();
		List<MethodInfo> methods = search.execute(args);// idの昇順で並んでいる
		mg.splitList(methods, startId);
		for (MethodInfo targetMethod : methods) {
			if (!mg.isCoverage100(targetMethod) || !mg.isBranchCoverage100(targetMethod))
				continue;
			logger.info("メソッドid " + targetMethod.getId() + "の進化開始");
			String targetAbsClassName = targetMethod.getClassName();
			File dstsrcDir = new File(properties.getProperty("targetsrcDir") + mg.toDirectoryName(targetAbsClassName));
			File dstclassDir = new File(
					properties.getProperty("targetclassDir") + mg.toDirectoryName(targetAbsClassName));
			String targetJavaFileName = new File(targetMethod.getFilePath()).getName();
			String targetClassName = FilenameUtils.removeExtension(targetJavaFileName);
			String projectJarFileName = properties.getProperty("projectJarFileName");
			String dependencies = properties.getProperty("dependencies");
			String[] targetArgs = { "-r", targetMethod.getReturnType(), "-p", targetMethod.getParameterType(), "-m",
					targetMethod.getMethodName(), "-P", properties.getProperty("targetProject") };
			List<MethodInfo> evolvedMethods = search.execute(targetArgs);
			logger.info("進化対象のメソッド数：" + evolvedMethods.size());

			Transformation trans = new Transformation();
			Controller ctr = new Controller();
			for (MethodInfo evMethod : evolvedMethods) {
				if (targetMethod.equals(evMethod) || evMethod.getStatementNumber() <= 1
						|| !mg.isSameClass(targetMethod, evMethod))
					continue;
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
					mg.taihi(dstsrcDir, dstclassDir, targetJavaFileName, targetClassFileNames);
					FileUtils.moveFileToDirectory(targetJavaFile, dstsrcDir, false);
					for (String targetClassFileName : targetClassFileNames) {
						FileUtils.moveFileToDirectory(
								new File(properties.getProperty("workingDir") + targetClassFileName), dstclassDir,
								false);
					}
					Thread th = new Thread(
							new TestCaseRunnerThread(targetClassName, mg.toPackageName(targetAbsClassName), testcase,properties));
					th.start();
					th.join(60000);
					if (th.isAlive()) {
						th.stop();
					}
					if (testcase.existsTestCase()) {
						logger.info("テストケース成功．Nopol起動");
						String[] arguments = { "-s", "work/commons-text/src/main/java", "-c", properties.getProperty("classpath"),"-t",
								targetAbsClassName + mg.suffixOfTestCase(targetClassName,
										mg.toPackageName(targetAbsClassName), properties), "-p", "z3.exe" };
						Thread nopol = new Thread(new Nopol(arguments));
						nopol.run();
						logger.info("Nopol終了");
					}
					mg.untaihi(dstsrcDir, dstclassDir, targetJavaFileName, targetClassFileNames);
				}
				if (isSuccess(targetMethod)) {
					logger.info("メソッドid " + targetMethod.getId() + "の自動生成が成功");
					break;
				}
			}
		}
	}

	public static boolean isSuccess(MethodInfo targetMethod) {
		boolean exists = false;
		File file = new File("patch_1.diff");
		if (file.exists()){
			File newFile = new File("patch_"+targetMethod.getId()+".diff");
			file.renameTo(newFile);
			exists = true;
		}
		return exists;
	}

}
