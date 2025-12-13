package com.safely.domain.member.entity;

import com.safely.domain.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 엔티티 클래스는 Member member = new Member();처럼 외부에서 아무나 막 생성하면 안 된다.
@AllArgsConstructor
@Builder
@Table(name = "members")
public class Member extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(nullable = false, length = 50)
    private String authority;

    // 회원 정보 수정
    public void updateProfile(String name, String profileImage) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (profileImage != null) {
            this.profileImage = profileImage;
        }
    }

    // 비밀번호 변경
    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }
}
