package org.jeecg.modules.mongodb.entity;

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
 * @author 游攀利
 * @version V1.0
 * @Description: 文件管理
 * @Date: 2020-06-10
 */
@Data
@TableName("sys_file")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "sys_file对象", description = "文件管理")
public class SysFile {

    /**
     * ID
     */
    @TableId(type = IdType.ID_WORKER_STR)
    @ApiModelProperty(value = "ID")
    private String id;
    /**
     * 业务ID
     */
    @Excel(name = "业务ID", width = 15)
    @ApiModelProperty(value = "业务ID")
    private String businessId;
    /**
     * 文件类型
     */
    @Excel(name = "文件类型", width = 15)
    @ApiModelProperty(value = "文件类型")
    private String fileType;
    /**
     * 项目ID
     */
    @Excel(name = "项目ID", width = 15)
    @ApiModelProperty(value = "项目ID")
    private String projectId;
    /**
     * 模块名称
     */
    @Excel(name = "模块名称", width = 15)
    @ApiModelProperty(value = "模块名称")
    private String moduleCode;
    /**
     * 文件名称
     */
    @Excel(name = "文件名称", width = 15)
    @ApiModelProperty(value = "文件名称")
    private String fileName;
    /**
     * 文件大小
     */
    @Excel(name = "文件大小", width = 15)
    @ApiModelProperty(value = "文件大小")
    private Long fileLength;
    /**
     * 文件路径
     */
    @Excel(name = "文件路径", width = 15)
    @ApiModelProperty(value = "文件路径")
    private String filePath;
    /**
     * 创建人
     */
    @Excel(name = "创建人", width = 15)
    @ApiModelProperty(value = "创建人")
    private String createBy;
    /**
     * 创建时间
     */
    @Excel(name = "创建时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;
    /**
     * 修改人
     */
    @Excel(name = "修改人", width = 15)
    @ApiModelProperty(value = "修改人")
    private String updateBy;
    /**
     * 修改时间
     */
    @Excel(name = "修改时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "修改时间")
    private Date updateTime;
    /**
     * 删除标识0-正常,1-已删除
     */
    @Excel(name = "删除标识0-正常,1-已删除", width = 15)
    @ApiModelProperty(value = "删除标识0-正常,1-已删除")
    private Integer delFlag;
}
