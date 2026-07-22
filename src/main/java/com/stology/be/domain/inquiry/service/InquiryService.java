package com.stology.be.domain.inquiry.service;

import com.stology.be.domain.inquiry.dto.request.InquiryReqDTO;
import com.stology.be.domain.inquiry.dto.response.InquiryResDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface InquiryService {

    InquiryResDTO.QuestionList getQuestions(Long studyId, Integer page, Integer size, Long memberId);

    InquiryResDTO.QuestionDetail getQuestionDetail(Long studyId, Long questionId, Long memberId);

    InquiryResDTO.WriteQuestionResult writeQuestion(Long studyId, Long memberId, InquiryReqDTO.WriteQuestion request);

    InquiryResDTO.UpdateQuestionResult updateQuestion(Long studyId, Long questionId, Long memberId, InquiryReqDTO.UpdateQuestion request);

    void deleteQuestion(Long studyId, Long questionId, Long memberId);

    InquiryResDTO.UploadImageResult uploadQuestionImages(Long studyId, Long questionId, Long memberId, List<MultipartFile> files);

    InquiryResDTO.StageImageResult stageQuestionImages(Long studyId, Long memberId, List<MultipartFile> files);

    InquiryResDTO.StageImageResult stageAnswerImages(Long studyId, Long questionId, Long memberId, List<MultipartFile> files);

    InquiryResDTO.WriteAnswerResult writeAnswer(Long studyId, Long questionId, Long memberId, InquiryReqDTO.WriteAnswer request);

    InquiryResDTO.UpdateAnswerResult updateAnswer(Long studyId, Long questionId, Long answerId, Long memberId, InquiryReqDTO.UpdateAnswer request);

    void deleteAnswer(Long studyId, Long questionId, Long answerId, Long memberId);

    InquiryResDTO.UploadImageResult uploadAnswerImages(Long studyId, Long questionId, Long answerId, Long memberId, List<MultipartFile> files);
}
