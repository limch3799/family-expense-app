package com.example.d105.domain.group.service;

import com.example.d105.domain.group.dto.request.GroupRequest;
import com.example.d105.domain.group.dto.request.SavingPlanRequest;
import com.example.d105.domain.group.dto.response.SavingPlanResponse;
import com.example.d105.domain.group.entity.Group;
import com.example.d105.domain.group.entity.SavingPlan;
import com.example.d105.domain.group.exception.GroupException;
import com.example.d105.domain.group.repository.GroupMemberRepository;
import com.example.d105.domain.group.repository.GroupRepository;
import com.example.d105.domain.group.repository.SavingPlanRepository;
import com.example.d105.domain.user.service.FcmService;
import com.example.d105.domain.user.service.UserService;
import com.example.d105.ssafy.saving.dto.request.SavingRequest;
import com.example.d105.ssafy.saving.dto.response.SavingResponse;
import com.example.d105.ssafy.saving.service.SsafySavingService;
import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class SavingPlanService {

    private final SsafySavingService ssafySavingService;
    private final GroupRepository groupRepository;
    private final SavingPlanRepository savingPlanRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserService userService;
    private final FcmService fcmService;

    //플랜 생성
    @Transactional
    public void createSavingAccount(Long userId,  SavingPlanRequest.CreatePlanRequest dto) throws FirebaseMessagingException {

        Group group = groupRepository.findById(dto.getGroupId())
                .orElseThrow( () -> new GroupException("GROUP_NOT_FOUND" , "해당 그룹 찾을 수 없음"));

        List<SavingPlan> savingPlanList = savingPlanRepository.findByGroup_GroupIdAndStatus(dto.getGroupId(), "진행중");

        if(!savingPlanList.isEmpty() || savingPlanList.size() > 0)
            throw new GroupException("ALREADY_EXISTS_PLAN","이미 플랜이 진행중");




        if(group.getOwner().getUserId() != userId)
            throw new GroupException("NOT_GROUP_OWNER", "그룹장이 아님");


        String cleanCode = dto.getSubscribePeriod().replace("\"", "").trim();
        System.out.println("Clean code: '" + cleanCode + "'");

        SavingRequest.SelectSubscripteionPeriod period =
                SavingRequest.SelectSubscripteionPeriod.fromCode(cleanCode);

        SavingRequest.SubscriptionPeriod periodday =
                SavingRequest.SubscriptionPeriod.fromCode(cleanCode);


        //적금 계좌 생성
        SavingResponse.CreateSavingAccountResponse response =  ssafySavingService.createSavingAccount(userId,period.getAccountTypeUniqueNo(),dto.getDepositBalance(), group.getSavingsAccountNo()  );

        SavingPlan savingPlan = new SavingPlan();
        savingPlan.setPlanTitle( dto.getPlanTitle());
        savingPlan.setGroup( group);
        savingPlan.setTargetAmount(dto.getTargetAmount());
        LocalDate targetDate = LocalDate.now().plusDays(Integer.parseInt(periodday.getDays()));
        savingPlan.setTargetDate(targetDate);
        savingPlan.setStatus("진행중");
        savingPlan.setAccountNo(response.getRec().getAccountNo());

        savingPlanRepository.save(savingPlan);
//        TokenRequest.SendMessageByGroupRequest request = new TokenRequest.SendMessageByGroupRequest();
//        request.setGroupId(dto.getGroupId());

        fcmService.sendMessageFinal(dto.getGroupId() , "✨ 새로운 플래너 알림!" ,"그룹에서 새로운 저축 플래너가 생성되었답니다. 확인해보세요!",1);

    }


    public SavingPlanResponse.PlanInfoResponse getPlanInfo(Long userId, SavingPlanRequest.PlanInfoRequest dto){

        SavingPlan savingPlan = savingPlanRepository.findById(dto.getPlanId())
                .orElseThrow(() -> new GroupException("SAVING_PLAN_NOT_FOUND", "해당 적금 플랜을 찾을 수 없음") );

        if(!groupMemberRepository.existsByGroup_GroupIdAndUser_UserIdAndAllowedAtIsNotNullAndExitedAtIsNull(savingPlan.getGroup().getGroupId() , userId)){
            throw new GroupException("NOT_GROUP_MEMBER" ,"해당 그룹에 속해있지 않음");
        }

        //적금 계좌 조회
        SavingResponse.InquireAccountResponse response1 = ssafySavingService.inquireAccountResponse(savingPlan.getGroup().getOwner().getUserId(),savingPlan.getAccountNo());
        SavingResponse.PaymentResponse response2 =  ssafySavingService.paymentResponse(savingPlan.getGroup().getOwner().getUserId(),savingPlan.getAccountNo());;

        SavingPlanResponse.PlanInfoResponse response = new SavingPlanResponse.PlanInfoResponse();
        List<SavingPlanResponse.Transcation> transcations = new ArrayList<>();
        for (SavingResponse.PaymentRec rec : response2.getRec()) {
            for (SavingResponse.PaymentInfo info : rec.getInfos()) {
                if(info.getStatus().equals("SUCCESS") ){
                    transcations.add(new SavingPlanResponse.Transcation(
                            savingPlan.getGroup().getOwner().getUserId(),
                            userService.getUserName(savingPlan.getGroup().getOwner().getUserId()),
                            Integer.parseInt(info.getPaymentBalance()),
                            info.getPaymentDate()
                    ));
                }

            }
        }

        response.setPlanId(savingPlan.getPlanId());
        response.setTitle(savingPlan.getPlanTitle());
        response.setTargetAmount(savingPlan.getTargetAmount() );
        response.setCreatedAt(savingPlan.getCreatedAt());
        response.setAmount(Integer.parseInt(response1.getRec().getTotalBalance()));
        LocalDate today = LocalDate.now();
        long dDay = ChronoUnit.DAYS.between(today, savingPlan.getTargetDate());

        if (dDay > 0) {
            response.setDDay((int) dDay);   // D-n (남은 일수)
        } else if (dDay == 0) {
            response.setDDay(0);            // 오늘이 목표일
        } else {
            response.setDDay((int) dDay);   // 음수 값 (지나간 일수)
        }

        response.setTranscationsList(transcations);

        return response;
    }

    //과거 이력 조회
    public SavingPlanResponse.PlanHistoryResponse getHistoryPlan(Long userId, GroupRequest.GroupId request){
        if(!groupMemberRepository.existsByGroup_GroupIdAndUser_UserIdAndAllowedAtIsNotNullAndExitedAtIsNull(request.getGroupId() , userId)){
            throw new GroupException("NOT_GROUP_MEMBER" ,"해당 그룹에 속해있지 않음");
        }


        List<SavingPlan> planHistorys = savingPlanRepository.findByGroup_GroupId(request.getGroupId());

        List<SavingPlanResponse.PlanHistory> histories = new ArrayList<>();
        for(SavingPlan plan : planHistorys){

            if(plan.getStatus().equals("진행중"))
                continue;

            SavingPlanResponse.PlanHistory history = new SavingPlanResponse.PlanHistory();
            history.setPlanId(plan.getPlanId());
            history.setTitle(plan.getPlanTitle());
            history.setCreatedAt(plan.getCreatedAt());
            history.setCompletedAt(plan.getCompletedAt());
            history.setStatus(plan.getStatus());
            histories.add(history);
        }

       SavingPlanResponse.PlanHistoryResponse response = new SavingPlanResponse.PlanHistoryResponse();
        response.setPlans(histories);

        return response;

    }

    //저축 중단
    @Transactional
    public void stopPlan(Long userId, SavingPlanRequest.PlanStopRequest dto){

        SavingPlan savingPlan = savingPlanRepository.findById(dto.getPlanId())
                .orElseThrow(() -> new GroupException("SAVING_PLAN_NOT_FOUND", "해당 적금 플랜을 찾을 수 없음") );
        if(!savingPlan.getStatus().equals("진행중"))
            throw new GroupException("ALREADY_COMPLETED_PLAN", "이미 종료됩 플랜");

        if(savingPlan.getGroup().getOwner().getUserId() != userId)
            throw new GroupException("NOT_GROUP_OWNER", "그룹장이 아님");

        savingPlan.setCompletedAt(LocalDateTime.now().toString());

        System.out.println("적금플랜의 계좌번호는 " + savingPlan.getAccountNo());
        SavingResponse.InquireAccountResponse response1 = ssafySavingService.inquireAccountResponse(savingPlan.getGroup().getOwner().getUserId(),savingPlan.getAccountNo());
        if(Integer.parseInt(response1.getRec().getTotalBalance()) >= savingPlan.getTargetAmount()){

            savingPlan.setStatus("완료");
        }else{
            savingPlan.setStatus("실패");
        }

    }
}
