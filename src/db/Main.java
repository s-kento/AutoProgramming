package db;

import java.sql.SQLException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

public class Main {
	public static void main(String[] args) throws ClassNotFoundException, SQLException, ParseException {
		Config conf = new Config();
		CommandLine cl = conf.getOptions(args);
		SQLite db = new SQLite(cl.getOptionValue("d"), cl.getOptionValue("t"));
		db.showTable(cl);
	}
}
