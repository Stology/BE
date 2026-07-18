# 코드 관례

아래는 현재 Stology Backend 코드에서 관찰한 사실입니다. 새 구조를 강제하는 규칙이 아니며, 변경 전에는 대상 소스를 다시 확인합니다. 작업 정책은 [AGENTS.md](../../AGENTS.md), 패키지 지도는 [도메인 온보딩](../onboarding/domain/README.md)을 따릅니다.

## 관찰된 배치

- 기능 코드는 `src/main/java/com/stology/be/domain` 아래 `auth`, `member`, `study`, `node`에 있고 공통 코드는 `global`에 있습니다.
- 인증 API는 `domain/auth/controller/AuthController.java`처럼 `controller`, `service`, `dto`, `repository`, `exception`으로 나뉜 경로를 사용합니다. 모든 도메인에 같은 구성이 존재한다고 가정하지 않습니다.
- JPA 모델은 `entity`에 두며, 예를 들어 `domain/member/entity/Member.java`는 Lombok 애너테이션과 JPA 애너테이션을 함께 사용합니다.
- API 응답과 예외 표현은 `global/apiPayload` 아래 타입과 `GeneralExceptionAdvice`를 통해 연결됩니다.

## 변경 시 확인

기존 도메인 안에서 변경할 때는 인접한 클래스의 패키지, DTO 형태, 응답·예외 코드를 먼저 따릅니다. 보안·설정·공개 경로는 추정하지 말고 `global/security` 및 `src/main/resources`의 실제 정의를 확인합니다. 현재 사실의 상세는 [온보딩](../onboarding/README.md)과 해당 도메인 문서에 기록합니다.
