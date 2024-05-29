package com.sms.dto;

import lombok.Data;

@Data
public class SmsDto {
    private Integer smsId;
    private Long mobNumber;
    private String otp;
    private String status;
}
