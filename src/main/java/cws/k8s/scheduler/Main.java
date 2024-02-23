package cws.k8s.scheduler;

import cws.k8s.scheduler.rest.ProvenanceRestClient;

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

    // @Autowired
    // private InfraProfiler InfraProfiler;

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
    // @PostConstruct
    // public void initProfiling() {
    //     runProfiling();
    //     parseFactor();
    //     updateNodeFactors();
    // }



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


        // Needs to call both getProvenanceData and Preprocessor periodically to directly process new map entries.
        // just for testing:
        ProvenanceRestClient provClient = new ProvenanceRestClient();
        log.info(provClient.getProvenanceData().toString());
    }

}
