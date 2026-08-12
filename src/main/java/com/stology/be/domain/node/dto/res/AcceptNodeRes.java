package com.stology.be.domain.node.dto.res;
import java.util.List;

public record AcceptNodeRes(

        List<AcceptInfo> acceptInfos

) {

    public static AcceptNodeRes from(
            List<AcceptInfo> acceptInfos
    ) {
        return new AcceptNodeRes(acceptInfos);
    }

    public record AcceptInfo(

            Long studyNodeId,
            Long nodeCandidateId,
            boolean success

    ) {

        public static AcceptInfo of(
                Long studyNodeId,
                Long nodeCandidateId
        ) {
            return new AcceptInfo(
                    studyNodeId,
                    nodeCandidateId,
                    true
            );
        }
    }
}