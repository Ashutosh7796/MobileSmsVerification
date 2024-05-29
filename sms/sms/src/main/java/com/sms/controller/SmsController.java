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

        // Check if mobile number is provided and is exactly 10 digits long
        Long mobileNo = smsDto.getMobNumber();
        if (mobileNo == null) {
            return "Mobile number is required.";
        } else if (String.valueOf(mobileNo).length() != 10) {
            return "Invalid mobile number. Please provide a 10-digit mobile number.";
        }

        // Check if the provided mobile number is valid for sending OTP again
        if (!smsService.canResendOtp(mobileNo)) {
            return "Please wait 3 minutes before requesting a new OTP.";
        }

        smsService.removePreviousOtp(mobileNo);

        String otp = OtpUtil.generateOtp(6);
        logger.info("Generated OTP: {}", otp);

        String apiKey = "QYX1V75W9cI3fhegGSonlzrktEBOjKZb4CuvpxPdiN68JUFLDM6EBkZzrWM3FdHTaLo8epOJ7vRQDqnV";
        String number = String.valueOf(mobileNo);

        smsService.sendSms(otp, number, apiKey);

        SmsEntity smsEntity = new SmsEntity();
        smsEntity.setMobNumber(mobileNo);
        smsEntity.setOtp(otp);
        smsEntity.setStatus("Pending");
        smsService.saveOtp(smsEntity);

        return "OTP sent successfully";
    }


    @PostMapping("/verify")
    public String verifyOtp(@RequestBody SmsDto smsDto) {
        boolean isValid = smsService.verifyOtp(smsDto);
        return isValid ? "OTP Verified Successfully" : "Invalid OTP. Please enter the valid OTP.";
    }
}