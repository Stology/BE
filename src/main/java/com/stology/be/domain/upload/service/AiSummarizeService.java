package com.stology.be.domain.upload.service;

import com.stology.be.domain.node.entity.StudyMaterial;
import com.stology.be.domain.node.repository.StudyMaterialRepository;
import com.stology.be.domain.upload.enums.DataState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AiSummarizeService {

    private final StudyMaterialRepository studyMaterialRepository;

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
