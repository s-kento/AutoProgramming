package cui;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Scanner;

import org.apache.commons.cli.ParseException;
import org.apache.commons.codec.DecoderException;

import register.Register;
import search.Search;
import transformation.Transformation;

/*
 * CUI操作を担当するクラス
 */
public class Main {
	public static void main(String[] args)
			throws ClassNotFoundException, SQLException, IOException, ParseException, DecoderException {
		while(true){
			System.out.print(">");
			Scanner scan = new Scanner(System.in);
			String in = scan.next();
			if(in.equals("register")){
				Register reg = new Register();
				String arg = scan.nextLine();
				reg.execute(arg.split("[\\s]+"));
			}else if(in.equals("search")){
				Search search = new Search();
				String arg = scan.nextLine();
				search.execute(arg.split("[\\s]+"));
			}else if(in.equals("trans")){
				Transformation trans = new Transformation();
				String arg = scan.nextLine();
				trans.execute(arg.split("[\\s]+"));
			}
			if(in.equals("exit"))
				break;

		}
	}
}
