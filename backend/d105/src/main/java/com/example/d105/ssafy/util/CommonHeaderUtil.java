package com.example.d105.ssafy.util;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
@Component
public class CommonHeaderUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmmss");

    /**
     * 나노초를 이용한 더욱 정밀한 일련번호 생성
     * @return 기관거래고유번호 (20자리)
     */
    public static String generateTransactionIdWithNanos() {
        LocalDateTime now = LocalDateTime.now();

        String dateStr = now.format(DATE_FORMATTER);
        String timeStr = now.format(TIME_FORMATTER);

        // 나노초를 이용한 더 정밀한 일련번호
        long nanos = System.nanoTime();
        String serialNumber = String.format("%06d", (nanos % 1000000));

        return dateStr + timeStr + serialNumber;
    }

    /**
     * 현재 날짜를 YYYYMMDD 형식의 문자열로 반환
     * @return 현재 날짜 (8자리) 예: "20240401"
     */
    public static String getCurrentDate(){
        return LocalDate.now().format(DATE_FORMATTER);
    }

    /**
     * 현재 시간을 HHMMSS 형식의 문자열로 반환
     * @return 현재 시간 (6자리) 예: "095500"
     */
    public static String getCurrentTime() {
        return LocalDateTime.now().format(TIME_FORMATTER);
    }

    //기관 코드 ( '00100') 으로 고정
    public static String getInstitutionCode(){
        return "00100";
    }

    //핀테크 앱 일련번호('001'로 고정)
    public static String getFintechAppNo(){
        return "001";
    }

    //은행 코드 (싸피은행 : 999)
    public static String getBankCode(){
        return "999";
    }

}
