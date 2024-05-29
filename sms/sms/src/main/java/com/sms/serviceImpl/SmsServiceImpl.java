package com.sms.serviceImpl;

import com.sms.OtpUtil;
import com.sms.dto.SmsDto;
import com.sms.entity.SmsEntity;
import com.sms.repository.SmsRepo;
import com.sms.service.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class SmsServiceImpl implements SmsService {


    private static final Logger logger = LoggerFactory.getLogger(SmsServiceImpl.class);

    private static final Map<Long, String> otpCache = new HashMap<>();
    private static final Map<Long, LocalDateTime> otpTimeCache = new HashMap<>();
    @Autowired
    private SmsRepo smsRepo;

    @Override
    public void sendSms(String message, String number, String apiKey) {
        try {
            String sendId = "FSTSMS";
            String language = "english";
            String route = "p";

            message = URLEncoder.encode(message, StandardCharsets.UTF_8);

            String myUrl = "https://www.fast2sms.com/dev/bulkV2?authorization=" + apiKey
                    + "&sender_id=" + sendId + "&message=" + message + "&language=" + language
                    + "&route=" + route + "&numbers=" + number;

            URL url = new URL(myUrl);
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            con.setRequestProperty("cache-control", "no-cache");

            int responseCode = con.getResponseCode();
            logger.info("Response Code: {}", responseCode);

            StringBuffer response = new StringBuffer();
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));

            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            br.close();

            logger.info("Response from SMS API: {}", response.toString());

        } catch (Exception e) {
            logger.error("Error while sending SMS: ", e);
        }
    }

    @Override
    public void saveOtp(SmsEntity smsEntity) {
        try {
            String salt = OtpUtil.generateSalt();
            String hashedOtp = OtpUtil.hashOtp(smsEntity.getOtp(), salt);
            smsEntity.setOtp(hashedOtp);
            smsEntity.setSalt(salt);
            smsEntity.setCreatedAt(LocalDateTime.now());
            smsRepo.save(smsEntity);
            otpCache.put(smsEntity.getMobNumber(), hashedOtp);
            otpTimeCache.put(smsEntity.getMobNumber(), LocalDateTime.now());
        } catch (Exception e) {
            logger.error("Error while saving OTP: ", e);
        }
    }

    @Override
    public boolean verifyOtp(SmsDto smsDto) {
        String cachedOtp = otpCache.get(smsDto.getMobNumber());
        SmsEntity smsEntity = smsRepo.findByMobNumberAndOtp(smsDto.getMobNumber(), cachedOtp);
        if (smsEntity != null) {
            try {
                String inputHashedOtp = OtpUtil.hashOtp(smsDto.getOtp(), smsEntity.getSalt());
                if (cachedOtp.equals(inputHashedOtp)) {
                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime createdAt = smsEntity.getCreatedAt();
                    if (ChronoUnit.MINUTES.between(createdAt, now) <= 3) {
                        smsEntity.setStatus("Verified");
                        smsRepo.save(smsEntity);
                        otpCache.remove(smsDto.getMobNumber());
                        otpTimeCache.remove(smsDto.getMobNumber());
                        return true;
                    } else {
                        smsRepo.delete(smsEntity);
                        otpCache.remove(smsDto.getMobNumber());
                        otpTimeCache.remove(smsDto.getMobNumber());
                    }
                }
            } catch (Exception e) {
                logger.error("Error while verifying OTP: ", e);
            }
        }
        return false;
    }

    public boolean canResendOtp(long mobileNo) {
        LocalDateTime lastSentTime = otpTimeCache.get(mobileNo);
        if (lastSentTime == null) {
            return true;
        }
        return ChronoUnit.MINUTES.between(lastSentTime, LocalDateTime.now()) > 3;
    }
    @Override
    public void removePreviousOtp(long mobileNo) {
        try {
            List<SmsEntity> smsEntities = smsRepo.findByMobNumber(mobileNo);
            smsRepo.deleteAll(smsEntities);
            otpCache.remove(mobileNo);
            otpTimeCache.remove(mobileNo);
        } catch (Exception e) {
            logger.error("Error while removing previous OTPs: ", e);
        }
    }
}
