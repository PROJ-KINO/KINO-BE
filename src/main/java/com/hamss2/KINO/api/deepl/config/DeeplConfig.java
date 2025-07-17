package com.hamss2.KINO.api.deepl.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "deepl")
@Getter
@Setter
public class DeeplConfig {
    private String apiKey;
    private String url;
}

//https://api-free.deepl.com/v2/translate