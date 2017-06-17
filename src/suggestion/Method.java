package suggestion;

/**
 * Created by matsumotojunnosuke on 2017/06/17.
 */
public class Method {
    private int id;
    private String sourceCode;

    public Method(String sourceCode, int id) {
        this.sourceCode = sourceCode;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getSourceCode() {
        return sourceCode;
    }
}
