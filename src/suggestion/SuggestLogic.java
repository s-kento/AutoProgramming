package suggestion;

import java.util.HashMap;

/**
 * Created by matsumotojunnosuke on 2017/06/17.
 */
class SuggestLogic {

    private DataStore store;
    private HashMap<String, Integer> cache = new HashMap<>();

    SuggestLogic(DataStore store) {
        this.store = store;
    }

    private void setCache(Method a, Method b, int length) {
        String key = getKey(a, b);
        cache.put(key, length);
    }

    private Integer getCache(Method a, Method b) {
        String key = getKey(a, b);
        return cache.get(key).intValue();
    }

    private String getKey(Method a, Method b) {
        int min = Math.min(a.getId(), b.getId());
        int max = Math.max(a.getId(), b.getId());
        return String.valueOf(min) + "-" + String.valueOf(max);
    }
}
