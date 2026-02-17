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
import com.safely.global.exception.ErrorCode;
import com.safely.global.exception.common.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementService {
    private final ExpenseRepository expenseRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final SettlementRepository settlementRepository;
    private final GroupRepository groupRepository;

    // 정산 프리뷰 (DB 저장 X, 계산 결과만 반환)
    public List<SettlementResponse> getSettlementPreview(Long groupId) {
        List<GroupMember> groupMembers = findGroupMembers(groupId);

        List<Expense> expenses = expenseRepository.findAllByGroupId(groupId);

        Map<Member, Long> resultMap = calculateSettlement(groupMembers, expenses);
        log.info("[*] 정산 프리뷰 계산 완료: GroupID={}, MemberCount={}", groupId, groupMembers.size());

        return resultMap.entrySet().stream()
                .map(entry -> SettlementResponse.of(entry.getKey(), entry.getValue()))
                .toList();
    }

    // 정산 완료 (계산 후 DB 저장)
    @Transactional
    public void completeSettlement(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.GROUP_NOT_FOUND));

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
        log.info("[+] 정산 확정 및 저장 완료: GroupID={}", groupId);
    }

    @Transactional
    public void cancelSettlement(Long groupId) {
        List<Settlement> settlements = settlementRepository.findAllByGroupId(groupId);
        for (Settlement settlement : settlements) {
            // 초기화 (netAmount=0, isSettled=false, settledAt=null)
            settlement.update(0L, false);
        }
        log.info("[-] 정산 내역 초기화(취소) 완료: GroupID={}", groupId);
    }

    private List<GroupMember> findGroupMembers(Long groupId) {
        return groupMemberRepository.findAllByGroupIdWithMember(groupId);
    }

    // 정산 알고리즘
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