package com.stology.be.global.security.config;

import com.stology.be.global.security.service.CustomOAuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private CustomOAuthService customOAuthService;

    @Test
    void constructor_storesProvidedCustomOAuthService() throws NoSuchFieldException, IllegalAccessException {
        SecurityConfig securityConfig = new SecurityConfig(customOAuthService);

        Field field = SecurityConfig.class.getDeclaredField("customOAuthService");
        field.setAccessible(true);

        assertThat(field.get(securityConfig)).isSameAs(customOAuthService);
    }

    @Test
    void filterChain_configuresAuthorizationOAuth2LoginAndLogoutThenBuildsChain() throws Exception {
        HttpSecurity httpSecurity = mock(HttpSecurity.class, Answers.RETURNS_SELF);
        SecurityFilterChain expectedChain = mock(SecurityFilterChain.class);
        given(httpSecurity.build()).willReturn(expectedChain);

        SecurityConfig securityConfig = new SecurityConfig(customOAuthService);
        SecurityFilterChain result = securityConfig.filterChain(httpSecurity);

        assertThat(result).isSameAs(expectedChain);
        verify(httpSecurity).authorizeHttpRequests(any());
        verify(httpSecurity).oauth2Login(any());
        verify(httpSecurity).logout(any());
        verify(httpSecurity).build();
    }
}