package com.safely;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableJpaAuditing
@EnableRetry // 두 명의 유저가 동시에 지출 내역을 수정하려고 할 때 충돌이 발생하면, 2~3회 재시도하게 하는 @Retryable 실행하기 위해 필요함.
public class SafelyApplication {

	public static void main(String[] args) {
		SpringApplication.run(SafelyApplication.class, args);
	}

}
