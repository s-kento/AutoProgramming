package transformation;

public class Main {
	public static void main(String[] args) {
		Controller cr = new Controller();
		// cr.extractJavaFile("MachineLearning.jar", "IdentifybyName.java");
		// cr.compile("MachineLearning.jar", "IdentifybyName.java");
		System.out.println(cr.run("MachineLearning.jar", "IdentifybyName.java", "ByName", "IdentifybyName",
				"matchANTLR","java.lang.String",new String[]{"Aexer"}));
	}
}
