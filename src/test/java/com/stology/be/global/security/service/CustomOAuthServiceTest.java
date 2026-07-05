package com.stology.be.global.security.service;

import com.stology.be.domain.member.entity.Member;
import com.stology.be.domain.member.enums.SocialType;
import com.stology.be.domain.member.exception.MemberException;
import com.stology.be.domain.member.exception.code.MemberErrorCode;
import com.stology.be.domain.member.repository.MemberRepository;
import com.stology.be.global.security.entity.OAuthMember;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.client.RestOperations;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Unit tests for {@link CustomOAuthService}. Real network access is avoided by injecting a
 * mocked {@link RestOperations} into the underlying {@code DefaultOAuth2UserService}, which
 * short-circuits the HTTP call made by {@code super.loadUser(...)}.
 */
@ExtendWith(MockitoExtension.class)
class CustomOAuthServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RestOperations restOperations;

    private CustomOAuthService customOAuthService;

    @BeforeEach
    void setUp() {
        customOAuthService = new CustomOAuthService(memberRepository);
        customOAuthService.setRestOperations(restOperations);
    }

    @Test
    void loadUser_existingKakaoMember_returnsOAuthMemberWrappingExistingMember() {
        stubUserInfoResponse("123456789", "tester@kakao.com", "tester");
        Member existingMember = Member.builder()
                .id(1L)
                .name("tester")
                .socialType(SocialType.KAKAO)
                .socialUid("123456789")
                .email("tester@kakao.com")
                .build();
        given(memberRepository.findBySocialTypeAndSocialUid(SocialType.KAKAO, "123456789"))
                .willReturn(Optional.of(existingMember));

        OAuth2User result = customOAuthService.loadUser(createUserRequest("kakao"));

        assertThat(result).isInstanceOf(OAuthMember.class);
        assertThat(((OAuthMember) result).getMember()).isSameAs(existingMember);
        verify(memberRepository, never()).save(ArgumentMatchers.any());
    }

    @Test
    void loadUser_newKakaoMember_savesAndReturnsNewMember() {
        stubUserInfoResponse("123456789", "tester@kakao.com", "tester");
        given(memberRepository.findBySocialTypeAndSocialUid(SocialType.KAKAO, "123456789"))
                .willReturn(Optional.empty());

        OAuth2User result = customOAuthService.loadUser(createUserRequest("kakao"));

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(memberCaptor.capture());
        Member saved = memberCaptor.getValue();
        assertThat(saved.getName()).isEqualTo("tester");
        assertThat(saved.getSocialType()).isEqualTo(SocialType.KAKAO);
        assertThat(saved.getSocialUid()).isEqualTo("123456789");
        assertThat(saved.getEmail()).isEqualTo("tester@kakao.com");

        assertThat(result).isInstanceOf(OAuthMember.class);
        assertThat(((OAuthMember) result).getMember()).isSameAs(saved);
    }

    @Test
    void loadUser_unsupportedSocialProvider_throwsMemberExceptionWithoutTouchingRepository() {
        stubUserInfoResponse("123456789", "tester@kakao.com", "tester");

        OAuth2UserRequest request = createUserRequest("google");

        assertThatThrownBy(() -> customOAuthService.loadUser(request))
                .isInstanceOf(MemberException.class)
                .extracting(ex -> ((MemberException) ex).getErrorCode())
                .isEqualTo(MemberErrorCode.MEMBER_NOT_SUPPORTED_SOCIAL_PROVIDER);

        verifyNoInteractions(memberRepository);
    }

    private void stubUserInfoResponse(String id, String email, String nickname) {
        Map<String, Object> profile = Map.of("nickname", nickname);
        Map<String, Object> kakaoAccount = Map.of("email", email, "profile", profile);
        Map<String, Object> responseAttributes = Map.of("id", Long.parseLong(id), "kakao_account", kakaoAccount);

        given(restOperations.exchange(
                ArgumentMatchers.<RequestEntity<?>>any(),
                ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any()))
                .willReturn(new ResponseEntity<>(responseAttributes, HttpStatus.OK));
    }

    private OAuth2UserRequest createUserRequest(String registrationId) {
        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId(registrationId)
                .clientId("test-client-id")
                .clientSecret("test-client-secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost:8080/login/oauth2/code/" + registrationId)
                .authorizationUri("http://kauth.kakao.com/oauth/authorize")
                .tokenUri("http://kauth.kakao.com/oauth/token")
                .userInfoUri("http://kapi.kakao.com/v2/user/me")
                .userNameAttributeName("id")
                .clientName(registrationId)
                .build();

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "test-access-token",
                Instant.now(),
                Instant.now().plusSeconds(3600));

        return new OAuth2UserRequest(clientRegistration, accessToken);
    }
}