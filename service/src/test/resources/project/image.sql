insert into images (id, type, parent_id)
values (101, 'PROFILE', 1), (102, 'PROFILE', 3),
       (103, 'PROJECT', 1), (104, 'PROJECT', 2);
-- POSTCONSTRUCT로 인해 스택 40개 생성되므로 이미지들의 ID 값은 1부터 시작 불가