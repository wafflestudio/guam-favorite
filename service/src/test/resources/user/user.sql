insert into users (id, firebase_uid, device_token, status, nickname, skills, github_url, blog_url, introduction, image_id,
                   created_at, modified_at)
values (1, 'firebase-1', 'device-1', 'ACTIVE', 'user-1', 'kotlin, python', null, null, null, 1, '2021-08-01 09:00:00', '2021-08-01 09:10:00'),
(2, 'firebase-3', null, 'ACTIVE', 'user-3', 'kotlin, python', null, null, null, null, '2021-08-01 09:00:00', '2021-08-01 09:10:00'),
(3, 'firebase-3', null, 'ACTIVE', 'user-3', 'kotlin, python', null, null, null, 2, '2021-08-01 09:00:00', '2021-08-01 09:10:00');