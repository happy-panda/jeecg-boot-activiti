package org.jeecg.modules.activiti.web;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricIdentityLink;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.Task;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.vo.ComboModel;
import org.jeecg.modules.activiti.entity.*;
import org.jeecg.modules.activiti.service.Impl.ActBusinessServiceImpl;
import org.jeecg.modules.activiti.service.Impl.ActZprocessServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author pmc
 */
@Slf4j
@RestController
@RequestMapping("/actProcessIns")
@Transactional
public class ActProcessInsController {

    @Autowired
    private ActZprocessServiceImpl actZprocessService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private ActBusinessServiceImpl actBusinessService;

    @Autowired
    private ISysBaseAPI sysBaseAPI;

    @Autowired
    private ProcessEngineConfiguration processEngineConfiguration;

/*通过流程定义id获取第一个任务节点*/
    @RequestMapping(value = "/getFirstNode", method = RequestMethod.GET)
    public Result getFirstNode(String procDefId){
        ProcessNodeVo node = actZprocessService.getFirstNode(procDefId);
        return Result.ok(node);
    }
    /*获取运行中的流程实例*/
    @RequestMapping(value = "/getRunningProcess", method = RequestMethod.GET)
    public Result<Object> getRunningProcess(@RequestParam(required = false) String name,
                                            @RequestParam(required = false) String categoryId,
                                            @RequestParam(required = false) String key
                                            ){

        List<ProcessInsVo> list = new ArrayList<>();

        ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery()
                .orderByProcessInstanceId().desc();

        if(StrUtil.isNotBlank(name)){
            query.processInstanceNameLike("%"+name+"%");
        }
        if(StrUtil.isNotBlank(categoryId)){
            query.processDefinitionCategory(categoryId);
        }
        if(StrUtil.isNotBlank(key)) {
            query.processDefinitionKey(key);
        }

        List<ProcessInstance> processInstanceList = query.list();
        processInstanceList.forEach(e -> {
            list.add(new ProcessInsVo(e));
        });
        List<ComboModel> allUser = sysBaseAPI.queryAllUser();
        Map<String, String> userMap = allUser.stream().collect(Collectors.toMap(ComboModel::getUsername, ComboModel::getTitle));
        list.forEach(e -> {
            List<HistoricIdentityLink> identityLinks = historyService.getHistoricIdentityLinksForProcessInstance(e.getId());
            for(HistoricIdentityLink hik : identityLinks){
                // 关联发起人
                if("starter".equals(hik.getType())&& StrUtil.isNotBlank(hik.getUserId())){
                    e.setApplyer(userMap.get(hik.getUserId()));
                }
            }
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
            // 关联流程表单路由
            ActZprocess actProcess = actZprocessService.getById(e.getProcDefId());
            if(actProcess!=null){
                e.setRouteName(actProcess.getRouteName());
            }
            // 关联业务表id
            ActBusiness actBusiness = actBusinessService.getById(e.getBusinessKey());
            if(actBusiness!=null){
                e.setTableId(actBusiness.getTableId());
                e.setTableName(actBusiness.getTableName());
            }
        });
        return Result.ok(list);
    }
    /*通过id删除运行中的实例*/
    @RequestMapping(value = "/delInsByIds/{ids}")
    public Result<Object> delInsByIds(@PathVariable String ids,
                                      @RequestParam(required = false) String reason){

        if(StrUtil.isBlank(reason)){
            reason = "";
        }
        for(String id : ids.split(",")){
            // 关联业务状态结束
            ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(id).singleResult();
            ActBusiness actBusiness = actBusinessService.getById(pi.getBusinessKey());
            actBusiness.setStatus(ActivitiConstant.STATUS_TO_APPLY);
            actBusiness.setResult(ActivitiConstant.RESULT_TO_SUBMIT);
            actBusinessService.updateById(actBusiness);
            runtimeService.deleteProcessInstance(id, ActivitiConstant.DELETE_PRE+reason);
        }
        return Result.ok("删除成功");
    }
    /*激活或挂起流程实例*/
    @RequestMapping(value = "/updateInsStatus", method = RequestMethod.POST)
    public Result<Object> updateStatus(@RequestParam String id,
                                       @RequestParam Integer status){

        if(ActivitiConstant.PROCESS_STATUS_ACTIVE.equals(status)){
            runtimeService.activateProcessInstanceById(id);
        }else if(ActivitiConstant.PROCESS_STATUS_SUSPEND.equals(status)){
            runtimeService.suspendProcessInstanceById(id);
        }

        return Result.ok("修改成功");
    }
    /*获取结束的的流程实例*/
    @RequestMapping(value = "/getFinishedProcess", method = RequestMethod.GET)
    public Result<Object> getFinishedProcess(@RequestParam(required = false) String name,
                                             @RequestParam(required = false) String categoryId,
                                             @RequestParam(required = false) String key,String startDate,String endDate){

        List<HistoricProcessInsVo> list = new ArrayList<>();

        HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().finished().
                orderByProcessInstanceEndTime().desc();

        if(StrUtil.isNotBlank(name)){
            query.processInstanceNameLike("%"+name+"%");
        }
        if(StrUtil.isNotBlank(categoryId)){
            query.processDefinitionCategory(categoryId);
        }
        if(StrUtil.isNotBlank(key)) {
            query.processDefinitionKey(key);
        }

        if(StrUtil.isNotBlank(startDate)&&StrUtil.isNotBlank(endDate)){
            Date start = DateUtil.parse(startDate);
            Date end = DateUtil.parse(endDate);
            query.finishedAfter(start);
            query.finishedBefore(DateUtil.endOfDay(end));
        }

        List<HistoricProcessInstance> processInstanceList = query.list();
        processInstanceList.forEach(e -> {
            list.add(new HistoricProcessInsVo(e));
        });
        List<ComboModel> allUser = sysBaseAPI.queryAllUser();
        Map<String, String> userMap = allUser.stream().collect(Collectors.toMap(ComboModel::getUsername, ComboModel::getTitle));
        list.forEach(e -> {
            List<HistoricIdentityLink> identityLinks = historyService.getHistoricIdentityLinksForProcessInstance(e.getId());
            for(HistoricIdentityLink hik : identityLinks){
                // 关联发起人
                if("starter".equals(hik.getType())&&StrUtil.isNotBlank(hik.getUserId())){
                    e.setApplyer(userMap.get(hik.getUserId()));
                }
            }
            // 关联流程表单路由
            ActZprocess actProcess = actZprocessService.getById(e.getProcDefId());
            if(actProcess!=null){
                e.setRouteName(actProcess.getRouteName());
            }
            // 关联业务表id和结果
            ActBusiness actBusiness = actBusinessService.getById(e.getBusinessKey());
            if(actBusiness!=null){
                e.setTableId(actBusiness.getTableId());
                e.setTableName(actBusiness.getTableName());
                String reason = e.getDeleteReason();
                if(reason==null){
                    e.setResult(ActivitiConstant.RESULT_PASS);
                }else if(reason.contains(ActivitiConstant.CANCEL_PRE)){
                    e.setResult(ActivitiConstant.RESULT_CANCEL);
                    if(reason.length()>9){
                        e.setDeleteReason(reason.substring(9));
                    }else{
                        e.setDeleteReason("");
                    }
                }else if(ActivitiConstant.BACKED_FLAG.equals(reason)){
                    e.setResult(ActivitiConstant.RESULT_FAIL);
                    e.setDeleteReason("");
                }else if(reason.contains(ActivitiConstant.DELETE_PRE)){
                    e.setResult(ActivitiConstant.RESULT_DELETED);
                    if(reason.length()>8){
                        e.setDeleteReason(reason.substring(8));
                    }else{
                        e.setDeleteReason("");
                    }
                }else{
                    e.setResult(ActivitiConstant.RESULT_PASS);
                }
            }
        });
        return Result.ok(list);
    }
    @RequestMapping(value = "/delHistoricInsByIds/{ids}")
    @ApiOperation(value = "通过id删除已结束的实例")
    public Result<Object> delHistoricInsByIds(@PathVariable String ids){

        for(String id : ids.split(",")){
            historyService.deleteHistoricProcessInstance(id);
        }
        return Result.ok("删除成功");
    }

}
