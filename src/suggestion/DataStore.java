package suggestion;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by matsumotojunnosuke on 2017/06/17.
 */
class DataStore {

    private List<Method> uncheckedMethods = new ArrayList<>();
    private List<Method> checkedMethods = new ArrayList<>();
    private List<Method> allMethods = new ArrayList<>();
    private Method nowMethod;

    public DataStore(List<String> methods) {
        int id = 0;
        for (String string: methods) {
            Method method = new Method(string, id);
            uncheckedMethods.add(method);
            allMethods.add(method);
            if (nowMethod == null) {
                nowMethod = method;
            } else if (nowMethod.getSourceCode().length() > method.getSourceCode().length()) {
                nowMethod = method;
            }
        }
        if (nowMethod == null) return;
        checkMethod(nowMethod);
    }

    void next(Method nextMethod) {
        checkMethod(nowMethod);
        nowMethod = nextMethod;
    }

    private void checkMethod(Method method) {
        int index = -1;
        int i = 0;
        for (Method m: uncheckedMethods) {
            if (method.getId() == m.getId()) {
                index = i;
                break;
            }
            i += 1;
        }
        if (index == -1) return;
        uncheckedMethods.remove(index);
        checkedMethods.add(method);
    }

    List<Method> getCheckedMethods() {
        return checkedMethods;
    }

    List<Method> getUncheckedMethods() {
        return uncheckedMethods;
    }

    List<Method> getAllMethods() {
        return allMethods;
    }

    Method getNowMethod() {
        return nowMethod;
    }
}
