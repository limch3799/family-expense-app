package com.example.d105.ssafy.user.client;

import com.example.d105.ssafy.config.SsafyApiConfig;
import com.example.d105.ssafy.user.dto.SsafyUserRequest;
import com.example.d105.ssafy.user.dto.SsafyUserResponse;
import com.example.d105.ssafy.exception.SsafyApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserApiClient {

    private final RestTemplate restTemplate;
    private final SsafyApiConfig config;

    @PostConstruct
    public void init() {
        restTemplate.getInterceptors().add((request, body, execution) -> {

            request.getHeaders().add("User-Agent", "MyApp/1.0");
            return execution.execute(request, body);
        });
    }

    //싸피망에 user를 등록합니다.
    public SsafyUserResponse getMember(String userId) {
        try {
            String url = config.getBaseUrl() + "/member";
            System.out.println("url : "  + url);

            SsafyUserRequest request = new SsafyUserRequest();
            request.setUserId(userId);
            request.setApiKey(config.getApiKey());

            ObjectMapper mapper = new ObjectMapper();
            log.info("Request JSON: {}", mapper.writeValueAsString(request));

            HttpEntity<SsafyUserRequest> entity = new HttpEntity<>(request);

            log.info("Calling Ssafy API: {}", url);

            ResponseEntity<SsafyUserResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    SsafyUserResponse.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                SsafyUserResponse tmpResponse = new SsafyUserResponse();

                String apiKey = getSearch(userId).getUserKey();
                tmpResponse.setUserKey(apiKey);
                return tmpResponse;

//                throw new SsafyApiException("API_CALL_FAILED",
//                        "API call failed with status: " + response.getStatusCode());
            }

            return response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("Client error calling Ssafy API: {}", e.getMessage());
            //----------------------임의로 수정
            SsafyUserResponse tmpResponse = new SsafyUserResponse();

            String apiKey = getSearch(userId).getUserKey();
            tmpResponse.setUserKey(apiKey);
            //----------------------임의로 수정
            return tmpResponse;

        } catch (HttpServerErrorException e) {
            log.error("Server error calling Ssafy API: {}", e.getMessage());
            throw new SsafyApiException("SERVER_ERROR", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error calling Ssafy API: {}", e.getMessage());
            throw new SsafyApiException("UNKNOWN_ERROR", e.getMessage());
        }
    }

    //싸피망에서 등록된 유저의 apiekey를 확인합니다.
    public SsafyUserResponse getSearch(String userId) {
        try {
            String url = config.getBaseUrl() + "/member/search";
            System.out.println("url : "  + url);

            SsafyUserRequest request = new SsafyUserRequest();
            request.setUserId(userId);
            request.setApiKey(config.getApiKey());

            ObjectMapper mapper = new ObjectMapper();
            log.info("Request JSON: {}", mapper.writeValueAsString(request));

            HttpEntity<SsafyUserRequest> entity = new HttpEntity<>(request);

            log.info("Calling Ssafy API: {}", url);

            ResponseEntity<SsafyUserResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    SsafyUserResponse.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new SsafyApiException("API_CALL_FAILED",
                        "API call failed with status: " + response.getStatusCode());
            }

            return response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("Client error calling Ssafy API: {}", e.getMessage());
            throw new SsafyApiException("CLIENT_ERROR", e.getMessage());
        } catch (HttpServerErrorException e) {
            log.error("Server error calling Ssafy API: {}", e.getMessage());
            throw new SsafyApiException("SERVER_ERROR", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error calling Ssafy API: {}", e.getMessage());
            throw new SsafyApiException("UNKNOWN_ERROR", e.getMessage());
        }
    }

}
