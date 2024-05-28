package com.sms.repository;

import com.sms.entity.SmsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface SmsRepo extends JpaRepository<SmsEntity,Integer> {

    SmsEntity findByMobNumberAndOtp(Long mobNumber, String otp);
}
