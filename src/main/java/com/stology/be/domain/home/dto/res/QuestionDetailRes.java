package com.stology.be.domain.home.dto.res;

import com.stology.be.domain.home.dto.QuestionActivity;
import com.stology.be.global.PageInfo;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class QuestionDetailRes {
    private PageInfo<String> pageInfo;
    private List<QuestionActivity> questions;

}