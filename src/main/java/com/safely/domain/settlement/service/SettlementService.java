package com.safely.domain.settlement.service;

import com.safely.domain.expense.entity.Expense;
import com.safely.domain.expense.repository.ExpenseRepository;
import com.safely.domain.group.entity.Group;
import com.safely.domain.group.entity.GroupMember;
import com.safely.domain.group.repository.GroupMemberRepository;
import com.safely.domain.group.repository.GroupRepository;
import com.safely.domain.member.entity.Member;
import com.safely.domain.settlement.dto.SettlementResponse;
import com.safely.domain.settlement.entity.Settlement;
import com.safely.domain.settlement.repository.SettlementRepository;
import com.safely.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementService {

    private final ExpenseRepository expenseRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final SettlementRepository settlementRepository;
    private final GroupRepository groupRepository;

    // 1. 정산 프리뷰 (DB 저장 X, 계산 결과만 반환)
    public List<SettlementResponse> getSettlementPreview(Long groupId) {
        // 그룹 멤버와 지출 내역 조회
        List<GroupMember> groupMembers = findGroupMembers(groupId);

        // 여기서 null 값 처리를 안 하는 이유는 List나 set은 값이 없을 경우 비어있는 리스트를 반환하기 때문임.
        List<Expense> expenses = expenseRepository.findAllByGroupId(groupId);

        // 계산 로직 수행
        Map<Member, Long> resultMap = calculateSettlement(groupMembers, expenses);

        // 결과 DTO 변환 및 반환
        return resultMap.entrySet().stream()
                .map(entry -> SettlementResponse.of(entry.getKey(), entry.getValue()))
                .toList();
    }

    // 2. 정산 완료 (계산 후 DB 저장)
    @Transactional
    public void completeSettlement(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(NotFoundException::new);

        List<GroupMember> groupMembers = findGroupMembers(groupId);
        List<Expense> expenses = expenseRepository.findAllByGroupId(groupId);

        Map<Member, Long> resultMap = calculateSettlement(groupMembers, expenses);

        // DB Upsert (기존 내역 있으면 업데이트, 없으면 생성)
        for (Map.Entry<Member, Long> entry : resultMap.entrySet()) {
            Member member = entry.getKey();
            Long netAmount = entry.getValue();

            // 여기서 orElse 대신 orElseGet(함수)를 사용하는 이유 : orElse는 new Settlement()를 실행해서 낭비발생할 여지가 있음.
            // 반면에 orElseGet()은 DB에 데이터가 있으면, 괄호안의 코드를 아예 실행하지 않아서 효율적임.
            Settlement settlement = settlementRepository.findByGroupIdAndMemberId(groupId, member.getId())
                    .orElseGet(() -> Settlement.builder()
                            .group(group)
                            .member(member)
                            .netAmount(0L)
                            .isSettled(false)
                            .build());

            // 정산 확정 (netAmount 저장, isSettled=true, settledAt=now)
            settlement.update(netAmount, true);
            settlementRepository.save(settlement);
        }
    }

    // 3. 정산 취소 (0원으로 초기화)
    // 여기서 settlementRepository.save(settlement);를 수행하지 않는 이유는 영속성 컨텍스트의 변경감지(Dirty Checking) 기능 때문임.
    // @Transactional 어노테이션 때문에, 영속성 컨텍스트가 활성화 되는데, 메서드 종료 시점에 JPA는 "최초 시점"과 "현재 객체 상태"를 비교.
    // 변경된 부분(Dirty)이 감지되면, JPA가 알아서 UPdATE SQL을 생성하여 DB에 날림.
    // 따라서, 조회한 엔티티의 값을 수정하는 로직에서는 save()를 명시적으로 호출하지 않는 것이 JPA의 표준 관례(Idiom)임.
    // 만약, @Transactional 어노테이션이 없으면 save()가 필요함.
    @Transactional
    public void cancelSettlement(Long groupId) {
        List<Settlement> settlements = settlementRepository.findAllByGroupId(groupId);
        for (Settlement settlement : settlements) {
            // 초기화 (netAmount=0, isSettled=false, settledAt=null)
            settlement.update(0L, false);
        }
    }

    // 헬퍼 메서드
    private List<GroupMember> findGroupMembers(Long groupId) {
        return groupMemberRepository.findAll().stream()
                .filter(gm -> gm.getGroup().getId().equals(groupId))
                .toList();
    }

    // 정산 알고리즘 (핵심)
    private Map<Member, Long> calculateSettlement(List<GroupMember> groupMembers, List<Expense> expenses) {
        Map<Member, Long> balanceMap = new HashMap<>();

        // 모든 멤버 0원으로 초기화
        for (GroupMember gm : groupMembers) {
            balanceMap.put(gm.getMember(), 0L);
        }

        for (Expense expense : expenses) {
            Member payer = expense.getPayer();
            Long totalAmount = expense.getAmount();

            // A. 결제자: (+) 받을 권리 증가
            balanceMap.put(payer, balanceMap.getOrDefault(payer, 0L) + totalAmount);

            // B. 참여자: (-) 내야 할 의무 증가
            expense.getParticipants().forEach(participant -> {
                Member m = participant.getMember();
                Long splitAmount = participant.getAmount();
                balanceMap.put(m, balanceMap.getOrDefault(m, 0L) - splitAmount);
            });
        }
        return balanceMap;
    }
}