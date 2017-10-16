package de.akull.bhmm;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.*;

/**
 * Logfile formatting.
 * <p/>
 * DateFormat - ClassName.MethodName - [Level] Message.
 *
 * @author akullpp@gmail.com
 * @version 1.0
 * @since 19.04.13
 */
class Format extends Formatter {
    private static final DateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");

    /**
     * Sets sensible names for levels.
     * <p/>
     * FINE = INFO
     * FINER = DEBUG
     * FINEST = VERBOSE
     *
     * @param lvl Original level.
     * @return New level name.
     */
    private String customLevel(Level lvl) {
        if (lvl == Level.FINE) {
            return "INFO";
        } else if (lvl == Level.FINER) {
            return "DEBUG";
        } else if (lvl == Level.FINEST) {
            return "VERBOSE";
        } else {
            return lvl.toString();
        }
    }

    /**
     * Assembles the format string.
     *
     * @param record Record containing every information about the logging event.
     * @return String to be printed int he logfile.
     */
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();
        sb.append(df.format(new Date(record.getMillis()))).append(" - ");
        sb.append(record.getSourceClassName());
        sb.append(".").append(record.getSourceMethodName()).append(" - ");
        sb.append("[").append(customLevel(record.getLevel())).append("] ");
        sb.append(formatMessage(record));
        sb.append("\n");

        return sb.toString();
    }
}

/**
 * Main class.
 *
 * @author akullpp@gmail.com
 * @version 1.0
 * @since 19.04.13
 */
public class Main {

    /**
     * Main method.
     *
     * @param args Logging Level FINE, FINER or FINEST.
     */
    public static void main(String[] args) {
        FileHandler fh;
        Logger l;
        Formatter f;
        Properties p;

        try {
            p = new Properties();
            p.load(new FileInputStream("config.properties"));

            Level lvl = (args.length == 1) ? Level.parse(args[0]) : Level.FINE;
            fh = new FileHandler(p.getProperty("log"));
            fh.setFormatter(new Format());

            l = Logger.getLogger(Main.class.getName());
            l.setUseParentHandlers(false);
            l.setLevel(lvl);
            l.addHandler(fh);

            BHMM bhmm = new BHMM(l, p);
            bhmm.run();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
