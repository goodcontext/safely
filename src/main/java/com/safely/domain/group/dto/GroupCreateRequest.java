package com.safely.domain.group.dto;

import com.safely.domain.group.entity.Group;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor // 테스트 코드 작성 위해서 추가함.
@Builder // 테스트 코드 작성 위해서 추가함.
public class GroupCreateRequest {
    @NotBlank(message = "그룹 이름은 필수입니다.")
    private String name;

    @NotNull(message = "여행 시작일은 필수입니다.")
    private LocalDate startDate;

    @NotNull(message = "여행 종료일은 필수입니다.")
    private LocalDate endDate;

    private String destination;

    // DTO -> Entity 변환 메서드 (깔끔한 코드를 위해 추천)
    public Group toEntity() {
        return Group.builder()
                .name(this.name)
                .startDate(this.startDate)
                .endDate(this.endDate)
                .destination(this.destination)
                .build();
    }
}
