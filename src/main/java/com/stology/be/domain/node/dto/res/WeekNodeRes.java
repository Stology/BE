package com.stology.be.domain.node.dto.res;

import com.stology.be.domain.node.entity.StudyNode;

import java.util.List;

public record WeekNodeRes(
        List<WeekNodeInfo> nodes
) {

    public record WeekNodeInfo(
            Long studyNodeId,
            String title,
            Integer activeLevel,
            Integer activationWeek
    ) {

        public static WeekNodeInfo from(StudyNode studyNode) {
            return new WeekNodeInfo(
                    studyNode.getId(),
                    studyNode.getTitle(),
                    studyNode.getActiveLevel(),
                    studyNode.getActivationWeek()
            );
        }
    }
}