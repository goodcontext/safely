package com.safely.domain.group.dto;

import com.safely.domain.group.entity.Group;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record GroupCreateRequest(
        @NotBlank(message = "그룹 이름은 필수입니다.")
        String name,

        @NotNull(message = "여행 시작일은 필수입니다.")
        LocalDate startDate,

        @NotNull(message = "여행 종료일은 필수입니다.")
        LocalDate endDate,

        String destination
) {
    public Group toEntity() {
        return Group.builder()
                .name(this.name)
                .startDate(this.startDate)
                .endDate(this.endDate)
                .destination(this.destination)
                .build();
    }
}