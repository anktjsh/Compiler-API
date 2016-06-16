/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compilerapi;

/**
 *
 * @author Aniket
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import tachyon.process.ProcessItem;

/**
 *
 * @author Aniket
 */
public class JavaFileManager {

    private static String JAVA_HOME;

    static {
        init();
    }

    private static List<String> options;

    public static void init() {
        options = getAvailableOptions();
        JAVA_HOME = getJavaHomeLocation();
        File f = new File(".cache");
        if (!f.exists()) {
            f.mkdirs();
        }
    }

    protected static String getJavaHomeLocation() {
        if (!options.isEmpty()) {
            return options.get(options.size() - 1);
        }
        return "";
    }

    private static List<String> getAvailableOptions() {
        String property = System.getProperty("os.name").toLowerCase();
        if (property.contains("windows")) {
            return windowList();
        } else {
            return macList();
        }
    }

    private static List<String> windowList() {
        ArrayList<String> al = new ArrayList<>();
        File f = new File("C:" + File.separator + "Program Files (x86)" + File.separator + "Java" + File.separator);
        if (f.exists()) {
            for (File fl : f.listFiles()) {
                if (fl.getName().substring(0, 3).equals("jdk")) {
                    al.add(fl.getAbsolutePath() + File.separator + "bin");
                }
            }
        }
        f = new File("C:" + File.separator + "Program Files" + File.separator + "Java" + File.separator);
        if (f.exists()) {
            for (File fl : f.listFiles()) {
                if (fl.getName().substring(0, 3).equals("jdk")) {
                    al.add(fl.getAbsolutePath() + File.separator + "bin");
                }
            }
        }
        return al;
    }

    private static List<String> macList() {
        File f = new File("/Library/Java/JavaVirtualMachines/");
        ArrayList<String> al = new ArrayList<>();
        if (f.exists()) {
            for (File fl : f.listFiles()) {
                if (fl.getName().substring(0, 3).equals("jdk")) {
                    al.add(f.getAbsolutePath() + File.separator + fl.getName() + "/Contents/Home/bin");
                }
            }
        }
        return al;
    }

    public static String getJavaHome() {
        return JAVA_HOME;
    }

    public static void setJavaHome(String s) {
        Path p = Paths.get(s);
        Path a = Paths.get(s + File.separator + "javac" + (OS.contains("win") ? ".exe" : ""));
        if (!Files.exists(p)) {
            throw new RuntimeException("Not a valid JDK Location");
        }
    }
    private static final String OS = System.getProperty("os.name").toLowerCase();
    private static JavaFileManager instance;

    public static JavaFileManager getIsolatedJavaFileManager() {
        if (instance == null) {
            instance = new JavaFileManager();
        }
        return instance;
    }

    public JavaFileManager() {
    }

    public void runIndividualFile(ProcessItem item, File f) {
        String name = getName(f);
        compileIsolatedFile(item, f);
        if (!item.isCancelled()) {
            ProcessBuilder pb = new ProcessBuilder(getRunFileString(name));
            pb.directory(new File(".cache"));
            try {
                Process start = pb.start();
                item.setName("Launching File " + f.getAbsolutePath());
                item.setProcess(start);
                OutputReader o;
                ErrorReader e;
                (new Thread(o = new OutputReader(start.getInputStream()))).start();
                (new Thread(e = new ErrorReader(start.getErrorStream()))).start();
                item.setError(e);
                item.setReader(o);
                int exitValue = start.waitFor();
                if (exitValue != 0) {
                    item.cancel();
                    item.setExitValue(exitValue);
                    item.setErrorMessage(item.getName() + " Failed");
                }
            } catch (IOException | InterruptedException e) {
            }
        }
    }

    private String getName(File f) {
        return f.getName().substring(0, f.getName().lastIndexOf(".java"));
    }

    private static String[] getRunFileString(String name) {
        String[] one;
        if (OS.contains("win")) {
            one = new String[]{"\"" + getJavaHome() + File.separator + "java\"", name};
        } else {
            one = new String[]{getJavaHome() + File.separator + "java", name};
        }
        return one;
    }

    private void compileIsolatedFile(ProcessItem item, File pro) {
        if (!item.isCancelled()) {
            ProcessBuilder pb;
            if (OS.contains("mac")) {
                pb = new ProcessBuilder(getMacCompileFileString(pro));
            } else {
                pb = new ProcessBuilder(getWindowsCompileFileString(pro));
            }
            pb.directory(pro.getParentFile());
            try {
                Process start = pb.start();
                item.setName("Compiling File " + pro.getAbsolutePath());
                item.setProcess(start);
                OutputReader o;
                ErrorReader e;
                (new Thread(o = new OutputReader(start.getInputStream()))).start();
                (new Thread(e = new ErrorReader(start.getErrorStream()))).start();
                item.setError(e);
                item.setReader(o);
                int exitValue = start.waitFor();
                if (exitValue != 0) {
                    item.cancel();
                    item.setExitValue(exitValue);
                    item.setErrorMessage(item.getName() + " Failed");
                }
            } catch (IOException | InterruptedException e) {
            }
        }
    }

    private static String[] getWindowsCompileFileString(File a) {
        String[] one = new String[]{"\"" + getJavaHome() + File.separator + "javac\"", a.getName(), "-d", new File(".cache").getAbsolutePath()};
//        String one = "\"" + getJavaHome()
//                + File.separator + "javac\"" + " "
//                + a.getName()
//                + " -d " + new File(".cache").getAbsolutePath();
        return one;
    }

    private static String[] getMacCompileFileString(File f) {
        String[] one = new String[]{getJavaHome() + File.separator + "javac", f.getName(), "-d", new File(".cache").getAbsolutePath()};
//        String one = getJavaHome()
//                + File.separator + "javac" + " "
//                + f.getName()
//                + " -d " + new File(".cache").getAbsolutePath();
        return one;
    }

    public static class OutputReader implements Runnable {

        private final InputStream strea;
        private final StringBuilder sb = new StringBuilder();

        public OutputReader(InputStream is) {
            strea = is;
        }

        public String get() {
            return sb.toString();
        }

        @Override
        public void run() {
            InputStreamReader isr = new InputStreamReader(strea);
            BufferedReader br = new BufferedReader(isr);
            int value;
            try {
                while ((value = br.read()) != -1) {
                    char c = (char) value;
                    sb.append(c);
                }
            } catch (IOException ex) {
            }
        }
    }

    public static class ErrorReader implements Runnable {

        private final InputStream strea;
        private final StringBuilder sb = new StringBuilder();

        public ErrorReader(InputStream is) {
            strea = is;
        }

        public String get() {
            return sb.toString();
        }

        @Override
        public void run() {
            InputStreamReader isr = new InputStreamReader(strea);
            BufferedReader br = new BufferedReader(isr);
            int value;
            try {
                while ((value = br.read()) != -1) {
                    char c = (char) value;
                    sb.append(c);
                }
            } catch (IOException ex) {
            }
        }
    }

}
