package com.stology.be.global.external.ai;

import com.stology.be.domain.node.entity.StudyMaterial;
import com.stology.be.domain.node.repository.StudyMaterialRepository;
import com.stology.be.domain.upload.enums.DataState;
import com.stology.be.global.external.ai.dto.AiSummaryResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AiSummarizeService {

    private final StudyMaterialRepository studyMaterialRepository;
    private final ChatClient chatClient;


    public AiSummarizeService(
            StudyMaterialRepository studyMaterialRepository,
            ChatClient.Builder chatClientBuilder
    ) {
        this.studyMaterialRepository = studyMaterialRepository;
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * AI에게 system prompt와 user input을 전달하고
     * 문자열 응답을 반환합니다.
     */
    public AiSummaryResult requestSummary(
            String userInput,
            String systemPrompt
    ) {

        AiSummaryResult result =
                chatClient.prompt()
                        .system(systemPrompt)
                        .user(userInput)
                        .call()
                        .entity(AiSummaryResult.class);


        if (result == null) {
            throw new IllegalStateException(
                    "AI 서버로부터 응답을 받지 못했습니다."
            );
        }

        return result;
    }

    @Transactional
    public void changeState(
            Long studyMaterialId,
            DataState dataState
    ) {


        StudyMaterial studyMaterial =
                studyMaterialRepository.findById(studyMaterialId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "존재하지 않는 업로드 자료입니다."
                                )
                        );

        studyMaterial.changeDataState(dataState);
    }
}
