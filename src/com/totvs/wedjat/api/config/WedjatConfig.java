package com.totvs.wedjat.api.config;

import com.totvs.wedjat.application.WedjatSystem;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WedjatConfig {

    @Bean
    WedjatSystem wedjatSystem() {
        return new WedjatSystem();
    }
}
