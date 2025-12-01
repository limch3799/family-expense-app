package com.example.d105.domain.user.repository;


import com.example.d105.domain.user.entity.FcmToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FcmTokenRepository  extends JpaRepository<FcmToken, Long>  {

    Optional<FcmToken> findByUserIdAndDeviceId(Long userId, String deviceId);
    List<FcmToken> findByGroupId(Long groupId);
    List<FcmToken> findByUserIdAndGroupId(Long userId, Long groupId);

}
