insert into tasks (id, project_id, position, user_id, user_state, created_at, modified_at, user_offset)
values (1, 1, 'FRONTEND', 1, 'LEADER', '2021-08-01 09:00:00', '2021-08-01 09:10:00', 0),
       (2, 2, 'BACKEND', 1, 'MEMBER', '2021-08-01 09:00:00', '2021-08-01 09:10:00', 1),
       (3, 3, 'BACKEND', 1, 'LEADER', '2021-08-01 09:00:00', '2021-08-01 09:10:00', 2),
       (4, 1, 'BACKEND', 2, 'GUEST', '2021-08-01 09:00:00', '2021-08-01 09:10:00', 0),
       (5, 1, 'BACKEND', 3, 'MEMBER', '2021-08-01 09:00:00', '2021-08-01 09:10:00', 0);