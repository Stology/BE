package com.stology.be.domain.template.repository;

import com.stology.be.domain.node.entity.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TemplateRepository extends JpaRepository<Template, Long> {
    List<Template> findByNameContainingIgnoreCase(String search);
}
