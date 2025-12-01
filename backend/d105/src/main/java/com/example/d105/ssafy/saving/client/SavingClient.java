package com.example.d105.ssafy.saving.client;

import com.example.d105.domain.user.service.UserService;
import com.example.d105.ssafy.config.SsafyApiConfig;
import com.example.d105.ssafy.saving.dto.request.SavingRequest;
import com.example.d105.ssafy.saving.dto.response.SavingResponse;
import com.example.d105.ssafy.exception.SsafyApiException;
import com.example.d105.ssafy.util.CommonHeaderUtil;
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

@Component
@RequiredArgsConstructor
@Slf4j
public class SavingClient {

    private final RestTemplate restTemplate;
    private final SsafyApiConfig config;
    private final UserService userService;

    //적금 계좌 상품 생성
    public SavingResponse.CreateSavingResponse createSavingResponse(SavingRequest.SubscriptionPeriod subscriptionPeriod) {
        try {
            String url = config.getBaseUrl() + "/edu/savings/createProduct";
            System.out.println("url : "  + url);

            SavingRequest.Header header = new SavingRequest.Header();
            header.setApiName("createProduct");
            header.setTransmissionDate(CommonHeaderUtil.getCurrentDate());
            header.setTransmissionTime(CommonHeaderUtil.getCurrentTime()); // 날짜/시간 분리
            header.setInstitutionCode(CommonHeaderUtil.getInstitutionCode());
            header.setFintechAppNo("001"); // null 방지
            header.setApiServiceCode("createProduct");
            header.setInstitutionTransactionUniqueNo(CommonHeaderUtil.generateTransactionIdWithNanos());
            header.setApiKey(config.getApiKey());


          SavingRequest.CreateSavingRequest request = new SavingRequest.CreateSavingRequest();
            request.setHeader(header);
            request.setBankCode(CommonHeaderUtil.getBankCode());
            request.setAccountName(subscriptionPeriod.getDays() +"일 적금");
            request.setAccountDescription(subscriptionPeriod.getDays() +"일 적금입니다");
            request.setSubscriptionPeriod(subscriptionPeriod.getDays());
            request.setMinSubscriptionBalance("1");
            request.setMaxSubscriptionBalance("1000000");
            request.setInterestRate("10");
            request.setRateDescription("10%이자율을 지급합니다.");


            ObjectMapper mapper = new ObjectMapper();
            log.info("Request JSON: {}", mapper.writeValueAsString(request));

            HttpEntity<SavingRequest.CreateSavingRequest> entity = new HttpEntity<>(request);

            log.info("Calling Ssafy API: {}", url);

            ResponseEntity<SavingResponse.CreateSavingResponse> response =
                    restTemplate.exchange(url, HttpMethod.POST, entity, SavingResponse.CreateSavingResponse.class);
            log.info("Response: {}", response);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new SsafyApiException("API_CALL_FAILED",
                        "API call failed with status: " + response.getStatusCode());
            }
            SavingResponse.CreateSavingResponse responseBody = response.getBody();
            log.info("Response body: {}", responseBody);

            return responseBody;

        } catch (HttpClientErrorException e) {
            log.error("Client error calling Ssafy API", e);
            throw new SsafyApiException("CLIENT_ERROR", e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            log.error("Server error calling Ssafy API", e);
            throw new SsafyApiException("SERVER_ERROR", e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Unexpected error calling Ssafy API", e);
            throw new SsafyApiException("UNKNOWN_ERROR", e.getMessage());
        }
    }

    //적금 계좌 생성
    public SavingResponse.CreateSavingAccountResponse createSavingAccountResponse(Long userId, String accountTypeUniqueNo, String depositBalance, String withdrawalAccountNo) {
        try {
            String url = config.getBaseUrl() + "/edu/savings/createAccount";
            System.out.println("url : "  + url);

            SavingRequest.UserHeader header = new SavingRequest.UserHeader();
            header.setApiName("createAccount");
            header.setTransmissionDate(CommonHeaderUtil.getCurrentDate());
            header.setTransmissionTime(CommonHeaderUtil.getCurrentTime()); // 날짜/시간 분리
            header.setInstitutionCode(CommonHeaderUtil.getInstitutionCode());
            header.setFintechAppNo("001"); // null 방지
            header.setApiServiceCode("createAccount");
            header.setInstitutionTransactionUniqueNo(CommonHeaderUtil.generateTransactionIdWithNanos());
            header.setApiKey(config.getApiKey());
            header.setUserKey(userService.getUserKey(userId));


            SavingRequest.CreateSavingAccountRequest request = new SavingRequest.CreateSavingAccountRequest();
            request.setHeader(header);
            request.setAccountTypeUniqueNo(accountTypeUniqueNo);
            request.setDepositBalance(depositBalance);
            request.setWithdrawalAccountNo(withdrawalAccountNo);


            ObjectMapper mapper = new ObjectMapper();
            log.info("Request JSON: {}", mapper.writeValueAsString(request));

            HttpEntity<SavingRequest.CreateSavingAccountRequest> entity = new HttpEntity<>(request);

            log.info("Calling Ssafy API: {}", url);

            ResponseEntity<SavingResponse.CreateSavingAccountResponse> response =
                    restTemplate.exchange(url, HttpMethod.POST, entity, SavingResponse.CreateSavingAccountResponse.class);
            log.info("Response: {}", response);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new SsafyApiException("API_CALL_FAILED",
                        "API call failed with status: " + response.getStatusCode());
            }
            SavingResponse.CreateSavingAccountResponse responseBody = response.getBody();
            log.info("Response body: {}", responseBody);

            return responseBody;

        } catch (HttpClientErrorException e) {
            log.error("Client error calling Ssafy API", e);
            throw new SsafyApiException("CLIENT_ERROR", e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            log.error("Server error calling Ssafy API", e);
            throw new SsafyApiException("SERVER_ERROR", e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Unexpected error calling Ssafy API", e);
            throw new SsafyApiException("UNKNOWN_ERROR", e.getMessage());
        }
    }

    //적금 계좌 조회 (단건)
    public SavingResponse.InquireAccountResponse inquireAccountResponse(Long userId, String accountNo) {
        try {
            String url = config.getBaseUrl() + "/edu/savings/inquireAccount";
            System.out.println("url : "  + url);

            SavingRequest.UserHeader header = new SavingRequest.UserHeader();
            header.setApiName("inquireAccount");
            header.setTransmissionDate(CommonHeaderUtil.getCurrentDate());
            header.setTransmissionTime(CommonHeaderUtil.getCurrentTime()); // 날짜/시간 분리
            header.setInstitutionCode(CommonHeaderUtil.getInstitutionCode());
            header.setFintechAppNo("001"); // null 방지
            header.setApiServiceCode("inquireAccount");
            header.setInstitutionTransactionUniqueNo(CommonHeaderUtil.generateTransactionIdWithNanos());
            header.setApiKey(config.getApiKey());
            header.setUserKey(userService.getUserKey(userId));


            SavingRequest.InquireAccountRequest request = new SavingRequest.InquireAccountRequest();
            request.setHeader(header);
            request.setAccountNo(accountNo);



            ObjectMapper mapper = new ObjectMapper();
            log.info("Request JSON: {}", mapper.writeValueAsString(request));

            HttpEntity<SavingRequest.InquireAccountRequest> entity = new HttpEntity<>(request);

            log.info("Calling Ssafy API: {}", url);

            ResponseEntity< SavingResponse.InquireAccountResponse> response =
                    restTemplate.exchange(url, HttpMethod.POST, entity,  SavingResponse.InquireAccountResponse.class);
            log.info("Response: {}", response);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new SsafyApiException("API_CALL_FAILED",
                        "API call failed with status: " + response.getStatusCode());
            }
            SavingResponse.InquireAccountResponse responseBody = response.getBody();
            log.info("적금 계좌 단일 조회의 Response body: {}", responseBody);

            return responseBody;

        } catch (HttpClientErrorException e) {
            log.error("Client error calling Ssafy API", e);
            throw new SsafyApiException("CLIENT_ERROR", e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            log.error("Server error calling Ssafy API", e);
            throw new SsafyApiException("SERVER_ERROR", e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Unexpected error calling Ssafy API", e);
            throw new SsafyApiException("UNKNOWN_ERROR", e.getMessage());
        }
    }

    //적금 납입 회차 조회
    public SavingResponse.PaymentResponse paymentResponse(Long userId, String accountNo) {
        try {
            String url = config.getBaseUrl() + "/edu/savings/inquirePayment";
            System.out.println("url : "  + url);

            SavingRequest.UserHeader header = new SavingRequest.UserHeader();
            header.setApiName("inquirePayment");
            header.setTransmissionDate(CommonHeaderUtil.getCurrentDate());
            header.setTransmissionTime(CommonHeaderUtil.getCurrentTime()); // 날짜/시간 분리
            header.setInstitutionCode(CommonHeaderUtil.getInstitutionCode());
            header.setFintechAppNo("001"); // null 방지
            header.setApiServiceCode("inquirePayment");
            header.setInstitutionTransactionUniqueNo(CommonHeaderUtil.generateTransactionIdWithNanos());
            header.setApiKey(config.getApiKey());
            header.setUserKey(userService.getUserKey(userId));


            SavingRequest.InquirePaymentRequest request = new SavingRequest.InquirePaymentRequest();
            request.setHeader(header);
            request.setAccountNo(accountNo);



            ObjectMapper mapper = new ObjectMapper();
            log.info("Request JSON: {}", mapper.writeValueAsString(request));

            HttpEntity<SavingRequest.InquirePaymentRequest> entity = new HttpEntity<>(request);

            log.info("Calling Ssafy API: {}", url);

            ResponseEntity< SavingResponse.PaymentResponse> response =
                    restTemplate.exchange(url, HttpMethod.POST, entity,  SavingResponse.PaymentResponse.class);
            log.info("Response: {}", response);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new SsafyApiException("API_CALL_FAILED",
                        "API call failed with status: " + response.getStatusCode());
            }
            SavingResponse.PaymentResponse responseBody = response.getBody();
            log.info("Response body: {}", responseBody);

            return responseBody;

        } catch (HttpClientErrorException e) {
            log.error("Client error calling Ssafy API", e);
            throw new SsafyApiException("CLIENT_ERROR", e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            log.error("Server error calling Ssafy API", e);
            throw new SsafyApiException("SERVER_ERROR", e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Unexpected error calling Ssafy API", e);
            throw new SsafyApiException("UNKNOWN_ERROR", e.getMessage());
        }
    }
}
