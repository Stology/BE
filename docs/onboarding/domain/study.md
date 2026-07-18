# 스터디

스터디 도메인은 현재 `domain/study/entity`의 JPA 모델로 구성됩니다. 컨트롤러·서비스·리포지터리 클래스는 이 패키지에서 확인되지 않습니다.

## 모델

- `Study`는 이름, 설명, 초대 링크, reviewer 수, 활성 상태, study leader를 가지며 `Template`을 다대일로 참조합니다.
- `MemberStudy`는 `Member`와 `Study`를 각각 다대일로 참조하는 참여 연결 엔티티입니다.
- `Question`은 `Study`를 다대일로 참조하고 제목, 내용, 회원 이름, 답변 수, 첨부 여부를 가집니다.
- `Answer`는 `Question`을 다대일로 참조하고 내용과 회원 이름을 가집니다.
- `QuestionImage`는 `Question`을, `AnswerImage`는 `Answer`를 각각 다대일로 참조하며 이미지 URL을 보관합니다.
- `Report`는 `Study`를 다대일로 참조합니다. 노드 수 집계와 JSON/text 컬럼의 주간 핵심 노드 목록, AI 검토 내용, 추천 노드 목록, 후속 내용, 회원 활동 통계를 가집니다.

모든 엔티티는 `BaseEntity`를 상속하여 생성·수정·삭제 시각 필드를 공유합니다.

## 관계 지도

```text
Template <- Study <- MemberStudy -> Member
                 <- Question <- Answer
                    <- QuestionImage   <- AnswerImage
                 <- Report
```

`StudyNode`는 노드 도메인에 있지만 `Study`를 다대일로 참조합니다. 스터디를 따라 노드 모델까지 볼 때는 [노드](node.md)를 함께 확인합니다.
