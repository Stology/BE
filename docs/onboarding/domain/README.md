# 도메인과 공통 구성요소

애플리케이션 패키지 루트는 `src/main/java/com/stology/be`입니다. 기능별 코드는 `domain` 아래에 있고 공통 구성요소는 `global` 아래에 있습니다. 이 문서는 현재 배치를 찾기 위한 지도이며 새 계층이나 책임을 정하지 않습니다.

## 도메인

- [인증](auth.md): `domain/auth`와 OAuth2·JWT·Redis 인증 구성요소의 탐색 경로
- [회원](member.md): `domain/member`의 회원 모델과 식별
- [스터디](study.md): `domain/study`의 JPA 모델
- [노드](node.md): `domain/node`의 템플릿·스터디 노드 모델

## 공통 탐색 경로

- 공통 엔티티 필드는 `global/entity/BaseEntity.java`에 있습니다.
- API 성공 응답과 오류 표현은 `global/apiPayload/ApiResponse.java`, `global/apiPayload/code/**`, `global/apiPayload/exception/**`에 있고 예외 변환은 `global/apiPayload/handler/GeneralExceptionAdvice.java`에 있습니다.
- 보안 filter chain과 OAuth2/JWT 구성은 `global/security/config/SecurityConfig.java`, `global/security/service/**`, `global/security/filter/JwtAuthFilter.java`, `global/security/handler/**`, `global/security/util/JwtUtil.java`에서 확인합니다.

현재 관찰된 코드 배치와 검토 시 구분할 기준은 [코드 관례](../../guides/code-conventions.md), 문서 갱신 조건은 [PR 검토](../../guides/pr-review.md)를 따릅니다.
