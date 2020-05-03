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
 * @Description: 流程节点扩展表
 * @Author: jeecg-boot
 * @Date:   2020-03-30
 * @Version: V1.0
 */
@Data
@TableName("act_z_node")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="act_z_node对象", description="流程节点扩展表")
public class ActNode {
    
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
	/**节点id*/
	@Excel(name = "节点id", width = 15)
    @ApiModelProperty(value = "节点id")
	private String nodeId;
	/**节点关联类型 0角色 1用户 2部门 3发起人 4发起人的部门负责人*/
	@Excel(name = "节点关联类型 0角色 1用户 2部门", width = 15)
    @ApiModelProperty(value = "节点关联类型 0角色 1用户 2部门")
	private Integer type;
	/**关联其他表id*/
	@Excel(name = "关联其他表id", width = 15)
    @ApiModelProperty(value = "关联其他表id")
	private String relateId;
}
