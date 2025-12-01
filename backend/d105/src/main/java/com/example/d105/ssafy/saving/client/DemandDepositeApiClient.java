package com.example.d105.ssafy.saving.client;

import com.example.d105.domain.group.entity.Group;
import com.example.d105.domain.group.exception.GroupException;
import com.example.d105.domain.group.repository.GroupRepository;
import com.example.d105.domain.user.service.UserService;
import com.example.d105.security.service.CryptoService;
import com.example.d105.ssafy.config.SsafyApiConfig;
import com.example.d105.ssafy.saving.dto.request.DemandDepositRequest;

import com.example.d105.ssafy.saving.dto.response.DemandDepositResponse;

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

import javax.annotation.PostConstruct;

//자유 입출급 관련 싸피망 연결을 위한 api 클라이언트
@Component
@RequiredArgsConstructor
@Slf4j
public class DemandDepositeApiClient {

    private final RestTemplate restTemplate;
    private final SsafyApiConfig config;
    private final UserService userService;
    private final GroupRepository groupRepository;
    private final CryptoService cryptoService;
    @PostConstruct
    public void init() {
        restTemplate.getInterceptors().add((request, body, execution) -> {

            request.getHeaders().add("User-Agent", "MyApp/1.0");
            return execution.execute(request, body);
        });
    }



//수시입출금 계좌 생성
    public DemandDepositResponse.CreateDepositResponse createDemandDeposit(Long userId) {
        try {
            String url = config.getBaseUrl() + "/edu/demandDeposit/createDemandDepositAccount";
            System.out.println("url : "  + url);

            DemandDepositRequest.UserHeader header = new DemandDepositRequest.UserHeader();
            header.setApiName("createDemandDepositAccount");
            header.setTransmissionDate(CommonHeaderUtil.getCurrentDate());
            header.setTransmissionTime(CommonHeaderUtil.getCurrentTime()); // 날짜/시간 분리
            header.setInstitutionCode(CommonHeaderUtil.getInstitutionCode());
            header.setFintechAppNo("001"); // null 방지
            header.setApiServiceCode("createDemandDepositAccount");
            header.setInstitutionTransactionUniqueNo(CommonHeaderUtil.generateTransactionIdWithNanos());
            header.setApiKey(config.getApiKey());
            header.setUserKey(userService.getUserKey(userId));

            System.out.println("userKey : " + header.getUserKey());
            DemandDepositRequest.CreateDemandDepositeRequest request = new DemandDepositRequest.CreateDemandDepositeRequest();
            request.setHeader(header);
            request.setAccountTypeUniqueNo("999-1-c1bbbd8df03545");

            ObjectMapper mapper = new ObjectMapper();
            log.info("Request JSON: {}", mapper.writeValueAsString(request));

            HttpEntity<DemandDepositRequest.CreateDemandDepositeRequest> entity = new HttpEntity<>(request);

            log.info("Calling Ssafy API: {}", url);

            ResponseEntity<DemandDepositResponse.CreateDepositResponse> response =
                    restTemplate.exchange(url, HttpMethod.POST, entity, DemandDepositResponse.CreateDepositResponse.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new SsafyApiException("API_CALL_FAILED",
                        "API call failed with status: " + response.getStatusCode());
            }

            return response.getBody();

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


    // 수시입출금 계좌 조회
    public DemandDepositResponse.DemandDepositeInfo getDepositeInfo(Long groupId) {
        try {

            String url = config.getBaseUrl() + "/edu/demandDeposit/inquireDemandDepositAccount";
            System.out.println("url : "  + url);

            DemandDepositRequest.GetDemandDepositeRequest request = new DemandDepositRequest.GetDemandDepositeRequest();

            DemandDepositRequest.UserHeader header = new DemandDepositRequest.UserHeader();
            header.setApiName("inquireDemandDepositAccount");
            header.setTransmissionDate(CommonHeaderUtil.getCurrentDate());
            header.setTransmissionTime(CommonHeaderUtil.getCurrentTime()); // 날짜/시간 분리
            header.setInstitutionCode(CommonHeaderUtil.getInstitutionCode());
            header.setFintechAppNo("001"); // null 방지
            header.setApiServiceCode("inquireDemandDepositAccount");
            header.setInstitutionTransactionUniqueNo(CommonHeaderUtil.generateTransactionIdWithNanos());
            header.setApiKey(config.getApiKey());
            header.setUserKey(getUserKey(groupId));

            request.setHeader(header);
            request.setAccountNo(getAccountNo(groupId));

            ObjectMapper mapper = new ObjectMapper();
            log.info("Request JSON: {}", mapper.writeValueAsString(request));

            HttpEntity<DemandDepositRequest.GetDemandDepositeRequest> entity = new HttpEntity<>(request);

            log.info("Calling Ssafy API: {}", url);

            ResponseEntity<DemandDepositResponse.DemandDepositeInfo> response =
                    restTemplate.exchange(url, HttpMethod.POST, entity, DemandDepositResponse.DemandDepositeInfo.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new SsafyApiException("API_CALL_FAILED",
                        "API call failed with status: " + response.getStatusCode());
            }

            return response.getBody();

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

    //그룹장의 유저키 반환
    public String getUserKey(Long groupId){

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupException("GROUP_NOT_FOUND" , "해당 그룹 찾을 수 없음"));

        return cryptoService.decryptAES(group.getOwner().getUserKey());
    }



    //그룹의 계좌번호를 반환
    public String getAccountNo(Long groupId){
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupException("GROUP_NOT_FOUND" , "해당 그룹 찾을 수 없음"));

        return group.getSavingsAccountNo();
    }



    public DemandDepositResponse.UpdateDepositResponse updateDepositResponse(Group group, Long balance) {
        try {

            String url = config.getBaseUrl() + "/edu/demandDeposit/updateDemandDepositAccountDeposit";
            System.out.println("url : "  + url);

            DemandDepositRequest.UpdateDemandDepositAccountRequest request = new DemandDepositRequest.UpdateDemandDepositAccountRequest();

            DemandDepositRequest.UserHeader header = new DemandDepositRequest.UserHeader();
            header.setApiName("updateDemandDepositAccountDeposit");
            header.setTransmissionDate(CommonHeaderUtil.getCurrentDate());
            header.setTransmissionTime(CommonHeaderUtil.getCurrentTime()); // 날짜/시간 분리
            header.setInstitutionCode(CommonHeaderUtil.getInstitutionCode());
            header.setFintechAppNo("001"); // null 방지
            header.setApiServiceCode("updateDemandDepositAccountDeposit");
            header.setInstitutionTransactionUniqueNo(CommonHeaderUtil.generateTransactionIdWithNanos());
            header.setApiKey(config.getApiKey());
            header.setUserKey(getUserKey(group.getGroupId()));

            request.setHeader(header);
            request.setAccountNo(group.getSavingsAccountNo());
            request.setTransactionBalance(balance);
            request.setTransactionSummary("(수시입출금) : 입금 ");


            ObjectMapper mapper = new ObjectMapper();
            log.info("Request JSON: {}", mapper.writeValueAsString(request));

            HttpEntity<DemandDepositRequest.UpdateDemandDepositAccountRequest> entity = new HttpEntity<>(request);

            log.info("Calling Ssafy API: {}", url);

            ResponseEntity<DemandDepositResponse.UpdateDepositResponse> response =
                    restTemplate.exchange(url, HttpMethod.POST, entity, DemandDepositResponse.UpdateDepositResponse.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new SsafyApiException("API_CALL_FAILED",
                        "API call failed with status: " + response.getStatusCode());
            }

            return response.getBody();

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
