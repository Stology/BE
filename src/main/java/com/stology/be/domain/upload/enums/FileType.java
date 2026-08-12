package com.stology.be.domain.upload.enums;



public enum FileType {
    MD("엠디"),TEXT("텍스트");


    FileType(String koreanName) {
        this.koreanName = koreanName;
    }

    private String koreanName;

}
