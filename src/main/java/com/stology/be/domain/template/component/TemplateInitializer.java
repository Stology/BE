package com.stology.be.domain.template.component;

import tools.jackson.databind.ObjectMapper;
import com.stology.be.domain.template.dto.TemplateImportDto;
import com.stology.be.domain.template.service.Neo4jTemplateImportService;
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

    private final Neo4jTemplateImportService importService;

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