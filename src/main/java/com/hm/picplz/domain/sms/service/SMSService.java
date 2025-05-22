package com.hm.picplz.domain.sms.service;

import com.hm.picplz.domain.sms.dto.SMSDto;
import lombok.RequiredArgsConstructor;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.exception.NurigoMessageNotReceivedException;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class SMSService {

    @Value("${coolsms.api.key}")
    private String apiKey;

    @Value("${coolsms.api.secret}")
    private String apiSecret;

    @Value("${coolsms.api.number}")
    private String from;

    private String domain = "https://api.coolsms.co.kr";

    @Transactional
    public void sendMessage(SMSDto.SMSSendReq request) {
        DefaultMessageService messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, domain);

        Message message = new Message();
        String number = generateNumber();

        message.setFrom(from);
        message.setTo(request.getTo());
        message.setText("픽플즈 핸드폰 본인 인증 번호 [" + number + "]");

        //TODO: number DB에 저장

        try {
            messageService.send(message);
        } catch (NurigoMessageNotReceivedException exception) {
            System.out.println(exception.getFailedMessageList());
            System.out.println(exception.getMessage());
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        }
    }

    private String generateNumber() {
        Random rand = new Random();
        StringBuilder numStr = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            numStr.append(rand.nextInt(10));
        }
        return numStr.toString();
    }
}
