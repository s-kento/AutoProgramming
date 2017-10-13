package xml;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.commons.cli.ParseException;
import org.apache.commons.codec.DecoderException;

import search.MethodInfo;
import search.Search;

public class CoverageRegister {
	Connection connection = null;
	Statement statement = null;
	String db = "autoprog.db";
	String table = "coverages";

	/**
	 * データベースにカバレッジ情報を登録する
	 *
	 * @param coverages
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws IOException
	 * @throws DecoderException
	 * @throws ParseException
	 */
	public void regist(List<CoverageInfo> coverages)
			throws ClassNotFoundException, SQLException, ParseException, DecoderException, IOException {
		Class.forName("org.sqlite.JDBC");
		connection = DriverManager.getConnection("jdbc:sqlite:sqlite/" + db);
		statement = connection.createStatement();

		/* テーブルがなければ作成する */
		String sql = "create table if not exists " + table + "(id integer primary key, coverage numeric)";
		statement.executeUpdate(sql);
		sql = "create index if not exists signature on " + table + "(id)";
		statement.executeUpdate(sql);

		int id;

		for (CoverageInfo coverageInfo : coverages) {
			id = getMethodID(coverageInfo);
			if (id > 0) {//一致するメソッドがあれば
				System.out.println(coverageInfo.getClassName()+" "+coverageInfo.getMethodName()+"登録開始");
				sql = "insert into " + table + " values(" + id + ", " + coverageInfo.getCoverage() + ")";
				statement.executeUpdate(sql);// SQL文の実行
			} else {//一致するメソッドがなければ
				System.out.println(coverageInfo.getClassName()+" "+coverageInfo.getMethodName()+" is not found");
			}
		}
		try {
			if (statement != null) {
				statement.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * CoverageInfoからメソッドIDを取得する 一致するメソッドが見つからなければ，-1を返す
	 *
	 * @param coverage
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws ParseException
	 * @throws DecoderException
	 * @throws IOException
	 */
	public int getMethodID(CoverageInfo coverage)
			throws ClassNotFoundException, SQLException, ParseException, DecoderException, IOException {
		int id = -1;
		Search search = new Search();
		String[] args = { "-c", coverage.getClassName(), "-m", coverage.getMethodName() };
		List<MethodInfo> methods = search.execute(args);
		if (methods.size() > 1) {
			for (MethodInfo method : methods) {
				if (coverage.getLineNumber() >= method.getStartLine()
						&& coverage.getLineNumber() <= method.getEndLine()) {
					id = method.getId();
					break;
				}
			}
		} else if (methods.size() == 1) {
			id = methods.get(0).getId();
		}
		return id;
	}
}
