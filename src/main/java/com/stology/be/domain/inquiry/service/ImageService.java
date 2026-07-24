package com.stology.be.domain.inquiry.service;

import com.stology.be.domain.inquiry.converter.InquiryConverter;
import com.stology.be.domain.inquiry.dto.response.InquiryResDTO;
import com.stology.be.domain.inquiry.repository.InquiryImageRepository;
import com.stology.be.domain.inquiry.repository.InquiryReplyImageRepository;
import com.stology.be.domain.study.entity.Answer;
import com.stology.be.domain.study.entity.AnswerImage;
import com.stology.be.domain.study.entity.Question;
import com.stology.be.domain.study.entity.QuestionImage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * inquiry 이미지의 DB row 저장/조회를 담당하고, 토큰 처리({@link ImageTokensService})와
 * S3 스토리지({@link ImageStorageService})를 조율한다.
 *
 * <p>이미지 처리는 두 단계로 나뉜다.
 * <ul>
 *   <li><b>업로드/검증(트랜잭션 밖)</b>: {@code validate*Tokens}, {@code uploadImages} — 느린 S3 I/O를 여기서 끝낸다.</li>
 *   <li><b>영속화(트랜잭션 안)</b>: {@code persistNew*Images}, {@code replace*Images} — URL로 row를 저장하고 토큰을 치환한다.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class ImageService {

    private final InquiryImageRepository inquiryImageRepository;
    private final InquiryReplyImageRepository inquiryReplyImageRepository;
    private final ImageTokensService tokens;
    private final ImageStorageService storage;

    /* ===================== 검증/업로드 위임 (트랜잭션 밖) ===================== */

    /** null·빈 파일을 걸러낸 이미지 목록. Swagger 등이 보내는 빈 파트를 무해하게 처리한다. */
    public List<MultipartFile> nonEmptyImages(List<MultipartFile> images) {
        if (images == null) {
            return List.of();
        }
        return images.stream()
                .filter(file -> file != null && !file.isEmpty())
                .collect(Collectors.toList());
    }

    public int count(List<MultipartFile> images) {
        return tokens.count(images);
    }

    public int textLength(String content) {
        return tokens.textLength(content);
    }

    public boolean hasImageToken(String content) {
        return tokens.hasToken(content);
    }

    public void validateCreateTokens(String content, int fileCount) {
        tokens.validateForCreate(content, fileCount);
    }

    public void validateUpdateTokens(String content, int fileCount, Set<Long> existingIds) {
        tokens.parse(content, fileCount, existingIds);
    }

    public List<String> uploadImages(String subDirectory, List<MultipartFile> images) {
        return storage.upload(subDirectory, images);
    }

    public Set<Long> questionImageIds(Long questionId) {
        return inquiryImageRepository.findByQuestionIdAndDeletedAtIsNullOrderByIdAsc(questionId).stream()
                .map(QuestionImage::getId)
                .collect(Collectors.toSet());
    }

    public Set<Long> answerImageIds(Long answerId) {
        return inquiryReplyImageRepository.findByAnswerIdAndDeletedAtIsNullOrderByIdAsc(answerId).stream()
                .map(AnswerImage::getId)
                .collect(Collectors.toSet());
    }

    /* ===================== 질문 이미지 (영속화, 트랜잭션 안) ===================== */

    /** 작성 시: 업로드된 URL로 row를 저장하고 content의 [[img:new:K]]를 실제 [[img:{imageId}]]로 치환한다. */
    public String persistNewQuestionImages(Question question, String content, List<String> uploadedUrls) {
        if (uploadedUrls.isEmpty()) {
            return content;
        }
        return tokens.rewriteNewTokens(content, saveQuestionImageRows(question, uploadedUrls));
    }

    /**
     * 수정 시: content에서 빠진 기존 이미지는 삭제(DB + 커밋 후 S3)하고,
     * 업로드된 새 URL로 row를 저장한 뒤 [[img:new:K]]를 실제 id로 치환한다.
     */
    public String replaceQuestionImages(Question question, String content, List<String> uploadedUrls) {
        List<QuestionImage> existing = inquiryImageRepository.findByQuestionIdAndDeletedAtIsNullOrderByIdAsc(question.getId());
        Set<Long> existingIds = existing.stream().map(QuestionImage::getId).collect(Collectors.toSet());
        ImageTokensService.Parsed parsed = tokens.parse(content, uploadedUrls.size(), existingIds);

        List<QuestionImage> dropped = existing.stream()
                .filter(image -> !parsed.keptIds().contains(image.getId()))
                .collect(Collectors.toList());
        removeImages(dropped, QuestionImage::getImageUrl, inquiryImageRepository::deleteAll);

        if (uploadedUrls.isEmpty()) {
            return content;
        }
        return tokens.rewriteNewTokens(content, saveQuestionImageRows(question, uploadedUrls));
    }

    /** 질문의 모든 이미지를 실제 삭제(DB row + 커밋 후 S3)한다. 질문 hard delete 시 사용. */
    public void deleteQuestionImages(Long questionId) {
        List<QuestionImage> images = inquiryImageRepository.findByQuestionIdAndDeletedAtIsNullOrderByIdAsc(questionId);
        removeImages(images, QuestionImage::getImageUrl, inquiryImageRepository::deleteAll);
    }

    public List<InquiryResDTO.ImageInfo> getQuestionImages(Long questionId) {
        return inquiryImageRepository.findByQuestionIdAndDeletedAtIsNullOrderByIdAsc(questionId).stream()
                .map(image -> new InquiryResDTO.ImageInfo(image.getId(), image.getImageUrl()))
                .collect(Collectors.toList());
    }

    private Map<Integer, Long> saveQuestionImageRows(Question question, List<String> urls) {
        List<QuestionImage> rows = urls.stream()
                .map(url -> InquiryConverter.toQuestionImage(url, question))
                .collect(Collectors.toList());
        inquiryImageRepository.saveAll(rows);   // IDENTITY라 각 행에 id가 채워진다
        return indexToId(rows.stream().map(QuestionImage::getId).collect(Collectors.toList()));
    }

    /* ===================== 답글 이미지 (영속화, 트랜잭션 안) ===================== */

    public String persistNewAnswerImages(Answer answer, String content, List<String> uploadedUrls) {
        if (uploadedUrls.isEmpty()) {
            return content;
        }
        return tokens.rewriteNewTokens(content, saveAnswerImageRows(answer, uploadedUrls));
    }

    public String replaceAnswerImages(Answer answer, String content, List<String> uploadedUrls) {
        List<AnswerImage> existing = inquiryReplyImageRepository.findByAnswerIdAndDeletedAtIsNullOrderByIdAsc(answer.getId());
        Set<Long> existingIds = existing.stream().map(AnswerImage::getId).collect(Collectors.toSet());
        ImageTokensService.Parsed parsed = tokens.parse(content, uploadedUrls.size(), existingIds);

        List<AnswerImage> dropped = existing.stream()
                .filter(image -> !parsed.keptIds().contains(image.getId()))
                .collect(Collectors.toList());
        removeImages(dropped, AnswerImage::getImageUrl, inquiryReplyImageRepository::deleteAll);

        if (uploadedUrls.isEmpty()) {
            return content;
        }
        return tokens.rewriteNewTokens(content, saveAnswerImageRows(answer, uploadedUrls));
    }

    /** 답글의 모든 이미지를 실제 삭제(DB row + 커밋 후 S3)한다. 답글 hard delete 시 사용. */
    public void deleteAnswerImages(Long answerId) {
        List<AnswerImage> images = inquiryReplyImageRepository.findByAnswerIdAndDeletedAtIsNullOrderByIdAsc(answerId);
        removeImages(images, AnswerImage::getImageUrl, inquiryReplyImageRepository::deleteAll);
    }

    /**
     * 여러 답글의 이미지를 한 번에 삭제한다(질문 hard delete 시 답글별 조회 N+1 방지).
     * IN 조회 1번으로 모아서 DB 삭제 + 커밋 후 S3 제거한다.
     */
    public void deleteAnswerImagesByAnswerIds(List<Long> answerIds) {
        if (answerIds == null || answerIds.isEmpty()) {
            return;
        }
        List<AnswerImage> images = inquiryReplyImageRepository.findByAnswerIdInAndDeletedAtIsNullOrderByIdAsc(answerIds);
        removeImages(images, AnswerImage::getImageUrl, inquiryReplyImageRepository::deleteAll);
    }

    /** 상세 조회 N+1 방지: 여러 답글의 이미지를 한 번에 조회해 answerId별 목록으로 묶는다. */
    public Map<Long, List<InquiryResDTO.ImageInfo>> getAnswerImagesByAnswerIds(List<Long> answerIds) {
        if (answerIds == null || answerIds.isEmpty()) {
            return Map.of();
        }
        return inquiryReplyImageRepository.findByAnswerIdInAndDeletedAtIsNullOrderByIdAsc(answerIds).stream()
                .collect(Collectors.groupingBy(
                        image -> image.getAnswer().getId(),
                        Collectors.mapping(
                                image -> new InquiryResDTO.ImageInfo(image.getId(), image.getImageUrl()),
                                Collectors.toList())));
    }

    private Map<Integer, Long> saveAnswerImageRows(Answer answer, List<String> urls) {
        List<AnswerImage> rows = urls.stream()
                .map(url -> InquiryConverter.toAnswerImage(url, answer))
                .collect(Collectors.toList());
        inquiryReplyImageRepository.saveAll(rows);
        return indexToId(rows.stream().map(AnswerImage::getId).collect(Collectors.toList()));
    }

    /* ===================== 공통 ===================== */

    /** 파일 index(K) → 저장된 imageId 매핑. rows는 업로드 순서와 같아 index가 그대로 K가 된다. */
    private Map<Integer, Long> indexToId(List<Long> savedIds) {
        Map<Integer, Long> map = new HashMap<>();
        for (int k = 0; k < savedIds.size(); k++) {
            map.put(k, savedIds.get(k));
        }
        return map;
    }

    /** DB row는 현재 트랜잭션에서 삭제하고, S3 객체 제거는 커밋 이후로 미룬다(질문/답글 이미지 공통). */
    private <T> void removeImages(List<T> images, Function<T, String> url, Consumer<List<T>> deleteAll) {
        if (images.isEmpty()) {
            return;
        }
        List<String> urls = images.stream().map(url).collect(Collectors.toList());
        deleteAll.accept(images);              // DB 삭제: 현재 트랜잭션 안
        storage.removeAfterCommit(urls);       // S3 삭제: 커밋 이후
    }
}
