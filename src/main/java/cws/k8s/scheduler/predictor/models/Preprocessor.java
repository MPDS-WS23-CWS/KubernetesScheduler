package estimators;

// TODO: Add these dependencies to pom.xml
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.javatuples.Septet;
import org.javatuples.Sextet;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

// TODO: Check how many argument we really need
// TODO: Also check estimateWith1DInput function if needed
public class LotaruA implements Estimator {
    public Septet<String, String, String, double[], double[], double[], double[]> estimateWith1DInput(String taskname, String resourceToPredict, double[] ids, double[] train_x, double[] train_y, double[] test_x, double[] test_y, double factor) {

        if (train_x.length != train_y.length) {
            throw new RuntimeException("Length of X should be equal to length Y");
        }

        var pearson = calculatePearson(train_x, train_y);


        if (pearson < 0.75 || Double.isNaN(pearson)) {
            DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics(train_y);

            double median_predicted = descriptiveStatistics.getPercentile(50);


            double[] toReturnError = new double[test_y.length];

            for (int i = 0; i < toReturnError.length; i++) {
                toReturnError[i] = Math.abs((median_predicted - test_y[i]) / test_y[i]);
            }

            double[] median_predicted_arr = new double[test_y.length];

            for (int i = 0; i < test_y.length; i++) {
                median_predicted_arr[i] = median_predicted;
            }



                return new Septet<>(taskname, "Lotaru-A", resourceToPredict, ids, median_predicted_arr, test_y, toReturnError);
        }

        // This is not needed and need to be replaced by implementation in LinearRegression.java
        /*
        ProcessBuilder processBuilder = new ProcessBuilder("python3", resolvePythonScriptPath("bayes.py"), StringUtils.join(train_x, ','), StringUtils.join(train_y, ','), StringUtils.join(test_x, ','), StringUtils.join(test_y, ','));
        processBuilder.redirectErrorStream(true);

        double predicted[] = new double[test_y.length];

        try {
            Process process = processBuilder.start();
            process.waitFor();
            List<String> results = readProcessOutput(process.getInputStream());

            for (String s : results) {
                if (s.contains("Prediction:")) {
                    predicted = Arrays.stream(s.split("\\[")[1].substring(0, s.split("\\[")[1].length() - 1).split(" ")).filter(str -> NumberUtils.isCreatable(str)).map(reg -> Double.valueOf(reg) * factor).mapToDouble(Double::valueOf).toArray();
                }
            }

            int exitCode = 0;

            exitCode = process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }

        double[] toReturnError = new double[test_y.length];

        for (int i = 0; i < predicted.length; i++) {
            toReturnError[i] = Math.abs((predicted[i] - test_y[i]) / test_y[i]);
            if (toReturnError[i] > 1) {
                System.out.println(i);
            }
        }

        return new Septet<>(taskname, "Lotaru-A", resourceToPredict, ids, predicted, test_y, toReturnError);

    }

    */

    // Not sure if we need this implementation of estimateWith2DInput
    public Sextet<String, String, String, Double, Double, Double> estimateWith2DInput(String taskname, String resourceToPredict, double[][] train_x, double[] train_y, double[][] test_x, double[] test_y, double factor) {


        if (train_x[0].length != test_x[0].length) {
            throw new RuntimeException("Length of X should be equal to length Y");
        }

        var pearson = 1.0; //calculatePearson(train_x, train_y);

        if (pearson < 0.8) {

            DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics(train_y);

            double median_predicted = descriptiveStatistics.getPercentile(50);

            return new Sextet<>(taskname, "LocallyJ", resourceToPredict, median_predicted, test_y[0], Math.abs((median_predicted - test_y[0]) / test_y[0]));
        }


        System.out.println(Arrays.deepToString(train_x).replaceAll("\\s+", ""));

        System.out.println(" " + Arrays.deepToString(train_x).replaceAll("\\s+", "") + " " + StringUtils.join(train_y, ',') + " " + Arrays.deepToString(test_x).replaceAll("\\s+", "") + " " + StringUtils.join(test_y, ','));

        //We again dont need this implementation
        /*

        ProcessBuilder processBuilder = new ProcessBuilder("python3", resolvePythonScriptPath("bayes_v2.py"), Arrays.deepToString(train_x).replaceAll("\\s+", ""), StringUtils.join(train_y, ','), Arrays.deepToString(test_x).replaceAll("\\s+", ""), StringUtils.join(test_y, ','));
        processBuilder.redirectErrorStream(true);

        double predicted = 0;

        try {
            Process process = processBuilder.start();
            process.waitFor();
            List<String> results = readProcessOutput(process.getInputStream());

            for (String s : results) {
                System.out.println(s);
                if (s.contains("Prediction:")) {
                    System.out.println(s);
                    predicted = Double.valueOf(s.split("\\[")[1].substring(0, s.split("\\[")[1].length() - 1)) * factor;
                }
            }

            int exitCode = 0;

            exitCode = process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new Sextet<>(taskname, "LocallyJ", resourceToPredict, predicted, test_y[0], Math.abs((predicted - test_y[0]) / test_y[0]));

    }

    */

   // We can completely use this function 
    public static double calculatePearson(double[] x, double[] y) {
        PearsonsCorrelation pearsonsCorrelation = new PearsonsCorrelation();

        return pearsonsCorrelation.correlation(x, y);
    }

    private static double calculateSpearman(double[] trainX, double[] trainY) {

        SpearmansCorrelation spearmansCorrelation = new SpearmansCorrelation();

        return spearmansCorrelation.correlation(trainX, trainY);

    }

    // Dont think we need these functions
    /*
    private static String resolvePythonScriptPath(String filename) {
        File file = new File("src/main/resources/" + filename);
        //File file = new File("" + filename);
        return file.getAbsolutePath();
    }

    private List<String> readProcessOutput(InputStream inputStream) throws IOException {
        try (BufferedReader output = new BufferedReader(new InputStreamReader(inputStream))) {
            return output.lines()
                    .collect(Collectors.toList());
        } */
    }
    
}
