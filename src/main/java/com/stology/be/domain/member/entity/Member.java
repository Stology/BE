package com.stology.be.domain.member.entity;

import com.stology.be.domain.member.enums.SocialType;
import com.stology.be.domain.node.entity.NodeCandidateVoteInfo;
import com.stology.be.global.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    private SocialType socialType;

    @NotBlank
    private String socialUid;

    @Email
    @NotBlank
    private String email;



    @OneToMany(
            mappedBy = "member",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<NodeCandidateVoteInfo> nodeCandidateVoteInfo = new ArrayList<>();




    public void softDelete() {
        setDeletedAt(LocalDateTime.now());
    }

    public void restore() {
        setDeletedAt(null);
    }
}
