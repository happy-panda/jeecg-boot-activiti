package org.jeecg.modules.activiti.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 流程业务扩展表
 * @Author: pmc
 * @Date:   2020-03-30
 * @Version: V1.0
 */
@Data
@TableName("act_z_business")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="act_z_business对象", description="流程业务扩展表")
public class ActBusiness {
	/**表单路由*/
	@TableField(exist=false)
	private String routeName;
	/**流程名称*/
	@TableField(exist=false)
    private String processName;
	/**id*/
	@TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "id")
	private String id;
	/**createBy*/
	@Excel(name = "createBy", width = 15)
    @ApiModelProperty(value = "createBy")
	private String createBy;
	/**createTime*/
	@Excel(name = "createTime", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "createTime")
	private Date createTime;
	/**delFlag*/
	@Excel(name = "delFlag", width = 15)
    @ApiModelProperty(value = "delFlag")
	private Integer delFlag;
	/**updateBy*/
	@Excel(name = "updateBy", width = 15)
    @ApiModelProperty(value = "updateBy")
	private String updateBy;
	/**updateTime*/
	@Excel(name = "updateTime", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "updateTime")
	private Date updateTime;
	/**流程定义id*/
	@Excel(name = "流程定义id", width = 15)
    @ApiModelProperty(value = "流程定义id")
	private String procDefId;
	/**流程实例id*/
	@Excel(name = "流程实例id", width = 15)
    @ApiModelProperty(value = "流程实例id")
	private String procInstId;
	/**结果状态 0未提交默认 1处理中 2通过 3驳回*/
	@Excel(name = "结果状态 0未提交默认 1处理中 2通过 3驳回", width = 15)
    @ApiModelProperty(value = "结果状态 0未提交默认 1处理中 2通过 3驳回")
	private Integer result ;
	/**状态 0草稿默认 1处理中 2结束*/
	@Excel(name = "状态 0草稿默认 1处理中 2结束", width = 15)
    @ApiModelProperty(value = "状态 0草稿默认 1处理中 2结束")
	private Integer status ;
	/**关联表的数据id*/
	@Excel(name = "关联表id", width = 15)
    @ApiModelProperty(value = "关联表id")
	private String tableId;
	/**申请标题*/
	@Excel(name = "申请标题", width = 15)
    @ApiModelProperty(value = "申请标题")
	private String title;
	/**创建用户id*/
	@Excel(name = "创建用户id", width = 15)
    @ApiModelProperty(value = "创建用户id")
	private String userId;
	/**提交申请时间*/
	@Excel(name = "提交申请时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "提交申请时间")
	private Date applyTime;
	/**历史标记*/
	@Excel(name = "历史标记", width = 15)
    @ApiModelProperty(value = "历史标记")
	private Boolean isHistory;
	/**数据表名*/
	@Excel(name = "数据表名", width = 15)
    @ApiModelProperty(value = "数据表名")
	private String tableName;


/**分配用户username*/
	@TableField(exist=false)
	private String assignees;
	/*任务优先级 默认0   0普通1重要2紧急*/
	@TableField(exist=false)
	@ApiModelProperty(value = "任务优先级 默认0")
	private Integer priority = 0;
/**当前任务*/
	@TableField(exist=false)
	private String currTaskName;
/**第一个节点是否为网关*/
	@TableField(exist=false)
	private Boolean firstGateway = false;
/**是否发送站内消息*/
	@TableField(exist=false)
	private Boolean sendMessage;
/**是否发送短信通知*/
	@TableField(exist=false)
	private Boolean sendSms;
/**是否发送邮件通知*/
	@TableField(exist=false)
	private Boolean sendEmail;
/**流程设置参数*/
	@JsonIgnore
	@TableField(exist=false)
	private Map<String, Object> params = new HashMap<>(16);
}
