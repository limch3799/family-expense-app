package com.example.d105.common.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.InputStream;

@Configuration
@Slf4j
public class FirebaseConfig {

    @PostConstruct
    public void init() {
        try {
            InputStream serviceAccount = new ClassPathResource("d105-ddbfb-firebase-adminsdk-fbsvc-6f79d7259a.json").getInputStream();

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) { // 이미 초기화된 경우 방지
                FirebaseApp.initializeApp(options);
                System.out.println(" FirebaseApp 초기화 성공");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(" FirebaseApp 초기화 실패: " + e.getMessage());
        }
    }
}
