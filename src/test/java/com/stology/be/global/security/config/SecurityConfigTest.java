package com.stology.be.global.security.config;

import com.stology.be.global.security.service.CustomOAuthService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.web.SecurityFilterChain;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigTest {

    @Test
    void constructor_acceptsCustomOAuthService() {
        CustomOAuthService mockService = Mockito.mock(CustomOAuthService.class);

        SecurityConfig config = new SecurityConfig(mockService);

        assertThat(config).isNotNull();
    }

    @Test
    void filterChainBean_isBuiltSuccessfully_withinSecurityContext() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.register(SecurityConfig.class, TestSupportConfig.class);
            context.refresh();

            SecurityFilterChain filterChain = context.getBean(SecurityFilterChain.class);

            assertThat(filterChain).isNotNull();
            assertThat(filterChain.getFilters()).isNotEmpty();
        }
    }

    @Configuration
    static class TestSupportConfig {

        @Bean
        CustomOAuthService customOAuthService() {
            return Mockito.mock(CustomOAuthService.class);
        }

        @Bean
        ClientRegistrationRepository clientRegistrationRepository() {
            ClientRegistration registration = ClientRegistration.withRegistrationId("kakao")
                    .clientId("test-client-id")
                    .clientSecret("test-client-secret")
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri("http://localhost:8080/login/oauth2/code/kakao")
                    .authorizationUri("https://kauth.kakao.com/oauth/authorize")
                    .tokenUri("https://kauth.kakao.com/oauth/token")
                    .userInfoUri("https://kapi.kakao.com/v2/user/me")
                    .userNameAttributeName("id")
                    .clientName("Kakao")
                    .build();
            return new InMemoryClientRegistrationRepository(registration);
        }
    }
}