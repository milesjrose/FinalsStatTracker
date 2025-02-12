package uk.ac.ed.inf;

// FileWriterUtil.java
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileWriterUtil {
    public static void writeStatsToFile(String stats) {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "stats_" + timestamp + ".txt";

        try (FileWriter writer = new FileWriter(fileName, true)) {
            writer.write("Timestamp: " + timestamp + "\n");
            writer.write("Stats:\n");
            writer.write(stats);
            writer.write("\n\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
