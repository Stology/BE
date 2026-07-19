package com.stology.be.domain.inquiry.repository;

import com.stology.be.domain.study.entity.QuestionImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InquiryImageRepository extends JpaRepository<QuestionImage, Long> {

    /**
     * 본문의 [[img:N]] 토큰이 이 목록의 인덱스를 가리키므로, 저장 순서(=id 순)를 반드시 보장해야 한다.
     * ORDER BY가 없으면 DB가 임의 순서로 돌려줄 때 이미지 위치가 뒤바뀐다.
     */
    List<QuestionImage> findByQuestionIdAndDeletedAtIsNullOrderByIdAsc(Long questionId);
}
