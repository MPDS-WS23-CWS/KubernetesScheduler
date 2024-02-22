package cws.k8s.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.info.BuildProperties;

import jakarta.annotation.PostConstruct;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

// Imports from original main
// TODO: Check, if better to split up functionality from main into classes because SpringBoot initializes everything.
/*
import domain.HistoricTask;
import estimators.*;
import helper.*;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.javatuples.Septet;
import org.javatuples.Triplet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
*/

@SpringBootApplication
@Slf4j
public class Main {

    private final BuildProperties buildProperties;

    @Autowired
    private InfraProfiler InfraProfiler;

    Main( @Autowired BuildProperties buildProperties ) {
        this.buildProperties = buildProperties;
    }

    public static void main(String[] args) {
        if( System.getenv( "SCHEDULER_NAME" ) == null || System.getenv( "SCHEDULER_NAME" ).isEmpty() ){
            throw new IllegalArgumentException( "Please define environment variable: SCHEDULER_NAME" );
        }
        SpringApplication.run(Main.class, args);
    }

    // TODO: Think of data structure to not only update the Nodes but also serve the final predicted runtime.
    @PostConstruct
    public void initProfiling() {
        runProfiling();
        parseFactor();
        updateNodeFactors();
    }



    @PostConstruct
    private void logVersion() {
        DateFormat format = new SimpleDateFormat("dd/MM/yy-HH:mm:ss");
        final long buildDate = Long.parseLong( buildProperties.get( "time" ) );
        Date date = new Date( buildDate );
        final String dateString = format.format( date );
        final String version = buildProperties.getVersion();
        String[] text = {
                "",
                "Common Workflow Scheduler for Kubernetes " + version + " (" + dateString + ")",
                "",
                "If you use the Common Workflow Scheduler for research purposes, please cite the following:",
                "Lehmann Fabian, Jonathan Bader, Friedrich Tschirpke, Lauritz Thamsen, and Ulf Leser.",
                "\"How Workflow Engines Should Talk to Resource Managers: A Proposal for a Common Workflow",
                "Scheduling Interface.\" In 2023 IEEE/ACM 23rd International Symposium on Cluster, Cloud and",
                "Internet Computing (CCGrid). Bangalore, India, 2023.",
                ""
        };

        int longest = 0;
        for ( String s : text ) {
            if ( s.length() > longest ) {
                longest = s.length();
            }
        }

        String info = "=".repeat( longest + 6 ) + "\n";
        for ( String s : text ) {
            info += "=  " + s + " ".repeat( longest - s.length() ) + "  =\n";
        }
        info += "=".repeat( longest + 6 ) + "\n";

        log.info( "\n\n\n" + info + "\n" );

    }

    // Just Step1: Shortened main from Lotaru
    // Of course we dont need second main. Need to be refactored.
    public class Main {


    public static void main(String[] args) throws IOException {

        DataProfile dataProfile = DataProfile.ALL_DATA;

        for (TargetMachine targetM : TargetMachine.values()) {

            for (Workflow wf : Workflow.values()) {

                System.out.println("Workflow: " + wf);

                for (int i = 0; i <= 1; i++) {

                    int crtNbr;

                    if (i == 1) {
                        crtNbr = 2;
                    } else {
                        crtNbr = 1;
                    }

                    executePrediction(wf, i, crtNbr, targetM, dataProfile);

                }


            }


        }


    }

    // We dont do localMachineTraining so we can exclude this
    // We can keep the target_tasks, we get them from the TaskProvenance class I would assume
    private static void executePrediction(Workflow workflow, int experiment_number, int control_number, TargetMachine targetMachine, DataProfile dataProfile) throws IOException {
        // To configure each run
        List<HistoricTask> localMachineTraining = CSVFileTaskReader.readCSVFile("execution_reports/local/results_" + workflow.toString().toLowerCase() + "/execution_report_" + "local" + ".csv").stream().filter(task -> task.getLabel().contains("train-" + control_number)).collect(Collectors.toList());

        List<HistoricTask> LocalMachineTrainingReducedCPUFreq;

        List<HistoricTask> target_tasks = CSVFileTaskReader.readCSVFile("execution_reports/" + targetMachine.toString().toLowerCase() + "/results_" + workflow.toString().toLowerCase() + "/execution_report_" + targetMachine.toString().toLowerCase() + ".csv").stream().filter(task -> task.getLabel().contains("test")).collect(Collectors.toList());


        if (targetMachine != TargetMachine.LOCAL) {

            LocalMachineTrainingReducedCPUFreq = CSVFileTaskReader.readCSVFile("execution_reports/wallyRedCpu/results_" + workflow.toString().toLowerCase() + "/execution_report_wallyRedCpu.csv").stream().filter(task -> task.getLabel().contains("train")).collect(Collectors.toList());

        } else {
            LocalMachineTrainingReducedCPUFreq = new ArrayList<>(localMachineTraining);
        }


        String to_predict = "Realtime";


        if (dataProfile == DataProfile.SAMPLED) {

        }

        // We can use these functions to group by Task Types and thus have the input data for the models per Type
        // We dont need the localTrainingReducedFrqencyGrouped

        // Bader: Entweder an der Stelle oder später noc zwischen den beiden Test Labeln unterscheider
        Map<String, List<HistoricTask>> localTrainingGrouped = localMachineTraining.stream()
                .collect(Collectors.groupingBy(HistoricTask::getTaskName));

        Map<String, List<HistoricTask>> localTrainingReducedFrquencyGrouped = LocalMachineTrainingReducedCPUFreq.stream()
                .collect(Collectors.groupingBy(HistoricTask::getTaskName));

        Map<String, List<HistoricTask>> targetTestGrouped = target_tasks.stream()
                .collect(Collectors.groupingBy(HistoricTask::getTaskName));


        ArrayList<Septet<String, String, String, double[], double[], double[], double[]>> estimates = new ArrayList<>();

        localTrainingGrouped.keySet().forEach(key -> {
            var trainingDataPoints = localTrainingGrouped.get(key);
            //var trainingDataPointsReducedFrequency = localTrainingReducedFrquencyGrouped.get(key);
            var testDataPoints = targetTestGrouped.get(key);


            // Lets evaluate what we need to keep from here.

            // Das sollte jetzt die Liste mit X/Y Paaren sein fürs Training
            var train_taskInputSizeUncompressed = trainingDataPoints.stream().map(task -> new Triplet<Double, Double, Double>(task.getTaskInputSizeUncompressed(), task.getRealtime(), task.getWorkflowInputSizeUncompressed())).collect(Collectors.toList());

            // Das sollte die Liste mit X/Y Paaren für das testSet sein
            var test_taskInputSizeUncompressed = testDataPoints.stream().map(task -> new Triplet<Double, Double, Double>(task.getTaskInputSizeUncompressed(), task.getRealtime(), task.getWorkflowInputSizeUncompressed())).collect(Collectors.toList());

            var x_train_lotare = train_taskInputSizeUncompressed.stream().map(task -> task.getValue0()).collect(Collectors.toList()).stream().limit(6).mapToDouble(d -> d).toArray();
            var y_train_lotare = train_taskInputSizeUncompressed.stream().map(task -> task.getValue1()).collect(Collectors.toList()).stream().limit(6).mapToDouble(d -> d).toArray();


            var x_test_lotare = test_taskInputSizeUncompressed.stream().map(task -> task.getValue0()).collect(Collectors.toList()).stream().mapToDouble(d -> d).toArray();
            var y_test_lotare = test_taskInputSizeUncompressed.stream().map(task -> task.getValue1()).collect(Collectors.toList()).stream().mapToDouble(d -> d).toArray();

            var lotare_id_WfInputSize = test_taskInputSizeUncompressed.stream().map(task -> task.getValue2()).collect(Collectors.toList()).stream().mapToDouble(d -> d).toArray();

            var train_taskRChar = trainingDataPoints.stream().map(task -> new Triplet<Double, Double, Double>(task.getRchar(), task.getRealtime(), task.getWorkflowInputSizeUncompressed())).collect(Collectors.toList());
            var test_taskRChar = testDataPoints.stream().map(task -> new Triplet<Double, Double, Double>(task.getRchar(), task.getRealtime(), task.getWorkflowInputSizeUncompressed())).collect(Collectors.toList());

            var x_train_rchar = train_taskRChar.stream().map(task -> task.getValue0()).collect(Collectors.toList()).stream().mapToDouble(d -> d).limit(5).toArray();
            var y_train_rchar = train_taskRChar.stream().map(task -> task.getValue1()).collect(Collectors.toList()).stream().mapToDouble(d -> d).limit(5).toArray();

            var x_test_rchar = test_taskRChar.stream().map(task -> task.getValue0()).collect(Collectors.toList()).stream().mapToDouble(d -> d).toArray();
            var y_test_rchar = test_taskRChar.stream().map(task -> task.getValue1()).collect(Collectors.toList()).stream().mapToDouble(d -> d).toArray();

            var rchar_id_WfInputSize = test_taskRChar.stream().map(task -> task.getValue2()).collect(Collectors.toList()).stream().mapToDouble(d -> d).toArray();

            System.out.println("Task:        " + key + ":");
            Estimator lotaruG = new LotaruG();

            if (targetMachine != TargetMachine.LOCAL) {
                estimates.add(lotaruG.estimateWith1DInput(key, to_predict, lotare_id_WfInputSize, x_train_lotare, y_train_lotare, x_test_lotare, y_test_lotare, BenchmarkHelper.defineFactor(targetMachine)));

            } else {
                estimates.add(lotaruG.estimateWith1DInput(key, to_predict, lotare_id_WfInputSize, x_train_lotare, y_train_lotare, x_test_lotare, y_test_lotare, 1));
            }

            // We dont need the baseline estimators
            /*
            Estimator onlineP = new Online(false, "OnlineP");
            Estimator onlineM = new Online(true, "OnlineM");
            Estimator naive = new Naive(true, "Naive");
            Estimator lotaruA = new LotaruA();
            Estimator perfect = new Perfect();
            

            estimates.add(onlineP.estimateWith1DInput(key, to_predict, lotare_id_WfInputSize, x_train_lotare, y_train_lotare, x_test_lotare, y_test_lotare, 1));

            estimates.add(onlineM.estimateWith1DInput(key, to_predict, lotare_id_WfInputSize, x_train_lotare, y_train_lotare, x_test_lotare, y_test_lotare, 1));

            estimates.add(naive.estimateWith1DInput(key, to_predict, lotare_id_WfInputSize, x_train_lotare, y_train_lotare, x_test_lotare, y_test_lotare, 1));

            double microbenchmarkLocal = BenchmarkHelper.medianBenchmarkValue(BenchmarkHelper.readBenchValue(key, TargetMachine.LOCAL, workflow).getRealtimes());
            double microbenchmarkTarget = BenchmarkHelper.medianBenchmarkValue(BenchmarkHelper.readBenchValue(key, targetMachine, workflow).getRealtimes());

            */

            double lotaruAFactor = microbenchmarkTarget / microbenchmarkLocal;
            if (lotaruAFactor == 1.0) {
                lotaruAFactor = BenchmarkHelper.estimateAverageForWorkflowMachine(workflow, targetMachine, localTrainingGrouped.keySet());
            }


            estimates.add(lotaruA.estimateWith1DInput(key, to_predict,lotare_id_WfInputSize, x_train_lotare, y_train_lotare, x_test_lotare, y_test_lotare, lotaruAFactor));

            // We dont need the perfect estimator.
            //estimates.add(perfect.estimateWith1DInput(key, to_predict, lotare_id_WfInputSize, x_train_lotare, y_train_lotare ,x_test_lotare, y_test_lotare, 1));
            //System.out.println("------------------------------");

        });

        try {

            // We dont need to write estimates to CSV. Maybe... but mainly they need to be passed to the Scheduler class field
            //WriteEstimatesToCSV.writeTasksToCSV("results/tasks_lotaru_" + targetMachine.toString().toLowerCase() + ".csv",targetMachine.toString().toLowerCase(), workflow, experiment_number, estimates);

            //Lotaru

            DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics(estimates.stream().filter(sextet -> sextet.getValue1().equalsIgnoreCase("Lotaru-A")).map(sextet -> sextet.getValue6()).flatMapToDouble(d -> DoubleStream.of(d)).toArray());

            double median_predicted_lotaruA = descriptiveStatistics.getPercentile(50);
            System.out.println("Lotaru-G median deviation: " + median_predicted_lotaruA);

            //WriteEstimatesToCSV.writeCompleteWorkflowToCSV("results/lotare-wf-" + targetMachine.toString().toLowerCase() + ".csv", workflow, experiment_number, dataProfile, targetMachine, targetMachine, "Lotaru-G", median_predicted_lotaruA, descriptiveStatistics.getSum());

            //Lotaru
            // TODO: Differentiate between LotaruA & LotaruG but basically we only need 1 Implementation and in our case thats LinearRegression
            descriptiveStatistics = new DescriptiveStatistics(estimates.stream().filter(sextet -> sextet.getValue1().equalsIgnoreCase("Lotaru-G")).map(sextet -> sextet.getValue6()).flatMapToDouble(d -> DoubleStream.of(d)).toArray());

            double median_predicted_lotaruG = descriptiveStatistics.getPercentile(50);
            System.out.println("Lotaru-A median deviation: " + median_predicted_lotaruG);

            // We dont need that
            // WriteEstimatesToCSV.writeCompleteWorkflowToCSV("results/lotare-wf-" + targetMachine.toString().toLowerCase() + ".csv", workflow, experiment_number, dataProfile, targetMachine, targetMachine, "Lotaru-A", median_predicted_lotaruG, descriptiveStatistics.getSum());

            
            // These are the other baseline. We dont need them.
            /*
            //OnlineM

            descriptiveStatistics = new DescriptiveStatistics(estimates.stream().filter(sextet -> sextet.getValue1().equalsIgnoreCase("OnlineM")).map(sextet -> sextet.getValue6()).flatMapToDouble(d -> DoubleStream.of(d)).toArray());

            double median_predicted_online_m = descriptiveStatistics.getPercentile(50);
            System.out.println("OnlineM median deviation: " + median_predicted_online_m);


            WriteEstimatesToCSV.writeCompleteWorkflowToCSV("results/lotare-wf-" + targetMachine.toString().toLowerCase() + ".csv", workflow, experiment_number, dataProfile, targetMachine, targetMachine, "OnlineM", median_predicted_online_m, descriptiveStatistics.getSum());

            //OnlineP

            descriptiveStatistics = new DescriptiveStatistics(estimates.stream().filter(sextet -> sextet.getValue1().equalsIgnoreCase("OnlineP")).map(sextet -> sextet.getValue6()).flatMapToDouble(d -> DoubleStream.of(d)).toArray());

            double median_predicted_online_p = descriptiveStatistics.getPercentile(50);
            System.out.println("OnlineP median deviation: " + median_predicted_online_p);


            WriteEstimatesToCSV.writeCompleteWorkflowToCSV("results/lotare-wf-" + targetMachine.toString().toLowerCase() + ".csv", workflow, experiment_number, dataProfile, targetMachine, targetMachine, "OnlineP", median_predicted_online_p, descriptiveStatistics.getSum());

            //Naive

            descriptiveStatistics = new DescriptiveStatistics(estimates.stream().filter(sextet -> sextet.getValue1().equalsIgnoreCase("Naive")).map(sextet -> sextet.getValue6()).flatMapToDouble(d -> DoubleStream.of(d)).toArray());

            double median_predicted_naive = descriptiveStatistics.getPercentile(50);
            System.out.println("Naive median deviation: " + median_predicted_naive);


            WriteEstimatesToCSV.writeCompleteWorkflowToCSV("results/lotare-wf-" + targetMachine.toString().toLowerCase() + ".csv", workflow, experiment_number, dataProfile, targetMachine, targetMachine, "Naive", median_predicted_naive, descriptiveStatistics.getSum());
            */

        } catch (
                IOException e) {
            e.printStackTrace();
        }
    }




}

}
