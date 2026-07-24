package com.stology.be.domain.inquiry.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

/**
 * 작성/수정 공통 패턴을 한곳에 모은다.
 * 업로드(트랜잭션 밖) 이후의 DB 쓰기를 짧은 트랜잭션으로 실행하고,
 * 커밋에 실패하면 이미 올라간 S3 객체를 보상 삭제한다(고아 객체 방지).
 */
@Service
@RequiredArgsConstructor
public class WriteTxService {

    private final PlatformTransactionManager transactionManager;
    private final ImageStorageService storage;

    public <T> T commitOrCompensate(List<String> uploadedUrls, TransactionCallback<T> work) {
        try {
            return new TransactionTemplate(transactionManager).execute(work);
        } catch (RuntimeException e) {
            storage.removeNow(uploadedUrls);   // 커밋 실패 → 방금 올린 S3 객체 정리
            throw e;
        }
    }
}
