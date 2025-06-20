package com.hhs.codeboard.gw.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    GW_RESPONSE_ERROR(101, "게이트웨이 응답중 오류가 발생했습니다.")
    , ALREADY_HAS_TOKEN(102, "이미 로그인 중입니다.");

    private final int code;
    private final String description;

    public String getErrorMessageAsJsonString() {
        return String.format("{code:%d,message:\"%s\"}", this.code, this.description);
    }

}
