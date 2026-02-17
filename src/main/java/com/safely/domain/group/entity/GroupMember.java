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
@Table(name = "travel_group_members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AttributeOverride(name = "created_at", column = @Column(name = "joined_at", nullable = false, updatable = false))
public class GroupMember extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_member_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

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
