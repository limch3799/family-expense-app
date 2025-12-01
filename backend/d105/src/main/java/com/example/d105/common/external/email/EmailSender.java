package com.example.d105.common.external.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * 이메일 발송을 담당하는 컴포넌트
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailSender {

    private final JavaMailSender javaMailSender;

    /**
     * 인증 코드를 HTML 형식의 이메일로 비동기 발송합니다.
     * @param to 수신자 이메일 주소
     * @param code 발송할 6자리 인증 코드
     * @throws MessagingException 메일 발송 관련 예외
     */
    @Async // 이메일 발송은 시간이 걸릴 수 있으므로 비동기로 처리
    public void sendVerificationEmail(String to, String code) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        // MimeMessageHelper를 사용하면 메일 콘텐츠를 쉽게 구성할 수 있습니다. (true는 멀티파트 메시지 여부)
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

        // 이메일 콘텐츠 (HTML)
        String htmlContent = "<h3>[가족 통장 서비스] 이메일 인증 코드입니다.</h3>"
                + "<p>아래 코드를 복사하여 인증 창에 입력해주세요.</p>"
                + "<h2>" + code + "</h2>";
        helper.setFrom("vaakheejn@naver.com"); // 이 줄 추가!
        helper.setTo(to); // 수신자 설정
        helper.setSubject("[가족 통장 서비스] 인증 코드 안내"); // 제목 설정
        helper.setText(htmlContent, true); // 내용 설정 (true: HTML 형식으로 전송)

        javaMailSender.send(mimeMessage);
    }
}