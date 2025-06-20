package com.hhs.codeboard.gw.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthDto {
    private String email;
    private String passwd;
    private String refreshToken;
    private String accessToken;
    private String message;
    private String jwt;

}