package com.stology.be.domain.study.entity;

import com.stology.be.domain.member.entity.Member;
import com.stology.be.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Question extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id")
    private Study study;

    /** 작성자. 소유권 판별은 이 FK로 하고, 화면에 노출할 이름은 작성 시점 스냅샷인 memberName을 쓴다. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private String title;
    
    private String content;
    
    private String memberName;
    
    @Builder.Default
    private Integer answerCount = 0;

    private Boolean isAttached;

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void increaseAnswerCount() {
        this.answerCount = this.answerCount + 1;
    }

    public void decreaseAnswerCount() {
        this.answerCount = Math.max(0, this.answerCount - 1);
    }

    public void updateAttached(boolean attached) {
        this.isAttached = attached;
    }
}
