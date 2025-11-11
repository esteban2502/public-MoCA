package com.ucp.moca.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class Config implements WebMvcConfigurer {

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {

        if(allowedOrigins.equals("*")){
            registry.addMapping("/**")
                    .allowedOriginPatterns("*")
                    .allowedMethods("GET", "POST", "PUT","PATCH","DELETE")
                    .allowedHeaders("*")
                    .exposedHeaders("Content-Disposition")
                    .allowCredentials(true);
        }else{
            registry.addMapping("/**")
                    .allowedOrigins(allowedOrigins.split(","))
                    .allowedMethods("GET", "POST", "PUT","PATCH","DELETE")
                    .allowedHeaders("*")
                    .exposedHeaders("Content-Disposition")
                    .allowCredentials(true);
        }

    }

}