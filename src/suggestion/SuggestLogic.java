package suggestion;

import org.apache.commons.codec.DecoderException;
import search.MethodInfo;
import search.Ranker;
import search.SQLite;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by matsumotojunnosuke on 2017/06/17.
 */
class SuggestLogic {

    private String methodName;
    private String returnType;
    private String parameterType;
    private SQLite db;

    public SuggestLogic(String methodName, String parameterType, String returnType, SQLite db) {
        this.methodName = methodName;
        this.returnType = returnType;
        this.parameterType = parameterType;
        this.db = db;
    }

    public List<MethodInfo> suggest() throws ClassNotFoundException, SQLException, IOException, DecoderException {
        List<MethodInfo> methods = db.getMethodInfo(parameterType, returnType);
        List<MethodInfo> resultMethods = new ArrayList<>();
        MethodInfo firstMethod = getFirstMethod(methods);
        methods.remove(firstMethod);
        resultMethods.add(firstMethod);
        MethodInfo next = getNextMethodInfo(resultMethods, methods);
        while (next != null) {
            methods.remove(next);
            resultMethods.add(next);
        }
        return resultMethods;
    }

    private MethodInfo getFirstMethod(List<MethodInfo> methods) {
        MethodInfo firstMethod = methods.get(0);
        Ranker ranker = new Ranker();
        float leven = ranker.calcLeven(firstMethod.getMethodName(), methodName);
        for (MethodInfo methodInfo: methods) {
            float tmpLeven = ranker.calcLeven(methodInfo.getMethodName(), methodName);
            if (tmpLeven > leven) {
                leven = tmpLeven;
                firstMethod = methodInfo;
            }
        }
        return  firstMethod;
    }

    private MethodInfo getNextMethodInfo(List<MethodInfo> checkedMethods, List<MethodInfo> uncheckedMethods) throws ClassNotFoundException, SQLException, DecoderException, IOException {
        Integer maxLength = 0;
        MethodInfo nextMethod = null;
        for (MethodInfo method: uncheckedMethods) {
            Integer totalLength = 0;
            for(MethodInfo checkedMethod: checkedMethods) {
                totalLength += db.getLength(method, checkedMethod).getValue();
            }
            if (totalLength > maxLength) {
                maxLength = totalLength;
                nextMethod = method;
            }
        }
        return nextMethod;
    }
}
