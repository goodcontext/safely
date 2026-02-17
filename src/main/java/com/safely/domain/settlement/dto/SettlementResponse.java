package com.safely.domain.settlement.dto;

import com.safely.domain.member.entity.Member;
import com.safely.domain.settlement.entity.Settlement;

public record SettlementResponse(
        Long memberId,
        String memberName,
        String profileImage,
        Long netAmount,      // 최종 정산 금액 (+/-)
        Long sendAmount,     // 보낼 돈 (양수 변환)
        Long receiveAmount   // 받을 돈
) {
    public static SettlementResponse of(Member member, Long netAmount) {
        long send = 0;
        long receive = 0;

        // 양수(+)면 받을 돈, 음수(-)면 보낼 돈
        if (netAmount > 0) receive = netAmount;
        else if (netAmount < 0) send = Math.abs(netAmount);

        return new SettlementResponse(
                member.getId(),
                member.getName(),
                member.getProfileImage(),
                netAmount,
                send,
                receive
        );
    }

    public static SettlementResponse from(Settlement settlement) {
        return SettlementResponse.of(settlement.getMember(), settlement.getNetAmount());
    }
}