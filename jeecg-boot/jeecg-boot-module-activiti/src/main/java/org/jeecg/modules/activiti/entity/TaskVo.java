package org.jeecg.modules.activiti.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.activiti.engine.task.Task;

import java.util.Date;

/**
 * @author pmc
 */
@Data
public class TaskVo {

    private String id;

    private String name;

    private String key;

    private String description;

    private String executionId;

    private String assignee;

    private String owner;

    private String procDefId;

    private String procInstId;

    private String applyer;

    private String category;

    private Integer priority;

    private Boolean isSuspended;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    private String processName;

    private String routeName;

    private String businessKey;

    private String tableId;
    private String tableName;

    public TaskVo(Task task){
        this.id = task.getId();
        this.name = task.getName();
        this.key = task.getTaskDefinitionKey();
        this.description = task.getDescription();
        this.executionId = task.getExecutionId();
        this.assignee = task.getAssignee();
        this.owner = task.getOwner();
        this.procDefId = task.getProcessDefinitionId();
        this.procInstId = task.getProcessInstanceId();
        this.priority = task.getPriority();
        this.isSuspended = task.isSuspended();
        this.category = task.getCategory();
        this.createTime = task.getCreateTime();
    }
}
