package suggestion;

import java.util.List;

/**
 * Created by matsumotojunnosuke on 2017/06/16.
 */
public class Suggestion {

    private DataStore store;
    private SuggestLogic logic;

    public Suggestion(List<String> list) {
        store = new DataStore(list);
    }
}
