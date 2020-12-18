/*扩展表*/
-- 流程定义扩展表
-- auto-generated definition
create table act_z_process
(
    id             varchar(255) not null
        primary key,
    create_by      varchar(255) null,
    create_time    datetime     null,
    del_flag       int          null,
    update_by      varchar(255) null,
    update_time    datetime     null,
    category_id    varchar(255) null comment '所属分类',
    deployment_id  varchar(255) null comment '部署id',
    description    varchar(255) null comment '描述/备注',
    diagram_name   varchar(255) null comment '流程图片名',
    latest         bit          null comment '最新版本',
    name           varchar(255) null comment '流程名称',
    process_key    varchar(255) null comment '流程标识名称',
    status         int          null comment '流程状态 部署后默认1激活',
    version        int          null comment '版本',
    business_table varchar(255) null comment '关联业务表名',
    route_name     varchar(255) null comment '关联前端表单路由名',
    roles          varchar(225) null comment '授权的角色'
)
    CHARSET=utf8 comment '流程定义扩展表';
create table act_z_business
(
    id           varchar(255)  not null
        primary key,
    create_by    varchar(255)  null,
    create_time  datetime      null,
    del_flag     int           null,
    update_by    varchar(255)  null,
    update_time  datetime      null,
    proc_def_id  varchar(255)  null comment '流程定义id',
    proc_inst_id varchar(255)  null comment '流程实例id',
    result       int default 0 null comment '结果状态 0未提交默认 1处理中 2通过 3驳回',
    status       int default 0 null comment '状态 0草稿默认 1处理中 2结束',
    table_id     varchar(255)  null comment '关联表id',
    title        varchar(255)  null comment '申请标题',
    user_id      varchar(255)  null comment '创建用户id',
    apply_time   datetime      null comment '提交申请时间',
    is_history   bit           null comment '历史标记',
    table_name   varchar(255)  null comment '数据表名'
) CHARSET=utf8 comment '流程业务扩展表';
create table act_z_node
(
    id          varchar(255) not null
        primary key,
    create_by   varchar(255) null,
    create_time datetime     null,
    del_flag    int          null,
    update_by   varchar(255) null,
    update_time datetime     null,
    node_id     varchar(255) null comment '节点id',
    type        varchar(255) null comment '节点关联类型 0角色 1用户 2部门',
    relate_id   varchar(255) null comment '关联其他表id'
) CHARSET=utf8 comment '流程节点扩展表';





/*菜单  添加完菜单后需配置菜单权限*/
INSERT INTO sys_permission (id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_route, is_leaf, keep_alive, hidden, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('1238331160370012162', null, '工作流', '/activiti', 'layouts/RouteView', null, null, 0, null, '1', 1.1, 0, 'cluster', 1, 0, 0, 0, null, 'admin', '2020-03-13 13:08:50', null, null, 0, 0, '1', 0);
INSERT INTO sys_permission (id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_route, is_leaf, keep_alive, hidden, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
 VALUES ('1238331682929958913', '1238331160370012162', '流程模型', '/activiti/ModelList', 'activiti/ModelList', null, null, 1, null, '1', 2, 0, 'bars', 1, 1, 0, 0, null, 'admin', '2020-03-13 13:10:55', null, null, 0, 0, '1', 0);
INSERT INTO sys_permission (id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_route, is_leaf, keep_alive, hidden, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
 VALUES ('1238331682929958914', '1238331160370012162', '已发布模型', '/activiti/ProcessModelList', 'activiti/ProcessModelList', null, null, 1, null, '1', 3, 0, 'bars', 1, 1, 0, 0, null, 'admin', '2020-03-13 13:10:55', null, null, 0, 0, '1', 0);
INSERT INTO sys_permission (id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_route, is_leaf, keep_alive, hidden, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
 VALUES ('1238331682929958915', '1238331160370012162', '我的申请', '/activiti/applyList', 'activiti/applyList', null, null, 1, null, '1', 4, 0, 'bars', 1, 1, 0, 0, null, 'admin', '2020-03-13 13:10:55', null, null, 0, 0, '1', 0);
INSERT INTO sys_permission (id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_route, is_leaf, keep_alive, hidden, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
 VALUES ('1238331682929958916', '1238331160370012162', '我的待办', '/activiti/todoManage', 'activiti/todoManage', null, null, 1, null, '1', 5, 0, 'bars', 1, 1, 0, 0, null, 'admin', '2020-03-13 13:10:55', null, null, 0, 0, '1', 0);
INSERT INTO sys_permission (id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_route, is_leaf, keep_alive, hidden, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
 VALUES ('1238331682929958917', '1238331160370012162', '我的已办', '/activiti/doneManage', 'activiti/doneManage', null, null, 1, null, '1', 6, 0, 'bars', 1, 1, 0, 0, null, 'admin', '2020-03-13 13:10:55', null, null, 0, 0, '1', 0);
INSERT INTO sys_permission (id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_route, is_leaf, keep_alive, hidden, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
 VALUES ('1238331682929958918', '1238331160370012162', '进行中的流程', '/activiti/processInsManage', 'activiti/processInsManage', null, null, 1, null, '1', 7, 0, 'bars', 1, 1, 0, 0, null, 'admin', '2020-03-13 13:10:55', null, null, 0, 0, '1', 0);
INSERT INTO sys_permission (id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_route, is_leaf, keep_alive, hidden, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
 VALUES ('1238331682929958919', '1238331160370012162', '已结束流程', '/activiti/processFinishManage', 'activiti/processFinishManage', null, null, 1, null, '1', 8, 0, 'bars', 1, 1, 0, 0, null, 'admin', '2020-03-13 13:10:55', null, null, 0, 0, '1', 0);
INSERT INTO sys_permission (id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_route, is_leaf, keep_alive, hidden, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('1238331682929958920', '1238331160370012162', '所有流程', '/activiti/applyHome', 'activiti/applyHome', null, null, 1, null, '1', 1, 0, 'bars', 1, 1, 0, 0, null, 'admin', '2020-03-13 13:10:55', null, null, 0, 0, '1', 0);


alter table act_z_business
    add type_id varchar(50) null comment '流程类型id';
alter table act_z_process
    add type_id varchar(50) null comment '流程类型id';
alter table act_z_process
    add sort int(5) null comment '排序';
alter table act_z_process
    add report_model_id varchar(50) null comment '流程表单报表ID';
alter table act_z_node
    add proc_def_id varchar(50) null comment '流程定义id';
