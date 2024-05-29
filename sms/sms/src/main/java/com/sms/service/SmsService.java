package com.sms.service;

import com.sms.dto.SmsDto;
import com.sms.entity.SmsEntity;

public interface SmsService {
    void sendSms(String message , String number , String apiKey);

    void saveOtp(SmsEntity smsEntity);

    boolean verifyOtp(SmsDto smsDto);


    public boolean canResendOtp(long mobileNo);

    public void removePreviousOtp(long mobileNo);
}
