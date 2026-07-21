package com.stology.be.domain.node.repository.neo4j.copy;


import com.stology.be.domain.node.dto.StudyNodePromptDto;
import com.stology.be.domain.node.entity.neo4j.copy.TemplateStudyGraphNode;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
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

    // 스터디 노드 안 id와 제목들을 List로 반환
    @Query("""
    MATCH (:TemplateStudy {
        studyId: $studyId,
        templateId: $templateId
    })
          -[:HAS_NODE]->
          (node:StudyNode)

    RETURN
        node.studyNodeId AS studyNodeId,
        node.title AS title
    ORDER BY node.studyNodeId
    """)
    List<StudyNodePromptDto> findPromptNodes(
            @Param("studyId") Long studyId,
            @Param("templateId") Long templateId
    );
}