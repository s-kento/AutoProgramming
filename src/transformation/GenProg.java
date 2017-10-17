package transformation;

import fr.inria.main.evolution.*;
import search.MethodInfo;

public class GenProg {
	public static void main(String[] args) throws Exception {
		GenProg gen = new GenProg();
		gen.execute(args);
	}

	public void execute(String[] args) throws Exception {
		AstorMain astor = new AstorMain();
		astor.execute(args);
	}

	public void execute(String[] args,MethodInfo targetMethod) throws Exception {
		AstorMain astor = new AstorMain(targetMethod);
		astor.execute(args);
	}
}
