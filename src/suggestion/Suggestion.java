package suggestion;

import org.apache.commons.codec.DecoderException;
import search.MethodInfo;

import java.util.List;

/**
 * Created by matsumotojunnosuke on 2017/06/16.
 */
public class Suggestion {

    private DataStore store;
    private SuggestLogic logic;

    public  Suggestion(List<MethodInfo> list, String methodName) throws DecoderException {
        store = new DataStore(list, methodName);
        logic = new SuggestLogic(store);
    }

    public Boolean hasNext() {
        return !store.getUncheckedMethods().isEmpty();
    }

    public String getNowSourceCode() {
        return store.getNowMethod().getInfo().getSourceCode();
    }

    public void next() {
        logic.next();
    }
}
