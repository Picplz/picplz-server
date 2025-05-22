package com.hm.picplz.domain.sms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SMSDto {

    @Data
    @NoArgsConstructor
    public static class SMSSendReq {
        @NotBlank
        private String to;
    }
}
