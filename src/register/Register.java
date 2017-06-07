package register;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.eclipse.jdt.core.dom.CompilationUnit;

import search.Config;
import search.SQLite;

/*
 * データベースに格納するクラス
 */

public class Register {
	public void execute(String[] args) throws ClassNotFoundException, SQLException, IOException, ParseException {
		Config conf = new Config();
		CommandLine cl = conf.getOptions(args);
		SourceFileAnalyzer sfa = new SourceFileAnalyzer(cl.getOptionValue("f"));
		System.out.println("ファイル格納完了");
		for (String filePath : sfa.getFileList()) {
			System.out.println(filePath);
			CompilationUnit unit = sfa.getAST(filePath);
			MyVisitor visitor = new MyVisitor(unit, filePath, cl.getOptionValue("d"), cl.getOptionValue("t"),
					cl.getOptionValue("p"));
			unit.accept(visitor);
		}
	}
}
