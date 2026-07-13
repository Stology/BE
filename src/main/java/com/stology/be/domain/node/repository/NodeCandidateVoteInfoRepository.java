package com.stology.be.domain.node.repository;

import com.stology.be.domain.node.entity.NodeCandidate2StudyMaterial;
import com.stology.be.domain.node.entity.NodeCandidateVoteInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NodeCandidateVoteInfoRepository extends JpaRepository<NodeCandidateVoteInfo, Long> {




}
