package com.stology.be.domain.node.entity.neo4j;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.*;

import java.util.HashSet;
import java.util.Set;

@Node("Template")
@Getter
@NoArgsConstructor
public class TemplateGraphNode {

    @Id
    private Long templateId;


    @Relationship(type = "HAS_NODE", direction = Relationship.Direction.OUTGOING)
    private Set<TemplateNodeGraphNode> templateNodes = new HashSet<>();

    public TemplateGraphNode(Long templateId) {
        this.templateId = templateId;
    }

    public void addTemplateNode(
            TemplateNodeGraphNode templateNode
    ) {
        this.templateNodes.add(templateNode);
    }
}