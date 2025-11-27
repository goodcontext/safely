INSERT INTO member (email, password, name, profile_image, authority)
VALUES (
    'user1@example.com',
    '$2a$12$poVNj4vpPcMwWlByfGNy1eqTnXqmjSp732XI88XZSELjeEbE15RN6', -- pass1234! 기본 비밀번호 배포전 삭제 예정
    '사용자1',
    NULL,
    'ROLE_USER'
);

INSERT INTO member (email, password, name, profile_image, authority)
VALUES (
    'admin@example.com',
    '$2a$12$poVNj4vpPcMwWlByfGNy1eqTnXqmjSp732XI88XZSELjeEbE15RN6', -- pass1234! 기본 비밀번호 배포전 삭제 예정
    '관리자',
    NULL,
    'ROLE_ADMIN'
);