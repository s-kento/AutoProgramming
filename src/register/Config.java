package register;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Config {
	public CommandLine getOptions(String[] args) throws ParseException {
		Options options = new Options();
		options.addOption("f", "filepath", true, "ファイルパス");
		options.addOption("d", "database", true, "データベース名");
		options.addOption("t", "table", true, "テーブル名");
		options.addOption("p","project",true,"プロジェクト名");

		CommandLineParser parser = new BasicParser();
		CommandLine commandLine;
		commandLine = parser.parse(options, args);

		return commandLine;
	}
}