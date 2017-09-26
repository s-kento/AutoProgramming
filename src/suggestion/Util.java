package suggestion;

import search.MethodInfo;

/**
 * Created by matsumotojunnosuke on 2017/09/22.
 */
public class Util {

    public static String getId(MethodInfo methodA, MethodInfo methodB) {
        String methodAName = methodA.getFQName();
        String methodBName = methodB.getFQName();
        if (methodAName.compareTo(methodBName) == -1) {
            return methodAName + "-" + methodBName;
        } else {
            return methodBName + "-" + methodAName;
        }
    }

}
