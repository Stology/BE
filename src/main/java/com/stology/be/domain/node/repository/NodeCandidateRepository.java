package com.stology.be.domain.node.repository;

import com.stology.be.domain.node.entity.NodeCandidate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NodeCandidateRepository extends JpaRepository<NodeCandidate, Long> {

    List<NodeCandidate> findAllByStudyMaterial_Id(Long materialId);
}
