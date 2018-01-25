import java.io.PrintStream;

public class Logging {

    public static final int CRITICAL = -1;
    public static final int ERROR = 0;
    public static final int INFO = 1;
    public static final int DEBUG = 2;

    private static PrintStream stream = System.out;
    public static int level = ERROR;

    public static void log(String msg, int lvl) {
        if (lvl <= level)
            stream.println(msg);
    }

    public static void debug(String msg) {
        log("[debug]: "+ msg, DEBUG);
    }

    public static void info(String msg) {

        log("[info]: "+ msg, INFO);
    }

    public static void error(String msg) {

        log("[ERROR]: "+ msg, ERROR);
    }

    public static void critical(String msg) {
        log("[CRITICAL]: "+ msg, CRITICAL);
    }


}

