package com.example.d105.domain.user.service;

import com.example.d105.domain.group.entity.Group;
import com.example.d105.domain.group.entity.GroupMember;
import com.example.d105.domain.group.exception.GroupException;
import com.example.d105.domain.group.repository.GroupMemberRepository;
import com.example.d105.domain.user.dto.request.TokenRequest;
import com.example.d105.domain.user.entity.FcmToken;
import com.example.d105.domain.user.entity.User;
import com.example.d105.domain.user.repository.FcmTokenRepository;
import com.example.d105.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FcmService {

    private final FcmTokenRepository fcmTokenRepository;
    private final UserRepository userRepository;


    @Transactional
    public void createFCMToken(Long userId, TokenRequest.CreateTokenRequest dto){

        Optional<FcmToken> token = fcmTokenRepository.findByUserIdAndDeviceId(userId, dto.getDeviceId());
        if (token.isPresent()) {

            FcmToken fcmToken = token.get();
           fcmToken.setFcmToken(dto.getToken());
           fcmToken.setUpdatedAt(ZonedDateTime.now());
           fcmTokenRepository.save(fcmToken);
        } else {

            FcmToken fcmToken = new FcmToken();
            fcmToken.setUserId(userId);
            fcmToken.setGroupId(dto.getGroupId());
            fcmToken.setFcmToken(dto.getToken());
            fcmToken.setDeviceId(dto.getDeviceId());
            fcmToken.setDeviceType(dto.getDeviceType());
            fcmToken.setCreatedAt(ZonedDateTime.now());
            fcmTokenRepository.save(fcmToken);
        }
    }

    public void sendMessage(TokenRequest.SendMessageRequest request) throws FirebaseMessagingException {

        String message = FirebaseMessaging.getInstance().send(Message.builder()
                .setNotification(Notification.builder()
                        .setTitle(request.getTitle())
                        .setBody(request.getBody())
                        .build())
                .setToken(request.getToken())  // 대상 디바이스의 등록 토큰
                .build());

        System.out.println("Sent message: " + message);
    }

    //플래너 알림 생성 알림 전송 (임시 )
    public void createPlannerSendMessage(TokenRequest.SendMessageByGroupRequest request) throws FirebaseMessagingException {

        List<FcmToken> tokens= fcmTokenRepository.findByGroupId(request.getGroupId());


        for(FcmToken token : tokens) {

                User user = userRepository.findById(token.getUserId())
                        .orElseThrow(() -> new GroupException("USER_NOT_FOUND" , "존재하지 않는 사용자"));

            if(user.getPlanerPushEnabled()){

                String message = FirebaseMessaging.getInstance().send(Message.builder()
                        .setNotification(Notification.builder()
                                .setTitle("✨ 새로운 플래너 알림!")
                                .setBody("그룹에서 새로운 저축 플래너가 생성되었답니다. 확인해보세요!")
                                .build())
                        .setToken(token.getFcmToken())  // 대상 디바이스의 등록 토큰
                        .build());

                System.out.println("Sent message: " + message);

            }
        }


    }


    //type 1 : planner, type2: 월간 리포트 , type3:거래내역 자동 업데이트
    public void sendMessageFinal(Long groupId, String title, String body, int type) throws FirebaseMessagingException {

        List<FcmToken> tokens= fcmTokenRepository.findByGroupId(groupId);


        for(FcmToken token : tokens) {

            User user = userRepository.findById(token.getUserId())
                    .orElseThrow(() -> new GroupException("USER_NOT_FOUND" , "존재하지 않는 사용자"));

            if((type ==1 && user.getPlanerPushEnabled()) || (type ==2 && user.getReporterPushEnabled()) ||(type ==3 && user.getTransactionPushEnabled())){

                String message = FirebaseMessaging.getInstance().send(Message.builder()
                        .setNotification(Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build())
                        .setToken(token.getFcmToken())  // 대상 디바이스의 등록 토큰
                        .build());

                System.out.println("Sent message: " + message);

            }
        }


    }
}
