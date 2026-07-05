package com.stology.be.global.security.service;

import com.stology.be.domain.member.entity.Member;
import com.stology.be.domain.member.enums.SocialType;
import com.stology.be.domain.member.exception.MemberException;
import com.stology.be.domain.member.exception.code.MemberErrorCode;
import com.stology.be.domain.member.repository.MemberRepository;
import com.stology.be.global.security.entity.OAuthMember;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
class CustomOAuthServiceTest {

    private static final String KAKAO_USER_INFO_URI = "https://kapi.kakao.com/v2/user/me";
    private static final String KAKAO_USER_INFO_JSON = """
            {
              "id": 1234567890123,
              "kakao_account": {
                "email": "kakao@example.com",
                "profile": {
                  "nickname": "KakaoNick"
                }
              }
            }
            """;

    @Mock
    private MemberRepository memberRepository;

    private CustomOAuthService customOAuthService;

    private CustomOAuthService withMockRestServer(String userInfoUri, String jsonResponse) {
        CustomOAuthService service = new CustomOAuthService(memberRepository);
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo(userInfoUri))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));
        service.setRestOperations(restTemplate);
        return service;
    }

    private ClientRegistration kakaoRegistration() {
        return ClientRegistration.withRegistrationId("kakao")
                .clientId("test-client-id")
                .clientSecret("test-client-secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost:8080/login/oauth2/code/kakao")
                .authorizationUri("https://kauth.kakao.com/oauth/authorize")
                .tokenUri("https://kauth.kakao.com/oauth/token")
                .userInfoUri(KAKAO_USER_INFO_URI)
                .userNameAttributeName("id")
                .clientName("Kakao")
                .build();
    }

    private OAuth2UserRequest kakaoUserRequest() {
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER, "test-access-token",
                Instant.now(), Instant.now().plusSeconds(3600));
        return new OAuth2UserRequest(kakaoRegistration(), accessToken);
    }

    @Test
    void loadUser_createsNewMember_whenNotFoundInRepository() {
        customOAuthService = withMockRestServer(KAKAO_USER_INFO_URI, KAKAO_USER_INFO_JSON);
        when(memberRepository.findBySocialTypeAndSocialUid(SocialType.KAKAO, "1234567890123"))
                .thenReturn(Optional.empty());

        OAuth2User result = customOAuthService.loadUser(kakaoUserRequest());

        assertThat(result).isInstanceOf(OAuthMember.class);
        Member savedNewMember = ((OAuthMember) result).getMember();
        assertThat(savedNewMember.getName()).isEqualTo("KakaoNick");
        assertThat(savedNewMember.getEmail()).isEqualTo("kakao@example.com");
        assertThat(savedNewMember.getSocialUid()).isEqualTo("1234567890123");
        assertThat(savedNewMember.getSocialType()).isEqualTo(SocialType.KAKAO);

        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(captor.capture());
        assertThat(captor.getValue().getSocialUid()).isEqualTo("1234567890123");
    }

    @Test
    void loadUser_returnsExistingMember_whenFoundInRepository() {
        customOAuthService = withMockRestServer(KAKAO_USER_INFO_URI, KAKAO_USER_INFO_JSON);
        Member existingMember = Member.builder()
                .id(42L)
                .name("Existing Name")
                .socialType(SocialType.KAKAO)
                .socialUid("1234567890123")
                .email("existing@example.com")
                .build();
        when(memberRepository.findBySocialTypeAndSocialUid(SocialType.KAKAO, "1234567890123"))
                .thenReturn(Optional.of(existingMember));

        OAuth2User result = customOAuthService.loadUser(kakaoUserRequest());

        assertThat(((OAuthMember) result).getMember()).isEqualTo(existingMember);
        verify(memberRepository, never()).save(any());
    }

    @Test
    void loadUser_returnsOAuthMemberExposingOriginalAttributes() {
        customOAuthService = withMockRestServer(KAKAO_USER_INFO_URI, KAKAO_USER_INFO_JSON);
        when(memberRepository.findBySocialTypeAndSocialUid(any(), any())).thenReturn(Optional.empty());

        OAuth2User result = customOAuthService.loadUser(kakaoUserRequest());

        assertThat(result.getAttributes()).containsKey("kakao_account");
        assertThat(result.getAttributes()).containsKey("id");
    }

    @Test
    void loadUser_throwsMemberException_forUnsupportedProvider() {
        String naverUserInfoUri = "https://openapi.naver.com/v1/nid/me";
        customOAuthService = withMockRestServer(naverUserInfoUri, KAKAO_USER_INFO_JSON);

        ClientRegistration naverRegistration = ClientRegistration.withRegistrationId("naver")
                .clientId("test-client-id")
                .clientSecret("test-client-secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost:8080/login/oauth2/code/naver")
                .authorizationUri("https://nid.naver.com/oauth2.0/authorize")
                .tokenUri("https://nid.naver.com/oauth2.0/token")
                .userInfoUri(naverUserInfoUri)
                .userNameAttributeName("id")
                .clientName("Naver")
                .build();
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER, "test-access-token",
                Instant.now(), Instant.now().plusSeconds(3600));
        OAuth2UserRequest naverUserRequest = new OAuth2UserRequest(naverRegistration, accessToken);

        assertThatThrownBy(() -> customOAuthService.loadUser(naverUserRequest))
                .isInstanceOf(MemberException.class)
                .satisfies(ex -> assertThat(((MemberException) ex).getErrorCode())
                        .isEqualTo(MemberErrorCode.MEMBER_NOT_SUPPORTED_SOCIAL_PROVIDER));
    }
}