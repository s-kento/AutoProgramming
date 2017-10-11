package xml;

public class CoverageInfo {
	private String className;
	private String methodName;
	private int lineNumber;
	private double coverage;

	public CoverageInfo(String className, String methodName, int lineNumber, double coverage){
		setClassName(className);
		setMethodName(methodName);
		setLineNumber(lineNumber);
		setCoverage(coverage);
	}

	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	public int getLineNumber() {
		return lineNumber;
	}
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	public double getCoverage() {
		return coverage;
	}

	public void setCoverage(double coverage) {
		this.coverage = coverage;
	}
}
