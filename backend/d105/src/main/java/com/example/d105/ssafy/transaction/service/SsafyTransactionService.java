package com.example.d105.ssafy.transaction.service;

import com.example.d105.ssafy.exception.SsafyServerException;
import com.example.d105.ssafy.transaction.dto.request.SsafyAccountTransactionRequest;
import com.example.d105.ssafy.transaction.dto.request.SsafyCardTransactionRequest;
import com.example.d105.ssafy.transaction.dto.response.SsafyAccountTransactionResponse;
import com.example.d105.ssafy.transaction.dto.response.SsafyCardTransactionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class SsafyTransactionService {

    private final RestTemplate restTemplate;

    @Value("${ssafy.api.base-url:https://finopenapi.ssafy.io/ssafy/api/v1}")
    private String baseUrl;

    @Value("${ssafy.api.api-key}")
    private String apiKey;

    /**
     * 계좌 거래내역 조회
     */
    public SsafyAccountTransactionResponse getAccountTransactionHistory(String userKey, String accountNo, String startDate, String endDate) {
        String url = baseUrl + "/edu/demandDeposit/inquireTransactionHistoryList";

        SsafyAccountTransactionRequest request = SsafyAccountTransactionRequest.of(userKey, apiKey, accountNo, startDate, endDate);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<SsafyAccountTransactionRequest> entity = new HttpEntity<>(request, headers);

        log.info("SSAFY 계좌 거래내역 조회 요청: accountNo={}, startDate={}, endDate={}", accountNo, startDate, endDate);

        try {
            ResponseEntity<SsafyAccountTransactionResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, SsafyAccountTransactionResponse.class
            );

            SsafyAccountTransactionResponse result = response.getBody();
            int transactionCount = (result.getRec() != null && result.getRec().getList() != null)
                    ? result.getRec().getList().size() : 0;
            log.info("SSAFY 계좌 거래내역 조회 성공: {} 건", transactionCount);

            return result;

        } catch (Exception e) {
            log.error("SSAFY 계좌 거래내역 조회 실패: accountNo={}, error={}", accountNo, e.getMessage());
            throw SsafyServerException.transactionApiError(e);
        }
    }

    /**
     * 카드 거래내역 조회
     */
    public SsafyCardTransactionResponse getCardTransactionHistory(String userKey, String cardNo, String cvc, String startDate, String endDate) {
        String url = baseUrl + "/edu/creditCard/inquireCreditCardTransactionList";

        SsafyCardTransactionRequest request = SsafyCardTransactionRequest.of(userKey, apiKey, cardNo, cvc, startDate, endDate);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<SsafyCardTransactionRequest> entity = new HttpEntity<>(request, headers);

        log.info("SSAFY 카드 거래내역 조회 요청: cardNo={}, startDate={}, endDate={}", cardNo, startDate, endDate);

        try {
            ResponseEntity<SsafyCardTransactionResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, SsafyCardTransactionResponse.class
            );

            SsafyCardTransactionResponse result = response.getBody();
            int transactionCount = (result.getRec() != null && result.getRec().getTransactionList() != null)
                    ? result.getRec().getTransactionList().size() : 0;
            log.info("SSAFY 카드 거래내역 조회 성공: {} 건", transactionCount);

            return result;

        } catch (Exception e) {
            log.error("SSAFY 카드 거래내역 조회 실패: cardNo={}, error={}", cardNo, e.getMessage());
            throw SsafyServerException.transactionApiError(e);
        }
    }
}