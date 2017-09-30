package search;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import register.MyVisitor;

import java.util.Optional;

/*
 * メソッド情報を持つクラス
 */
public class MethodInfo {
	private String filePath;
	private String returnType;
	private String methodName;
	private String parameterType;
	private String className;
	private String projectName;
	private int startLine;
	private String sourceCode;

	public MethodInfo(String filePath, String className, String methodName, String returnType, String parameterType,
			String projectName, int startLine, String sourceCode) throws DecoderException {
		setFilePath(filePath);
		setClassName(className);
		setMethodName(methodName);
		setReturnType(returnType);
		setParameterType(parameterType);
		setProjectName(projectName);
		setStartLine(startLine);
		setSourceCode(sourceCode);
	}

	public MethodInfo(MyVisitor visitor) throws DecoderException {
		setFilePath(visitor.getFilePath());
		setClassName(visitor.getClassName());
		setMethodName(visitor.getMethodName());
		setReturnType(visitor.getReturnType());
		boolean seen = false;
		String acc = null;
		for (String s : visitor.getParameterType()) {
			if (!seen) {
				seen = true;
				acc = s;
			} else {
				acc = acc + "," + s;
			}
		}
		setParameterType((seen ? Optional.of(acc) : Optional.<String>empty()).get());
		setStartLine(visitor.getStartLine());
		setSourceCode(visitor.getSourceCode());
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getReturnType() {
		return returnType;
	}

	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getParameterType() {
		return parameterType;
	}

	public void setParameterType(String parameterType) {
		this.parameterType = parameterType;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public int getStartLine() {
		return startLine;
	}

	public void setStartLine(int startLine) {
		this.startLine = startLine;
	}

	public String getSourceCode() {
		return sourceCode;
	}

	public void setSourceCode(String sourceCode) throws DecoderException {
		this.sourceCode = toString(sourceCode);
	}

	public String getFQName() {
		return getClassName() + "." + getMethodName();
	}

	public String toString(String hexstr) throws DecoderException{
		byte[] sbyte = Hex.decodeHex(hexstr.toCharArray());
		String str = new String(sbyte);
		return str;
	}

	public boolean equals(MethodInfo method){
		boolean equal = false;
		if(getFilePath().equals(method.getFilePath()) && getStartLine()==method.getStartLine())
			equal=true;
		return equal;
	}

	public int hashCode(){
		return getFilePath().hashCode()+getStartLine();
	}
}
