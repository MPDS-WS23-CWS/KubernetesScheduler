package cws.k8s.scheduler;

import cws.k8s.scheduler.predictor.domain.SimpleProfiler;
import cws.k8s.scheduler.rest.ProvenanceRestClient;
import cws.k8s.scheduler.predictor.model.PreProcessor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.info.BuildProperties;

import jakarta.annotation.PostConstruct;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@SpringBootApplication
@Slf4j
public class Main {

    private final BuildProperties buildProperties;

//    @Autowired
//    private SimpleProfiler simpleProfiler;


    Main( @Autowired BuildProperties buildProperties ) {
        this.buildProperties = buildProperties;
    }

    public static void main(String[] args) {
        if( System.getenv( "SCHEDULER_NAME" ) == null || System.getenv( "SCHEDULER_NAME" ).isEmpty() ){
            throw new IllegalArgumentException( "Please define environment variable: SCHEDULER_NAME" );
        }
        SpringApplication.run(Main.class, args);
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




//     @Scheduled(fixedRate = 10000)
//     public void scheduledProvenanceDataFetch() {
//
//        ProvenanceRestClient provClient = new ProvenanceRestClient();
//        log.info(provClient.getProvenanceData().toString());
//
//        // Create PreProcessor object to process data and fit models
//        //PreProcessor preProcessor = new PreProcessor();
//        PreProcessor preProcessor = new PreProcessor(simpleProfiler);
//
//        preProcessor.splitData(provClient.getProvenanceData());
//
//     }

    @Bean
    // avoid DataBufferLimitException for provenance storage
    public WebClient webClient() {
        final int size = 256 * 1024 * 1024;
        final ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(size))
                .build();
        return WebClient.builder()
                .exchangeStrategies(strategies)
                .build();
    }

}

