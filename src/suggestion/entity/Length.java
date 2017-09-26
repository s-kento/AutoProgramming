package suggestion.entity;

import search.MethodInfo;

import java.util.Arrays;
import java.util.List;

/**
 * Created by matsumotojunnosuke on 2017/09/19.
 */
public class Length {

    private MethodInfo methodA;
    private MethodInfo methodB;
    private Integer value;
    private String id;

    public Length(MethodInfo methodA, MethodInfo methodB, Integer value, String id) {
        this.methodA = methodA;
        this.methodB = methodB;
        this.value = value;
        this.id = id;
    }

    public Integer getValue() {
        return value;
    }

    public List<MethodInfo> getMethods() {
        return Arrays.asList(methodA, methodB);
    }

    public Boolean isInclude(MethodInfo method) {
        return method.getFQName() == methodA.getFQName() || method.getFQName() == methodB.getFQName();
    }

    public String getId() {
        return id;
    }
}
