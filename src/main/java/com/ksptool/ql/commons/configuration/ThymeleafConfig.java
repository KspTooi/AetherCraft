package com.ksptool.ql.commons.configuration;

import com.ksptool.ql.commons.dialect.SecurityDialect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ThymeleafConfig {
    
    @Bean
    public SecurityDialect securityDialect() {
        return new SecurityDialect();
    }
} 