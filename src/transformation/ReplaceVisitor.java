package transformation;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.text.edits.TextEditGroup;

import search.MethodInfo;

public class ReplaceVisitor extends ASTVisitor {
	private CompilationUnit unit;
	private MethodInfo method;
	private MethodDeclaration node;
	private MethodDeclaration replacement;
	private ASTRewrite rewriter;
	private boolean replace;

	public ReplaceVisitor(CompilationUnit unit, MethodInfo method) {
		replace = false;
		this.method = method;
		this.unit = unit;
	}

	public MethodDeclaration getMethodNode() {
		return node;
	}

	public void setMethodNode(MethodDeclaration node) {
		this.node = node;
	}

	public MethodDeclaration getReplacement() {
		return replacement;
	}

	public void setReplacement(MethodDeclaration replacement) {
		AST ast = replacement.getAST();
		replacement.setName(ast.newSimpleName(method.getMethodName()));
		this.replacement = replacement;
	}

	public void doReplace() {
		replace = true;
	}

	public ASTRewrite getRewriter() {
		return rewriter;
	}

	public void setRewriter(ASTRewrite rewriter) {
		this.rewriter = rewriter;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		if (node.getName().toString().equals(method.getMethodName())) {
			setMethodNode(node);

			if (replace) {
				AST ast = unit.getAST();
				ASTRewrite rewriter = ASTRewrite.create(ast);
				TextEditGroup editGroup = new TextEditGroup("Replacing nodes");
				rewriter.replace(node, getReplacement(), editGroup);
				node.setName(ast.newSimpleName(method.getMethodName()));
				setRewriter(rewriter);
			}
		}
		return true;
	}
}
