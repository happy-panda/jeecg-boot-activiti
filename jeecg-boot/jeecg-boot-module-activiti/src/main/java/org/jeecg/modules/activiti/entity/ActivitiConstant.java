package org.jeecg.modules.activiti.entity;

public interface ActivitiConstant {

    /**
     * 流程状态 已激活
     */
    Integer PROCESS_STATUS_ACTIVE = 1;

    /**
     * 流程状态 暂停挂起
     */
    Integer PROCESS_STATUS_SUSPEND = 0;

    /**
     * 资源类型 XML
     */
    Integer RESOURCE_TYPE_XML = 0;

    /**
     * 资源类型 图片
     */
    Integer RESOURCE_TYPE_IMAGE = 1;

    /**
     * 状态 待提交申请
     */
    Integer STATUS_TO_APPLY = 0;

    /**
     * 状态 处理中
     */
    Integer STATUS_DEALING = 1;

    /**
     * 状态 处理结束
     */
    Integer STATUS_FINISH = 2;

    /**
     * 状态 已撤回
     */
    Integer STATUS_CANCELED = 3;

    /**
     * 结果 待提交
     */
    Integer RESULT_TO_SUBMIT = 0;

    /**
     * 结果 处理中
     */
    Integer RESULT_DEALING = 1;

    /**
     * 结果 通过
     */
    Integer RESULT_PASS = 2;

    /**
     * 结果 驳回
     */
    Integer RESULT_FAIL = 3;

    /**
     * 结果 撤回
     */
    Integer RESULT_CANCEL = 4;

    /**
     * 结果 删除
     */
    Integer RESULT_DELETED = 5;

    /**
     * 节点类型 开始节点
     */
    Integer NODE_TYPE_START = 0;

    /**
     * 节点类型 用户任务
     */
    Integer NODE_TYPE_TASK = 1;

    /**
     * 节点类型 结束
     */
    Integer NODE_TYPE_END = 2;

    /**
     * 节点类型 排他网关
     */
    Integer NODE_TYPE_EG = 3;

    /**
     * 节点类型 平行网关
     */
    Integer NODE_TYPE_PG = 4;

    /**E
     * 节点关联类型 角色
     */
    Integer NODE_ROLE = 0;

    /**
     * 节点关联类型 用户
     */
    Integer NODE_USER = 1;

    /**
     * 节点关联类型 部门
     */
    Integer NODE_DEPARTMENT = 2;

    /**
     * 节点关联类型 操作人的部门负责人
     */
    Integer NODE_DEP_HEADER = 3;

    /**
     * 待办消息id
     */
    String MESSAGE_TODO_ID = "124717033060306944";

    /**
     * 通过消息id
     */
    String MESSAGE_PASS_ID = "124743474544119808";

    /**
     * 驳回消息id
     */
    String MESSAGE_BACK_ID = "124744392996032512";

    /**
     * 委托消息id
     */
    String MESSAGE_DELEGATE_ID = "124744706050494464";

    /**
     * 待办消息
     */
    String MESSAGE_TODO_CONTENT = "待审批";

    /**
     * 通过消息
     */
    String MESSAGE_PASS_CONTENT = "申请流程已通过";

    /**
     * 驳回消息
     */
    String MESSAGE_BACK_CONTENT = "申请流程已驳回";

    /**
     * 委托消息
     */
    String MESSAGE_DELEGATE_CONTENT = "被委托待审批";

    /**
     * 执行任务用户类型
     */
    String EXECUTOR_TYPE = "actualExecutor";

    /**
     * 删除理由前缀
     */
    String DELETE_PRE = "deleted-";

    /**
     * 取消理由前缀
     */
    String CANCEL_PRE = "canceled-";

    /**
     * 驳回标记
     */
    String BACKED_FLAG = "backed";

    /**
     * 通过标记
     */
    String PASSED_FLAG = "completed";
    /**标题关键字*/
    String titleKey = "title";
}
