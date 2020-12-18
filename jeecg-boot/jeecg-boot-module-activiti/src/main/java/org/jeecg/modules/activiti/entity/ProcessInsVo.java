package org.jeecg.modules.activiti.entity;

import lombok.Data;
import org.activiti.engine.runtime.ProcessInstance;

/**
 * @author pmc
 */
@Data
public class ProcessInsVo {

    private String id;

    private String name;

    private String key;

    private Integer version;

    private String description;

    private String businessKey;

    private String tableId;
    private String tableName;

    private String parentId;

    private String procDefId;

    private String procInstId;

    private String actId;

    private Boolean isSuspended;

    private String tenantId;

    private String deployId;

    private String currTaskName;

    private String routeName;

    private String applyer;

    public ProcessInsVo(ProcessInstance pi){
        this.id = pi.getId();
        this.name = pi.getName();
        this.key = pi.getProcessDefinitionKey();
        this.version = pi.getProcessDefinitionVersion();
        this.description = pi.getDescription();
        this.businessKey = pi.getBusinessKey();
        this.parentId = pi.getParentId();
        this.procDefId = pi.getProcessDefinitionId();
        this.procInstId = pi.getProcessInstanceId();
        this.actId = pi.getActivityId();
        this.isSuspended = pi.isSuspended();
        this.tenantId = pi.getTenantId();
        this.deployId = pi.getDeploymentId();
    }
}
