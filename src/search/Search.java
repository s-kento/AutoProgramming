package search;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.apache.commons.codec.DecoderException;
import suggestion.Suggestion;

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

		String methodName = cl.getOptionValue("s");
		if (methods.isEmpty()) return;
		System.out.println(methods.size() + " methods Hits\n");

		long start = System.currentTimeMillis();
		Suggestion suggestion = new Suggestion(methods, methodName);
		while (true) {
			System.out.println(suggestion.getNowSourceCode());
			if (!suggestion.hasNext()) break;
			suggestion.next();
		}
		long end = System.currentTimeMillis();
		System.out.println("runtime: " + (end - start)  + "ms");
	}
}
