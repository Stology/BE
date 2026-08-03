package com.stology.be.domain.home.dto.res;

import com.stology.be.domain.upload.enums.DataState;
import com.stology.be.global.PageInfo;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class MaterialDetailRes {

    private PageInfo pageInfo;

    private List<MaterialInfo> materials;

    @Getter
    @Builder
    public static class MaterialInfo {

        private Long studyMaterialId;
        private Long studyId;

        private DataState dataState;
        private String dataTitle;
        private String studyName;

        private long week;
        private String uploaderName;
        private LocalDate uploadedDate;
    }
}