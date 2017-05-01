package analysis;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

/*
 * メソッドのシグネチャを取得するためのvisitor
 * @author s-kento
 */
public class MyVisitor extends ASTVisitor {
	private CompilationUnit unit;
	private String returnType;
	private String methodName;
	private List<String> parameterType = new ArrayList<String>();

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

	public List<String> getParameterType() {
		return parameterType;
	}

	public void setParameterType(List<String> parameterType) {
		this.parameterType = parameterType;
	}

	public MyVisitor(CompilationUnit unit) {
		this.unit = unit;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		if (!node.isConstructor()) { //コンストラクタは無視する
			setMethodName(node.getName().toString());
			setReturnType(node.getReturnType2().toString());
			List<SingleVariableDeclaration> pTypes = node.parameters();
			List<String> tmp = new ArrayList<String>();
			for (SingleVariableDeclaration pType : pTypes) {
				tmp.add(pType.getType().toString());
			}
			setParameterType(tmp);
		}
		return true;
	}
}
