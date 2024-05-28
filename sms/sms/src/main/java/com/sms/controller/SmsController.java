package com.sms.controller;

import com.sms.OtpUtil;
import com.sms.dto.SmsDto;
import com.sms.entity.SmsEntity;
import com.sms.service.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sms")
public class SmsController {

    private static final Logger logger = LoggerFactory.getLogger(SmsController.class);

    @Autowired
    private SmsService smsService;

    @PostMapping("/send")
    public String sendMessage(@RequestBody SmsDto smsDto) {
        logger.info("Program Started....");

        String otp = OtpUtil.generateOtp(6);
        logger.info("Generated OTP: {}", otp);

        String apiKey = "QYX1V75W9cI3fhegGSonlzrktEBOjKZb4CuvpxPdiN68JUFLDM6EBkZzrWM3FdHTaLo8epOJ7vRQDqnV";
        String number = String.valueOf(smsDto.getMobNumber());

        smsService.sendSms(otp, number, apiKey);

        SmsEntity smsEntity = new SmsEntity();
        smsEntity.setMobNumber(smsDto.getMobNumber());
        smsEntity.setOtp(otp);
        smsService.saveOtp(smsEntity);

        return "OTP sent successfully";
    }

    @PostMapping("/verify")
    public String verifyOtp(@RequestBody SmsDto smsDto) {
        boolean isValid = smsService.verifyOtp(smsDto);
        return isValid ? "OTP is valid" : "OTP is not ";
    }
}
