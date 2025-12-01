package com.example.d105.ssafy.saving.service;

import com.example.d105.domain.group.entity.Group;
import com.example.d105.domain.group.exception.GroupException;
import com.example.d105.domain.group.repository.GroupRepository;
import com.example.d105.ssafy.saving.client.DemandDepositeApiClient;
import com.example.d105.ssafy.saving.dto.response.DemandDepositResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SsafyDemandDepositeService {

    private final DemandDepositeApiClient client;
    private final GroupRepository groupRepository;



    public DemandDepositResponse.CreateDepositResponse createDeposit(Long userId){
        return client.createDemandDeposit(userId);
    }
    public  DemandDepositResponse.DemandDepositeInfo getDemandDepositeInfo(Long groupId){
        return client.getDepositeInfo(groupId);
    }

    public DemandDepositResponse.UpdateDepositResponse updateDepositResponse( Long groupId, Long balance){

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupException("GROUP_NOT_FOUND" ,"해당 그룹을 찾을 수 없음"));
        return client.updateDepositResponse(group, balance);
    }
}
