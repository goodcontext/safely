package com.safely.domain.auth.dto;

import lombok.AllArgsConstructor;
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
@NoArgsConstructor // 토큰 관련한 DTO는 다른 계층/서비스에서 JACKSON으로 JSON 역직렬화 가능성이 있으므로 기본생성자가 필요할 수 있음.
@AllArgsConstructor // @Builder 사용해도 되지만, new TokenResponse(accessToken, refreshToken); 형식이 간단해서 이렇게 사용함.
public class TokenResponse {
    private String accessToken;
    private String refreshToken;
}
