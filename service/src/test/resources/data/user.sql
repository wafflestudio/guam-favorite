insert into users (id, firebase_uid, device_id, status, nickname, skills, github_url, blog_url, introduction, image_id,
                   created_at, updated_at)
values (1, 'firebase-1', 'device-1', 'ACTIVE', 'user-1', 'kotlin, python', null, null, null, 1, '2021-08-01 09:00:00',
        '2021-08-01 09:10:00'),
       (2, 'firebase-2', 'device-2', 'ACTIVE', 'user-2', 'javascript', null, null, null, 2, '2021-08-01 09:00:00',
        '2021-08-01 09:10:00'),
       (3, 'firebase-3', 'device-3', 'ACTIVE', 'user-3', 'ruby', null, null, null, 3, '2021-08-01 09:00:00',
        '2021-08-01 09:10:00'),
       (4, 'firebase-4', null, 'ACTIVE', 'user-4', null, null, null, null, null, '2021-08-01 09:00:00',
        '2021-08-01 09:10:00'),
       (5, 'firebase-5', null, 'ACTIVE', '', null, null, null, null, null, '2021-08-01 09:00:00',
        '2021-08-01 09:00:00');