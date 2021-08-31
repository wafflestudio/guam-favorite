insert into users (id, firebase_uid, device_token, status, nickname, skills, github_url, blog_url, introduction, image_id, created_at, modified_at)
values (1, 'firebase-1', 'device-1', 'ACTIVE', '사용자 1', 'kotlin, python', null, null, null, null, '2021-08-01 09:00:00', '2021-08-01 09:10:00');

insert into comments (id, thread_id, user_id, content, created_at, modified_at)
values (1, 1, 1, '댓글 내용 1', '2021-08-02 09:00:00', '2021-08-02 09:10:00');

insert into images (id, type, parent_id)
values (1, 'THREAD', 1),
       (2, 'THREAD', 1),
       (3, 'THREAD', 1),
       (4, 'PROFILE', 1),
       (5, 'COMMENT', 1);

update users set image_id = 4 where id = 1;