package com.stology.be.domain.node.repository.neo4j;

import com.stology.be.domain.node.entity.neo4j.TemplateNodeGraphNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TemplateNodeGraphRepository
        extends Neo4jRepository<TemplateNodeGraphNode, Long> {

    Optional<TemplateNodeGraphNode> findByTemplateNodeId(
            Long templateNodeId
    );

    /**
     * 특정 Template에 속한 TemplateNode 전체 조회.
     * RELATED_TO 관계는 조회하지 않는다.
     */
    @Query("""
            MATCH (:Template {templateId: $templateId})
                -[:HAS_NODE]->
                (templateNode:TemplateNode)

            RETURN templateNode
            ORDER BY templateNode.templateNodeId
            """)
    List<TemplateNodeGraphNode> findAllByTemplateId(
            @Param("templateId") Long templateId
    );

    /**
     * 특정 Template에 속한 노드와
     * 각 노드의 RELATED_TO 관계까지 조회한다.
     */
    @Query("""
            MATCH (:Template {templateId: $templateId})
                -[:HAS_NODE]->
                (templateNode:TemplateNode)

            OPTIONAL MATCH (templateNode)
                -[relatedRelation:RELATED_TO]->
                (relatedNode:TemplateNode)

            RETURN templateNode,
                   collect(relatedRelation),
                   collect(relatedNode)
            """)
    List<TemplateNodeGraphNode> findAllWithRelationsByTemplateId(
            @Param("templateId") Long templateId
    );

    /**
     * 특정 Template 안에 속한 단일 TemplateNode와
     * 해당 노드의 관계까지 조회한다.
     */
    @Query("""
            MATCH (:Template {templateId: $templateId})
                -[:HAS_NODE]->
                (templateNode:TemplateNode {
                    templateNodeId: $templateNodeId
                })

            OPTIONAL MATCH (templateNode)
                -[relatedRelation:RELATED_TO]->
                (relatedNode:TemplateNode)

            RETURN templateNode,
                   collect(relatedRelation),
                   collect(relatedNode)
            """)
    Optional<TemplateNodeGraphNode> findWithRelations(
            @Param("templateId") Long templateId,
            @Param("templateNodeId") Long templateNodeId
    );

    /**
     * 두 TemplateNode 사이에 RELATED_TO 관계를 생성한다.
     */
    @Query("""
            MATCH (source:TemplateNode {
                templateNodeId: $sourceTemplateNodeId
            })

            MATCH (target:TemplateNode {
                templateNodeId: $targetTemplateNodeId
            })

            MERGE (source)-[relatedRelation:RELATED_TO]->(target)

            SET relatedRelation.relation = $relation
            """)
    void createRelation(
            @Param("sourceTemplateNodeId") Long sourceTemplateNodeId,
            @Param("targetTemplateNodeId") Long targetTemplateNodeId,
            @Param("relation") String relation
    );

    /**
     * 두 TemplateNode 사이의 RELATED_TO 관계를 삭제한다.
     */
    @Query("""
            MATCH (source:TemplateNode {
                templateNodeId: $sourceTemplateNodeId
            })
            -[relatedRelation:RELATED_TO]->
            (target:TemplateNode {
                templateNodeId: $targetTemplateNodeId
            })

            DELETE relatedRelation
            """)
    void deleteRelation(
            @Param("sourceTemplateNodeId") Long sourceTemplateNodeId,
            @Param("targetTemplateNodeId") Long targetTemplateNodeId
    );
}