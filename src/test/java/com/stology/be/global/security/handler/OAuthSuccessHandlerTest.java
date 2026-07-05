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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OAuthSuccessHandlerTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private Authentication contextAuthentication;

    private OAuthSuccessHandler handler;

    @BeforeEach
    void setUp() {
        handler = new OAuthSuccessHandler(jwtUtil);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void onAuthenticationSuccess_writesSuccessResponseWithTokens() throws Exception {
        Member member = Member.builder().name("Kakao User").socialType(SocialType.KAKAO)
                .socialUid("uid-1").email("test@example.com").build();
        OAuthMember principal = new OAuthMember(member, Map.of("id", 1L));

        when(contextAuthentication.getPrincipal()).thenReturn(principal);
        SecurityContextHolder.getContext().setAuthentication(contextAuthentication);

        when(jwtUtil.createAccessToken(any(AuthMember.class))).thenReturn("access-token-123");
        when(jwtUtil.createRefreshToken(any(AuthMember.class))).thenReturn("refresh-token-456");

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(request, response, null);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentType()).contains("application/json");

        String body = response.getContentAsString();
        assertThat(body).contains("MEMBER200_1");
        assertThat(body).contains("access-token-123");
        assertThat(body).contains("refresh-token-456");
    }

    @Test
    void onAuthenticationSuccess_handlesNullMemberOnPrincipal() throws Exception {
        OAuthMember principal = new OAuthMember(null, Map.of());
        when(contextAuthentication.getPrincipal()).thenReturn(principal);
        SecurityContextHolder.getContext().setAuthentication(contextAuthentication);

        when(jwtUtil.createAccessToken(any(AuthMember.class))).thenReturn("access-token");
        when(jwtUtil.createRefreshToken(any(AuthMember.class))).thenReturn("refresh-token");

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(request, response, null);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).contains("access-token", "refresh-token");
    }
}