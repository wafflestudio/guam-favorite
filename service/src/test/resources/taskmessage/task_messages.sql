insert into task_messages (id, task_id, content, status, created_at, modified_at)
values (1, 10, 'task 10의 완료된 작업현황', 'DONE', '2021-08-01 09:00:00', '2021-08-01 09:10:00'),
       (2, 10, 'task 10의 진행중인 작업현황', 'ONGOING', '2021-08-01 09:00:00', '2021-08-01 09:10:00'),
       (3, 11, 'task 11의 삭제된 작업현황', 'DELETED', '2021-08-01 09:00:00', '2021-08-01 09:10:00');
