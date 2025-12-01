package com.example.d105.domain.user.service;

import com.example.d105.domain.group.exception.GroupException;
import com.example.d105.domain.user.dto.request.NotificationRequest;
import com.example.d105.domain.user.dto.response.NotificationResponse;
import com.example.d105.domain.user.entity.User;
import com.example.d105.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final UserRepository userRepository;

    @Transactional
    public void notificationSetting(Long userId, NotificationRequest.SettingNotificationRequest request){
        User user = userRepository.findById(userId)
                .orElseThrow( () -> new GroupException("USER_NOT_FOUND" , "존재하지 않는 사용자"));

        user.setPlanerPushEnabled(request.getPlanerPushEnabled());
        user.setReporterPushEnabled(request.getReporterPushEnabled());
        user.setTransactionPushEnabled(request.getTransactionPushEnabled());

        userRepository.save(user);


    }


    public NotificationResponse.NotificationSettingInfo getInfo(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow( () -> new GroupException("USER_NOT_FOUND" , "존재하지 않는 사용자"));

        NotificationResponse.NotificationSettingInfo info = new NotificationResponse.NotificationSettingInfo();
        info.setPlanerPushEnabled(user.getPlanerPushEnabled());
        info.setTransactionPushEnabled(user.getTransactionPushEnabled());
        info.setReporterPushEnabled(user.getReporterPushEnabled());

        return info;


    }


}
