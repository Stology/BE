package com.stology.be.domain.node.repository.neo4j.copy;


import com.stology.be.domain.node.entity.neo4j.copy.TemplateStudyGraphNode;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TemplateStudyGraphRepository
        extends Neo4jRepository<TemplateStudyGraphNode, Long> {

    Optional<TemplateStudyGraphNode> findByStudyId(Long studyId);

    boolean existsByStudyId(Long studyId);

    @Query("""
            MATCH (study:TemplateStudy {studyId: $studyId})
            OPTIONAL MATCH (study)-[:HAS_NODE]->(studyNode:StudyNode)
            DETACH DELETE studyNode, study
            """)
    void deleteStudyGraph(
            @Param("studyId") Long studyId
    );

    @Query("""
            MATCH (study:TemplateStudy {studyId: $studyId})
            MATCH (studyNode:StudyNode {studyNodeId: $studyNodeId})
            MERGE (study)-[:HAS_NODE]->(studyNode)
            """)
    void connectStudyNode(
            @Param("studyId") Long studyId,
            @Param("studyNodeId") Long studyNodeId
    );
}