<div align=center>

<br/>

<h2> 개인 포트폴리오 Safely </h2>

</div>

<br/>

## 자기 소개

<div align=center>

|                                                                신상호                                                                |
| :----------------------------------------------------------------------------------------------------------------------------------: |
| <a href="https://github.com/goodcontext"> <img src="https://avatars.githubusercontent.com/u/80084943?v=4" width=100px alt="_"/> </a> |
|                                                            BE (1인 개발)                                                             |

</div>

<br/>

---

## Introduce
여행 갔을 때 비용 계산해주는 더치페이 계산기입니다.

## 디자인 시안

![Safely-디자인-시안](https://github.com/user-attachments/assets/d3d75ed3-e20a-43dc-abf1-0b5f7e4d3d26)

## Deploy URL:
http://safely-goodcontext.ap-northeast-2.elasticbeanstalk.com  

## 개발 기간
- 1차 개발(MVP): 2025.11.23 ~ 2025.12.13  
- 2차 개발(리빌딩): 2026.02.15 ~ ing

## 개발 환경
- Java 21(Eclipse Temurin JDK 21.0.9)
- Spring Boot 3.5.8
- IDE : IntelliJ IDEA 2025.2.5 (Community Edition)

## 브랜치 전략
main - develop - feat 브랜치

## 시스템 아키텍처

![Safely_시스템_아키텍처](https://github.com/user-attachments/assets/1648e978-9b9f-40d9-b871-10088629772c)

## 백엔드 사용 기술

![Safely_백엔드-사용기술](https://github.com/user-attachments/assets/fded4a4c-86a5-4ff8-83d1-bc5488cfae0f)

## ERD

<img width="1530" height="812" alt="Safely_ERD_ver 3 0 0" src="https://github.com/user-attachments/assets/380eccd5-2e46-41b2-88e5-4a2b20047022" />

## Technical Decision

<details>
<summary>Global Exception Handler (전역 예외 처리)</summary>

[배경 및 문제점]  
  - try-catch 구문을 사용하거나 일관되지 않은 방식으로 에러를 처리하면 여러 가지 단점이 있을 수 있습니다.
  - (운영 효율, 트러블슈팅 효율, 프론트엔드 단과의 협업 효율 저하)


[기술적 의사결정]
  - 비즈니스 로직과 예외 처리 로직의 분리로 코드가 깔끔해집니다.
  - 항상 동일한 구조의 JSON 응답을 받게 되므로 프론트엔드 단과의 협업 효율이 높아집니다.
  - 알 수 없는 예외는 서버 500에러 메시지만 내보냄으로써 해커 등 외부인에게 불필요한 정보를 제공하지 않습니다.
  - 일관된 에러 메시지를 제공하여 운영 시 트러블슈팅 시간을 단축할 수 있습니다.
</details>  

<details>
<summary>AOP(Aspect-Oriented Programming) 기반 로깅</summary>

[배경 및 문제점]  
  - 로깅과 비즈니스 로직이 섞여 있으면 가독성이 떨어지게 되어 유지보수성이 나빠집니다.
  - 로그에 기능을 추가해달라는 요청이 있을 때는 파일들을 일일이 수정하는 번거로움이 발생하게 됩니다.


[기술적 의사결정]
  - 로깅을 공통 관심사로 분리하여 비즈니스 로직에만 집중할 수 있도록 했습니다.
  - 컨트롤러의 진입, 종료, 예외 발생 시간을 자동으로 기록하여 로그에 남길 수 있게 되어 있습니다.
  - 실행 시간을 측정하여 성능 저하 지점을 쉽게 파악할 수 있습니다.
</details>  

<details>
<summary>JWT 토큰 방식과 Redis 서버</summary>

[배경 및 문제점]  
  - 세션 방식은 서버 메모리 부하와 확장성 문제가 있어서 Stateless한 JWT 토큰 방식을 도입할 필요성이 있습니다.
  - 하지만 JWT 토큰은 한 번 탈취되면 보안 위협이 크고, 로그아웃 처리가 까다롭다는 단점이 있습니다.


[기술적 의사결정]
  - RDB보다 조회 속도가 빠르고 TTL(Time To Live) 방식으로 관리가 쉬운 Redis에 Refresh 토큰 방식을 사용하면 토큰 탈취에 비교적 안전합니다.
  - 사용자의 로그아웃 시 블랙리스트 방식을 사용하여, 남은 유효기간 동안 해당 Access Token의 재사용을 원천 차단했습니다.
</details>
  
## Troubleshooting

<details>
<summary>MySQL 8.0 이상 버전에서 SQL 문법 오류 발생 해결</summary>

[문제 상황]
  - Group 엔티티 클래스의 테이블 명을 예약어 피하기 위하여 Groups로 지정했는데, 지속적 오류 발생했습니다.

[원인 분석 및 해결]
  - MySQL 8.0.2 버전부터 groups가 예약어로 추가된 사실을 확인했습니다.
  - 테이블 명을 groups에서 travel_groups로 변경하여 해결했습니다.
</details>  

<details>
<summary>로그인 주소 접근 시 403 오류 해결</summary>

[문제 상황]
  - SecurityConfig에서 로그인(`/api/auth/login`) 경로를 `.permitAll()`로 설정하여 접근을 허용했음에도 불구하고, 실제 요청 시 `403 Forbidden` 에러가 발생하여 로그인이 불가능한 현상이 발생했습니다.

[원인 분석 및 해결]
  - 로그인은 `POST` 방식이므로 `csrf` 필터가 동작하게 되어 `403 Forbidden` 오류가 떴습니다. `http.csrf(csrf -> csrf.disable());` 구문으로 `csrf` 필터 기능을 비활성화해서 해결했습니다.
</details>  

## API 명세서

### 1. API Documentation
서버 실행 후 Swagger UI를 통해 상세한 API 명세와 테스트 기능을 확인할 수 있습니다.
- **Swagger UI**: [http://safely-goodcontext.ap-northeast-2.elasticbeanstalk.com/swagger-ui/index.html](http://safely-goodcontext.ap-northeast-2.elasticbeanstalk.com/swagger-ui/index.html)
- **JSON Spec**: `/v3/api-docs`

### 2. Endpoints
주요 기능별 API 명세입니다.
Path는 @PathVariable, Body는 @RequestBody, Param은 @RequestParam입니다.

#### Auth (인증)
| Method | URI | Description | Request (Body/Param) |
| :---: | :--- | :--- | :--- |
| `POST` | `/api/auth/signup` | 회원가입 | `email`, `password`, `name` |
| `POST` | `/api/auth/login` | 로그인 | `email`, `password` |
| `POST` | `/api/auth/refresh` | 토큰 재발급 | `refreshToken` |
| `POST` | `/api/auth/logout` | 로그아웃 | Header: `Authorization` |

#### Member (회원)
| Method | URI | Description | Request (Body/Param) |
| :---: | :--- | :--- | :--- |
| `GET` | `/api/members/me` | 내 정보 조회 | - |
| `PATCH` | `/api/members/me` | 내 정보 수정 | `request`(JSON), `file`(Image) |
| `DELETE` | `/api/members/me` | 회원 탈퇴 | - |

#### Group (여행 그룹)
| Method | URI | Description | Request (Body/Param)                                                 |
| :---: | :--- | :--- |:---------------------------------------------------------------------|
| `POST` | `/api/groups` | 여행 그룹 생성 | `name`, `startDate`, `endDate`, `destination`                        |
| `GET` | `/api/groups` | 내 그룹 목록 조회 | -                                                                    |
| `GET` | `/api/groups/{groupId}` | 그룹 상세 조회 | Path: `groupId`                                                      |
| `PATCH` | `/api/groups/{groupId}` | 그룹 정보 수정 | Path: `groupId`, Body: `name`, `startDate`, `endDate`, `destination` |
| `DELETE` | `/api/groups/{groupId}` | 그룹 삭제 | Path: `groupId`                                                      |
| `POST` | `/api/groups/join` | 초대 코드로 그룹 가입 | Query: `inviteCode`                                                  |

#### Expense (지출)
| Method | URI | Description | Request (Body/Param) |
| :---: | :--- | :--- | :--- |
| `POST` | `/api/groups/{groupId}/expenses` | 지출 내역 등록 | `spentDate`, `location`, `category`, `amount`, `payerId`, `participantMemberIds` |
| `GET` | `/api/groups/{groupId}/expenses` | 지출 목록 조회 | Path: `groupId` |
| `PUT` | `/api/groups/{groupId}/expenses/{expenseId}` | 지출 내역 수정 | (등록과 동일) |
| `DELETE` | `/api/groups/{groupId}/expenses/{expenseId}` | 지출 내역 삭제 | Path: `expenseId` |

#### Settlement (정산)
| Method | URI | Description | Request (Body/Param) |
| :---: | :--- | :--- | :--- |
| `GET` | `/api/groups/{groupId}/settlements/preview` | 정산 결과 미리보기 | Path: `groupId` |
| `POST` | `/api/groups/{groupId}/settlements/complete` | 정산 결과 확정(저장) | Path: `groupId` |
| `POST` | `/api/groups/{groupId}/settlements/cancel` | 정산 취소(초기화) | Path: `groupId` |

#### System
| Method | URI | Description | Note |
| :---: | :--- | :--- | :--- |
| `GET` | `/` | Home | "HOME!" |
| `GET` | `/health` | Health Check | "UP!" |

## 프로젝트 구조

<details>
<summary>프로젝트 구조</summary>

```
src
├ main
│   ├ java
│   │   └ com
│   │      └ safely
│   │           ├─ domain
│   │           │     ├ auth
│   │           │     │   ├ controller
│   │           │     │   │   └ AuthController.java
│   │           │     │   ├ dto
│   │           │     │   │   ├ LoginRequest.java
│   │           │     │   │   ├ LoginResponse.java
│   │           │     │   │   ├ RefreshTokenRequest.java
│   │           │     │   │   ├ SignupRequest.java
│   │           │     │   │   ├ SignupResponse.java
│   │           │     │   │   └ TokenResponse.java
│   │           │     │   ├ entity
│   │           │     │   │   └ CustomUserDetails.java
│   │           │     │   └ service
│   │           │     │       ├ AuthService.java
│   │           │     │       └ CustomUserDetailsService.java
│   │           │     ├ common
│   │           │     │   └ entity
│   │           │     │       └ BaseEntity.java
│   │           │     ├ expense
│   │           │     │   ├ controller
│   │           │     │   │   └ ExpenseController.java
│   │           │     │   ├ dto
│   │           │     │   │   ├ ExpenseCreateRequest.java
│   │           │     │   │   └ ExpenseResponse.java
│   │           │     │   ├ entity
│   │           │     │   │   ├ Expense.java
│   │           │     │   │   └ ExpenseParticipant.java
│   │           │     │   ├ repository
│   │           │     │   │   └ ExpenseRepository.java
│   │           │     │   ├ service
│   │           │     │   │   └ ExpenseService.java
│   │           │     │   └ ExpenseCategory.java
│   │           │     ├ group
│   │           │     │   ├ controller
│   │           │     │   │   └ GroupController.java
│   │           │     │   ├ dto
│   │           │     │   │   ├ GroupCreateRequest.java
│   │           │     │   │   ├ GroupDetailResponse.java
│   │           │     │   │   ├ GroupResponse.java
│   │           │     │   │   └ GroupUpdateRequest.java
│   │           │     │   ├ entity
│   │           │     │   │   ├ Group.java
│   │           │     │   │   └ GroupMember.java
│   │           │     │   ├ repository
│   │           │     │   │   ├ GroupMemberRepository.java
│   │           │     │   │   └ GroupRepository.java
│   │           │     │   ├ service
│   │           │     │   │   └ GroupService.java
│   │           │     │   └ GroupRole.java
│   │           │     ├ member
│   │           │     │   ├ controller
│   │           │     │   │   └ MemberController.java
│   │           │     │   ├ dto
│   │           │     │   │   ├ MemberResponse.java
│   │           │     │   │   ├ MemberUpdateRequest.java
│   │           │     │   │   └ PasswordUpdateRequest.java
│   │           │     │   ├ entity
│   │           │     │   │   └ Member.java
│   │           │     │   ├ repository
│   │           │     │   │   └ MemberRepository.java
│   │           │     │   └ service
│   │           │     │       └ MemberService.java
│   │           │     └ settlement
│   │           │         ├ controller
│   │           │         │   └ SettlementController.java
│   │           │         ├ dto
│   │           │         │   └ SettlementResponse.java
│   │           │         ├ entity
│   │           │         │   └ Settlement.java
│   │           │         ├ repository
│   │           │         │   └ SettlementRepository.java
│   │           │         └ service
│   │           │             └ SettlementService.java
│   │           ├ global
│   │           │     ├ config
│   │           │     │   ├ LogAspect.java
│   │           │     │   ├ RedisConfig.java
│   │           │     │   └ SwaggerConfig.java
│   │           │     ├ controller
│   │           │     │   └ HomeController.java
│   │           │     ├ exception
│   │           │     │   ├ auth
│   │           │     │   │   ├ EmailDuplicateException.java
│   │           │     │   │   ├ InvalidTokenException.java
│   │           │     │   │   ├ LoginFailedException.java
│   │           │     │   │   ├ PasswordMismatchException.java
│   │           │     │   │   └ RefreshTokenNotFoundException.java
│   │           │     │   ├ common
│   │           │     │   │   └ EntityNotFoundException.java
│   │           │     │   ├ group
│   │           │     │   │   ├ AlreadyJoinedGroupException.java
│   │           │     │   │   ├ GroupPermissionDeniedException.java
│   │           │     │   │   ├ InvalidInviteCodeException.java
│   │           │     │   │   └ NotGroupMemberException.java
│   │           │     │   ├ upload
│   │           │     │   │   ├ FileUploadException.java
│   │           │     │   │   └ InvalidFileExtensionException.java
│   │           │     │   ├ BusinessException.java
│   │           │     │   ├ ErrorCode.java
│   │           │     │   ├ ErrorResponse.java
│   │           │     │   └ GlobalExceptionHandler.java
│   │           │     ├ s3
│   │           │     │   └ S3Service.java
│   │           │     └ security
│   │           │         ├ config
│   │           │         │   └ SecurityConfig.java
│   │           │         ├ filter
│   │           │         │   └ JwtAuthenticationFilter.java
│   │           │         ├ handler
│   │           │         │   ├ CustomAccessDeniedHandler.java
│   │           │         │   └ CustomAuthenticationEntryPoint.java
│   │           │         └ jwt
│   │           │             ├ JwtProperties.java
│   │           │             └ JwtProvider.java
│   │           └ SafelyApplication.java
│   └ resources
│       ├ static
│       │  └ images
│       │       ├ Safely-디자인-시안.jpg
│       │       ├ Safely_Backend_Skills.jpg
│       │       ├ Safely_ERD_ver.3.0.0.jpg
│       │       ├ Safely_System_Architecture.jpg
│       │       ├ 더미_이미지_4MB.jpg
│       │       └ 더미_이미지_300KB.jpg
│       ├ templates
│       ├ application.yml
│       ├ application-dev.yml
│       ├ application-prod.yml
│       ├ data.sql
│       └ old_schema.sql
└ test
    ├ java
    │   └ com
    │      └ safely
    │           ├ domain
    │           │     ├ auth
    │           │     │   └ service
    │           │     │       ├ AuthServiceIntegrationTest.java
    │           │     │       └ AuthServiceUnitTest.java
    │           │     ├ expense
    │           │     │   └ service
    │           │     │       └ ExpenseServiceConcurrencyTest.java
    │           │     └ group
    │           │         ├ entity
    │           │         │   └ GroupEntityUnitTest.java
    │           │         └ service
    │           │             ├ GroupServiceIntegrationTest.java
    │           │             └ GroupServiceUnitTest.java
    │           └ SafelyApplicationTests.java
    └ resources
        └ application.yml
.gitattributes
.gitignore
build.gradle
docker-compose.yml
Dockerfile
gradlew
gradlew.bat
HELP.md
README.md
settings.gradle
```

</details>

## Commit Convention

- `build`: 빌드 파일 작업
- `chore`: src, test 경로의 파일을 수정하지 않는 기타 변경사항(프로덕션 코드 변경 X)
- `ci`: 배포 방식 변경 및 새로 추가
- `comment`: 주석 추가 및 변경
- `design`: CSS 등 사용자 UI 디자인 변경
- `docs`: 문서, Swagger 변경
- `feat`: 새로운 기능 추가
- `fix`: 버그 해결
- `refactor`: 프로덕션 코드 리팩토링
- `remove`: 파일 및 폴더 삭제
- `rename`: 파일 및 폴더명 수정하거나 옮기는 작업
- `style`: 코드 포맷 변경, 세미 콜론 누락, 코드 수정이 없는 경우
- `test`: 테스트 추가, 테스트 리팩토링(프로덕션 코드 변경 X)
