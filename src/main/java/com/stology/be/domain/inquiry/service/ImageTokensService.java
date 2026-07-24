package com.stology.be.domain.inquiry.service;

import com.stology.be.domain.inquiry.exception.InquiryErrorCode;
import com.stology.be.domain.inquiry.exception.InquiryException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ImageTokensService {

    /** 그룹1은 "new:"(있으면 새 파일), 그룹2는 숫자(new면 파일 index K, 아니면 imageId). */
    private static final Pattern IMAGE_TOKEN = Pattern.compile("\\[\\[img:(new:)?(\\d+)]]");

    /** 질문/답글 하나당 이미지 최대 개수. */
    private static final int MAX_IMAGE_COUNT = 5;

    public int count(List<MultipartFile> images) {
        return images == null ? 0 : images.size();
    }

    /** 이미지 자리표시자를 제외한 실제 텍스트 길이(본문 길이 제한 계산용). */
    public int textLength(String content) {
        if (content == null) {
            return 0;
        }
        return IMAGE_TOKEN.matcher(content).replaceAll("").length();
    }

    public boolean hasToken(String content) {
        return content != null && IMAGE_TOKEN.matcher(content).find();
    }

    /** 작성용 검증: 유지할 기존 이미지가 없으므로 모든 토큰은 [[img:new:K]]여야 한다. */
    public void validateForCreate(String content, int fileCount) {
        parse(content, fileCount, Set.of());
    }

    /**
     * 토큰 파싱 + 검증.
     * <ul>
     *   <li>[[img:new:K]] : K는 0 이상 fileCount 미만, 중복 불가, 모든 파일(0..fileCount-1)이 빠짐없이 참조돼야 함</li>
     *   <li>[[img:{id}]]  : existingIds에 포함된 id여야 하고 중복 불가(작성 시엔 existingIds가 비어 있어 항상 오류)</li>
     *   <li>최종 이미지 개수(새 파일 + 유지)가 {@link #MAX_IMAGE_COUNT}를 넘으면 거부</li>
     * </ul>
     */
    public Parsed parse(String content, int fileCount, Set<Long> existingIds) {
        Set<Integer> newKeys = new HashSet<>();
        Set<Long> keptIds = new HashSet<>();

        Matcher matcher = IMAGE_TOKEN.matcher(content == null ? "" : content);
        while (matcher.find()) {
            boolean isNew = matcher.group(1) != null;
            long number = Long.parseLong(matcher.group(2));
            if (isNew) {
                int key = (int) number;
                if (key < 0 || key >= fileCount || !newKeys.add(key)) {
                    throw new InquiryException(InquiryErrorCode.IMAGE_TOKEN_MISMATCH);
                }
            } else if (!existingIds.contains(number) || !keptIds.add(number)) {
                throw new InquiryException(InquiryErrorCode.IMAGE_URL_INVALID);
            }
        }
        if (newKeys.size() != fileCount) {
            throw new InquiryException(InquiryErrorCode.IMAGE_TOKEN_MISMATCH);
        }
        if (newKeys.size() + keptIds.size() > MAX_IMAGE_COUNT) {
            throw new InquiryException(InquiryErrorCode.IMAGE_LIMIT_EXCEEDED);
        }
        return new Parsed(newKeys, keptIds);
    }

    /** content의 [[img:new:K]]만 [[img:{id}]]로 치환한다(기존 [[img:{id}]]는 그대로 둔다). */
    public String rewriteNewTokens(String content, Map<Integer, Long> newIds) {
        Matcher matcher = IMAGE_TOKEN.matcher(content);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String replacement = (matcher.group(1) != null)
                    ? "[[img:" + newIds.get(Integer.parseInt(matcher.group(2))) + "]]"
                    : matcher.group();
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /** parse 결과: 새 파일 index 집합과 유지되는 기존 imageId 집합. */
    public record Parsed(Set<Integer> newKeys, Set<Long> keptIds) {}
}
