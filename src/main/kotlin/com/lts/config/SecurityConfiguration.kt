package com.lts.config

import com.lts.security.ADMIN
import com.lts.security.jwt.JWTFilter
import com.lts.security.jwt.TokenProvider
import tech.jhipster.config.JHipsterProperties
import org.springframework.boot.autoconfigure.security.SecurityProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter.Mode
import org.springframework.security.web.server.header.ReferrerPolicyServerHttpHeadersWriter
import org.springframework.security.web.server.savedrequest.NoOpServerRequestCache
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher
import org.springframework.util.StringUtils
import org.springframework.web.cors.reactive.CorsWebFilter
import org.zalando.problem.spring.webflux.advice.security.SecurityProblemSupport

import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers

@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@Import(SecurityProblemSupport::class)
class SecurityConfiguration(
    private val tokenProvider: TokenProvider,
    private val jHipsterProperties: JHipsterProperties,
    private val problemSupport: SecurityProblemSupport,
    private val corsWebFilter: CorsWebFilter
) {

    @Bean
    fun userDetailsService(properties: SecurityProperties): MapReactiveUserDetailsService {
        val user = properties.user
        val userDetails = User
            .withUsername(user.name)
            .password("{noop}${user.password}")
            .roles(*StringUtils.toStringArray(user.roles))
            .build()
        return MapReactiveUserDetailsService(userDetails)
    }

    @Bean
    fun reactiveAuthenticationManager(userDetailsService: ReactiveUserDetailsService): ReactiveAuthenticationManager {
        return UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService)
    }

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        // @formatter:off
        http
            .securityMatcher(
                NegatedServerWebExchangeMatcher(
                    OrServerWebExchangeMatcher(
                        pathMatchers("/app/**", "/_app/**", "/i18n/**", "/img/**", "/content/**", "/swagger-ui/**", "/v3/api-docs/**", "/test/**"),
                        pathMatchers(HttpMethod.OPTIONS, "/**")
                    )
                )
            )
            .csrf()
            .disable()
            .addFilterBefore(corsWebFilter, SecurityWebFiltersOrder.REACTOR_CONTEXT)
            .addFilterAt(JWTFilter(tokenProvider), SecurityWebFiltersOrder.HTTP_BASIC)
            .exceptionHandling()
            .accessDeniedHandler(problemSupport)
            .authenticationEntryPoint(problemSupport)
        .and()
            .headers()
            .contentSecurityPolicy(jHipsterProperties.security.contentSecurityPolicy)
        .and()
            .referrerPolicy(ReferrerPolicyServerHttpHeadersWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
        .and()
            .permissionsPolicy().policy("camera=(), fullscreen=(self), geolocation=(), gyroscope=(), magnetometer=(), microphone=(), midi=(), payment=(), sync-xhr=()")
        .and()
            .frameOptions().mode(Mode.DENY)
        .and()
            .requestCache()
            .requestCache(NoOpServerRequestCache.getInstance())
        .and()
            .authorizeExchange()
            .pathMatchers("/api/authenticate").permitAll()
            .pathMatchers("/api/admin/**").hasAuthority(ADMIN)
            .pathMatchers("/api/**").authenticated()
            .pathMatchers("/management/health").permitAll()
            .pathMatchers("/management/info").permitAll()
            .pathMatchers("/management/prometheus").permitAll()
            .pathMatchers("/management/**").hasAuthority(ADMIN)
        // @formatter:on
        return http.build()
    }
}
