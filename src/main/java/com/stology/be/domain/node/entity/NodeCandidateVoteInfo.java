package com.stology.be.domain.node.entity;

import com.stology.be.domain.member.entity.Member;
import com.stology.be.domain.node.enums.VoteType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "node_candidate_vote_info",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_candidate_member_vote",
                        columnNames = {
                                "node_candidate_id",
                                "member_id"
                        }
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class NodeCandidateVoteInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "node_candidate_vote_id")
    private Long id;

    //어떤 노드 후보인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "node_candidate_id", nullable = false)
    private NodeCandidate nodeCandidate;

    //투표 참여자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "vote_type", nullable = false)
    private VoteType voteType;



    public void updateVote(VoteType voteType) {
        this.voteType = voteType;
    }
}