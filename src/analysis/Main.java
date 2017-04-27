package analysis;

import java.io.IOException;

import org.eclipse.jdt.core.dom.CompilationUnit;

public class Main {
	public static void main(String[] args) throws IOException {
		SourceFileAnalyzer sfa = new SourceFileAnalyzer(args[0]);
		for (String filePath : sfa.getFileList()) {
			CompilationUnit unit = sfa.getAST(filePath);
			MyVisitor visitor = new MyVisitor(unit);
			unit.accept(visitor);
			System.out.println(visitor.getMethodName());
			System.out.println(visitor.getReturnType());
		}
	}
}
