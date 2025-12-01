package com.example.d105.domain.group.controller;

import com.example.d105.domain.group.dto.request.GroupRequest;
import com.example.d105.domain.group.dto.request.SavingPlanRequest;
import com.example.d105.domain.group.dto.response.SavingPlanResponse;
import com.example.d105.domain.group.service.SavingPlanService;
import com.example.d105.security.dto.CustomUserDetails;
import com.example.d105.ssafy.saving.dto.request.SavingRequest;
import com.example.d105.ssafy.saving.dto.response.SavingResponse;
import com.example.d105.ssafy.saving.service.SsafySavingService;
import com.google.firebase.messaging.FirebaseMessagingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/savings/plans")
@RequiredArgsConstructor
@Tag(name = "6. 저축 플래너", description = "Savings Planner")
public class SavingController {

    private final SsafySavingService ssafySavingService;
    private final SavingPlanService savingPlanService;

    @PostMapping("/createproduct")
    @Operation(
            summary = "적금 상품 생성",
            description = "   7D , 1M, 3m, 6M, 1Y \n" +
                    "적금 상품 생성 위해 임의로 만든 api ( 적금상품은 최대 1년까지 가능합니다.) + 현재 계좌에 depositBalance 이상의 돈이 있어야함 (임의로 GROUP 계좌에 돈 입금하는 API 만들어둠 )!!"
    )

    public ResponseEntity<SavingResponse.CreateSavingResponse> createGroup(
            @RequestBody String subscriptionPeriodCode
    ) {
        String cleanCode = subscriptionPeriodCode.replace("\"", "").trim();
        System.out.println("Clean code: '" + cleanCode + "'");

        SavingRequest.SubscriptionPeriod period =
                SavingRequest.SubscriptionPeriod.fromCode(cleanCode);

        ssafySavingService.createSavingProduct(period);

        return ResponseEntity.ok().build();
    }



    @PostMapping("/create")
    @Operation(
            summary = "플랜 계획 생성",
            description = "   7D , 1M, 3m, 6M, 1Y \n" +
                    "해당 그룹의 플랜을 새로 생성합니다. (적금 계좌 같이 생성됨)"
    )
    @ApiResponses(value = {
            @ApiResponse(description = "GROUP_NOT_FOUND: 해당 그룹 찾을 수 없음"),
            @ApiResponse(description = "NOT_GROUP_OWNER: 그룹장이 아님"),
    })
    public ResponseEntity<Void> createSavingAccount(
            @AuthenticationPrincipal CustomUserDetails user,
           @RequestBody SavingPlanRequest.CreatePlanRequest request
    ) {


        try {
            savingPlanService.createSavingAccount(user.getUser().getUserId(), request);
        } catch (FirebaseMessagingException e) {
            throw new RuntimeException(e);
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/current")
    @Operation(
            summary = "현재 플랜 상세 조회",
            description = "현재 플렌을 상세 조회 합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(description = "GROUP_NOT_FOUND: 해당 그룹 찾을 수 없음"),
            @ApiResponse(description = "SAVING_PLAN_NOT_FOUND: 해당 적금 플랜을 찾을 수 없음"),
    })
    public ResponseEntity<SavingPlanResponse.PlanInfoResponse> getCurrentPlan(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody  SavingPlanRequest.PlanInfoRequest request
    ) {




        return ResponseEntity.ok(savingPlanService.getPlanInfo(user.getUser().getUserId(), request));
    }

    @PostMapping("/history")
    @Operation(
            summary = "플랜 과거 이력 조회",
            description = "플랜들의 과거 이력을 조회합니다. ( 진행중인 것 제외)"
    )
    @ApiResponses(value = {
            @ApiResponse(description = "GROUP_NOT_FOUND: 해당 그룹 찾을 수 없음"),
            @ApiResponse(description = "SAVING_PLAN_NOT_FOUND: 해당 적금 플랜을 찾을 수 없음"),
    })
    public ResponseEntity<SavingPlanResponse.PlanHistoryResponse> getCurrentPlan(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody  GroupRequest.GroupId request
    ) {




        return ResponseEntity.ok(savingPlanService.getHistoryPlan(user.getUser().getUserId(), request));
    }

    @PostMapping("/delete")
    @Operation(
            summary = "플랜 종료",
            description = "현재 진행중인 플랜을 종료합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(description = "SAVING_PLAN_NOT_FOUND: 해당 적금 플랜을 찾을 수 없음"),
            @ApiResponse(description = "NOT_GROUP_OWNER: 그룹장이 아님"),
    })
    public ResponseEntity<Void> stopPlan(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody  SavingPlanRequest.PlanStopRequest dto
    ) {

        savingPlanService.stopPlan(user.getUser().getUserId(), dto);


        return ResponseEntity.ok().build();
    }
}
