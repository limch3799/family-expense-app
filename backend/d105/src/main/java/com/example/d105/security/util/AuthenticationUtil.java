package com.example.d105.security.util;

import com.example.d105.common.exception.ResourceNotFoundException;
import com.example.d105.domain.user.entity.User;
import com.example.d105.domain.user.repository.UserRepository;
import com.example.d105.security.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationUtil {

    private final UserRepository userRepository;

    public Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new AccessDeniedException("인증 정보가 없습니다.");
        }

        // 방법 1: CustomUserDetails에서 직접 추출 (가장 효율적)
        if (authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();
            if (user != null && user.getUserId() != null) {
                return user.getUserId();
            }
        }

        // 방법 2: UserDetails에서 이메일 추출 후 DB 조회 (백업)
        if (authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String email = userDetails.getUsername();

            log.debug("Extracting userId for email: {}", email);

            User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                    .orElseThrow(() -> new ResourceNotFoundException("사용자"));

            return user.getUserId();
        }

        throw new AccessDeniedException("인증 정보 형식이 올바르지 않습니다.");
    }

    public String getEmailFromAuthentication(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new AccessDeniedException("인증 정보가 없습니다.");
        }

        if (authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return userDetails.getUsername();
        }

        throw new AccessDeniedException("인증 정보 형식이 올바르지 않습니다.");
    }
}