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
(현재 REST API만 구현되어 있으며 추후 MVC 기반 웹앱 구현 예정입니다. 테스트는 포스트맨으로 진행했습니다.)

## 개발 기간
2025년 11월 22일 ~ 2025년 12월 13일 (총 67시간 23분 = 8일 3시간 23분(하루 8시간 기준으로 계산))  
기획, 디자인 시안, 기타 문서 작업 작성 시간 제외. README.md 파일 작성시간 제외. 책 읽거나 인터넷 검색 시간 포함.

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
│   │           │     │   │  └ AuthController.java
│   │           │     │   ├ dto
│   │           │     │   │  ├ LoginRequest.java
│   │           │     │   │  ├ LoginResponse.java
│   │           │     │   │  ├ RefreshTokenRequest.java
│   │           │     │   │  ├ SignupRequest.java
│   │           │     │   │  ├ SignupResponse.java
│   │           │     │   │  └ TokenResponse.java
│   │           │     │   ├ entity
│   │           │     │   │  └ CustomUserDetails.java
│   │           │     │   └ service
│   │           │     │      ├ AuthService.java
│   │           │     │      └ CustomUserDetailsService.java
│   │           │     ├ common
│   │           │     │   └ entity
│   │           │     │      └ BaseEntity.java
│   │           │     ├ expense
│   │           │     │   ├ controller
│   │           │     │   │   └ ExpenseController.java
│   │           │     │   ├ dto
│   │           │     │   │   ├ ExpenseCreateRequest.java
│   │           │     │   │   └ ExpenseResponse.java
│   │           │     │   ├ entity
│   │           │     │   │   ├ Expense
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
│   │           │       ├ config
│   │           │       │  ├ RedisConfig.java
│   │           │       │  └ SwaggerConfig.java
│   │           │       ├ controller
│   │           │       │  └ HomeController.java
│   │           │       ├ exception
│   │           │       │  ├ GlobalExceptionHandler.java
│   │           │       │  └ NotFoundException.java
│   │           │       ├ s3
│   │           │       │  └ S3Service.java
│   │           │       └ security
│   │           │          ├ config
│   │           │          │    └ SecurityConfig.java
│   │           │          ├ filter
│   │           │          │    └ JwtAuthenticationFilter.java
│   │           │          └ jwt
│   │           │               ├ JwtProperties.java
│   │           │               └ JwtProvider.java
│   │           └ SafelyApplication.java
│   └ resources
│       ├ static
│       │  └ images
│       │       ├ 더미_이미지_4MB.jpg
│       │       └ 더미_이미지_300KB.jpg
│       ├ templates
│       ├ application.yml
│       ├ application-dev.yml
│       ├ application-prod.yml
│       ├ data.sql
│       └ old_schema.sql
└ test
    └ java
        └ com
           └ safely
                ├ domain
                │    ├ auth
                │    │   └ service
                │    │        ├ AuthServiceIntegrationTest.java
                │    │        └ AuthServiceUnitTest.java
                │    └ group
                │         ├ entity
                │         │   └ GroupEntityUnitTest.java
                │         └ service
                │             ├ GroupServiceIntegrationTest.java
                │             └ GroupServiceUnitTest.java
                └ SafelyApplicationTests.java
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
