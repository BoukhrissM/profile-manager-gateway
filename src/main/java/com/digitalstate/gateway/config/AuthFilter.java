package com.digitalstate.gateway.config;

import com.digitalstate.gateway.model.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
@RefreshScope
@Component
public class AuthFilter implements GatewayFilter {

    private final RouterVAlidator routerValidator;//custom route validator
    private final WebClient.Builder webClientBuilder;

    @Value("${ms.auth.url}")
    private String authUrl;
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        if(!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            throw new RuntimeException("Authorization header is missing in request");
        }
        String authHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
        String[] parts = authHeader.split(" ");
        if(parts.length != 2 || !"Bearer".equals(parts[0])) {
            throw new RuntimeException("Incorrect Auth structure");
        }

        return webClientBuilder.build().post()
                .uri(authUrl + "validate-token?token=" + parts[1])
                .retrieve().bodyToMono(UserDto.class)
                .map(userDto -> {
                    exchange.getRequest()
                            .mutate()
                            .header("x-auth-user-id", String.valueOf(userDto.getId()));
                    return exchange;
                }).flatMap(chain::filter);


    }

    /*PRIVATE*/

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }


}
