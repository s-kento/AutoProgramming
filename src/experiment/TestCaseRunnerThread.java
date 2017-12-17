package experiment;

import java.io.IOException;
import java.util.Properties;

public class TestCaseRunnerThread implements Runnable {
	String className;
	String packageName;
	TestCaseInfo testcase;
	Properties properties;

	TestCaseRunnerThread(String className, String packageName, TestCaseInfo testcase, Properties properties) {
		this.className = className;
		this.packageName = packageName;
		this.testcase = testcase;
		this.properties=properties;
	}

	public void run() {
		try {
			MethodGenerator mg = new MethodGenerator();
			if (mg.testFailed(className, packageName, properties)) {
				testcase.setTestCase(true);
			}
		} catch (ClassNotFoundException | IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

}
