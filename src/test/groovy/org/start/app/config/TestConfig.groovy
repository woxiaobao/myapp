package org.start.app.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.web.filter.CharacterEncodingFilter
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.core.Ordered

@TestConfiguration
@EnableAutoConfiguration(exclude = [
    DataSourceAutoConfiguration,
    DataSourceTransactionManagerAutoConfiguration,
    HibernateJpaAutoConfiguration
])
class TestConfig {
    
    @Bean
    CharacterEncodingFilter characterEncodingFilter() {
        return new CharacterEncodingFilter("UTF-8", true)
    }

    @Bean
    FilterRegistrationBean clientAbortFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean()
        registration.setFilter(new org.springframework.web.filter.OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(
                    javax.servlet.http.HttpServletRequest request,
                    javax.servlet.http.HttpServletResponse response,
                    javax.servlet.FilterChain filterChain) throws javax.servlet.ServletException, java.io.IOException {
                try {
                    filterChain.doFilter(request, response)
                } catch (Exception e) {
                    if (!isClientAbortException(e)) {
                        throw e
                    }
                }
            }

            private boolean isClientAbortException(Exception e) {
                return e instanceof org.apache.catalina.connector.ClientAbortException ||
                       (e.cause instanceof java.io.IOException && 
                        e.cause.message == "Connection reset by peer")
            }
        })
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE)
        return registration
    }
} 