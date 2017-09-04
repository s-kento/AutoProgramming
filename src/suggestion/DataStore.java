package suggestion;

import org.apache.commons.codec.DecoderException;
import search.MethodInfo;
import search.Ranker;

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

    public DataStore(List<MethodInfo> methods, String methodName) throws DecoderException {
        int id = 0;
        Ranker ranker = new Ranker();
        float nowMethodLeven = 0;
        for (MethodInfo methodInfo: methods) {
            Method method = new Method(methodInfo, id);
            uncheckedMethods.add(method);
            allMethods.add(method);
            if (nowMethod == null) {
                nowMethod = method;
                if (methodName != null) {
                    nowMethodLeven = ranker.calcLeven(nowMethod.getInfo().getMethodName(), methodName);
                }
            } else if (methodName != null) {
                float leven = ranker.calcLeven(method.getInfo().getMethodName(), methodName);
                if (leven > nowMethodLeven) {
                    nowMethodLeven = leven;
                    nowMethod = method;
                }
            } else if (nowMethod.getInfo().getSourceCode().length() > method.getInfo().getSourceCode().length()) {
                nowMethod = method;
            }
            id += 1;
        }
        if (nowMethod == null) return;
        checkMethod(nowMethod);
    }

    void next(Method nextMethod) {
        nowMethod = nextMethod;
        checkMethod(nowMethod);
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
