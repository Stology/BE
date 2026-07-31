package com.stology.be.domain.study.repository;

import com.stology.be.domain.study.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;
import java.time.LocalDateTime;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    Optional<Question> findTopByStudyIdOrderByUpdatedAtDesc(Long studyId);

    Integer countByStudyId(Long studyId);

    List<Question> findByStudyIdAndCreatedAtBetween(Long studyId, LocalDateTime start, LocalDateTime end);
}
