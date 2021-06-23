package dialogue;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.logging.*;

/**
 * Simplified logging commands
 */
public class EventLogger {
    private final Logger log = Logger.getLogger(this.getClass().getName());

    /**
     * Create a new logger for this class
     */
    public EventLogger() {
        setupLogger();
    }

    /**
     * Define the logging format and filehandler
     */
    private void setupLogger() {
        try {
            // Make dir if not exists
            String logPath = System.getProperty("user.home") + "/.config/cian/dialogue/";
            File logDir = new File(logPath);
            if (!logDir.exists()){
                log.info("Making config directory");
                logDir.mkdirs();
            }

            // Open the logfile
            FileHandler logFile = new FileHandler(logPath + "dialogue.log", true);

            // Single log output
            logFile.setFormatter(new SimpleFormatter() {
                @Override
                public String format(LogRecord lr) {
                    // Set the log file format to a one liner
                    return String.format("[%1$tF %1$tT] [%2$-4s] %3$s %n", new Date(lr.getMillis()),
                            lr.getLevel().getLocalizedName(), lr.getMessage());
                }
            });
            // Add events to log file with info level set
            log.addHandler(logFile);
            log.setLevel(Level.FINE);
        } catch (IOException e) {
            System.err.println("Couldn't open logfile: " + e);
            System.exit(1);
        }
    }

    /**
     * Log a new event
     *
     * @param message The string to log
     */
    public void info(String message) {
        log.log(Level.INFO, message);
    }

    /**
     * Log a new warning
     *
     * @param message The string to log
     */
    public void warning(String message) {
        log.log(Level.WARNING, message);
    }

    /**
     * Log a new error
     *
     * @param message The string to log
     */
    public void error(String message) {
        log.log(Level.SEVERE, message);
    }

}
