package experiment;

import search.MethodInfo;

public class TestCaseInfo {
	private MethodInfo method;
	private boolean testCase;
	private int sigNumber;

	public TestCaseInfo(MethodInfo method){
		setMethod(method);
		setTestCase(false);
		sigNumber=0;
	}

	public MethodInfo getMethod() {
		return method;
	}
	public void setMethod(MethodInfo method) {
		this.method = method;
	}
	public boolean existsTestCase() {
		return testCase;
	}
	public void setTestCase(boolean testCase) {
		this.testCase = testCase;
	}
	public int getSigNumber() {
		return sigNumber;
	}
	public void setSigNumber(int signumber) {
		this.sigNumber = signumber;
	}
}
