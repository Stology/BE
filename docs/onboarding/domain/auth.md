# 인증

인증 코드는 `domain/auth`와 `global/security`에 나뉘어 있습니다. API 흐름을 볼 때는 `AuthController`와 `AuthService`부터, 보안 체인을 확인할 때는 `SecurityConfig`부터 읽습니다.

## 구성요소

- `AuthController`는 `/api/auth` 아래에서 토큰 재발급(`POST /reissue`), 로그아웃(`POST /logout`), 회원 탈퇴(`DELETE /delete`) 요청을 받고 refresh token 쿠키를 읽어 `AuthService`에 위임합니다. 재발급과 로그아웃·탈퇴 뒤에는 refresh token 쿠키를 설정하거나 만료합니다.
- `AuthService`는 `JwtUtil`, `RefreshTokenRepository`, `MemberRepository`를 사용합니다. 재발급 시 JWT와 Redis 저장값을 확인하고 기존 저장값을 삭제한 뒤 새 토큰 쌍을 발급·저장합니다. 로그아웃은 JWT subject로 저장된 refresh token을 삭제하며, 탈퇴는 그 삭제 후 `AuthMember`의 `Member`를 soft delete합니다.
- `RefreshTokenRepository`는 저장·조회·삭제 추상화이고, `RedisRefreshTokenRepository`가 `StringRedisTemplate`으로 `refresh:{uid}` 키와 TTL을 구현합니다.
- `JwtUtil`은 `AuthMember`로 access/refresh JWT를 만들고, JWT의 subject와 `social_type` claim을 읽거나 유효성을 검사합니다.
- `JwtAuthFilter`는 `Authorization: Bearer ...` 토큰을 검증한 뒤 `CustomUserDetailsService`로 회원을 읽어 `AuthMember` 인증을 `SecurityContext`에 설정합니다.

## OAuth 로그인 흐름

1. `SecurityConfig`는 stateless filter chain에 OAuth2 로그인을 설정하고 `CustomOAuthService`, `OAuthSuccessHandler`, `JwtAuthFilter`를 연결합니다.
2. `CustomOAuthService`는 OAuth2 사용자 정보를 받아 현재 구현에서 Kakao 정보를 `OAuthDTO`/`KakaoDTO`로 매핑합니다. `MemberRepository`에서 social type과 UID로 회원을 찾거나 `MemberConverter.toMember`로 생성·저장하고, soft-deleted 회원은 복구한 뒤 `OAuthMember`로 반환합니다.
3. `OAuthSuccessHandler`는 `OAuthMember`의 `Member`로 `AuthMember`를 만들고 `JwtUtil`로 토큰을 발급합니다. refresh token은 `HttpOnly`, `Secure`, `SameSite=None` 속성의 쿠키와 `RefreshTokenRepository`에 저장하고, access token은 `MemberConverter.toLogin` 결과로 응답합니다. `POST /api/auth/reissue`도 새 refresh token을 같은 속성의 쿠키로 교체합니다.

`AuthMember`는 `Member`를 감싼 `UserDetails`이며 username으로 social UID를 사용합니다. `OAuthMember`는 같은 회원과 OAuth 속성을 감싼 `OAuth2User`입니다.

## 로그아웃 경로의 중복 설정

`AuthController`와 `SecurityConfig` 모두 `/api/auth/logout`을 선언합니다. 컨트롤러는 `AuthService.logout`을 호출해 refresh token을 삭제하고 성공 응답을 만들며, `SecurityConfig`의 logout 설정은 같은 URL에 `CustomLogoutSuccessHandler`를 연결합니다. 이 핸들러도 유효한 refresh token이면 저장값을 삭제하고 쿠키 만료 및 응답 작성을 수행합니다. 두 선언의 실제 요청 처리 우선순위나 최종 컨트롤러 계약은 이 코드만으로 단정하지 말고 Spring Security 실행 설정과 통합 동작으로 확인합니다.
