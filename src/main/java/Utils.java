import java.io.*;
import java.util.Scanner;

public class Utils {
    // runs arbitrary command; returns the output, or null if failed
    public static String runCommand(String command) {
        try {
            // the 'sh -c ..' is required to pass runEchoHelloWorld and runMvn tests
            Process pr = Runtime.getRuntime().exec(new String[] {"sh", "-c", command});
            BufferedReader input = new BufferedReader(new InputStreamReader(
                    pr.getInputStream()));

            String result = "";
            String line;

            while ((line = input.readLine()) != null) {
                result += line + "\n";
            }

            int exitVal = pr.waitFor();
            if (exitVal == 0)
                return result;
            else
                return null;
        } catch (IOException | InterruptedException e) {
            return null;
        }
    }

    public static String readFileToString(String filename) {
        try {
            // https://stackoverflow.com/a/3403112/1543290
            return new Scanner(new File(filename)).useDelimiter("\\Z").next();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }


}
