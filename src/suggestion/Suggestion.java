package suggestion;

import org.apache.commons.codec.DecoderException;
import search.MethodInfo;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by matsumotojunnosuke on 2017/06/16.
 */
public class Suggestion {

    public Suggestion() {}

    public boolean regist(MethodInfo targetMethodInfo) {
        try {
            RegistLogic logic = new RegistLogic(targetMethodInfo);
            return logic.regist();
        } catch (DecoderException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<MethodInfo> suggest(String methodName, String parameterType, String returnType) {
        SuggestLogic logic = new SuggestLogic(methodName, parameterType, returnType);
        try {
            return logic.suggest();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DecoderException e) {
            e.printStackTrace();
        }
        return null;
    }
}
