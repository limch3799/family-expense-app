package com.example.d105.domain.group.controller;

import com.example.d105.domain.group.dto.request.GroupMemberRequest;
import com.example.d105.domain.group.dto.request.GroupRequest;
import com.example.d105.domain.group.dto.response.GroupResponse;
import com.example.d105.domain.group.service.GroupMemberService;
import com.example.d105.domain.group.service.GroupService;
import com.example.d105.security.dto.CustomUserDetails;

import com.example.d105.ssafy.saving.dto.response.SavingResponse;
import com.example.d105.ssafy.saving.service.SsafyDemandDepositeService;
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

import java.util.Map;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
@Tag(name = "2. 그룹 관리", description = "Group Management")
public class GroupController {

    private final GroupService groupService;
    private final GroupMemberService groupMemberService;
    private final SsafyDemandDepositeService ssafyDemandDepositeService;
    @PostMapping("/create")
    @Operation(
            summary = "그룹 생성",
            description = "그룹장이 그룹을 생성합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(description = "USER_NOT_FOUND: 존재하지 않는 사용자"),
            @ApiResponse(description = "ALREADY_GROUP_MEMBER: 이미 그룹에 가입된 사용자"),
    })
    public ResponseEntity<Void> createGroup(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody GroupRequest.createGroupDTO dto
    ) {
        groupService.createGroup(user.getUser().getUserId(), dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/invitation/info")
    @Operation(
            summary = "그룹 코드로 그룹 조회",
            description = "그룹 크드로 그룹을 조회합니다. 그룹의 인원수는 조장 포함해서 반환합니다."
    )
    public ResponseEntity<GroupResponse.GroupInfoByCode> groupInfoByCode(@RequestBody GroupRequest.GroupInfoByCode dto){

        return ResponseEntity.ok(groupService.getGroupByCode(dto.getInvitationCode()));

    }

    @PostMapping("/invitation/join")
    @Operation(
            summary = "그룹 코드로 가입 신청",
            description = "그룹 코드로 그룹 가입을 신청합니다. 그룹 가입 시 초기 상태는 대기상태 (0) 으로 지정됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(description = "USER_NOT_FOUND: 존재하지 않는 사용자"),
            @ApiResponse(description = "ALREADY_GROUP_MEMBER: 이미 그룹에 가입된 사용자"),
    })
    public ResponseEntity<Void> joinGroupByCode(@AuthenticationPrincipal CustomUserDetails user, @RequestBody GroupMemberRequest.createGroupMemberDTO dto){
        groupMemberService.addGroupMember(user.getUser().getUserId(),dto );
        return ResponseEntity.ok().build();

    }

    @PostMapping("/approve-member")
    @Operation(
            summary = "가입 승인",
            description = "memberId는 userId가 아닙니다. group member 테이블에 있는 memberId로 보내주세요"

    )
    @ApiResponses(value = {
            @ApiResponse(description = "NOT_GROUP_OWNER: 그룹장이 아님"),
            @ApiResponse(description = "GROUP_NOT_FOUND: 해당 그룹을 찾을 수 없음"),
    })
    public ResponseEntity<Void> allowMember(@AuthenticationPrincipal CustomUserDetails user, @RequestBody  GroupMemberRequest.AllowGroupMemberDTO dto){
        groupMemberService.allowMember(user.getUser().getUserId(),dto );
        return ResponseEntity.ok().build();

    }

    @PostMapping("/reject-member")
    @Operation(
            summary = "가입 거절",
            description = "memberId는 userId가 아닙니다. group member 테이블에 있는 memberId로 보내주세요"
    )
    @ApiResponses(value = {
            @ApiResponse(description = "NOT_GROUP_OWNER: 그룹장이 아님"),
            @ApiResponse(description = "GROUP_NOT_FOUND: 해당 그룹을 찾을 수 없음"),
    })
    public ResponseEntity<Void> rejectMember(@AuthenticationPrincipal CustomUserDetails user, @RequestBody  GroupMemberRequest.AllowGroupMemberDTO dto){
        groupMemberService.rejectMember(user.getUser().getUserId(),dto );
        return ResponseEntity.ok().build();

    }
    @PostMapping("/info")
    @Operation(
            summary = "그룹 정보 조회",
            description = "그룹 id로 그룹 정보를 조회합니다"
    )
    @ApiResponses(value = {
            @ApiResponse(description = "GROUP_NOT_FOUND: 해당 그룹 찾을 수 없음")
    })
    public ResponseEntity<GroupResponse.GroupInfo> getGroupId(@RequestBody  GroupRequest.GroupId dto){

        return ResponseEntity.ok(groupService.getGroupInfoById(dto.getGroupId()));

    }

    @PostMapping("/update")
    @Operation(
            summary = "그룹 정보 수정",
            description = "그룹 정보를 수정합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(description = "GROUP_NOT_FOUND: 해당 그룹 찾을 수 없음"),
            @ApiResponse(description = "NOT_GROUP_OWNER: 그룹장이 아님")
    })

    public ResponseEntity<Void> modifyGroup(@AuthenticationPrincipal CustomUserDetails user, @RequestBody  GroupRequest.ModifyGroupInfo dto){
        groupService.modifyGroupInfo(user.getUser().getUserId(), dto);
        return ResponseEntity.ok().build();

    }

    @PostMapping("/update/member")
    @Operation(
            summary = "그룹 맴버 display 수정(memberId임 userId 아님)",
            description = "그룹 맴버의 display를 수정"
    )

    public ResponseEntity<Void> updateGroupMember(@AuthenticationPrincipal CustomUserDetails user, @RequestBody GroupMemberRequest.UpdateMemberDisplayRequest request ){
        groupMemberService.updateDisplayName(user.getUser().getUserId(), request);
        return ResponseEntity.ok().build();

    }

    @PostMapping("/leave")
    @Operation(
            summary = "그룹 나가기",
            description = "그룹 맴버가 해당 그룹을 나갑니다."
    )
    @ApiResponses(value = {
            @ApiResponse(description = "GROUP_NOT_FOUND: 해당 그룹 찾을 수 없음"),
            @ApiResponse(description = "CANNOT_LEAVE_OWNER: 그룹장은 그룹을 나갈 수 없음"),
            @ApiResponse(description = "MEMBER_NOT_FOUND: 해당 멤버를 찾을 수 없음"),
            @ApiResponse(description = "MEMBER_NOT_FOUND: 해당 멤버를 찾을 수 없음")
    })
    public ResponseEntity<Void> leaveGroup(@AuthenticationPrincipal CustomUserDetails user, @RequestBody  GroupRequest.GroupId dto){
        groupMemberService.leaveGroup(user.getUser().getUserId(), dto);
        return ResponseEntity.ok().build();

    }

    @PostMapping("/delete")
    @Operation(
            summary = "그룹 삭제",
            description = "그룹장이 해당 그룹을 나갑니다"
    )
    @ApiResponses(value = {
            @ApiResponse(description = "GROUP_NOT_FOUND: 해당 그룹 찾을 수 없음"),
            @ApiResponse(description = "NOT_GROUP_OWNER: 그룹장이 아님")
    })
    public ResponseEntity<Void> deleteGroup(@AuthenticationPrincipal CustomUserDetails user, @RequestBody  GroupRequest.GroupId dto){
       groupService.deleteGroup(user.getUser().getUserId(), dto);
        return ResponseEntity.ok().build();

    }

    @PostMapping("/contain")
    @Operation(
            summary = "그룹 가입 여부",
            description = "해당 멤버가 그룹에 포함되어 있는지 확인"
    )

    public ResponseEntity<Map<String, Object>> getContainsGroup(@AuthenticationPrincipal CustomUserDetails user){
        return ResponseEntity.ok(groupMemberService.getContainGroup(user.getUser().getUserId()));

    }

    @PostMapping("/updatedeposit")
    @Operation(
            summary = "자유 입출금 계좌 입금",
            description = "자유 입출금 계좌에 임의로 입금하는 임의 api"
    )

    public ResponseEntity<SavingResponse.CreateSavingResponse> createGroup(
            @RequestBody GroupRequest.UpdateDeposit dto
    ) {


        ssafyDemandDepositeService.updateDepositResponse(dto.getGroupId(), dto.getBalance());

        return ResponseEntity.ok().build();
    }
    @PostMapping("/owner")
    @Operation(
            summary = "현재 그룹에 그룹장인지 확인",
            description = "현재 그룹의 그룹장인지 확인하는 api"
    )

    public ResponseEntity<Boolean> checkOwner(
            @AuthenticationPrincipal CustomUserDetails user, @RequestBody  GroupRequest.GroupId dto
    ) {




        return ResponseEntity.ok(groupService.isOwner(user.getUser().getUserId(), dto.getGroupId()));
    }
}
