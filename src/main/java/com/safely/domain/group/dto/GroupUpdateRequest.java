package com.safely.domain.group.dto;

import java.time.LocalDate;

public record GroupUpdateRequest(
        String name,
        LocalDate startDate,
        LocalDate endDate,
        String destination
) {}