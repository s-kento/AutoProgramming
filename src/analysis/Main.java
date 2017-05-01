package analysis;

import java.io.IOException;
import java.sql.SQLException;

import org.eclipse.jdt.core.dom.CompilationUnit;

import db.SQLite;

public class Main {
	public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException{
		SourceFileAnalyzer sfa = new SourceFileAnalyzer(args[0]);
		for (String filePath : sfa.getFileList()) {
			System.out.println(filePath);
			CompilationUnit unit = sfa.getAST(filePath);
			MyVisitor visitor = new MyVisitor(unit, filePath);
			unit.accept(visitor);
		}
	}
}
