package search;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.apache.commons.codec.DecoderException;
import suggestion.Suggestion;
import suggestion.Util;

/*
 * データベースを参照するクラス
 */

public class Search {
        public static void main(String[] args) throws ClassNotFoundException, SQLException, ParseException, DecoderException, IOException {
		Search sr = new Search();
		List<MethodInfo> methods=sr.execute(args);
		for(MethodInfo method:methods){
			System.out.println(method.getSourceCode());
		}
	}


	public List<MethodInfo> execute(String[] args) throws ClassNotFoundException, SQLException, ParseException, DecoderException, IOException {
		Config conf = new Config();
		CommandLine cl = conf.getOptions(args);
		SQLite db = new SQLite(cl.getOptionValue("d"), cl.getOptionValue("t"));
		Suggestion suggestion = new Suggestion(db);
		List<MethodInfo> methods = suggestion.suggest(cl.getOptionValue("m"), Util.getParameter(cl.getOptionValues("p")), cl.getOptionValue("r"));
		return methods;
	}
}
