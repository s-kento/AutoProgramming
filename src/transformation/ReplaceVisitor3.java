package transformation;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.text.edits.TextEditGroup;

import search.MethodInfo;

public class ReplaceVisitor3 extends ASTVisitor {
	private CompilationUnit unitA;// メソッドAを含むクラスのAST
	private CompilationUnit unitB;
	private MethodInfo methodA;
	private MethodInfo methodB;
	private MethodDeclaration methodANode;// method declaration
	private MethodDeclaration methodBNode;
	private TypeDeclaration parent;
	public boolean firstSearch = true;
	private AST ast;
	private ASTRewrite rewriter;

	private TextEditGroup editGroup = new TextEditGroup("Replacing nodes");

	public ReplaceVisitor3(CompilationUnit unitA, CompilationUnit unitB, MethodInfo methodA, MethodInfo methodB) {
		setUnitA(unitA);
		setUnitB(unitB);
		setMethodA(methodA);
		setMethodB(methodB);
		ast = unitA.getAST();
		rewriter = ASTRewrite.create(ast);
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

	public CompilationUnit getUnitB() {
		return unitB;
	}

	public void setUnitB(CompilationUnit unitB) {
		this.unitB = unitB;
	}

	public CompilationUnit getUnitA() {
		return unitA;
	}

	public void setUnitA(CompilationUnit unitA) {
		this.unitA = unitA;
	}

	public MethodDeclaration getMethodANode() {
		return methodANode;
	}

	public void setMethodANode(MethodDeclaration mdNode) {
		this.methodANode = mdNode;
	}

	public MethodDeclaration getMethodBNode() {
		return methodBNode;
	}

	public void setMethodBNode(MethodDeclaration mdNode) {
		this.methodBNode = mdNode;
	}

	public ASTRewrite getRewriter() {
		return rewriter;
	}

	public TypeDeclaration getParent() {
		return parent;
	}

	public void setParent(TypeDeclaration parent) {
		this.parent = parent;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		if (firstSearch) {// methodAクラスの探索
			if (node.getName().toString().equals(methodA.getMethodName())) {
				setMethodANode(node);
				setParent(getParentTypeDeclaration(node));
				firstSearch = false;
			}
		} else {// methodBクラスの探索
			if (node.getName().toString().equals(methodB.getMethodName())) {// methodBなら，メソッド名をmethodAに変更してnodeをreplace
				node.setName(unitB.getAST().newSimpleName(methodA.getMethodName()));
				rewriter.replace(methodANode, node, editGroup);
				//methodANode.setName(ast.newSimpleName(methodA.getMethodName()));
			} else {// methodB以外なら，methodAクラスにつっこむ
				MethodDeclaration copiedNode = (MethodDeclaration) ASTNode.copySubtree(ast, node);
				ListRewrite lrw = rewriter.getListRewrite(parent, parent.BODY_DECLARATIONS_PROPERTY);
				lrw.insertLast(copiedNode, null);
			}
		}
		return true;
	}

	@Override
	public boolean visit(FieldDeclaration node) {
		if (!firstSearch) {
			FieldDeclaration copiedNode = (FieldDeclaration) ASTNode.copySubtree(ast, node);
			ListRewrite lrw = rewriter.getListRewrite(parent, parent.BODY_DECLARATIONS_PROPERTY);
			lrw.insertLast(copiedNode, null);
		}
		return true;
	}

	public TypeDeclaration getParentTypeDeclaration(ASTNode node) {
		ASTNode parent = (ASTNode) node;
		while (true) {
			parent = parent.getParent();
			if (parent instanceof TypeDeclaration)
				break;
		}
		return (TypeDeclaration) parent;
	}
}
