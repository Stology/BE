package com.stology.be.domain.study.entity;

import com.stology.be.domain.member.entity.Member;
import com.stology.be.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Answer extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question;

    /** 작성자. 소유권 판별은 이 FK로 하고, 화면에 노출할 이름은 작성 시점 스냅샷인 memberName을 쓴다. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private String content;

    private String memberName;

    /**
     * 질문 작성자가 이 답글을 읽은 시각. NULL이면 아직 안 읽음.
     * 답글 알림을 받는 대상이 질문 작성자 1명뿐이라 (회원 × 답글) N:M이 아니어서
     * 별도 읽음 테이블 없이 컬럼으로 둔다. 질문 읽음은 대상이 스터디원 전원이라
     * {@link QuestionRead} 테이블로 관리한다.
     *
     * <p>boolean이 아니라 시각인 이유: 알림함이 "읽은 알림을 자정까지만 흐리게 유지하고 다음날 제거"하므로
     * 오늘 읽은 것과 어제 읽은 것을 구분해야 한다. 덕분에 정리 배치 없이 조회 조건만으로 처리된다
     * ({@code read_at_by_asker IS NULL OR read_at_by_asker >= 오늘 자정}).
     */
    private LocalDateTime readAtByAsker;

    public void updateContent(String content) {
        this.content = content;
    }
}
