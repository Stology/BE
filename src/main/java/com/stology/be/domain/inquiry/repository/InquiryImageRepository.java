package com.stology.be.domain.inquiry.repository;

import com.stology.be.domain.study.entity.QuestionImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InquiryImageRepository extends JpaRepository<QuestionImage, Long> {

    /**
     * 본문의 [[img:{imageId}]] 토큰이 이미지를 id로 참조하므로 순서 자체는 매핑에 영향을 주지 않지만,
     * 응답 목록의 안정적인 순서를 위해 저장 순서(=id 순)로 정렬해 돌려준다.
     */
    List<QuestionImage> findByQuestionIdAndDeletedAtIsNullOrderByIdAsc(Long questionId);
}
