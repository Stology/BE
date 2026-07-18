# PR 검토

이 문서는 팀 검토 기준입니다. 현재 코드의 배치는 [코드 관례](code-conventions.md), 작업 정책은 [AGENTS.md](../../AGENTS.md), 문서 위치는 [문서 인덱스](../README.md)에서 확인합니다.

## 자동으로 확인할 항목

자동화 가능한 범위에서는 변경한 코드의 컴파일·테스트·정적 검사 결과와 설정 형식을 확인합니다. 실패를 숨기거나 검사를 사람의 판단으로 대체하지 않습니다. 실행할 명령과 범위는 변경 내용과 [온보딩](../onboarding/README.md)의 실행 기반에 맞춥니다.

## 사람이 판단할 항목

### 문서

- 수정한 Markdown 링크와 heading anchor가 유효하고 [문서 인덱스](../README.md)의 탐색 경로가 유지되는지 확인합니다.
- 현재 소스·설정과 대조해 API, 인증, 설정 설명을 동기화하고, 상세 사실은 해당 소유 문서에만 둡니다.
- 문서 변경이 소유 경계를 바꾸거나 되돌리기 어려운 결정을 확정하면 [ADR](../adr/README.md) 조건을 검토합니다.

### 코드·인증·설정

- 변경이 `domain` 또는 `global`의 현재 책임과 탐색 경로를 불필요하게 흐리지 않는지 확인합니다.
- API 응답·오류는 관련 소스로, 인증 흐름·쿠키 속성은 보안 구현으로, 설정 키·프로필은 설정 파일로 각각 확인합니다.
- 코드·설정 변경에 따라 [온보딩](../onboarding/README.md), 도메인 문서, 이 가이드의 갱신이 필요한지 확인합니다.

## 기록: 문서 전용 정비의 검증 경계

아래는 과거 문서 전용 정비 PR에만 적용한 허용 범위이며, 모든 제품 변경의 규칙이 아닙니다. 당시 허용한 16개 문서는 `AGENTS.md`, `CLAUDE.md`, `GEMINI.md`, `.github/copilot-instructions.md`, `.gemini/styleguide.md`, `docs/README.md`, `docs/onboarding/README.md`, `docs/onboarding/domain/README.md`, `docs/onboarding/domain/auth.md`, `docs/onboarding/domain/member.md`, `docs/onboarding/domain/study.md`, `docs/onboarding/domain/node.md`, `docs/adr/README.md`, `docs/guides/README.md`, `docs/guides/code-conventions.md`, `docs/guides/pr-review.md`입니다.

현재처럼 Markdown만 수정하는 후속 정정은 해당 문서 링크·anchor와 허용 파일 diff를 확인합니다. 제품 소스·설정·빌드 산출물에 영향을 주지 않으므로 그 문서 전용 범위에서는 Gradle 테스트·빌드·formatter를 요구하지 않습니다. 코드·설정 변경 PR에는 이 예외를 적용하지 않고 변경 범위에 맞는 자동 검사를 실행합니다.
