package experiment;

import java.io.IOException;
import java.io.PrintWriter;

public class TestCaseRunnerThread extends Thread {
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
			if (Main.testFailed(className, packageName)) {
				testcase.setTestCase(true);
			}
		} catch (ClassNotFoundException | IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

}
