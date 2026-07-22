package com.stology.be.domain.inquiry.controller;

import com.stology.be.domain.inquiry.dto.request.InquiryReqDTO;
import com.stology.be.domain.inquiry.dto.response.InquiryResDTO;
import com.stology.be.domain.inquiry.exception.InquirySuccessCode;
import com.stology.be.domain.inquiry.service.InquiryService;
import com.stology.be.global.apiPayload.ApiResponse;
import com.stology.be.global.security.entity.AuthMember;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/study/{studyId}/question")
public class InquiryController {

    private final InquiryService inquiryService;

    @GetMapping
    public ApiResponse<InquiryResDTO.QuestionList> getQuestions(
            @PathVariable Long studyId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @AuthenticationPrincipal AuthMember authMember
    ) {
        return ApiResponse.onSuccess(InquirySuccessCode.GET_INQUIRIES,
                inquiryService.getQuestions(studyId, page, size, authMember.getMemberId()));
    }

    @GetMapping("/{questionId}")
    public ApiResponse<InquiryResDTO.QuestionDetail> getQuestionDetail(
            @PathVariable Long studyId,
            @PathVariable Long questionId,
            @AuthenticationPrincipal AuthMember authMember
    ) {
        return ApiResponse.onSuccess(InquirySuccessCode.GET_INQUIRY_DETAIL,
                inquiryService.getQuestionDetail(studyId, questionId, authMember.getMemberId()));
    }

    @PostMapping
    public ApiResponse<InquiryResDTO.WriteQuestionResult> writeQuestion(
            @PathVariable Long studyId,
            @AuthenticationPrincipal AuthMember authMember,
            @RequestBody InquiryReqDTO.WriteQuestion request
    ) {
        return ApiResponse.onSuccess(InquirySuccessCode.WRITE_INQUIRY,
                inquiryService.writeQuestion(studyId, authMember.getMemberId(), request));
    }

    @PostMapping("/{questionId}")
    public ApiResponse<InquiryResDTO.UpdateQuestionResult> updateQuestion(
            @PathVariable Long studyId,
            @PathVariable Long questionId,
            @AuthenticationPrincipal AuthMember authMember,
            @RequestBody InquiryReqDTO.UpdateQuestion request
    ) {
        return ApiResponse.onSuccess(InquirySuccessCode.UPDATE_INQUIRY,
                inquiryService.updateQuestion(studyId, questionId, authMember.getMemberId(), request));
    }

    @DeleteMapping("/{questionId}")
    public ApiResponse<Void> deleteQuestion(
            @PathVariable Long studyId,
            @PathVariable Long questionId,
            @AuthenticationPrincipal AuthMember authMember
    ) {
        inquiryService.deleteQuestion(studyId, questionId, authMember.getMemberId());
        return ApiResponse.onSuccess(InquirySuccessCode.DELETE_INQUIRY, null);
    }

    /**
     * 질문 작성/수정 모달에서 questionId 없이 이미지를 먼저 올린다.
     * 응답의 imageUrl을 질문 작성/수정 요청 body의 imageUrls에 담아 보내면 질문에 연결된다.
     * 경로가 {questionId}와 겹쳐 보이지만 Spring은 리터럴 세그먼트를 우선 매칭한다.
     */
    @PostMapping("/image")
    public ApiResponse<InquiryResDTO.StageImageResult> stageQuestionImages(
            @PathVariable Long studyId,
            @AuthenticationPrincipal AuthMember authMember,
            @RequestParam("images") List<MultipartFile> images
    ) {
        return ApiResponse.onSuccess(InquirySuccessCode.ATTACH_IMAGE,
                inquiryService.stageQuestionImages(studyId, authMember.getMemberId(), images));
    }

    /** 답글 작성 시점에도 answerId가 없으므로 동일하게 선업로드를 제공한다. */
    @PostMapping("/{questionId}/answer/image")
    public ApiResponse<InquiryResDTO.StageImageResult> stageAnswerImages(
            @PathVariable Long studyId,
            @PathVariable Long questionId,
            @AuthenticationPrincipal AuthMember authMember,
            @RequestParam("images") List<MultipartFile> images
    ) {
        return ApiResponse.onSuccess(InquirySuccessCode.ATTACH_IMAGE,
                inquiryService.stageAnswerImages(studyId, questionId, authMember.getMemberId(), images));
    }

    @PostMapping("/{questionId}/image")
    public ApiResponse<InquiryResDTO.UploadImageResult> uploadQuestionImages(
            @PathVariable Long studyId,
            @PathVariable Long questionId,
            @AuthenticationPrincipal AuthMember authMember,
            @RequestParam("images") List<MultipartFile> images
    ) {
        return ApiResponse.onSuccess(InquirySuccessCode.ATTACH_IMAGE,
                inquiryService.uploadQuestionImages(studyId, questionId, authMember.getMemberId(), images));
    }

    @PostMapping("/{questionId}/answer")
    public ApiResponse<InquiryResDTO.WriteAnswerResult> writeAnswer(
            @PathVariable Long studyId,
            @PathVariable Long questionId,
            @AuthenticationPrincipal AuthMember authMember,
            @RequestBody InquiryReqDTO.WriteAnswer request
    ) {
        return ApiResponse.onSuccess(InquirySuccessCode.WRITE_REPLY,
                inquiryService.writeAnswer(studyId, questionId, authMember.getMemberId(), request));
    }

    @PatchMapping("/{questionId}/answer/{answerId}")
    public ApiResponse<InquiryResDTO.UpdateAnswerResult> updateAnswer(
            @PathVariable Long studyId,
            @PathVariable Long questionId,
            @PathVariable Long answerId,
            @AuthenticationPrincipal AuthMember authMember,
            @RequestBody InquiryReqDTO.UpdateAnswer request
    ) {
        return ApiResponse.onSuccess(InquirySuccessCode.UPDATE_REPLY,
                inquiryService.updateAnswer(studyId, questionId, answerId, authMember.getMemberId(), request));
    }

    @DeleteMapping("/{questionId}/answer/{answerId}")
    public ApiResponse<Void> deleteAnswer(
            @PathVariable Long studyId,
            @PathVariable Long questionId,
            @PathVariable Long answerId,
            @AuthenticationPrincipal AuthMember authMember
    ) {
        inquiryService.deleteAnswer(studyId, questionId, answerId, authMember.getMemberId());
        return ApiResponse.onSuccess(InquirySuccessCode.DELETE_REPLY, null);
    }

    @PostMapping("/{questionId}/answer/{answerId}/image")
    public ApiResponse<InquiryResDTO.UploadImageResult> uploadAnswerImages(
            @PathVariable Long studyId,
            @PathVariable Long questionId,
            @PathVariable Long answerId,
            @AuthenticationPrincipal AuthMember authMember,
            @RequestParam("images") List<MultipartFile> images
    ) {
        return ApiResponse.onSuccess(InquirySuccessCode.ATTACH_IMAGE,
                inquiryService.uploadAnswerImages(studyId, questionId, answerId, authMember.getMemberId(), images));
    }
}
