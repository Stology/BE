# Stology Backend 문서

현재 코드와 설정을 탐색하기 위한 인덱스입니다. 정책 정본은 루트의 [AGENTS.md](../AGENTS.md)이며, 상세 사실은 아래 소유 문서에서 관리합니다.

## 권장 읽기 경로

1. 처음 참여하거나 환경을 확인할 때: [온보딩](onboarding/README.md)
   - [데이터베이스 스키마](onboarding/database-schema.md)
2. 기능 변경 전: [도메인 지도](onboarding/domain/README.md)와 해당 도메인 문서
   - [인증](onboarding/domain/auth.md)
   - [회원](onboarding/domain/member.md)
   - [스터디](onboarding/domain/study.md)
   - [노드](onboarding/domain/node.md)
3. 구현·검토 시: [가이드](guides/README.md)
4. 실제로 확정된 되돌리기 어려운 결정을 기록할 때: [ADR 안내](adr/README.md)

## 문서 소유권

- 온보딩은 실행 환경과 현재 구조의 사실을 설명합니다.
- 데이터베이스 스키마 문서는 현재 JPA 엔티티와 설정에서 관찰되는 관계형 저장 구조를 설명합니다.
- 도메인 문서는 각 패키지의 현재 모델과 탐색 경로를 설명합니다.
- 가이드는 코드·문서 갱신 및 검토 기준을 제공합니다.
- ADR은 미래의 실제 결정을 기록하는 형식만 제공합니다. 기존 구현의 의도를 소급해 확정하지 않습니다.
