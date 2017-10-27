package register;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Statement;

public class CountStatementVisitor extends ASTVisitor{

	private int statementNumber=0;

	public int getStatementNumber() {
		return statementNumber;
	}

	public void setStatementNumber(int statementNumber) {
		this.statementNumber = statementNumber;
	}

	@Override
	public boolean preVisit2(ASTNode node){
		if(node instanceof Statement && !(node instanceof Block)){
			statementNumber++;
		}
		return true;
	}
}
