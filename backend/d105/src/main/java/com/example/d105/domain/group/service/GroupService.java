package com.example.d105.domain.group.service;

import com.example.d105.domain.group.dto.request.GroupRequest;
import com.example.d105.domain.group.dto.response.GroupMemberResponse;
import com.example.d105.domain.group.dto.response.GroupResponse;
import com.example.d105.domain.group.entity.Group;
import com.example.d105.domain.group.entity.GroupMember;
import com.example.d105.domain.group.entity.SavingPlan;
import com.example.d105.domain.group.exception.GroupException;
import com.example.d105.domain.group.repository.GroupMemberRepository;
import com.example.d105.domain.group.repository.GroupRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.d105.domain.group.repository.SavingPlanRepository;
import com.example.d105.domain.user.entity.FcmToken;
import com.example.d105.domain.user.entity.User;
import com.example.d105.domain.user.repository.FcmTokenRepository;
import com.example.d105.domain.user.repository.UserRepository;
import com.example.d105.domain.user.service.UserService;
import com.example.d105.security.service.CryptoService;
import com.example.d105.ssafy.saving.service.SsafyDemandDepositeService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class GroupService {


    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final SsafyDemandDepositeService ssafyDemandDepositeService;

    private final GroupMemberService groupMemberService;
    private final UserService userService;
    private final CryptoService cryptoService;
    private final FcmTokenRepository fcmTokenRepository;
    private final SavingPlanRepository savingPlanRepository;

    //그룹 생성하기
    @Transactional
    public void createGroup(Long userId, GroupRequest.createGroupDTO dto){

        User user = userRepository.findById(userId)
                .orElseThrow( () -> new GroupException("USER_NOT_FOUND" , "존재하지 않는 사용자"));

        //해당 사용자가 이미 그룹이 있는지
        if(groupMemberService.isContainGroup(user)){
            throw new GroupException("ALREADY_GROUP_MEMBER", "이미 그룹에 가입된 사용자");
        }

        Group group = new Group();
        group.setOwner( user);
        group.setDescription( dto.getDescription());
        group.setName(dto.getName());
        group.setImgId(dto.getImgId());

        //invitecode 추가
        group.setInvitationCode(createUniqueInviteCode());
        //자유입출금 계좌 한개 생성
        String accountNo = ssafyDemandDepositeService.createDeposit(userId).getRec().getAccountNo();
        group.setSavingsAccountNo(accountNo);
        Group newGroup = groupRepository.save(group);

        //구릅장도 그룹 맴버에 추가
        groupMemberService.addGroupMember(user, newGroup);

    }


    //초대 코드 생성
    private String createUniqueInviteCode() {
        String inviteCode;
        int maxAttempts = 1000;
        int attempts = 0;

        do {
            if (attempts++ > maxAttempts) {
                throw new RuntimeException("Unique invite code generation failed after max attempts");
            }

            // 1. UUID 생성
            String uuidString = UUID.randomUUID().toString();

            // 2. SHA-256 해시
            byte[] hashBytes;
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                hashBytes = md.digest(uuidString.getBytes(StandardCharsets.UTF_8));
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }

            // 3. 해시 앞 6바이트를 16진수로 변환 -> 12자리 초대코드
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02x", hashBytes[i]));
            }
            inviteCode = sb.toString();

        } while (groupRepository.existsByInvitationCode(inviteCode)); // DB 중복 확인

        return inviteCode;
    }

    //초대코드로 그룹 조회하기
    public GroupResponse.GroupInfoByCode getGroupByCode(String code){
        Group group = groupRepository.findByInvitationCode(code);

        if(group==null)
            throw new GroupException("GROUP_NOT_FOUND" , "해당 그룹 찾을 수 없음");

        if(group.getDeletedAt() != null)
            throw new GroupException("DELETE_GROUP" , "이미 삭제된 그룹입니다");

        GroupResponse.GroupInfoByCode response = new GroupResponse.GroupInfoByCode();
        response.setSavingsAccountNo(group.getSavingsAccountNo());
        response.setName(group.getName());
        response.setDescription(group.getDescription());
        response.setOwnerName(userService.getUserName(group.getOwner().getUserId()) );
        response.setMemberCount(groupMemberService.getCount(group.getGroupId()));

        return response;
    }

    //그룹 id로 그룹 정보 조회
    public GroupResponse.GroupInfo getGroupInfoById(Long groupId){
        Group group = groupRepository.findById(groupId)
                .orElseThrow( () -> new GroupException("GROUP_NOT_FOUND" , "해당 그룹 찾을 수 없음"));

        if(group.getDeletedAt() != null)
            throw new GroupException("DELETE_GROUP" , "이미 삭제된 그룹");

        List<GroupMember> allowedGroupMember = groupMemberRepository.findByGroup_GroupIdAndAllowedAtIsNotNull(groupId);
        List<GroupMemberResponse.GroupMemberInfo> finalAllowedGroupMember = new ArrayList<>();
        for(GroupMember groupMember : allowedGroupMember){
           if(groupMember.getExitedAt() != null)
               continue;
            GroupMemberResponse.GroupMemberInfo info = new GroupMemberResponse.GroupMemberInfo();
            info.setMemberId(groupMember.getMemberId());
            info.setUserId(groupMember.getUser().getUserId());
            info.setUsername(cryptoService.decryptAES(groupMember.getUser().getUsername()));
            info.setDisplayname(groupMember.getDisplayName());
            finalAllowedGroupMember.add(info);
        }
        List<GroupMember> waitingMember = groupMemberRepository.findByGroup_GroupIdAndAllowedAtIsNullAndRejectedAtIsNull(groupId);
        List<GroupMemberResponse.GroupMemberInfo> finalWaitingMembers = new ArrayList<>();
        for(GroupMember groupMember : waitingMember){
            if(groupMember.getMemberStatus() == 1 || groupMember.getExitedAt() != null)
                continue;
            GroupMemberResponse.GroupMemberInfo info = new GroupMemberResponse.GroupMemberInfo();
            info.setMemberId(groupMember.getMemberId());
            info.setUserId(groupMember.getUser().getUserId());
            info.setUsername(cryptoService.decryptAES(groupMember.getUser().getUsername()));
            info.setDisplayname(groupMember.getDisplayName());
            finalWaitingMembers.add(info);
        }

        GroupResponse.GroupInfo info = new GroupResponse.GroupInfo();
        info.setInviteCode(group.getInvitationCode());
        info.setSavingAccountNo(group.getSavingsAccountNo());
        info.setJoinedMembers(finalAllowedGroupMember);
        info.setWaitingMember(finalWaitingMembers);
        info.setAmount(Integer.parseInt(ssafyDemandDepositeService.getDemandDepositeInfo(groupId).getRec().getAccountBalance()));
        info.setImgId(group.getImgId());
        info.setCreatedAt(group.getCreatedAt().toString());
        info.setGroupName(group.getName());
        info.setGroupDescription(group.getDescription());

        List<SavingPlan> planList = savingPlanRepository.findByGroup_GroupIdAndStatus(groupId, "진행중");
        if(planList.size() ==0 || planList.isEmpty())
            info.setPlanId(0L);
        else
            info.setPlanId(planList.getFirst().getPlanId());

        return info;

    }

    //그룹 정보 수정
    @Transactional
    public void modifyGroupInfo(Long userId, GroupRequest.ModifyGroupInfo dto){

        //해당 유저가 그룹장이 아닌 경우
        List<GroupMember> groupMember = groupMemberRepository.findByGroup_GroupIdAndUser_UserId(dto.getGroupId(), userId);
        boolean isOwner = false;
        for(GroupMember member: groupMember){
            if(member.getMemberStatus() ==1 && member.getAllowedAt() != null && member.getExitedAt() == null){
                isOwner = true;
                break;
            }
        }

        if(!isOwner){
            throw new GroupException("NOT_GROUP_OWNER", "그룹장이 아님");
        }

        Group group = groupRepository.findById(dto.getGroupId())
                .orElseThrow(() -> new GroupException("GROUP_NOT_FOUND" ,"해당 그룹을 찾을 수 없음"));

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));

        group.setName(dto.getName());
        group.setDescription(dto.getDescription());
        group.setUpdatedAt(now);
        group.setImgId(dto.getImgId());

        groupRepository.save(group);
    }

    //그룹 삭제
    @Transactional
    public void deleteGroup(Long userId, GroupRequest.GroupId dto){
        Group group = groupRepository.findById(dto.getGroupId())
                .orElseThrow(() -> new GroupException("GROUP_NOT_FOUND" , "해당 그룹 찾을 수 없음"));

        if(group.getOwner().getUserId() != userId)
            throw new GroupException("NOT_GROUP_OWNER" , "그룹장이 아님");
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        group.setDeletedAt(now);
        groupRepository.save(group);

        //그룹원들 다 탈퇴처리
        List<GroupMember> groupMembersContaineGroup = groupMemberRepository.findByGroup(group);
        for(GroupMember groupMember : groupMembersContaineGroup){
            groupMember.setExitedAt(LocalDateTime.now().toString());
            groupMemberRepository.save(groupMember);
        }

        List<FcmToken> tokens = fcmTokenRepository.findByGroupId(group.getGroupId());
        fcmTokenRepository.deleteAll(tokens);

    }

    public boolean isOwner(Long userId, Long groupId){

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupException("GROUP_NOT_FOUND" , "해당 그룹 찾을 수 없음"));

        if(group.getOwner().getUserId() == userId)
            return true;
        else
            return false;
    }




}