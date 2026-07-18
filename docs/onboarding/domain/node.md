# 노드

노드 도메인은 `domain/node/entity`의 템플릿·스터디 노드·후보 모델과 `CandidateState` 열거형으로 구성됩니다.

## 모델과 관계

- `Template`은 업로더 `Member`를 다대일로 참조하고 이름과 설명을 가집니다.
- `TemplateNode`는 `Template`을 다대일로 참조합니다.
- `StudyNode`는 `Study`를 다대일로 참조하고 `activeLevel`을 기본값 `0`으로 둡니다.
- `StudyMaterial`은 파일 URL을 보관합니다.
- `NodeCandidate`는 `StudyMaterial`과 `StudyNode`를 각각 다대일로 참조합니다. 상태는 `CandidateState`이며 기본값은 `PENDING`, accept count 기본값은 `0`입니다.

모든 엔티티는 `BaseEntity`를 상속합니다.

## 관계 지도

```text
Member <- Template <- TemplateNode
Study  <- StudyNode <- NodeCandidate -> StudyMaterial
```

`Study.template`은 `Template`을 다대일로 참조하고, `StudyNode.study`는 `Study`를 다대일로 참조합니다. 스터디의 질문·답변·리포트 모델은 [스터디](study.md)에서 확인합니다.
