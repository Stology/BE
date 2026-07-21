package com.stology.be.domain.template.component;

import com.stology.be.domain.node.repository.neo4j.TemplateGraphRepository;
import com.stology.be.domain.template.dto.TemplateNodeCountProjection;
import com.stology.be.domain.template.service.TemplateImportService;
import tools.jackson.databind.ObjectMapper;
import com.stology.be.domain.template.dto.TemplateImportDto;
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
    private final TemplateGraphRepository templateGraphRepository;

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


        // importService.importTemplate(dto);


/* DB 확인용 
        System.out.println("==============================");
        System.out.println("Template Count     : "
                + templateGraphRepository.count());

        System.out.println("TemplateNode Count : "
                + templateGraphRepository.countTemplateNodes());


        for (TemplateNodeCountProjection info :
                templateGraphRepository.findTemplateNodeCounts()) {

            System.out.println(
                    "TemplateId = " + info.templateId()
                            + ", NodeCount = " + info.nodeCount()
            );
        }

        System.out.println("==============================");*/



    }
}