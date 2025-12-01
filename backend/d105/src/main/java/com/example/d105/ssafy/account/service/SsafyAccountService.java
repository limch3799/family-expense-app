package com.example.d105.ssafy.account.service;

import com.example.d105.ssafy.account.dto.request.SsafyAccountListRequest;
import com.example.d105.ssafy.account.dto.request.SsafyCardListRequest;
import com.example.d105.ssafy.account.dto.response.SsafyAccountListResponse;
import com.example.d105.ssafy.account.dto.response.SsafyCardListResponse;
import com.example.d105.ssafy.config.SsafyApiConfig;
import com.example.d105.ssafy.exception.SsafyApiException;
import com.example.d105.ssafy.util.CommonHeaderUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class SsafyAccountService {

    private final RestTemplate restTemplate;
    private final SsafyApiConfig config;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SsafyAccountListResponse getAccountList(String userKey) {
        try {
            String url = config.getBaseUrl() + "/edu/demandDeposit/inquireDemandDepositAccountList";

            SsafyAccountListRequest request = createAccountListRequest(userKey);

            log.info("Account List Request JSON: {}", objectMapper.writeValueAsString(request));

            HttpEntity<SsafyAccountListRequest> entity = new HttpEntity<>(request);

            log.info("Calling Ssafy Account API: {}", url);

            ResponseEntity<SsafyAccountListResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    SsafyAccountListResponse.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new SsafyApiException("ACCOUNT_API_CALL_FAILED",
                        "Account API call failed with status: " + response.getStatusCode());
            }

            SsafyAccountListResponse responseBody = response.getBody();
            if (responseBody != null && responseBody.getHeader() != null) {
                String responseCode = responseBody.getHeader().getResponseCode();
                if (!"H0000".equals(responseCode)) {
                    throw new SsafyApiException("ACCOUNT_API_ERROR",
                            "Account API returned error: " + responseBody.getHeader().getResponseMessage());
                }
            }

            return responseBody;

        } catch (HttpClientErrorException e) {
            log.error("Client error calling Account API: {}", e.getMessage());
            throw new SsafyApiException("ACCOUNT_CLIENT_ERROR", e.getMessage());
        } catch (HttpServerErrorException e) {
            log.error("Server error calling Account API: {}", e.getMessage());
            throw new SsafyApiException("ACCOUNT_SERVER_ERROR", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error calling Account API: {}", e.getMessage());
            throw new SsafyApiException("ACCOUNT_UNKNOWN_ERROR", e.getMessage());
        }
    }

    public SsafyCardListResponse getCardList(String userKey) {
        try {
            String url = config.getBaseUrl() + "/edu/creditCard/inquireSignUpCreditCardList";

            SsafyCardListRequest request = createCardListRequest(userKey);

            log.info("Card List Request JSON: {}", objectMapper.writeValueAsString(request));

            HttpEntity<SsafyCardListRequest> entity = new HttpEntity<>(request);

            log.info("Calling Ssafy Card API: {}", url);

            ResponseEntity<SsafyCardListResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    SsafyCardListResponse.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new SsafyApiException("CARD_API_CALL_FAILED",
                        "Card API call failed with status: " + response.getStatusCode());
            }

            SsafyCardListResponse responseBody = response.getBody();
            if (responseBody != null && responseBody.getHeader() != null) {
                String responseCode = responseBody.getHeader().getResponseCode();
                if (!"H0000".equals(responseCode)) {
                    throw new SsafyApiException("CARD_API_ERROR",
                            "Card API returned error: " + responseBody.getHeader().getResponseMessage());
                }
            }

            return responseBody;

        } catch (HttpClientErrorException e) {
            log.error("Client error calling Card API: {}", e.getMessage());
            throw new SsafyApiException("CARD_CLIENT_ERROR", e.getMessage());
        } catch (HttpServerErrorException e) {
            log.error("Server error calling Card API: {}", e.getMessage());
            throw new SsafyApiException("CARD_SERVER_ERROR", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error calling Card API: {}", e.getMessage());
            throw new SsafyApiException("CARD_UNKNOWN_ERROR", e.getMessage());
        }
    }

    private SsafyAccountListRequest createAccountListRequest(String userKey) {
        SsafyAccountListRequest.Header header = SsafyAccountListRequest.Header.builder()
                .apiName("inquireDemandDepositAccountList")
                .transmissionDate(CommonHeaderUtil.getCurrentDate())
                .transmissionTime(CommonHeaderUtil.getCurrentTime())
                .institutionCode(CommonHeaderUtil.getInstitutionCode())
                .fintechAppNo(CommonHeaderUtil.getFintechAppNo())
                .apiServiceCode("inquireDemandDepositAccountList")
                .institutionTransactionUniqueNo(CommonHeaderUtil.generateTransactionIdWithNanos())
                .apiKey(config.getApiKey())
                .userKey(userKey)
                .build();

        return SsafyAccountListRequest.builder()
                .header(header)
                .build();
    }

    private SsafyCardListRequest createCardListRequest(String userKey) {
        SsafyCardListRequest.Header header = SsafyCardListRequest.Header.builder()
                .apiName("inquireSignUpCreditCardList")
                .transmissionDate(CommonHeaderUtil.getCurrentDate())
                .transmissionTime(CommonHeaderUtil.getCurrentTime())
                .institutionCode(CommonHeaderUtil.getInstitutionCode())
                .fintechAppNo(CommonHeaderUtil.getFintechAppNo())
                .apiServiceCode("inquireSignUpCreditCardList")
                .institutionTransactionUniqueNo(CommonHeaderUtil.generateTransactionIdWithNanos())
                .apiKey(config.getApiKey())
                .userKey(userKey)
                .build();

        return SsafyCardListRequest.builder()
                .header(header)
                .build();
    }
}