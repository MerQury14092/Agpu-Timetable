package com.merqury.agpu.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Configuration
public class CryptConfig {
    @Bean
    public MessageDigest digest() throws NoSuchAlgorithmException {
        return MessageDigest.getInstance("SHA-256");
    }
}
