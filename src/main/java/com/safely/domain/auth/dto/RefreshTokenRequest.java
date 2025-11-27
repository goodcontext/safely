package com.safely.domain.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

/*
 * 최신 흐름은 DTO에 setter를 사용하지 않음.
 * 1. 불변 객체가 테스트하기 쉽고 버그가 적음
 * 2. 서비스 계층에서 의도치 않게 DTO 수정하는 실수를 미리 차단
 * 3. JSON 매핑에는 setter가 필요 없어서 굳이 넣지 않음
 * 4. Clean Architecture / DDD에 맞는 방식
 * 5. 팀 전체 코드의 안정성 증가 (출처 : ChatGPT-5.1)
 */
@Getter
@NoArgsConstructor
public class RefreshTokenRequest {
    private String refreshToken;
}
