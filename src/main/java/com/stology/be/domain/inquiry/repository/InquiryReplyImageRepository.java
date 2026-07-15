package com.stology.be.domain.inquiry.repository;

import com.stology.be.domain.study.entity.AnswerImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InquiryReplyImageRepository extends JpaRepository<AnswerImage, Long> {

    List<AnswerImage> findByAnswerIdAndDeletedAtIsNull(Long answerId);
}
