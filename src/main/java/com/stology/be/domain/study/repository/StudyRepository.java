package com.stology.be.domain.study.repository;

import com.stology.be.domain.study.entity.Study;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StudyRepository extends JpaRepository<Study, Long> {
    boolean existsByName(String name);
    Optional<Study> findByInvitationToken(String token);
}
