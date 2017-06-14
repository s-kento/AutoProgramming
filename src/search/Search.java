package search;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.apache.commons.codec.DecoderException;

/*
 * データベースを参照するクラス
 */

public class Search {
	public void execute(String[] args)
			throws ClassNotFoundException, SQLException, ParseException, DecoderException, IOException {
		Config conf = new Config();
		CommandLine cl = conf.getOptions(args);
		SQLite db = new SQLite(cl.getOptionValue("d"), cl.getOptionValue("t"));
		List<MethodInfo> methods = db.getMethodInfo(cl);
		Ranker rank = new Ranker();
		methods = rank.sortByMethodNameSimilarity("read", methods);
		for(MethodInfo method:methods){
			System.out.println(method.getSourceCode());
		}
	}
}
