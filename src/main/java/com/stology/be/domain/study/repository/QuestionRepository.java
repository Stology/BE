package com.stology.be.domain.study.repository;

import com.stology.be.domain.study.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    Optional<Question> findTopByStudyIdOrderByUpdatedAtDesc(Long studyId);
}
