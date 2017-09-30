package suggestion;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.codec.DecoderException;
import search.MethodInfo;
import search.SQLite;
import suggestion.entity.Length;
import suggestion.entity.Method;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by matsumotojunnosuke on 2017/09/19.
 */
public class RegistLogic {

    final private int THREAD_NUM = 5;
    private ExecutorService service;
    private Method targetMethod;
    private List<Method> methods;
    private SQLite db;

    class TMPFuture {
        private Future<String> future;
        private Method method;
        private TMPFuture(Future<String> future, Method method) {
            this.future = future;
            this.method = method;
        }
    }

    public RegistLogic(MethodInfo targetMethodInfo, SQLite db) throws DecoderException, SQLException, ClassNotFoundException, IOException {
        this.targetMethod = new Method(targetMethodInfo, 0);
        this.db = db;
        methods = new ArrayList<Method>();
        int i = 1;
        for (MethodInfo methodInfo: db.getMethodInfo(targetMethodInfo.getParameterType(), targetMethodInfo.getReturnType())) {
            methods.add(new Method(methodInfo, i));
            i += 1;
        }
        makeJavaFiles();
    }

    public Boolean regist() {
        List<Length> lengths = calculateLength();
        if (lengths == null) {
            return  false;
        }
        try {
            for (Length length : lengths) {
                db.regist(length.getMethods().get(0), length.getMethods().get(1), length.getValue());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return true;
    }

    private List<Length> calculateLength() {
        service = Executors.newFixedThreadPool(THREAD_NUM);
        List<TMPFuture> futureList = new ArrayList<>();
        for (Method method: methods) {
            Future<String> future = executeGumtree(targetMethod, method);
            futureList.add(new TMPFuture(future, method));
        }
        service.shutdown();
        List<Length> lengths = new ArrayList<Length>();
        try {
            for (TMPFuture future: futureList) {
                String text = future.future.get();
                Method method = future.method;
                JsonParser parser = new JsonParser();
                JsonObject object = parser.parse(text).getAsJsonObject();
                JsonArray array = object.getAsJsonArray("actions");
                int size = array.size();
                if (size <= 40) {
                    System.out.println(size);
                    return null;
                }
                Length length = new Length(method.getInfo(), targetMethod.getInfo(), size, Util.getId(method.getInfo(), targetMethod.getInfo()));
                lengths.add(length);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return lengths;
    }

    private Future<String> executeGumtree(Method a, Method b) {
        String pathA = javaFilePath(a);
        String pathB = javaFilePath(b);
        CommandLine commandLine = new CommandLine();
        commandLine.processBuilder = new ProcessBuilder(getGumtreePath(), "jsondiff", pathA, pathB);
        Future<String> future = service.submit(commandLine);
        return future;
    }

    private void makeJavaFiles() {
        File newDir = new File("./tmp");
        newDir.mkdir();
        for (Method method: methods) {
            makeJavaFile(method);
        }
        makeJavaFile(targetMethod);
    }

    private void makeJavaFile(Method method) {
        File newFile = new File(javaFilePath(method));
        try {
            newFile.createNewFile();
            FileWriter writer = new FileWriter(newFile);
            writer.write(getCompleteSourceCode(method));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String javaFilePath(Method method) {
        return "./tmp/" + getClassName(method) + ".java";
    }

    private String getCompleteSourceCode(Method method) {
        return "class " + getClassName(method) + " {\n" + method.getInfo().getSourceCode() + "\n}";
    }

    private String getClassName(Method method) {
        return "Class" + method.getId();
    }

    private String getGumtreePath() {
        return "./gumtree/bin/gumtree";
    }
}
