package com.example.d105.domain.group.controller;

import com.example.d105.domain.group.dto.request.GroupMemberRequest;
import com.example.d105.domain.group.dto.request.GroupRequest;
import com.example.d105.domain.group.dto.response.GroupResponse;
import com.example.d105.domain.group.service.GroupMemberService;
import com.example.d105.domain.group.service.GroupService;
import com.example.d105.domain.user.entity.User;
import com.example.d105.domain.user.fixture.TestUserFactory;
import com.example.d105.security.dto.CustomUserDetails;
import com.example.d105.ssafy.saving.service.SsafyDemandDepositeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("GroupController 단위 테스트")
public class GroupControllerTest {

    @Mock
    private GroupService groupService;

    @Mock
    private GroupMemberService groupMemberService;

    @Mock
    private SsafyDemandDepositeService ssafyDemandDepositeService;

    @InjectMocks
    private GroupController groupController;

    private User testUser;
    private CustomUserDetails customUserDetails;

    @BeforeEach
    void setUp() {
        testUser = TestUserFactory.createTestUser();
        customUserDetails = new CustomUserDetails(testUser);
    }

    @Test
    @DisplayName("그룹 생성 성공")
    void createGroup_Success() {
        // given
        GroupRequest.createGroupDTO request = new GroupRequest.createGroupDTO(
                "테스트 그룹", "테스트 그룹 설명", 1
        );

        doNothing().when(groupService).createGroup(eq(1L), any(GroupRequest.createGroupDTO.class));

        // when
        ResponseEntity<Void> response = groupController.createGroup(customUserDetails, request);

        // then
        assertEquals(200, response.getStatusCodeValue());
        verify(groupService, times(1)).createGroup(eq(1L), any(GroupRequest.createGroupDTO.class));
    }

    @Test
    @DisplayName("그룹 생성 시 서비스에서 예외 발생")
    void createGroup_ServiceThrowsException() {
        // given
        GroupRequest.createGroupDTO request = new GroupRequest.createGroupDTO(
                "테스트 그룹", "테스트 그룹 설명", 1
        );

        doThrow(new RuntimeException("그룹 생성 실패"))
                .when(groupService).createGroup(eq(1L), any(GroupRequest.createGroupDTO.class));

        // when & then
        assertThrows(RuntimeException.class, () ->
                groupController.createGroup(customUserDetails, request)
        );

        verify(groupService, times(1)).createGroup(eq(1L), any(GroupRequest.createGroupDTO.class));
    }

    @Test
    @DisplayName("그룹 코드로 그룹 조회 성공")
    void groupInfoByCode_Success() {
        // given
        GroupRequest.GroupInfoByCode request = new GroupRequest.GroupInfoByCode("TESTCODE");
        GroupResponse.GroupInfoByCode expectedResponse = new GroupResponse.GroupInfoByCode(
                "123456789", "테스트 그룹", "테스트 설명", "그룹장", 5L
        );

        given(groupService.getGroupByCode("TESTCODE")).willReturn(expectedResponse);

        // when
        ResponseEntity<GroupResponse.GroupInfoByCode> response = groupController.groupInfoByCode(request);

        // then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("테스트 그룹", response.getBody().getName());
        assertEquals("테스트 설명", response.getBody().getDescription());
        assertEquals("그룹장", response.getBody().getOwnerName());
        assertEquals(5L, response.getBody().getMemberCount());

        verify(groupService, times(1)).getGroupByCode("TESTCODE");
    }

    @Test
    @DisplayName("그룹장인지 확인")
    void checkOwner(){

        GroupRequest.GroupId request = new GroupRequest.GroupId(1L);
        Boolean expectedResponse = true;

        given(groupService.isOwner(customUserDetails.getUser().getUserId(), 1L)).willReturn(expectedResponse);
        ResponseEntity<Boolean> response = groupController.checkOwner(customUserDetails , request);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(true, true);
    }
    @Test
    @DisplayName("그룹 멤버의 displayName 수정")
    void updateGroupMember(){
        GroupMemberRequest.UpdateMemberDisplayRequest request = new GroupMemberRequest.UpdateMemberDisplayRequest(1L , "수정됨");
        doNothing().when(groupMemberService).updateDisplayName(eq(1L), any(GroupMemberRequest.UpdateMemberDisplayRequest.class));

        // when
        ResponseEntity<Void> response = groupController.updateGroupMember(customUserDetails, request);

        // then
        assertEquals(200, response.getStatusCodeValue());
        verify(groupMemberService, times(1)).updateDisplayName(eq(1L), any(GroupMemberRequest.UpdateMemberDisplayRequest.class));

    }

    @Test
    @DisplayName("그룹 나가기")
    void leaveGroup(){
        GroupRequest.GroupId request = new GroupRequest.GroupId(1L);
        doNothing().when(groupMemberService).leaveGroup(eq(1L), any(GroupRequest.GroupId.class));

        ResponseEntity<Void> response = groupController.leaveGroup(customUserDetails, request);

        assertEquals(HttpStatus.OK,  response.getStatusCode());
        verify(groupMemberService, times(1)).leaveGroup(eq(1L), any( GroupRequest.GroupId.class));
    }

    @Test
    @DisplayName("그룹 삭제")
    void deleteGroup(){
        GroupRequest.GroupId request = new GroupRequest.GroupId(1L);
        doNothing().when(groupService).deleteGroup(eq(1L) , any(GroupRequest.GroupId.class));

        ResponseEntity<Void> response = groupController.deleteGroup(customUserDetails, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(groupService, times(1)).deleteGroup(eq(1L) , any(GroupRequest.GroupId.class));

    }


}
