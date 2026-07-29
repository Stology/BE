package com.stology.be.domain.inquiry.service;

import com.stology.be.domain.inquiry.repository.InquiryReadRepository;
import com.stology.be.domain.inquiry.repository.InquiryReplyRepository;
import com.stology.be.domain.inquiry.repository.InquiryRepository;
import com.stology.be.domain.member.repository.MemberRepository;
import com.stology.be.domain.study.entity.QuestionRead;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 알림함용 읽음 처리. 별도 API 없이 질문 상세 조회의 부수 효과로 실행된다
 * (알림함의 "질문 보기"와 "답글 보기"가 둘 다 질문 상세로 들어오므로 두 경로가 한 번에 커버된다).
 *
 * <p>질문 읽음은 스터디원 각자의 상태라 {@link QuestionRead} 행으로 남기고,
 * 답글 읽음은 대상이 질문 작성자 1명뿐이라 answer의 컬럼을 갱신한다.
 *
 * <p>상세 조회는 readOnly 트랜잭션이므로 REQUIRES_NEW로 쓰기 트랜잭션을 따로 열어 처리한다.
 * 이때 외부 트랜잭션의 커넥션을 물고 있는 상태에서 하나를 더 빌리므로, 처리할 게 있을 때만 호출해야 한다
 * (판단은 호출부가 외부 트랜잭션 안에서 미리 끝낸다). 실패해도 조회 응답은 나가야 하므로 예외도 호출부에서 흡수한다.
 */
@Service
@RequiredArgsConstructor
public class ReadService {

    private final InquiryReadRepository inquiryReadRepository;
    private final InquiryReplyRepository inquiryReplyRepository;
    private final InquiryRepository inquiryRepository;
    private final MemberRepository memberRepository;

    /**
     * 질문과 그 질문에 연결된 답글을 읽음 처리한다.
     * 무엇을 처리할지는 호출부가 이미 판단해서 넘기므로(읽을 게 없으면 호출 자체를 하지 않는다)
     * 여기서 다시 조회해 확인하지 않는다. 동시 요청으로 인한 중복 저장은 유니크 제약이 막는다.
     *
     * @param markQuestion 이 회원의 질문 읽음 기록을 남길지 (이미 있으면 false로 넘어온다)
     * @param markAnswers  이 질문의 답글을 읽음 처리할지. 답글 알림은 질문 작성자만 받으므로
     *                     조회자가 작성자이고 안 읽은 답글이 있을 때만 true로 넘어온다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markRead(Long questionId, Long memberId, boolean markQuestion, boolean markAnswers) {
        if (markAnswers) {
            inquiryReplyRepository.markAllReadByAsker(questionId);
        }

        if (markQuestion) {
            // getReferenceById: FK만 채우면 되므로 프록시로 받아 불필요한 SELECT를 피한다
            inquiryReadRepository.save(QuestionRead.builder()
                    .member(memberRepository.getReferenceById(memberId))
                    .question(inquiryRepository.getReferenceById(questionId))
                    .build());
        }
    }
}
