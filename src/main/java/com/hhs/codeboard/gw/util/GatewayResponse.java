package com.hhs.codeboard.gw.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.hhs.codeboard.gw.enumeration.ErrorCode;
import com.hhs.codeboard.gw.expt.NotEnoughBuilderException;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Setter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;


/**
 * route를 타기전에 gw에서 응답처리함.
 * @param
 * @return
 */
@Setter
public class GatewayResponse<T> {

    @NotNull
    private T object;
    private ServerWebExchange exchange;
    private HttpStatus status;
    private boolean alreadyJson;

    public GatewayResponse(ServerWebExchange exchange, HttpStatus status, T object) {
        this.object = object;
        this.exchange = exchange;
        this.status = status;
        this.alreadyJson = false;
    }

    public GatewayResponse(ServerWebExchange exchange, HttpStatus status, T object, boolean alreadyJson) {
        this.object = object;
        this.exchange = exchange;
        this.status = status;
        this.alreadyJson = alreadyJson;
    }

    public Mono<Void> process() {
        String result;
        try {
            if (Objects.isNull(this.object)) {
                throw new NotEnoughBuilderException("Not have data");
            } else if (object instanceof String) {
                result = alreadyJson ? (String)object : String.format("{\"message\":\"%s\"}", object);
            } else if (object instanceof JsonObject) {
                result = ((JsonObject) object).toString();
            } else {
                ObjectMapper mapper = new ObjectMapper();
                result = mapper.writeValueAsString(object);
            }
        } catch (NotEnoughBuilderException | JsonProcessingException e) {
            result = ErrorCode.GW_RESPONSE_ERROR.getErrorMessageAsJsonString();
        }

        ServerHttpResponse response = this.exchange.getResponse();

        DataBuffer dataBuffer = response.bufferFactory().wrap(result.getBytes());
        // response header 수정
        response.setStatusCode(this.status);
        // header 강제 주입
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, String.valueOf(MediaType.APPLICATION_JSON));
        return response.writeWith(Mono.just(dataBuffer));
    }
}
