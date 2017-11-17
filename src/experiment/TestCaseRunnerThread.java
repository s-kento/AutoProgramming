package experiment;

import java.io.IOException;

public class TestCaseRunnerThread implements Runnable {
	String className;
	String packageName;
	TestCaseInfo testcase;

	TestCaseRunnerThread(String className, String packageName, TestCaseInfo testcase) {
		this.className = className;
		this.packageName = packageName;
		this.testcase = testcase;
	}

	public void run() {
		try {
			MethodGenerator mg = new MethodGenerator();
			if (mg.testFailed(className, packageName)) {
				testcase.setTestCase(true);
			}
		} catch (ClassNotFoundException | IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

}
