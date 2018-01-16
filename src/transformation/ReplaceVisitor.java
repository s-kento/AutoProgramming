package transformation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.text.edits.TextEditGroup;

import register.MyVisitor;
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
		if (node.getName().toString().equals(method.getMethodName())
				&& getParameterString(node.parameters()).equals(method.getParameterType())) {
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

	public String getParameterString(List<SingleVariableDeclaration> pTypes) {
		String parameterStr = null;
		MyVisitor v = new MyVisitor();
		List<String> tmp = new ArrayList<String>();
		if (pTypes.size() == 0) {// 引数がない場合，nullと格納．
			tmp.add("null");
		}
		for (SingleVariableDeclaration pType : pTypes) {
			tmp.add(v.getFQName(pType.getType()));
		}
		Collections.sort(tmp);
		for (int i = 0; i < tmp.size(); i++) {
			parameterStr += tmp.get(i);
			if (i < tmp.size() - 1) {
				parameterStr += ",";
			}
		}
		return parameterStr;
	}
}
