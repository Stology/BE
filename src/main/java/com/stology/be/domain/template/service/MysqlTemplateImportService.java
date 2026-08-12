package com.stology.be.domain.template.service;

import com.stology.be.domain.node.entity.Template;
import com.stology.be.domain.template.dto.TemplateImportDto;
import com.stology.be.domain.template.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MysqlTemplateImportService {

    private final TemplateRepository templateRepository;

    @Transactional
    public Long saveTemplate(TemplateImportDto dto) {

        Template template =
                Template.builder()
                        .title(dto.title().trim())
                        .description(dto.description())
                        .build();

        Template savedTemplate =
                templateRepository.save(template);


        return savedTemplate.getId();
    }

    @Transactional(
            transactionManager = "transactionManager"
    )
    public void deleteTemplate(Long templateId) {

        templateRepository.deleteById(templateId);
    }
}