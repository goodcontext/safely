package com.safely.domain.group.entity;

import com.safely.domain.common.entity.BaseEntity;
import com.safely.domain.group.GroupRole;
import com.safely.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "group_members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// BaseEntity의 created_at 컬럼을 joined_at으로 이름 변경하여 매핑
@AttributeOverride(name = "created_at", column = @Column(name = "joined_at", nullable = false, updatable = false))
public class GroupMember extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_members_id")
    private Long id;

    // 지연 로딩 (LAZY) 사용하는 이유
    // 1. 불필요한 조인 방지
    // 2. N+1 문제 완화
    // 3. 메모리 절약
    // 결론 : 실무에서는 무조건 LAZY 사용하는 게 좋고, @ManyToOne, @OneToOne은 기본값이 EAGER이므로 반드시 LAZY로 지정해야 함.
    // 참고로 @OneToMany는 기본값이 LAZY이므로 안 붙여도 되지만, 명시적으로 적어두는 것도 좋음.
    // 나중에 진짜로 Group 정보를 한 번에 가져오려면 JPA의 Fetch Join이라는 기술을 사용해서 해결함. 또는 @EntityGraph 사용.
    //
    //
    // JPA N+1문제 해결하는 방법 3가지
    //
    // 1. Fetch Join (직접 쿼리 작성) (가장 추천, 난이도 : 중)
    // 가장 전통적이고 가장 많이 쓰이는 방식입니다. JPQL을 직접 작성하여 "내가 원하는 대로 조인"합니다.
    // 특징: SQL과 가장 유사하여 세밀한 제어가 가능합니다.
    // 장점: Inner Join을 사용하여 불필요한 null 데이터를 배제할 수 있어 성능상 유리한 경우가 많습니다. 단점: 쿼리가 길어집니다.
    // 동작: 기본적으로 INNER JOIN이 나갑니다. (LEFT JOIN 명시 가능)
    //
    // 2. @EntityGraph (선언적 방식) (난이도 : 하)
    // JPQL 직접 안 짜고, @EntityGraph 어노테이션만 붙여서 사용하므로 간편함.
    // 특징: JPA 표준 스펙입니다. 코드가 깔끔합니다. 장점: JPQL을 몰라도 됩니다. 단점: 관계가 복잡해지면 제어하기 어렵습니다.
    // 동작: 기본적으로 LEFT OUTER JOIN이 나갑니다.
    //
    // 3. Batch Size 설정 (기본 적용, 난이도 : 최하(설정만 하면 됨.))
    // 쿼리를 수정하는 방식이 아니고, 옵션 설정으로 해결하는 방법입니다.
    // 장점: 코드를 하나도 안 고치고 N+1 문제를 N+1 -> 1+1 정도로 획기적으로 줄일 수 있습니다. 페이징 처리할 때 특히 유용합니다.
    // 설정 방법 : application.yml
    // spring:
    //  jpa:
    //    properties:
    //      hibernate:
    //        default_batch_fetch_size: 100  # 보통 100 또는 1000 권장
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    // 지연 로딩 (LAZY) 사용하는 이유
    // 1. 불필요한 조인 방지
    // 2. N+1 문제 완화
    // 3. 메모리 절약
    // 결론 : 실무에서는 무조건 LAZY 사용하는 게 좋고, @ManyToOne, @OneToOne은 기본값이 EAGER이므로 반드시 LAZY로 지정해야 함.
    // 참고로 @OneToMany는 기본값이 LAZY이므로 안 붙여도 되지만, 명시적으로 적어두는 것도 좋음.
    // 나중에 진짜로 Group 정보를 한 번에 가져오려면 JPA의 Fetch Join이라는 기술을 사용해서 해결함. 또는 @EntityGraph 사용.
    //
    //
    // JPA N+1문제 해결하는 방법 3가지
    //
    // 1. Fetch Join (직접 쿼리 작성) (가장 추천, 난이도 : 중)
    // 가장 전통적이고 가장 많이 쓰이는 방식입니다. JPQL을 직접 작성하여 "내가 원하는 대로 조인"합니다.
    // 특징: SQL과 가장 유사하여 세밀한 제어가 가능합니다.
    // 장점: Inner Join을 사용하여 불필요한 null 데이터를 배제할 수 있어 성능상 유리한 경우가 많습니다. 단점: 쿼리가 길어집니다.
    // 동작: 기본적으로 INNER JOIN이 나갑니다. (LEFT JOIN 명시 가능)
    //
    // 2. @EntityGraph (선언적 방식) (난이도 : 하)
    // JPQL 직접 안 짜고, @EntityGraph 어노테이션만 붙여서 사용하므로 간편함.
    // 특징: JPA 표준 스펙입니다. 코드가 깔끔합니다. 장점: JPQL을 몰라도 됩니다. 단점: 관계가 복잡해지면 제어하기 어렵습니다.
    // 동작: 기본적으로 LEFT OUTER JOIN이 나갑니다.
    //
    // 3. Batch Size 설정 (기본 적용, 난이도 : 최하(설정만 하면 됨.))
    // 쿼리를 수정하는 방식이 아니고, 옵션 설정으로 해결하는 방법입니다.
    // 장점: 코드를 하나도 안 고치고 N+1 문제를 N+1 -> 1+1 정도로 획기적으로 줄일 수 있습니다. 페이징 처리할 때 특히 유용합니다.
    // 설정 방법 : application.yml
    // spring:
    //  jpa:
    //    properties:
    //      hibernate:
    //        default_batch_fetch_size: 100  # 보통 100 또는 1000 권장
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false) // MANAGER, MEMBER
    private GroupRole role;

    @Column(name = "member_name", nullable = false)
    private String memberName;

    @Builder
    public GroupMember(Group group, Member member, GroupRole role, String memberName) {
        this.group = group;
        this.member = member;
        this.role = role;
        this.memberName = memberName;
    }
}
