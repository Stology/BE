package com.stology.be.global.config;

import jakarta.persistence.EntityManagerFactory;
import org.neo4j.driver.Driver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.neo4j.core.DatabaseSelectionProvider;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.data.neo4j.core.mapping.Neo4jMappingContext;
import org.springframework.data.neo4j.core.transaction.Neo4jTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class TransactionConfig {

    /**
     * MySQL / JPA용 기본 트랜잭션 매니저.
     *
     * JPA Repository가 기본적으로 찾는 이름이
     * transactionManager이므로 반드시 이 이름으로 등록한다.
     */
    @Bean(name = "transactionManager")
    @Primary
    public PlatformTransactionManager transactionManager(
            EntityManagerFactory entityManagerFactory
    ) {
        return new JpaTransactionManager(
                entityManagerFactory
        );
    }

    /**
     * Neo4j 전용 트랜잭션 매니저.
     */
    @Bean(name = "neo4jTransactionManager")
    public Neo4jTransactionManager neo4jTransactionManager(
            Driver driver,
            DatabaseSelectionProvider databaseSelectionProvider
    ) {
        return new Neo4jTransactionManager(
                driver,
                databaseSelectionProvider
        );
    }

    /**
     * Neo4jTemplate에 Neo4jTransactionManager를 명시적으로 연결한다.
     */
    @Bean(name = "neo4jTemplate")
    public Neo4jTemplate neo4jTemplate(
            Neo4jClient neo4jClient,
            Neo4jMappingContext neo4jMappingContext,
            Neo4jTransactionManager neo4jTransactionManager
    ) {
        return new Neo4jTemplate(
                neo4jClient,
                neo4jMappingContext,
                neo4jTransactionManager
        );
    }
}