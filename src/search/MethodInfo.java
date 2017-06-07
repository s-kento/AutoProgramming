package search;

import java.util.ArrayList;
import java.util.List;

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

	MethodInfo(String filePath, String className, String methodName, String returnType, String parameterType,
			String projectName) {
		setFilePath(filePath);
		setClassName(className);
		setMethodName(methodName);
		setReturnType(returnType);
		setParameterType(parameterType);
		setProjectName(projectName);
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
}
