# 온보딩

이 문서는 현재 빌드와 설정의 사실을 요약합니다. 값이 필요한 환경 변수에는 비밀값을 문서나 저장소에 기록하지 않습니다.

## 실행 기반

- Java toolchain은 **17**이고 Spring Boot 플러그인 버전은 **4.1.0**입니다.
- Gradle Wrapper를 사용합니다. 예: `./gradlew test` (필요한 검증 범위가 정해진 경우에만 실행).
- `Dockerfile`은 빌드 단계에 `gradle:8.14-jdk17`, 실행 단계에 `eclipse-temurin:17-jdk`를 사용합니다.
- 주요 의존성 범주는 Spring Web/Security/Data JPA/Validation/OAuth2 Client/Data Redis, JWT, Springdoc OpenAPI, AWS S3, MySQL 런타임 드라이버, Lombok입니다.

## 설정과 프로필

기본 설정 파일은 `src/main/resources/application.yaml`이며 활성 프로필은 `local`입니다. 프로덕션 오버라이드는 `application-prod.yaml`에 있습니다. `application-local.yaml`의 존재를 전제하지 마십시오.

- 두 설정 모두 MySQL datasource와 JPA(MySQL dialect, SQL 표시/포맷), Kakao OAuth2, AWS 자격 증명, JWT secret 및 access/refresh 만료 설정을 둡니다.
- 기본 설정의 JPA `ddl-auto`는 `create`이고 Redis는 `localhost:6379`입니다. 로컬 프로필의 embedded Redis 설정은 `global/security/config/EmbeddedRedisConfig`에서 확인합니다.
- 프로덕션 설정의 JPA `ddl-auto`는 `update`이고 Redis host/port/password는 환경 변수로 받습니다. Kakao redirect URI는 `BASE_URL`을 사용하고 S3 path도 환경 변수로 받습니다.

## 환경 변수

설정에 나타나는 변수 이름만 기록합니다. 실제 값은 제공하거나 커밋하지 않습니다.

- 공통: `KAKAO_REST_API_KEY`, `KAKAO_REST_API_SECRET`, `DB_URL`, `DB_USER`, `DB_PW`, `AWS_ACCESS_KEY`, `AWS_SECRET_KEY`, `JWT_SECRET_KEY`
- 프로덕션 추가: `BASE_URL`, `REDIS_HOST`, `REDIS_PORT`, `REDIS_PW`, `S3_PATH`
- 선택 만료 설정: `JWT_ACCESS_EXPIRATION`, `JWT_REFRESH_EXPIRATION` (설정에 기본값이 있음)

## 다음 읽기

패키지와 공통 구성요소는 [도메인 온보딩](domain/README.md)에서, 구현·검토 기준은 [가이드](../guides/README.md)에서 확인합니다.
