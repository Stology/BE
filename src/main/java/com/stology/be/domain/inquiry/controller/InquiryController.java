package com.stology.be.domain.inquiry.controller;

import com.stology.be.domain.inquiry.dto.request.InquiryReqDTO;
import com.stology.be.domain.inquiry.dto.response.InquiryResDTO;
import com.stology.be.domain.inquiry.exception.InquirySuccessCode;
import com.stology.be.domain.inquiry.service.AnswerService;
import com.stology.be.domain.inquiry.service.QuestionService;
import com.stology.be.global.apiPayload.ApiResponse;
import com.stology.be.global.security.entity.AuthMember;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.beans.PropertyEditorSupport;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/study/{studyId}/question")
public class InquiryController {

    private final QuestionService questionService;
    private final AnswerService answerService;

    /**
     * 이미지 없이 보낼 때 Swagger가 images에 빈 문자열("")을 넣어 String→MultipartFile 변환이 실패(COMMON_400)한다.
     * 파일 필드로 온 텍스트 값은 무시(null 처리)해 @ModelAttribute 바인딩이 깨지지 않게 한다. 실제 파일은 영향 없음.
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(MultipartFile.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(null);
            }
        });
    }

    @GetMapping
    public ApiResponse<InquiryResDTO.QuestionList> getQuestions(
            @PathVariable Long studyId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @AuthenticationPrincipal AuthMember authMember
    ) {
        return ApiResponse.onSuccess(InquirySuccessCode.GET_INQUIRIES,
                questionService.getQuestions(studyId, page, size, authMember.getMemberId()));
    }

    @GetMapping("/{questionId}")
    public ApiResponse<InquiryResDTO.QuestionDetail> getQuestionDetail(
            @PathVariable Long studyId,
            @PathVariable Long questionId,
            @AuthenticationPrincipal AuthMember authMember
    ) {
        return ApiResponse.onSuccess(InquirySuccessCode.GET_INQUIRY_DETAIL,
                questionService.getQuestionDetail(studyId, questionId, authMember.getMemberId()));
    }

    /**
     * 텍스트(title/content)와 이미지(images)를 한 번의 multipart form-data 요청으로 받는다.
     * content의 [[img:new:K]] 토큰 순서가 images 파일 순서와 대응해 인라인 순서가 보존된다.
     * 이미지가 없으면 images를 생략하고 title/content만 보내면 된다.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<InquiryResDTO.WriteQuestionResult> writeQuestion(
            @PathVariable Long studyId,
            @AuthenticationPrincipal AuthMember authMember,
            @ModelAttribute InquiryReqDTO.WriteQuestion request
    ) {
        return ApiResponse.onSuccess(InquirySuccessCode.WRITE_INQUIRY,
                questionService.writeQuestion(studyId, authMember.getMemberId(), request, request.getImages()));
    }

    /**
     * 질문 수정. content의 [[img:{imageId}]]는 기존 이미지 유지, [[img:new:K]]는 images의 K번째 새 파일이며,
     * content에서 빠진 기존 이미지는 삭제된다.
     */
    @PatchMapping(value = "/{questionId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<InquiryResDTO.UpdateQuestionResult> updateQuestion(
            @PathVariable Long studyId,
            @PathVariable Long questionId,
            @AuthenticationPrincipal AuthMember authMember,
            @ModelAttribute InquiryReqDTO.UpdateQuestion request
    ) {
        return ApiResponse.onSuccess(InquirySuccessCode.UPDATE_INQUIRY,
                questionService.updateQuestion(studyId, questionId, authMember.getMemberId(), request, request.getImages()));
    }

    @DeleteMapping("/{questionId}")
    public ApiResponse<Void> deleteQuestion(
            @PathVariable Long studyId,
            @PathVariable Long questionId,
            @AuthenticationPrincipal AuthMember authMember
    ) {
        questionService.deleteQuestion(studyId, questionId, authMember.getMemberId());
        return ApiResponse.onSuccess(InquirySuccessCode.DELETE_INQUIRY, null);
    }

    /**
     * 답글 작성. 질문과 동일하게 텍스트(content)와 이미지(images)를 한 번의 multipart로 받는다.
     */
    @PostMapping(value = "/{questionId}/answer", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<InquiryResDTO.WriteAnswerResult> writeAnswer(
            @PathVariable Long studyId,
            @PathVariable Long questionId,
            @AuthenticationPrincipal AuthMember authMember,
            @ModelAttribute InquiryReqDTO.WriteAnswer request
    ) {
        return ApiResponse.onSuccess(InquirySuccessCode.WRITE_REPLY,
                answerService.writeAnswer(studyId, questionId, authMember.getMemberId(), request, request.getImages()));
    }

    /**
     * 답글 수정. 토큰 규약은 질문 수정과 동일하다([[img:{imageId}]] 유지 / [[img:new:K]] 추가).
     */
    @PatchMapping(value = "/{questionId}/answer/{answerId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<InquiryResDTO.UpdateAnswerResult> updateAnswer(
            @PathVariable Long studyId,
            @PathVariable Long questionId,
            @PathVariable Long answerId,
            @AuthenticationPrincipal AuthMember authMember,
            @ModelAttribute InquiryReqDTO.UpdateAnswer request
    ) {
        return ApiResponse.onSuccess(InquirySuccessCode.UPDATE_REPLY,
                answerService.updateAnswer(studyId, questionId, answerId, authMember.getMemberId(), request, request.getImages()));
    }

    @DeleteMapping("/{questionId}/answer/{answerId}")
    public ApiResponse<Void> deleteAnswer(
            @PathVariable Long studyId,
            @PathVariable Long questionId,
            @PathVariable Long answerId,
            @AuthenticationPrincipal AuthMember authMember
    ) {
        answerService.deleteAnswer(studyId, questionId, answerId, authMember.getMemberId());
        return ApiResponse.onSuccess(InquirySuccessCode.DELETE_REPLY, null);
    }
}
