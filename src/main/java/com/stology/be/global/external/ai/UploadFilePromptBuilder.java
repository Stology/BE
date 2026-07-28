package com.stology.be.global.external.ai;

import com.stology.be.domain.node.dto.StudyNodePromptDto;
import com.stology.be.domain.node.entity.StudyMaterial;
import com.stology.be.domain.node.repository.StudyMaterialRepository;
import com.stology.be.domain.node.repository.neo4j.copy.TemplateStudyGraphRepository;
import com.stology.be.domain.study.entity.Study;
import com.stology.be.domain.study.exception.StudyException;
import com.stology.be.domain.study.exception.code.StudyErrorCode;
import com.stology.be.domain.study.repository.StudyRepository;
import com.stology.be.domain.upload.exception.UploadException;
import com.stology.be.domain.upload.exception.code.UploadErrorCode;
import com.stology.be.global.external.s3.S3Reader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UploadFilePromptBuilder {

    private final StudyMaterialRepository studyMaterialRepository;
    private final StudyRepository studyRepository;
    private final TemplateStudyGraphRepository templateStudyGraphRepository;
    private final S3Reader s3Reader;

    private static final String RULE = """
너는 학습 자료를 분석하는 AI이다.

1. 자료를 3~5문장으로 요약한다.
2. 아래 Node List 중에서 자료의 핵심 개념과 가장 관련 있는 노드를 최대 3개 선택한다.
3. Node List에 없는 개념은 절대 선택하지 않는다.
4. 관련 노드가 하나도 없으면 keywords는 빈 배열([])을 반환한다.
5. 반드시 아래 JSON 형식으로만 응답한다.

{
  "summary":"...",
  "keywords":[
      {
          "Id": 1,
          "title": "..."
      }
  ]
}

[Node List]

%s
""";

    public String makeUserPrompt(Long studyMaterialId) {

        StudyMaterial studyMaterial = getStudyMaterial(studyMaterialId);

        String description = "";
        // 안에 저장된 설명이 있다면
        if(studyMaterial.getContent() != null) {
            description = studyMaterial.getContent();
        }

        String fileContent= "";
        // 안에 저장된 파일이 있다면
        if(studyMaterial.getObjectKey()!=null){
            fileContent =
                    s3Reader.readString(
                            studyMaterial.getObjectKey()
                    );

        }


        return buildPrompt(
                studyMaterial.getDataTitle(),
                description,
                fileContent
        );
    }


    public String makeSystemPrompt(Long studyId) {

        Study study = studyRepository.findById(studyId)
                .orElseThrow(() ->
                        new StudyException(StudyErrorCode.STUDY_NOT_FOUND));

        //null 불가능, 자료를 올리려면 템플릿이 등록된 스터디가 있어야함.
        Long templateId = study.getTemplate().getId();

        //1. 탬플릿 정보 전부다 가져오기 | 템플릿 정보 = {노드 이름, 노드 ID}
        //2. 템플릿 정보로 만들기 { 떡볶이: 1, 고추장 : 2, 오뎅 : 3 ,... }
        //4. 템플릿 이름 정보 + RulE을 합쳐서 시스템 프롬프트 만들기
        List<StudyNodePromptDto> nodes =
                templateStudyGraphRepository.findPromptNodes(studyId, templateId);

        String nodeList =
                nodes.stream()
                        .map(node ->
                                "%d : %s".formatted(
                                        node.studyNodeId(),
                                        node.title()
                                )
                        )
                        .collect(Collectors.joining("\n"));

        return RULE.formatted(nodeList);


    }





    private String buildPrompt(
            String title,
            String description,
            String fileContent
    ) {
        return """
                다음 학습 자료를 분석하고 요약하세요.

                [자료 제목]
                %s
                
                [자료 설명]
                %s

                [자료 내용]
                %s
                """.formatted(
                title,
                description,
                fileContent
        );
    }
    private StudyMaterial getStudyMaterial(Long studyMaterialId) {

        return studyMaterialRepository.findById(studyMaterialId).orElseThrow(() ->
                                new UploadException(UploadErrorCode.STUDY_MATERIAL_NOT_FOUND)
                        );

    }
}