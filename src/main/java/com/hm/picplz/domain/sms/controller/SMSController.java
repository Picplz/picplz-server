package com.hm.picplz.domain.sms.controller;

import com.hm.picplz.domain.sms.dto.SMSDto;
import com.hm.picplz.domain.sms.service.SMSService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/sms")
@Tag(name="SMS")
public class SMSController {

    private final SMSService smsService;

    @Operation(summary = "휴대폰 본인 인증 문자 전송")
    @PostMapping
    public void send(@RequestBody SMSDto.SMSSendReq smsSendReq) {
        smsService.sendMessage(smsSendReq);
    }

}
