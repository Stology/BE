package com.stology.be.domain.template.repository;

import com.stology.be.domain.node.entity.Template;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemplateRepository extends JpaRepository<Template, Long> {
}
