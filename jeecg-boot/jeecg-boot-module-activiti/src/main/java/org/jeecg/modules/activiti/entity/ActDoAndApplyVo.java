package org.jeecg.modules.activiti.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class ActDoAndApplyVo {
    private String id;
    /**
     * 流程定义key
     */
    private String procDefId;
    /**
     * 流程部署id
     */
    private String procDepId;
    /**
     * 流程实例id
     */
    private String procInstId;
    /**
     * 流程表单id
     */
    private String tableId;
    /**
     * 流程表名
     */
    private String tableName;

    private String title;

    /**
     * 数据类型（我的申请；我的已办）
     */
    private String type;

    private String processName;

    private String routeName;

    /**
     * 所属流程类型
     */
    private String proType;

    /**
     * 流程发起人
     */
    private String assignees;

    /**
     * 流程发起人
     */
    private String applyer;

    /**
     * 任务优先级
     */
    private Integer priority = 0;

    /**
     * 当前任务
     * */
    @TableField(exist=false)
    private String currTaskName;

    /**
     * 第一个节点是否为网关
     */
    private Boolean firstGateway = false;

    /**
     * 创建用户id
     *
     * */
    private String userId;

    /**
     * 提交申请时间
     *
     * */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date applyTime;

    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 历史标记
     * */
    private Boolean isHistory;

    /**
     * 结果状态 0未提交默认 1处理中 2通过 3驳回
     * */
    private Integer result;

    /**
     * 状态 0草稿默认 1处理中 2结束
     * */
    private Integer status;

    /**
     * 审批操作
     */
    private String deleteReason;

    /**
     * 审批意见
     */
    private String comment;

    private String procInstStatus;

}
