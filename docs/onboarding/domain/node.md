# 노드

노드 도메인은 템플릿·스터디 노드·후보 모델과 후보 검토 제출 API로 구성됩니다.

## 모델과 관계

- `Template`은 업로더 `Member`를 다대일로 참조하고 이름과 설명을 가집니다.
- `TemplateNode`는 `Template`을 다대일로 참조합니다.
- `StudyNode`는 `Study`를 다대일로 참조하고 `activeLevel`을 기본값 `0`으로 둡니다.
- `StudyMaterial`은 파일 URL을 보관합니다.
- `NodeCandidate`는 `StudyMaterial`과 `StudyNode`를 각각 다대일로 참조합니다. 상태는 `CandidateState`이며 기본값은 `PENDING`, accept count 기본값은 `0`입니다.
- `CandidateReview`는 `NodeCandidate`와 검토자 `Member`를 각각 다대일로 참조하고 `ReviewDecision`의 `ACCEPT` 또는 `DECLINE`을 저장합니다. 같은 후보와 검토자 조합은 한 번만 저장됩니다.

모든 엔티티는 `BaseEntity`를 상속합니다.

## 관계 지도

```text
Member <- Template <- TemplateNode
Study  <- StudyNode <- NodeCandidate -> StudyMaterial
Member <- CandidateReview -> NodeCandidate
```

`Study.template`은 `Template`을 다대일로 참조하고, `StudyNode.study`는 `Study`를 다대일로 참조합니다. 스터디의 질문·답변·리포트 모델은 [스터디](study.md)에서 확인합니다.

## 후보 검토 제출

- `POST /api/studies/{studyId}/materials/{materialId}/reviews`는 인증된 스터디 멤버의 자료 단위 검토를 제출합니다.
- 요청은 자료에 속한 모든 후보를 정확히 한 번씩 포함해야 하며, 같은 멤버는 같은 자료의 검토를 다시 제출할 수 없습니다.
- 종료된 스터디와 이미 확정된 후보에는 새 검토를 제출할 수 없습니다.
- 후보별 승인 수가 스터디의 `reviewerCount` 이상이고 반려가 없으면 `ACCEPT`, 반려가 하나라도 있으면 `DECLINED`, 그 외에는 `PENDING`으로 판정합니다.
- `CandidateReviewService`가 권한·요청 완전성·중복 제출을 검증하고, 검토 저장과 후보 상태 변경을 하나의 트랜잭션에서 처리합니다.
