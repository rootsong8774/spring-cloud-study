package com.example.apigatewayservice.filter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config> {
    
    private final Environment environment;
    
    public static class Config {
    
    }
    
    public AuthorizationHeaderFilter(Environment environment) {
        super(Config.class);
        this.environment = environment;
    }
    
    // login -> token -> users (with token) -> header(include token)
    @Override
    public GatewayFilter apply(Config config) {
       
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            if(!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "no authorization header", HttpStatus.UNAUTHORIZED);
            }
    
            String authorizationHeader = Objects.requireNonNull(
                request.getHeaders().get(HttpHeaders.AUTHORIZATION)).get(0);
            String jwt = authorizationHeader.replace("Bearer ", "");
            
            if(!isJwtValid(jwt)){
                return onError(exchange, "JWT is not valid", HttpStatus.UNAUTHORIZED);
            }
            return chain.filter(exchange);
            
            
        };
    }
    
    private boolean isJwtValid(String jwt) {
        boolean returnValue = true;
        
        String subject = null;
        try {
            subject = Jwts.parser().setSigningKey(environment.getProperty("token.secret"))
                .parseClaimsJws(jwt).getBody()
                .getSubject();
        } catch (Exception e) {
            returnValue = false;
        }
    
        if (subject == null || subject.isEmpty()) {
            returnValue = false;
        }
        
    
        return returnValue;
    }
    
    // Mono, Flux -> Spring WebFlux (Spring 5.0에서 추가됨)
    private Mono<Void> onError(ServerWebExchange exchange, String error,
        HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        
        log.error(error);
        return response.setComplete();
    }
    
   
}
