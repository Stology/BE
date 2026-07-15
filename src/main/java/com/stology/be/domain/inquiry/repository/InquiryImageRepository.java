package com.stology.be.domain.inquiry.repository;

import com.stology.be.domain.study.entity.QuestionImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InquiryImageRepository extends JpaRepository<QuestionImage, Long> {

    List<QuestionImage> findByQuestionIdAndDeletedAtIsNull(Long questionId);
}
