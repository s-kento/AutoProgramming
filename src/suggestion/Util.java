package suggestion;

import org.apache.commons.cli.*;
import search.MethodInfo;

import java.util.Arrays;

/**
 * Created by matsumotojunnosuke on 2017/09/22.
 */
public class Util {

    public static String getId(MethodInfo methodA, MethodInfo methodB) {
        String methodAName = methodA.getFQName();
        String methodBName = methodB.getFQName();
        if (methodAName.compareTo(methodBName) < 0) {
            return methodAName + "-" + methodBName;
        } else {
            return methodBName + "-" + methodAName;
        }
    }

    public static String getParameter(String[] parameters) {
        if (parameters == null) return null;
        String text = "";
        Arrays.sort(parameters);
        for (int i = 0; i < parameters.length; i++) {
            text += parameters[i];
            if (i < parameters.length - 1) {
                text += ",";
            }
        }
        return text;
    }
}
