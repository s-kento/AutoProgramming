package db;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Config {
	public CommandLine getOptions(String[] args) throws ParseException{
		Options options = new Options();
		options.addOption("p","parametertype",true,"パラメータの型");
		options.addOption("f","filepath",true,"ファイルパス");
		options.addOption("c","classname",true,"クラス名");
		options.addOption("m","methodname",true,"メソッド名");
		options.addOption("r","returntype",true,"返値の型");

		CommandLineParser parser = new BasicParser();
		CommandLine commandLine;
		commandLine = parser.parse(options, args);

		return commandLine;
	}
}