package suggestion;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by matsumotojunnosuke on 2017/06/17.
 */
class SuggestLogic {

    final private int THREAD_NUM = 5;
    private ExecutorService service;
    private DataStore store;
    private HashMap<String, Integer> cache = new HashMap<>();
    private Boolean madeFileFlag = false;

    SuggestLogic(DataStore store) {
        this.store = store;
    }

    void next() {
        calculateLength(store.getNowMethod());
        int maxLength = 0;
        Method nextMethod = null;
        for (Method method: store.getUncheckedMethods()) {
            int length = 0;
            for (Method m: store.getCheckedMethods()) {
                length += getCache(method, m);
            }
            System.out.println(method.getId() + " length: " + length);
            if (maxLength < length) {
                nextMethod = method;
                maxLength = length;
            }
        }
        System.out.println("next: " + nextMethod.getId() + "\n");
        store.next(nextMethod);
    }

    private void calculateLength(Method sourceMethod) {
        class TMPFuture {
            private Future<String> future;
            private Method method;
            private TMPFuture(Future<String> future, Method method) {
                this.future = future;
                this.method = method;
            }
        }
        if (!madeFileFlag) makeJavaFiles();
        service = Executors.newFixedThreadPool(THREAD_NUM);
        List<TMPFuture> futureList = new ArrayList<>();
        for (Method method: store.getAllMethods()) {
            if (getCache(method, sourceMethod) != null) continue;
            Future<String> future = executeGumtree(sourceMethod, method);
            futureList.add(new TMPFuture(future, method));
        }
        service.shutdown();
        try {
            for (TMPFuture future: futureList) {
                String text = future.future.get();
                Method method = future.method;
                JsonParser parser = new JsonParser();
                JsonObject object = parser.parse(text).getAsJsonObject();
                JsonArray array = object.getAsJsonArray("actions");
                int length = array.size();
                System.out.println("(" + sourceMethod.getId() + ", " + method.getId() + "): " + length);
                setCache(method, sourceMethod, length);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
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
        for (Method method: store.getAllMethods()) {
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
        madeFileFlag = true;
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

    private void setCache(Method a, Method b, int length) {
        String key = getKey(a, b);
        cache.put(key, length);
    }

    private Integer getCache(Method a, Method b) {
        String key = getKey(a, b);
        return cache.get(key);
    }

    private String getKey(Method a, Method b) {
        int min = Math.min(a.getId(), b.getId());
        int max = Math.max(a.getId(), b.getId());
        return String.valueOf(min) + "-" + String.valueOf(max);
    }

    private String getGumtreePath() {
        return "./gumtree/bin/gumtree";
    }
}
