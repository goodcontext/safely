package com.safely.domain.group.entity;

import com.safely.domain.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "groups") // DB 예약어 중에 GROUP BY가 있어서 예약어와 충돌날 수 있으므로 groups라는 테이블명을 명시함. (충돌 방지)
public class Group extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Long id;

    // DB에서는 group_name처럼 명시하는 게 좋지만, 스프링에서는 개발자들이 group.getGroupName() 같은
    // 변수명에 클래스 이름을 중복으로 넣는 것을 스머프 네이밍이라고 부르면 피하려는 경향이 있음.
    // 스머프 네이밍(Smurf Naming) : 스머프 들이 "스머프 밥", "스머프 집" 하는 것 같은 이름 명명 방식
    @Column(name = "group_name", nullable = false)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "destination")
    private String destination;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private List<GroupMember> groupMembers = new ArrayList<>();

    @Builder
    public Group(String name, LocalDate startDate, LocalDate endDate, String destination) {
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.destination = destination;
    }
}
