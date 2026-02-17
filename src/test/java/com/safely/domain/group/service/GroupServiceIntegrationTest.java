package com.safely.domain.group.service;

import com.safely.domain.group.GroupRole;
import com.safely.domain.group.dto.GroupCreateRequest;
import com.safely.domain.group.entity.Group;
import com.safely.domain.group.entity.GroupMember;
import com.safely.domain.group.repository.GroupMemberRepository;
import com.safely.domain.group.repository.GroupRepository;
import com.safely.domain.member.entity.Member;
import com.safely.domain.member.repository.MemberRepository;
import com.safely.global.exception.common.EntityNotFoundException;
import com.safely.global.exception.group.InvalidInviteCodeException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnectionFactory; // 추가
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class GroupServiceIntegrationTest {

    @Autowired GroupService groupService;
    @Autowired MemberRepository memberRepository;
    @Autowired GroupRepository groupRepository;
    @Autowired GroupMemberRepository groupMemberRepository;
    @Autowired EntityManager em;

    // GroupService 테스트지만 ApplicationContext가 뜰 때 Redis를 찾을 수 있으므로 안전장치 추가
    @MockitoBean RedisConnectionFactory redisConnectionFactory;

    @Test
    @DisplayName("통합: 그룹 생성 시 멤버가 MANAGER로 등록되고 날짜가 저장된다.")
    void createGroup_Integration() {
        // 1. Given
        Member member = memberRepository.save(Member.builder()
                .email("maker@safely.com")
                .password("1234")
                .name("생성자")
                .authority("ROLE_USER")
                .build());

        GroupCreateRequest request = GroupCreateRequest.builder()
                .name("제주도 여행")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(3))
                .destination("Jeju")
                .build();

        // 2. When
        Long groupId = groupService.createGroup(member.getId(), request);

        // 영속성 컨텍스트 초기화 (DB 반영 및 캐시 삭제)
        em.flush();
        em.clear();

        // 3. Then
        Group savedGroup = groupRepository.findById(groupId).orElseThrow();
        assertThat(savedGroup.getName()).isEqualTo("제주도 여행");

        List<GroupMember> members = groupMemberRepository.findByGroup(savedGroup);
        assertThat(members).hasSize(1);

        GroupMember manager = members.get(0);
        assertThat(manager.getRole()).isEqualTo(GroupRole.MANAGER);
        assertThat(manager.getCreatedAt()).isNotNull(); // Auditing 확인
        assertThat(manager.getMember().getId()).isEqualTo(member.getId());
    }

    @Test
    @DisplayName("실패: 존재하지 않는 회원이 그룹을 생성하려 하면 예외가 발생한다.")
    void createGroup_Fail_MemberNotFound() {
        // 1. Given
        Long invalidMemberId = 999999L;

        GroupCreateRequest request = GroupCreateRequest.builder()
                .name("유령 여행")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .build();

        // 2. When & Then
        assertThatThrownBy(() -> groupService.createGroup(invalidMemberId, request))
                .isInstanceOf(EntityNotFoundException.class); // 예외 타입 변경
    }

    @Test
    @DisplayName("실패: 그룹 이름 등 필수 데이터가 누락되면 DB 예외가 발생한다.")
    void createGroup_Fail_DataIntegrity() {
        // 1. Given
        Member member = memberRepository.save(Member.builder()
                .email("integrity@test.com")
                .password("1234")
                .name("테스터")
                .authority("ROLE_USER")
                .build());

        // 이름(Name)이 NULL인 잘못된 요청 객체 생성
        // Group 엔티티에 @Column(name = "group_name", nullable = false)가 있음.
        GroupCreateRequest badRequest = GroupCreateRequest.builder()
                .name(null)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now())
                .build();

        // 2. When & Then
        // 스프링 데이터 JPA는 DB 제약조건 위반 시 DataIntegrityViolationException을 던짐
        assertThatThrownBy(() -> groupService.createGroup(member.getId(), badRequest))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("그룹 가입 성공: 유효한 초대 코드로 가입하면 GroupMember가 생성된다.")
    void joinGroupByCode_Success() {
        // 방장과 그룹 생성
        Member owner = memberRepository.save(Member.builder().email("owner@a.com").password("1").name("방장").authority("USER").build());
        Long groupId = groupService.createGroup(owner.getId(), new GroupCreateRequest("테스트그룹", LocalDate.now(), LocalDate.now(), "Seoul"));

        Group group = groupRepository.findById(groupId).orElseThrow();
        String inviteCode = group.getInviteCode();

        // 가입할 새로운 유저 생성
        Member joiner = memberRepository.save(Member.builder().email("joiner@a.com").password("1").name("참여자").authority("USER").build());

        // 가입 요청
        groupService.joinGroupByCode(joiner.getId(), inviteCode);

        // 검증
        em.flush();
        em.clear(); // DB 반영 후 캐시 비움

        boolean isMember = groupMemberRepository.existsByGroupIdAndMemberId(groupId, joiner.getId());
        assertThat(isMember).isTrue();
    }

    @Test
    @DisplayName("그룹 가입 실패: 초대 코드가 틀리면 InvalidInviteCodeException 발생")
    void joinGroupByCode_Fail_InvalidCode() {
        Member member = memberRepository.save(Member.builder().email("wrong@a.com").password("1").name("멤버").authority("USER").build());

        assertThatThrownBy(() -> groupService.joinGroupByCode(member.getId(), "INVALID_CODE"))
                .isInstanceOf(InvalidInviteCodeException.class);
    }
}