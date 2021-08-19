insert into images (id, type, parent_id)
values (101, 'PROFILE', 1), (102, 'PROFILE', 3),
       (103, 'PROJECT', 1), (104, 'PROJECT', 2),
       (105, 'THREAD', 1), (106, 'THREAD', 1), (107, 'THREAD', 1), (108, 'THREAD', 2),
       (109, 'COMMENT', 1), (110, 'COMMENT', 1), (111, 'COMMENT', 1), (112, 'COMMENT', 2),
       (113, 'THREAD', 5), (114, 'COMMENT', 4);
-- POSTCONSTRUCT로 인해 스택 40개 생성되므로 이미지들의 ID 값은 1부터 시작 불가