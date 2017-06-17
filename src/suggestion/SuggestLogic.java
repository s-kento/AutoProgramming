package suggestion;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.util.HashMap;

/**
 * Created by matsumotojunnosuke on 2017/06/17.
 */
class SuggestLogic {

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
        if (!madeFileFlag) makeJavaFiles();
        for (Method method: store.getAllMethods()) {
            if (getCache(method, sourceMethod) != null ) continue;
            String text = executeGumtree(sourceMethod, method);
            JsonParser parser = new JsonParser();
            JsonObject object = parser.parse(text).getAsJsonObject();
            JsonArray array = object.getAsJsonArray("actions");
            int length = array.size();
            System.out.println("(" + sourceMethod.getId() + ", " + method.getId() + "): " + length);
            setCache(method, sourceMethod, length);
        }
    }

    private String executeGumtree(Method a, Method b) {
        String pathA = javaFilePath(a);
        String pathB = javaFilePath(b);
        String command = "/Users/matsumotojunnosuke/.bin/gum/bin/gumtree jsondiff " + pathA + " " + pathB;
        Runtime runtime = Runtime.getRuntime();

        Process process;
        try {
            process = runtime.exec(command);
            process.waitFor();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String text = "";
            while (true) {
                String line = bufferedReader.readLine();
                if (line == null) break;
                text += "\n" + line;
            }
            bufferedReader.close();
            process.destroy();
            return text;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "";
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
        return "class " + getClassName(method) + " {\n" + method.getSourceCode() + "\n}";
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
}
