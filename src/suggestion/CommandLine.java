package suggestion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

/**
 * Created by matsumotojunnosuke on 2017/06/19.
 */
public class CommandLine implements Callable<String> {

    public String command = "";
    ProcessBuilder processBuilder;

    @Override
    public String call() throws Exception {
        try {
            Process process = processBuilder.start();
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
}
