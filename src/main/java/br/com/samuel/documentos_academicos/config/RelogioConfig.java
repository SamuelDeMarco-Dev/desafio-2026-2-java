package br.com.samuel.documentos_academicos.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RelogioConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}