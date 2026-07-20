package com.stology.be.domain.node.repository.neo4j;

import com.stology.be.domain.node.entity.neo4j.TemplateGraphNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TemplateGraphRepository
        extends Neo4jRepository<TemplateGraphNode, Long> {

    /*
     * templateId가 @Id이므로 아래 기본 메서드는 이미 제공된다.
     *
     * save(templateGraphNode)
     * findById(templateId)
     * existsById(templateId)
     * deleteById(templateId)
     */

    Optional<TemplateGraphNode> findByTemplateId(
            Long templateId
    );

    @Query("""
            MATCH (template:Template {templateId: $templateId})
            MATCH (templateNode:TemplateNode {
                templateNodeId: $templateNodeId
            })
            MERGE (template)-[:HAS_NODE]->(templateNode)
            """)
    void connectTemplateNode(
            @Param("templateId") Long templateId,
            @Param("templateNodeId") Long templateNodeId
    );

    @Query("""
            MATCH (template:Template {templateId: $templateId})

            OPTIONAL MATCH (template)
                -[:HAS_NODE]->
                (templateNode:TemplateNode)

            DETACH DELETE templateNode, template
            """)
    void deleteTemplateGraph(
            @Param("templateId") Long templateId
    );
}