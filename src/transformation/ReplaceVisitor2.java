package transformation;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import search.MethodInfo;

public class ReplaceVisitor2 extends ASTVisitor {

	private CompilationUnit unitB;// メソッドBを含むクラスのAST
	private MethodInfo methodA;
	private MethodInfo methodB;
	private MethodDeclaration mdNode;// method declaration
	public boolean firstSearch=true;

	public ReplaceVisitor2(CompilationUnit unitB, MethodInfo methodA, MethodInfo methodB) {
		setUnit(unitB);
		setMethodA(methodA);
	}

	public MethodInfo getMethodA() {
		return methodA;
	}

	public void setMethodA(MethodInfo methodA) {
		this.methodA = methodA;
	}

	public MethodInfo getMethodB() {
		return methodB;
	}

	public void setMethodB(MethodInfo methodB) {
		this.methodB = methodB;
	}

	public CompilationUnit getUnit() {
		return unitB;
	}

	public void setUnit(CompilationUnit unit) {
		this.unitB = unit;
	}

	public MethodDeclaration getMdNode() {
		return mdNode;
	}

	public void setMdNode(MethodDeclaration mdNode) {
		this.mdNode = mdNode;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		if(firstSearch){//methodBクラスの探索
			if(node.getName().toString().equals(methodB.getMethodName())){
				setMdNode(node);
				firstSearch=false;
			}
		}
		else{//methodAクラスの探索
			if(node.getName().toString().equals(methodA.getMethodName())){//methodA

			}
			else{//methodA以外

			}
		}
		return true;
	}

	@Override
	public boolean visit(FieldDeclaration node){
		if(!firstSearch){//methodAクラスの探索

		}
		return true;
	}

}
