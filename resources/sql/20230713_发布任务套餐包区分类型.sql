alter table wallets add column advertTotalTaskNum bigint;
alter table wallets add column advertHasTaskNum bigint;

alter table wallets alter column advertTotalTaskNum set default 0;
alter table wallets alter column advertHasTaskNum set default 0;

update wallets set advertTotalTaskNum=0;
update wallets set advertHasTaskNum=0;

alter table tiktok_task_prices add column type  tinyint(2);
update tiktok_task_prices set type=1;

/*广告套餐包*/
INSERT INTO `tiktok_task_prices` VALUES (4, 1000, 699, 1, '2023-02-27 14:57:36', '2023-02-27 14:57:36', 2);
INSERT INTO `tiktok_task_prices` VALUES (5, 3000, 1980, 1, '2023-02-27 14:57:36', '2023-02-27 14:57:36', 2);
INSERT INTO `tiktok_task_prices` VALUES (6, 10000, 6800, 1, '2023-02-27 14:57:36', '2023-02-27 14:57:36', 2);

