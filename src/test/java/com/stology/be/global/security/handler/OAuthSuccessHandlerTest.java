package com.stology.be.global.security.handler;

import com.stology.be.domain.member.entity.Member;
import com.stology.be.domain.member.enums.SocialType;
import com.stology.be.global.security.entity.AuthMember;
import com.stology.be.global.security.entity.OAuthMember;
import com.stology.be.global.security.util.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OAuthSuccessHandlerTest {

    @Mock
    private JwtUtil jwtUtil;

    private OAuthSuccessHandler handler;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        handler = new OAuthSuccessHandler(jwtUtil);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void onAuthenticationSuccess_writesJsonResponseWithTokensAndSuccessCode() throws Exception {
        Member member = Member.builder()
                .id(1L)
                .name("tester")
                .socialType(SocialType.KAKAO)
                .socialUid("uid-1")
                .email("test@test.com")
                .build();
        OAuthMember oAuthMember = new OAuthMember(member, Map.of("id", 1L));
        Authentication authentication = new TestingAuthenticationToken(oAuthMember, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        given(jwtUtil.createAccessToken(any(AuthMember.class))).willReturn("access-token-value");
        given(jwtUtil.createRefreshToken(any(AuthMember.class))).willReturn("refresh-token-value");

        handler.onAuthenticationSuccess(request, response, authentication);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentType()).contains("application/json");
        String body = response.getContentAsString();
        assertThat(body).contains("access-token-value");
        assertThat(body).contains("refresh-token-value");
        assertThat(body).contains("MEMBER200_1");
    }
}