package com.example.d105.domain.group.service;

import com.example.d105.domain.group.dto.request.GroupMemberRequest;
import com.example.d105.domain.group.dto.request.GroupRequest;
import com.example.d105.domain.group.dto.response.GroupResponse;
import com.example.d105.domain.group.entity.Group;
import com.example.d105.domain.group.entity.GroupMember;
import com.example.d105.domain.group.exception.GroupException;
import com.example.d105.domain.group.repository.GroupMemberRepository;
import com.example.d105.domain.group.repository.GroupRepository;
import com.example.d105.domain.user.entity.FcmToken;
import com.example.d105.domain.user.entity.User;
import com.example.d105.domain.user.repository.FcmTokenRepository;
import com.example.d105.domain.user.repository.UserRepository;
import com.example.d105.domain.user.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class GroupMemberService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserService userService;
    private final FcmTokenRepository fcmTokenRepository;

    //그룹에 맴버 추가하기
    @Transactional
    public void addGroupMember(Long userId, GroupMemberRequest.createGroupMemberDTO dto) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GroupException("USER_NOT_FOUND","존재하지 않는 사용자"));


        if (isContainGroup(user)) {
            throw new GroupException("ALREADY_GROUP_MEMBER", "이미 그룹에 가입된 사용자");
        }


        Group group = groupRepository.findByInvitationCode(dto.getInviteCode());




        GroupMember member = new GroupMember();
        member.setMemberStatus(Short.valueOf((short) 3));
        member.setUser(user);
        member.setGroup(group);
        member.setDisplayName(userService.getUserName(user.getUserId()));

        groupMemberRepository.save(member);

    }


    //그룹맴버에 주장 추가하기
    @Transactional
    public void addGroupMember(User user, Group group) {


        GroupMember member = new GroupMember();
        member.setMemberStatus(Short.valueOf((short) 1));
        member.setUser(user);
        member.setGroup(group);
        member.setDisplayName(userService.getUserName(user.getUserId()));
        member.setAllowedAt(LocalDateTime.now().toString());


        groupMemberRepository.save(member);
    }

    //해당 그룹에 포함된 멤버 수
    public Long getCount(Long gruoupId) {
        return groupMemberRepository.countByGroup_GroupId(gruoupId);
    }
//
//    //해당 사용자가 그룹에 포함되어 있는지
    public boolean isContainGroup(User user) {
//        이미 가입 되어있는지 확인하는법
//        allowedat이 null이 아니면 exitedat이 null이 아니여야함
//        allowedat이 null이면 rejectedat이 null이아니여야함

       List<GroupMember> members = groupMemberRepository.findByUser(user);
       for(GroupMember member : members){
           if(member.getAllowedAt() != null && member.getExitedAt() == null)
               return true;

           if(member.getAllowedAt() == null && member.getRejectedAt() ==null)
               return true;
       }

       return false;
    }

    //그룹 가입 승인
    @Transactional
    public void allowMember(Long userId, GroupMemberRequest.AllowGroupMemberDTO dto){

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

        GroupMember allowMember = groupMemberRepository.findById(dto.getMemberId())
                .orElseThrow( () -> new GroupException("GROUP_NOT_FOUND" , "해당 그룹을 찾을 수 없음"));

        allowMember.setAllowedAt(LocalDateTime.now().toString());
        groupMemberRepository.save(allowMember);

    }

    //그룹 가입 거절
    @Transactional
    public void rejectMember(Long userId, GroupMemberRequest.AllowGroupMemberDTO dto){

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

        GroupMember allowMember = groupMemberRepository.findById(dto.getMemberId())
                .orElseThrow( () -> new GroupException("GROUP_NOT_FOUND" , "해당 그룹을 찾을 수 없음"));

        allowMember.setRejectedAt(LocalDateTime.now().toString());
        groupMemberRepository.save(allowMember);

    }

    //그룹 나가기
    @Transactional
    public void leaveGroup(Long userId,  GroupRequest.GroupId dto){
        GroupMember findGroupMember = null;


        List <GroupMember> groupMember = groupMemberRepository.findByGroup_GroupIdAndUser_UserId(dto.getGroupId(), userId);
        for(GroupMember member: groupMember){
            if(member.getAllowedAt() != null && member.getExitedAt() == null)
            {
                findGroupMember = member;
                break;
            }
        }
        //그룹 소유자는 그룹 못나감
        Group group = groupRepository.findById(dto.getGroupId())
                .orElseThrow(() -> new GroupException("GROUP_NOT_FOUND" , "해당 그룹을 찾을 수 없음"));

        if(group.getOwner().getUserId() == userId)
            throw new GroupException("CANNOT_LEAVE_OWNER" , "그룹장은 그룹을 나갈 수 없음");


        //해당 그룹 멤버가 존재 하지 않다면
        if(findGroupMember == null)
            throw new   GroupException("MEMBER_NOT_FOUND" , "해당 멤버를 찾을 수 없음");


        findGroupMember.setExitedAt(LocalDateTime.now().toString());
        groupMemberRepository.save(findGroupMember);

        List<FcmToken> tokens = fcmTokenRepository.findByUserIdAndGroupId(userId, dto.getGroupId());
        fcmTokenRepository.deleteAll(tokens);
    }

    //그룹에 포함되어있는지 확인
    public Map<String,Object> getContainGroup(Long userId){
    Map<String,Object> result = new HashMap<>();
    result.put("response" ,groupMemberRepository.existsByUser_UserIdAndAllowedAtIsNotNullAndExitedAtIsNull(userId) );
    return result ;

    }

    //그룹 멤버 닉네임 수정
    @Transactional
    public void updateDisplayName(Long userId, GroupMemberRequest.UpdateMemberDisplayRequest request ){


        GroupMember groupMember = groupMemberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new GroupException("NOT_FOUND_GROUPMEMBER" , "해당 그룹원을 찾을 수 없음"));


        if(groupMember.getExitedAt() != null){
            throw new GroupException("ALEADY_EXISTS_MEMBER" , "이미 탈퇴한 맴버");
        }


        if(userId != groupMember.getGroup().getOwner().getUserId() && userId != groupMember.getUser().getUserId()){
            throw new GroupException("CANT_UPDATE_DISPLAYNAME" , "본인이나 그룹장만 닉네임 수정 가능");
        }
        groupMember.setDisplayName(request.getDisplayName());
        groupMemberRepository.save(groupMember);

    }


//    //해당 유저의 해당 그룹 반환
//    public Optional<GroupMember> groupMember(Long groupId, Long userId){
//        return   groupMemberRepository.findByGroup_GroupIdAndUser_UserId(groupId, userId);
//    }
//
//    public List<GroupMember> allowedGroupMember(Long groupId){
//        return groupMemberRepository.findByGroup_GroupIdAndAllowedAtIsNotNull(groupId);
//    }
//
//    public List<GroupMember> waitingGroupMember(Long groupId){
//        return groupMemberRepository.findByGroup_GroupIdAndAllowedAtIsNullAndRejectedAtIsNull(groupId);
//    }


}
