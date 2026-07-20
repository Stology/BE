package com.stology.be.global.external.ai;

import com.stology.be.domain.node.entity.StudyMaterial;
import com.stology.be.domain.node.entity.TemplateNode;
import com.stology.be.domain.node.repository.StudyMaterialRepository;
import com.stology.be.domain.study.entity.Study;
import com.stology.be.domain.study.repository.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UploadFilePromptBuilder {

    private final StudyMaterialRepository studyMaterialRepository;
    private final StudyRepository studyRepository;

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
          "id":101,
          "name":"..."
      }
  ]
}

[Node List]

%s
""";

    public String makeUserPrompt(Long studyMaterialId) {

        StudyMaterial studyMaterial = getStudyMaterial(studyMaterialId);

        String fileContent = readFileFromUrl(studyMaterial.getFileUrl());


        if (fileContent.isBlank()) {
            throw new IllegalStateException(
                    "업로드된 파일의 내용이 비어 있습니다."
            );
        }

        return buildPrompt(
                studyMaterial.getDataTitle(),
                fileContent
        );
    }


    public String makeSystemPrompt(Long studyId) {
        Study study = studyRepository.findById(studyId).orElse(null);

        //1. 탬플릿 정보 전부다 가져오기 | 템플릿 정보 = {노드 이름, 노드 ID}
        //2. 템플릿 정보로 만들기 { 떡볶이: 1, 고추장 : 2, 오뎅 : 3 ,... }
        //4. 템플릿 이름 정보 + RulE을 합쳐서 시스템 프롬프트 만들기

        /*
        List<TemplateNode> nodes;

        String nodeList =
                nodes.stream()
                        .map(node ->
                                "%d : %s".formatted(
                                        node.getId(),
                                        node.getName()
                                )
                        )
                        .collect(Collectors.joining("\n"));

        return RULE.formatted(nodeList);'
     */


        return null;
    }










    private String readFileFromUrl(String fileUrl) {

        HttpURLConnection connection = null;

        try {
            URI uri = URI.create(fileUrl);

            connection =
                    (HttpURLConnection) uri.toURL()
                            .openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5_000);
            connection.setReadTimeout(30_000);

            int statusCode = connection.getResponseCode();

            if (statusCode < 200 || statusCode >= 300) {
                throw new IllegalStateException(
                        "파일 다운로드에 실패했습니다. statusCode="
                                + statusCode
                );
            }

            try (var inputStream = connection.getInputStream()) {
                return new String(
                        inputStream.readAllBytes(),
                        StandardCharsets.UTF_8
                );
            }

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "업로드된 파일을 읽는 중 오류가 발생했습니다.",
                    exception
            );
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String buildPrompt(
            String title,
            String content
    ) {
        return """
                다음 학습 자료를 분석하고 요약하세요.

                [자료 제목]
                %s

                [자료 내용]
                %s
                """.formatted(
                title,
                content
        );
    }
    private StudyMaterial getStudyMaterial(Long studyMaterialId) {

        StudyMaterial studyMaterial =
                studyMaterialRepository.findById(studyMaterialId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "존재하지 않는 업로드 자료입니다."
                                )
                        );

        String fileUrl = studyMaterial.getFileUrl();

        if (fileUrl == null || fileUrl.isBlank()) {
            throw new IllegalStateException(
                    "업로드 자료의 파일 URL이 존재하지 않습니다."
            );
        }
        return studyMaterial;
    }
}