package com.example.d105.domain.group.scheduler;

import com.example.d105.domain.group.entity.SavingPlan;
import com.example.d105.domain.group.repository.SavingPlanRepository;
import com.example.d105.domain.user.service.FcmService;
import com.example.d105.ssafy.saving.dto.response.SavingResponse;
import com.example.d105.ssafy.saving.service.SsafySavingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SavingPlanSchedulerService {

    private final SavingPlanRepository savingPlanRepository;
    private final SsafySavingService ssafySavingService;
    private final FcmService fcmService;
    /**
     * 매일 자정에 실행되는 스케줄러
     * 어제 목표일이었던 저축 플랜들을 체크하여 완료 처리
     */
    @Scheduled(cron = "0 0 0 * * *") // 매일 자정 실행
    @Transactional
    public void checkCompletedSavingPlans() {
        log.info("저축 플랜 완료 체크 스케줄러 시작");

        LocalDate yesterday = LocalDate.now().minusDays(1);

        // 어제 목표일이었고 아직 진행중인 저축 플랜들 조회
        List<SavingPlan> targetsPlans = savingPlanRepository
                .findByTargetDateAndStatus(yesterday, "진행중");

        if (targetsPlans.isEmpty()) {
            log.info("어제 목표일인 진행중 저축 플랜이 없습니다.");
            return;
        }

        log.info("체크할 저축 플랜 수: {}", targetsPlans.size());


        for (SavingPlan plan : targetsPlans) {
            try {
                //해당 계좌의 잔액 조회
                SavingResponse.InquireAccountResponse response1 = ssafySavingService.inquireAccountResponse(plan.getGroup().getOwner().getUserId(),plan.getAccountNo());
                plan.setCompletedAt(LocalDateTime.now().toString());
                if(Integer.parseInt(response1.getRec().getTotalBalance()) >= plan.getTargetAmount()){

                    plan.setStatus("완료");
                    fcmService.sendMessageFinal(plan.getGroup().getGroupId() , "✨ 새로운 성공 알림!" ,"새로운 저축 플래너 목표에 달성되었습니다. 확인해보세요!" ,1);
                }else{
                    plan.setStatus("실패");
                }

            } catch (Exception e) {
                log.error("저축 플랜 처리 중 오류 발생. planId: {}, error: {}",
                        plan.getPlanId(), e.getMessage(), e);

            }
        }


    }
}
