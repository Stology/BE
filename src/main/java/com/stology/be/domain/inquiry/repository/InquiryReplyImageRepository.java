package com.stology.be.domain.inquiry.repository;

import com.stology.be.domain.study.entity.AnswerImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InquiryReplyImageRepository extends JpaRepository<AnswerImage, Long> {

    /** 질문 이미지와 같은 이유로 저장 순서(=id 순)를 보장한다. */
    List<AnswerImage> findByAnswerIdAndDeletedAtIsNullOrderByIdAsc(Long answerId);
}
