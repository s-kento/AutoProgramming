package transformation;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.text.edits.TextEditGroup;

import search.MethodInfo;

public class ReplaceVisitor2 extends ASTVisitor {

	private CompilationUnit unitB;// メソッドBを含むクラスのAST
	private MethodInfo methodA;
	private MethodInfo methodB;
	private MethodDeclaration mdNode;// method declaration
	public boolean firstSearch=true;
	private AST ast;
	private ASTRewrite rewriter;

	private TextEditGroup editGroup = new TextEditGroup("Replacing nodes");

	public ReplaceVisitor2(CompilationUnit unitB, MethodInfo methodA, MethodInfo methodB) {
		setUnit(unitB);
		setMethodA(methodA);
		setMethodB(methodB);
		ast=unitB.getAST();
		rewriter=ASTRewrite.create(ast);
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

	public ASTRewrite getRewriter() {
		return rewriter;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		if(firstSearch){//methodBクラスの探索
			if(node.getName().toString().equals(methodB.getMethodName())){
				//methodBノード(subtree)をコピー，挿入
				MethodDeclaration copiedNode = (MethodDeclaration)ASTNode.copySubtree(ast, node);
				copiedNode.setName(ast.newSimpleName(methodA.getMethodName()));
				TypeDeclaration parent=getParentTypeDeclaration(node);
				ListRewrite lrw = rewriter.getListRewrite(parent,parent.BODY_DECLARATIONS_PROPERTY);
				lrw.insertLast(copiedNode, null);
				firstSearch=false;
			}
		}
		else{//methodAクラスの探索
			if(!node.getName().toString().equals(methodA.getMethodName())){//methodA以外

			}
		}
		return true;
	}

	@Override
	public boolean visit(FieldDeclaration node){
		if(!firstSearch){//methodAクラスの探索
			List<VariableDeclarationFragment> fragments = node.fragments();
		}
		return true;
	}

	public TypeDeclaration getParentTypeDeclaration(MethodDeclaration node){
		ASTNode parent=(ASTNode)node;
		while(true){
			parent=parent.getParent();
			if(parent instanceof TypeDeclaration)
				break;
		}
		return (TypeDeclaration)parent;
	}

}
