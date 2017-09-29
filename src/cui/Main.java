package cui;

import java.util.List;
import java.util.Scanner;

import register.Register;
import search.MethodInfo;
import search.Search;
import transformation.Transformation;

/*
 * CUI操作を担当するクラス
 */
public class Main {
	public static void main(String[] args)
			throws Exception {
		while(true){
			System.out.print(">");
			Scanner scan = new Scanner(System.in);
			String in = scan.next();
			if(in.equals("register")){
				long startTime = System.currentTimeMillis();
				Register reg = new Register();
				String arg = scan.nextLine();
				reg.execute(arg.split("[\\s]+"));
				long endTime = System.currentTimeMillis();
				System.out.println((endTime - startTime) + "ms");
			}else if(in.equals("search")){
				Search search = new Search();
				String arg = scan.nextLine();
				List<MethodInfo> methods=search.execute(arg.split("[\\s]+"));
				for(MethodInfo method:methods){
					System.out.println(method.getSourceCode());
				}
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
