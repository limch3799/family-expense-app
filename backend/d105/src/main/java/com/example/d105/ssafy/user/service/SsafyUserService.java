package com.example.d105.ssafy.user.service;

import com.example.d105.ssafy.user.client.UserApiClient;
import com.example.d105.ssafy.user.dto.SsafyUserRequest;
import com.example.d105.ssafy.user.dto.SsafyUserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SsafyUserService {

    private final UserApiClient ssafyApiClient;

    // 사용자 등록 메서드
    public String registUser(SsafyUserRequest.RegistUser registUser) {
        log.info("SSAFY API 사용자 등록 요청: {}", registUser.getUserId());

        // getMember 메서드를 사용해서 사용자 등록 (주석에 따르면 등록 기능)
        SsafyUserResponse response = ssafyApiClient.getMember(registUser.getUserId());

        // SsafyUserResponse에서 직접 userKey 추출
        String userKey = response.getUserKey();

        log.info("SSAFY API 사용자 등록 성공. UserKey: {}", userKey);
        return userKey;
    }

    public SsafyUserResponse getMemberWithCode(String email){
        return ssafyApiClient.getMember(email);
    }

    public SsafyUserResponse getSearchWithCode(String email){
        return ssafyApiClient.getSearch(email);
    }
}