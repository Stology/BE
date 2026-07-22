package com.stology.be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

@EnableJpaAuditing
@SpringBootApplication
@EnableScheduling
//@EnableNeo4jRepositories(basePackages = {"com.stology.be.domain.study.repository.neo4j"})
public class BeApplication {

    public static void main(String[] args) {
        SpringApplication.run(BeApplication.class, args);
    }

}
