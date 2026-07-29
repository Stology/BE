package com.stology.be.domain.study.entity;

import com.stology.be.domain.member.entity.Member;
import com.stology.be.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 질문 읽음 기록. (회원 × 질문) N:M을 풀어낸 중간 테이블이다.
 * 행의 존재 자체가 "이 회원이 이 질문을 읽었다"는 뜻이고, 행이 없으면 안읽음(알림함에 노출)이다.
 * 읽은 시각은 행 생성 시각과 같다.
 * 같은 회원이 같은 질문을 여러 번 열어도 행이 중복되지 않도록 유니크 제약을 둔다.
 */
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(
        name = "question_read",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_question_read",
                columnNames = {"member_id", "question_id"}
        )
)
public class QuestionRead extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question;
}
