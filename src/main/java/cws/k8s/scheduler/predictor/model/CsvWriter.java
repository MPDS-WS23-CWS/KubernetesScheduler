package cws.k8s.scheduler.predictor.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class CsvWriter {
    public void writeTestAccuracyIntoCSV(String processName, double rSquaredValue, String path) {
        String filePath = path;

        try (FileWriter fw = new FileWriter(filePath, true);
             PrintWriter pw = new PrintWriter(fw)) {

            File file = new File(filePath);
            if (!file.exists() || file.length() == 0) {
                pw.println("ProcessName,R_Squared");
            }

            pw.println(processName + "," + rSquaredValue);

        } catch (IOException e) {
            System.err.println("An error occurred while writing to the CSV file: " + e.getMessage());
        }
    }
}
