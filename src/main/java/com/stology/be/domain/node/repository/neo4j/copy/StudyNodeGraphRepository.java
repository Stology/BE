package com.stology.be.domain.node.repository.neo4j.copy;

import com.stology.be.domain.node.entity.neo4j.copy.StudyNodeGraphNode;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudyNodeGraphRepository
        extends Neo4jRepository<StudyNodeGraphNode, Long> {

    Optional<StudyNodeGraphNode> findByStudyNodeId(
            Long studyNodeId
    );

    @Query("""
            MATCH (:TemplateStudy {studyId: $studyId})
                  -[:HAS_NODE]->
                  (studyNode:StudyNode)
            RETURN studyNode
            ORDER BY studyNode.studyNodeId
            """)
    List<StudyNodeGraphNode> findAllByStudyId(
            @Param("studyId") Long studyId
    );

    @Query("""
            MATCH (:TemplateStudy {studyId: $studyId})
                  -[:HAS_NODE]->
                  (studyNode:StudyNode {studyNodeId: $studyNodeId})
            RETURN studyNode
            """)
    Optional<StudyNodeGraphNode> findByStudyIdAndStudyNodeId(
            @Param("studyId") Long studyId,
            @Param("studyNodeId") Long studyNodeId
    );

    @Query("""
            MATCH (:TemplateStudy {studyId: $studyId})
                  -[:HAS_NODE]->
                  (studyNode:StudyNode)

            OPTIONAL MATCH (studyNode)
                  -[similarRelation:SIMILAR_TO]->
                  (relatedNode:StudyNode)

            RETURN studyNode,
                   collect(similarRelation),
                   collect(relatedNode)
            """)
    List<StudyNodeGraphNode> findAllWithRelationsByStudyId(
            @Param("studyId") Long studyId
    );

    @Query("""
            MATCH (source:StudyNode {studyNodeId: $sourceStudyNodeId})
            MATCH (target:StudyNode {studyNodeId: $targetStudyNodeId})

            MERGE (source)-[similarRelation:SIMILAR_TO]->(target)
            SET similarRelation.relation = $relation
            """)
    void createSimilarRelation(
            @Param("sourceStudyNodeId") Long sourceStudyNodeId,
            @Param("targetStudyNodeId") Long targetStudyNodeId,
            @Param("relation") String relation
    );

    @Query("""
            MATCH (source:StudyNode {studyNodeId: $sourceStudyNodeId})
                  -[similarRelation:SIMILAR_TO]->
                  (target:StudyNode {studyNodeId: $targetStudyNodeId})
            DELETE similarRelation
            """)
    void deleteSimilarRelation(
            @Param("sourceStudyNodeId") Long sourceStudyNodeId,
            @Param("targetStudyNodeId") Long targetStudyNodeId
    );
}