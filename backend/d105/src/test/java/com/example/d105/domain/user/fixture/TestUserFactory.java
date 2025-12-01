package com.example.d105.domain.user.fixture;

import com.example.d105.domain.user.entity.User;
import com.example.d105.domain.user.repository.UserRepository;


public class TestUserFactory {


    public static User createTestUser(){
        return User.builder()
                .userId(1L)
                .email("test123@gmail.com")
                .username("R8LKKS3fOHQ/DGOPWBv8Rw==")
                .userKey("gVjk3oJzvYUHApqfN5tYtK8cUvzeTU6c+cpENtcVicN3pnuMwtG/1BL6C8wHF6PI")
                .phoneNumber("R8LKKS3fOHQ/DGOPWBv8Rw==")
                .birthDate("Vtafcj01LzTqCMgGtuJrGA==")
                .gender("Vtafcj01LzTqCMgGtuJrGA==")
                .createdAt("1757996002548")
                .updatedAt("1757996002548")
                .build();

    }


}
