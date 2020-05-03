package org.jeecg.modules.activiti.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Description: 流程定义扩展表
 * @Author: pmc
 * @Date:   2020-03-22
 * @Version: V1.0
 */
@Data
@TableName("act_z_process")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="act_z_process对象", description="流程定义扩展表")
public class ActZprocess {
    
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
	/**所属分类*/
	@Excel(name = "所属分类", width = 15)
    @ApiModelProperty(value = "所属分类")
	private String categoryId;
	/**部署id*/
	@Excel(name = "部署id", width = 15)
    @ApiModelProperty(value = "部署id")
	private String deploymentId;
	/**描述/备注*/
	@Excel(name = "描述/备注", width = 15)
    @ApiModelProperty(value = "描述/备注")
	private String description;
	/**流程图片名*/
	@Excel(name = "流程图片名", width = 15)
    @ApiModelProperty(value = "流程图片名")
	private String diagramName;
	/**最新版本*/
	@Excel(name = "最新版本", width = 15)
    @ApiModelProperty(value = "最新版本")
	private Boolean latest;
	/**流程名称*/
	@Excel(name = "流程名称", width = 15)
    @ApiModelProperty(value = "流程名称")
	private String name;
	/**流程标识名称*/
	@Excel(name = "流程标识名称", width = 15)
    @ApiModelProperty(value = "流程标识名称")
	private String processKey;
	/**流程状态 部署后默认1激活*/
	@Excel(name = "流程状态 部署后默认1激活", width = 15)
    @ApiModelProperty(value = "流程状态 部署后默认1激活")
	private Integer status;
	/**版本*/
	@Excel(name = "版本", width = 15)
    @ApiModelProperty(value = "版本")
	private Integer version;
	/**关联业务表名*/
	@Excel(name = "关联业务表名", width = 15)
    @ApiModelProperty(value = "关联业务表名")
	private String businessTable;
	/**关联前端表单路由名*/
	@Excel(name = "关联前端表单路由名", width = 15)
    @ApiModelProperty(value = "关联前端表单路由名")
	private String routeName;
	/**授权的角色*/
	@Excel(name = "授权的角色", width = 15)
    @ApiModelProperty(value = "授权的角色")
	private String roles;
}
