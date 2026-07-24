package com.stology.be.domain.inquiry.repository;

import com.stology.be.domain.study.entity.AnswerImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InquiryReplyImageRepository extends JpaRepository<AnswerImage, Long> {

    /** 질문 이미지와 같은 이유로 저장 순서(=id 순)를 보장한다. */
    List<AnswerImage> findByAnswerIdAndDeletedAtIsNullOrderByIdAsc(Long answerId);

    /** 상세 조회 N+1 방지: 여러 답글의 이미지를 한 번에 가져와 메모리에서 answerId별로 묶는다. */
    List<AnswerImage> findByAnswerIdInAndDeletedAtIsNullOrderByIdAsc(List<Long> answerIds);
}
