package transformation;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class ReplaceVisitor2 extends ASTVisitor {

	private CompilationUnit unit;
	private MethodDeclaration mdNode;

	public CompilationUnit getUnit() {
		return unit;
	}

	public void setUnit(CompilationUnit unit) {
		this.unit = unit;
	}

	public MethodDeclaration getMdNode() {
		return mdNode;
	}

	public void setMdNode(MethodDeclaration mdNode) {
		this.mdNode = mdNode;
	}

	@Override
	public boolean visit(MethodDeclaration node) {

		return true;
	}
}
