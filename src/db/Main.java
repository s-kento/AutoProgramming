package db;

import java.sql.SQLException;

public class Main {
	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		SQLite db= new SQLite("hoge.db");
		db.test();
	}
}
