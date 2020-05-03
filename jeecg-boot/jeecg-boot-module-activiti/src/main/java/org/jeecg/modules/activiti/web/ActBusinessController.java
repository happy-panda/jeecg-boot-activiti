package org.jeecg.modules.activiti.web;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.activiti.entity.ActBusiness;
import org.jeecg.modules.activiti.entity.ActZprocess;
import org.jeecg.modules.activiti.entity.ActivitiConstant;
import org.jeecg.modules.activiti.service.Impl.ActBusinessServiceImpl;
import org.jeecg.modules.activiti.service.Impl.ActZprocessServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 *@author PanMeiCheng
 *@date 2020-04-02
 *@version 1.0
 */
@RestController
@RequestMapping("/actBusiness")
@Slf4j
@Transactional
public class ActBusinessController {
    @Autowired
    ActBusinessServiceImpl actBusinessService;
    @Autowired
    ActZprocessServiceImpl actZprocessService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    ISysBaseAPI sysBaseAPI;
    /*添加申请草稿状态*/
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result add(HttpServletRequest request){
        String procDefId = request.getParameter("procDefId");
        String procDeTitle = request.getParameter("procDeTitle");
        String tableName = request.getParameter("tableName");
        /*保存业务表单数据到数据库表*/
        String tableId = IdUtil.simpleUUID();
        actBusinessService.saveApplyForm(tableId,request);
        // 保存至我的申请业务
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String username = sysUser.getUsername();
        ActBusiness actBusiness = new ActBusiness();
        actBusiness.setUserId(username);
        actBusiness.setTableId(tableId);
        actBusiness.setProcDefId(procDefId);
        String title = request.getParameter(ActivitiConstant.titleKey);
        if (StrUtil.isNotBlank(title)){
            actBusiness.setTitle(title);
        }else {
            actBusiness.setTitle(procDeTitle);
        }
        actBusiness.setTableName(tableName);
        actBusinessService.save(actBusiness);
        return Result.ok();
    }
    /*获取业务表单信息*/
    @RequestMapping(value = "/getForm", method = RequestMethod.GET)
    public Result getForm(HttpServletRequest request){
        /*保存业务表单数据到数据库表*/
        String tableId = request.getParameter("tableId");
        String tableName = request.getParameter("tableName");
        if (StrUtil.isBlank(tableName)){
            return Result.error("参数缺省！");
        }
        Map<String, Object> applyForm = actBusinessService.getApplyForm(tableId, tableName);
        return Result.ok(applyForm);
    }
    /*修改业务表单信息*/
    @RequestMapping(value = "/editForm", method = RequestMethod.POST)
    public Result editForm(HttpServletRequest request){
        /*保存业务表单数据到数据库表*/
        String tableId = request.getParameter("id");
        actBusinessService.saveApplyForm(tableId,request);
        return Result.ok();
    }
    /*通过id删除草稿状态申请*/
    @RequestMapping(value = "/delByIds", method = RequestMethod.POST)
    public Result delByIds(String ids){

        for(String id : ids.split(",")){
            ActBusiness actBusiness = actBusinessService.getById(id);
            if(actBusiness.getStatus()!=0){
                return Result.error("删除失败, 仅能删除草稿状态的申请");
            }
            // 删除关联业务表
            actBusinessService.deleteBusiness(actBusiness.getTableName(), actBusiness.getTableId());
            actBusinessService.removeById(id);
        }
        return Result.ok("删除成功");
    }
    /*提交申请 启动流程*/
    @RequestMapping(value = "/apply", method = RequestMethod.POST)
    public Result apply(ActBusiness act){

        ActBusiness actBusiness = actBusinessService.getById(act.getId());
        if(actBusiness==null){
            return Result.error("actBusiness表中该id不存在");
        }
        String tableId = actBusiness.getTableId();
        String tableName = actBusiness.getTableName();
        act.setTableId(tableId);
        Map<String, Object> busiData = actBusinessService.getBaseMapper().getBusiData(tableId, tableName);

        if (MapUtil.isNotEmpty(busiData)&&busiData.get(ActivitiConstant.titleKey)!=null){
            //如果表单里有 标题  更新一下
            actBusiness.setTitle(busiData.get(ActivitiConstant.titleKey)+"");
        }
        String processInstanceId = actZprocessService.startProcess(act);
        actBusiness.setProcInstId(processInstanceId);
        actBusiness.setStatus(1);
        actBusiness.setResult(1);
        actBusiness.setApplyTime(new Date());
        actBusinessService.updateById(actBusiness);
        return Result.ok("操作成功");
    }
    /*撤回申请*/
    @RequestMapping(value = "/cancel", method = RequestMethod.POST)
    public Result<Object> cancel(@RequestParam String id,
                                 @RequestParam String procInstId,
                                 @RequestParam(required = false) String reason){

        if(StrUtil.isBlank(reason)){
            reason = "";
        }
        runtimeService.deleteProcessInstance(procInstId, "canceled-"+reason);
        ActBusiness actBusiness = actBusinessService.getById(id);
        actBusiness.setStatus(ActivitiConstant.STATUS_CANCELED);
        actBusiness.setResult(ActivitiConstant.RESULT_TO_SUBMIT);
        actBusinessService.updateById(actBusiness);
        return Result.ok("操作成功");
    }
    /**/
    @RequestMapping(value = "/listData")
    public Result listData(ActBusiness param, HttpServletRequest request){
        LambdaQueryWrapper<ActBusiness> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(ActBusiness::getCreateTime);
        if (StrUtil.isNotBlank(param.getTitle())) queryWrapper.like(ActBusiness::getTitle,param.getTitle());
        if (param.getStatus()!=null) queryWrapper.eq(ActBusiness::getStatus,param.getStatus());
        if (param.getResult()!=null) queryWrapper.eq(ActBusiness::getResult,param.getResult());
        if (StrUtil.isNotBlank(request.getParameter("createTime_begin"))) queryWrapper.ge(ActBusiness::getTitle,param.getTitle());
        if (StrUtil.isNotBlank(request.getParameter("createTime_end"))) queryWrapper.le(ActBusiness::getTitle,param.getTitle());

        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        queryWrapper.eq(ActBusiness::getUserId,loginUser.getUsername());
        List<ActBusiness> actBusinessList = actBusinessService.list(queryWrapper);

        actBusinessList.forEach(e -> {
            if(StrUtil.isNotBlank(e.getProcDefId())){
                ActZprocess actProcess = actZprocessService.getById(e.getProcDefId());
                e.setRouteName(actProcess.getRouteName());
                e.setProcessName(actProcess.getName());
            }
            if("1".equals(e.getStatus())){
                // 关联当前任务
                List<Task> taskList = taskService.createTaskQuery().processInstanceId(e.getProcInstId()).list();
                if(taskList!=null&&taskList.size()==1){
                    e.setCurrTaskName(taskList.get(0).getName());
                }else if(taskList!=null&&taskList.size()>1){
                    StringBuilder sb = new StringBuilder();
                    for(int i=0;i<taskList.size()-1;i++){
                        sb.append(taskList.get(i).getName()+"、");
                    }
                    sb.append(taskList.get(taskList.size()-1).getName());
                    e.setCurrTaskName(sb.toString());
                }
            }
        });
        return Result.ok(actBusinessList);
    }
}
