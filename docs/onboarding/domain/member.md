# 회원

회원 도메인은 `domain/member`에 있고, OAuth 로그인과 JWT 인증이 이 모델을 참조합니다.

## 모델과 저장소

- `Member`는 `BaseEntity`를 상속하는 JPA 엔티티입니다. 이름, `SocialType`, social UID, 이메일을 보유하며 `softDelete()`와 `restore()`로 `deletedAt`을 설정하거나 해제합니다.
- `MemberRepository`는 `JpaRepository<Member, Long>`이며 social type과 social UID 조합, 또는 social UID로 회원을 조회합니다.
- `MemberConverter`는 `OAuthDTO`를 `Member`로 변환하고 access token을 `AuthResDTO.Login`으로 변환합니다.
- `SocialType`은 회원의 소셜 제공자 식별에 쓰입니다. 현재 `CustomOAuthService`의 제공자 처리에는 Kakao 분기가 있습니다.
- `MemberException`과 `MemberErrorCode`/`MemberSuccessCode`는 회원 도메인의 예외·응답 코드를 둡니다.

## 관계와 흐름

- OAuth 로그인에서 `CustomOAuthService`는 `MemberRepository.findBySocialTypeAndSocialUid`로 회원을 찾고, 없으면 `MemberConverter.toMember` 결과를 저장합니다. 이미 soft-deleted 상태인 회원은 `restore()`합니다.
- `OAuthMember`와 `AuthMember`는 각각 OAuth2와 Spring Security 인증에서 `Member`를 감쌉니다. `JwtAuthFilter`는 JWT의 social type·UID로 `CustomUserDetailsService`를 호출하고, 이 서비스가 `MemberRepository`에서 회원을 찾아 `AuthMember`를 만듭니다.
- `AuthService.deleteMember`는 인증된 `AuthMember`가 보유한 `Member`에 `softDelete()`를 호출합니다.
- `MemberStudy`는 `Member`와 `Study`를 각각 다대일로 참조하여 스터디 참여 연결을 표현합니다. `Template`은 업로더를 `Member`로 다대일 참조합니다.
