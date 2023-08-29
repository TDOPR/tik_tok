/*添加审核人字段*/
alter table wallets_usd_withdraw add column auditId int(0);
alter table app_user_task add column auditId int(0);

/*增加任务渠道 1=tiktok   2=抖音 */
alter table tiktok_task add column channel tinyint(2);
update tiktok_task set channel=1;

/*app用户添加节点等级字段 0=未购买 1=社区节点 2=股东节点 */
alter table app_users add column nodeLevel tinyint(2);
update app_users set nodeLevel=0;
update  app_users set  nodeLevel=0 where nodeLevel is null;
alter table app_users alter column nodeLevel set default 0;

alter table wallets add column advertTotalTaskNum bigint set default 0;
alter table wallets add column advertHasTaskNum bigint set default 0;


/*安卓版本更新*/
update app_versions set active=0 where systemName='android';
INSERT INTO app_versions(systemName,version,znUpdateDesc,enUpdateDesc,inUpdateDesc,thUpdateDesc,viUpdateDesc,active,forceUpdate)  VALUES ('android', 'v1.0.3', '1.优化功能', '1.Optimization function', '1.Optimization function', '1.เพิ่มประสิทธิภาพการทำงาน', '1.Chức năng tối ưu hóa', 1, 1);

/*ios版本更新*/
update app_versions set active=0 where systemName='ios';
INSERT INTO app_versions(systemName,version,znUpdateDesc,enUpdateDesc,inUpdateDesc,thUpdateDesc,viUpdateDesc,active,forceUpdate)  VALUES ('ios', 'v1.0.3', '1.优化功能', '1.Optimization function', '1.Optimization function', '1.เพิ่มประสิทธิภาพการทำงาน', '1.Chức năng tối ưu hóa', 1, 0);

