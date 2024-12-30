package com.digitalstate.gateway;

import com.digitalstate.gateway.config.AuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GatewayApplication {

    @Autowired
    AuthFilter authFilter;

    @Value("${ms.auth.url}")
    private String authUrl;

    @Value("${ms.profilemanager.url}")
    private String msProfileManager;


    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
    @Bean
    public RouteLocator myRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(p -> p
                        .path("/api/profile-manager/**")
                        .filters(f -> f.filter(authFilter).rewritePath("/api/profile-manager(?<segment>/?.*)", "$\\{segment}"))
                        .uri(msProfileManager))
                .route(p -> p
                        .path("/api/auth/**")
                        .filters(f -> f.rewritePath("/api/auth(?<segment>/?.*)", "$\\{segment}"))
                        .uri(authUrl))
                .build();
    }
}
