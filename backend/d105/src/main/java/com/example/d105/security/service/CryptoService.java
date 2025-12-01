package com.example.d105.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class CryptoService {

    private final Argon2PasswordEncoder argon2PasswordEncoder;

    @Value("${aes.key}")
    private String aesSecretKey;

    @Value("${aes.iv}") // 16바이트 IV
    private String aesIv;

    private static final String AES_ALGORITHM = "AES";
    private static final String AES_TRANSFORMATION = "AES/CBC/PKCS5Padding";

    // Argon2 암호화 (간편비밀번호용)
    public String encryptSimplePassword(String simplePassword) {
        return argon2PasswordEncoder.encode(simplePassword);
    }

    // Argon2 검증
    public boolean verifySimplePassword(String rawSimplePassword, String encodedSimplePassword) {
        return argon2PasswordEncoder.matches(rawSimplePassword, encodedSimplePassword);
    }

    // AES128 암호화 (기타 데이터용)
    public String encryptAES(String plainText) {
        try {
            log.debug("암호화하려고 받은 plainText: {}", plainText);
            SecretKeySpec secretKey = new SecretKeySpec(getAESKey(), AES_ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(getAESIv());

            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            log.error("AES 암호화 실패: {}", e.getMessage());
            throw new IllegalStateException("암호화 처리 중 오류가 발생했습니다.", e);
        }
    }

    // AES128 복호화
    public String decryptAES(String encryptedText) {
        log.debug("복호화하려는 정보: {}", encryptedText);
        try {
            SecretKeySpec secretKey = new SecretKeySpec(getAESKey(), AES_ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(getAESIv());

            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("AES 복호화 실패: {}", e.getMessage());
            throw new IllegalStateException("복호화 처리 중 오류가 발생했습니다.", e);
        }
    }

    // AES 키 생성 (16바이트)
    private byte[] getAESKey() {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] key = sha.digest(aesSecretKey.getBytes(StandardCharsets.UTF_8));
            byte[] aesKey = new byte[16]; // AES-128은 16바이트
            System.arraycopy(key, 0, aesKey, 0, 16);
            return aesKey;
        } catch (Exception e) {
            throw new RuntimeException("AES 키 생성 실패", e);
        }
    }

    // AES IV 생성 (16바이트)
    private byte[] getAESIv() {
        try {
            byte[] ivBytes = aesIv.getBytes(StandardCharsets.UTF_8);
            if (ivBytes.length != 16) {
                throw new RuntimeException("AES IV는 반드시 16바이트여야 합니다.");
            }
            return ivBytes;
        } catch (Exception e) {
            throw new RuntimeException("AES IV 생성 실패", e);
        }
    }
}
