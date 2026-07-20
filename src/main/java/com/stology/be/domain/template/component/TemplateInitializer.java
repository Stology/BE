package com.stology.be.domain.template.component;

import com.stology.be.domain.node.repository.neo4j.TemplateGraphRepository;
import tools.jackson.databind.ObjectMapper;
import com.stology.be.domain.template.dto.TemplateImportDto;
import com.stology.be.domain.template.service.TemplateImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TemplateInitializer
        implements CommandLineRunner {

    private final ObjectMapper objectMapper;

    private final TemplateImportService importService;

    @Override
    public void run(String... args) throws Exception {

        Resource resource =
                new ClassPathResource(
                        "templates/tteokbokki.json"
                );

        TemplateImportDto dto =
                objectMapper.readValue(
                        resource.getInputStream(),
                        TemplateImportDto.class
                );


//        importService.importTemplate(dto);

    }
}