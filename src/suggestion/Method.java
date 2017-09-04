package suggestion;

import org.apache.commons.codec.DecoderException;
import search.MethodInfo;

/**
 * Created by matsumotojunnosuke on 2017/06/17.
 */

public class Method {
    private int id;
    private MethodInfo info;

    Method(MethodInfo methodInfo, int id) throws DecoderException {
        info = methodInfo;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public MethodInfo getInfo() {
        return info;
    }
}
