import java.io.IOException;
import java.util.Date;
import java.util.logging.*;

public class EventLogger {
    private final Logger log = Logger.getLogger(this.getClass().getName());

    public EventLogger() {
        setupLogger();
    }

    /**
     * Define the logging format and filehandler
     */
    private void setupLogger() {
        try {
            // Open the logfile
            FileHandler log_file = new FileHandler("dialogue.log", true);

            // Single log output
            log_file.setFormatter(new SimpleFormatter() {
                @Override
                public String format(LogRecord lr) {
                    // Set the log file format to a one liner
                    return String.format("[%1$tF %1$tT] [%2$-4s] %3$s %n", new Date(lr.getMillis()),
                            lr.getLevel().getLocalizedName(), lr.getMessage());
                }
            });
            // Add events to log file with info level set
            log.addHandler(log_file);
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
