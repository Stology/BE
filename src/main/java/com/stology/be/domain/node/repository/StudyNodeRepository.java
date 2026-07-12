package com.stology.be.domain.node.repository;

import com.stology.be.domain.node.entity.StudyNode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StudyNodeRepository extends JpaRepository<StudyNode, Long> {
    Optional<StudyNode> findTopByStudyIdOrderByUpdatedAtDesc(Long studyId);
}
