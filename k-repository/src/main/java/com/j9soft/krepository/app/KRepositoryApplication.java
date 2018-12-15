package com.j9soft.krepository.app;

import com.j9soft.krepository.app.logic.ProcessingTopology;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KRepositoryApplication {

    @Autowired
    private ProcessingTopology processingTopology;

    public static void main(String[] args) {

        SpringApplication.run(KRepositoryApplication.class, args);

        /* @TODO https://kafka.apache.org/21/documentation/streams/developer-guide/write-streams.html

        // Java 8+, using lambda expressions
        streams.setUncaughtExceptionHandler((Thread thread, Throwable throwable) -> {
            // here you should examine the throwable/exception and perform an appropriate action!
        });
        */

        /* @TODO
        https://kafka.apache.org/21/documentation/streams/tutorial

        // attach shutdown handler to catch control-c
        Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
            @Override
            public void run() {
                streams.close();
                latch.countDown();
            }
        });
         */
    }

}

