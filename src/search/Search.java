package search;

import java.sql.SQLException;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

/*
 * データベースを参照するクラス
 */

public class Search {
	public void execute(String[] args) throws ClassNotFoundException, SQLException, ParseException {
		Config conf = new Config();
		CommandLine cl = conf.getOptions(args);
		SQLite db = new SQLite(cl.getOptionValue("d"), cl.getOptionValue("t"));
		List<MethodInfo> methods = db.getMethodInfo(cl);
		Ranker rank = new Ranker();
		methods = rank.sortByMethodNameSimilarity("swap", methods);
		for(MethodInfo method:methods){
			System.out.println(method.getFilePath()+", "+method.getMethodName());
		}
	}
}
