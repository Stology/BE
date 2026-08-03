package com.stology.be.domain.study.repository;

import com.stology.be.domain.study.entity.Study;
import com.stology.be.domain.upload.enums.DataState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StudyRepository extends JpaRepository<Study, Long> {
    boolean existsByName(String name);
    Optional<Study> findByInvitationToken(String token);
    java.util.List<Study> findByIsActiveTrue();
    
    @org.springframework.data.jpa.repository.Query("SELECT s.id FROM Study s WHERE s.isActive = true")
    java.util.List<Long> findIdsByIsActiveTrue();

    
}
