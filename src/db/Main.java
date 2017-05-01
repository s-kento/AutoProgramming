package db;

import java.sql.SQLException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

public class Main {
	public static void main(String[] args) throws ClassNotFoundException, SQLException, ParseException {
		SQLite db= new SQLite("autoprog.db");
		Config conf = new Config();
		CommandLine cl = conf.getOptions(args);
		db.showTable(cl);
	}
}
