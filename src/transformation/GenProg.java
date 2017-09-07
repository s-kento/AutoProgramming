package transformation;

import fr.inria.main.evolution.*;

public class GenProg {
	public static void main(String[] args) throws Exception {
		GenProg gen = new GenProg();
		gen.execute(args);
	}

	public void execute(String[] args) throws Exception {
		AstorMain astor = new AstorMain();
		astor.execute(args);
	}
}
