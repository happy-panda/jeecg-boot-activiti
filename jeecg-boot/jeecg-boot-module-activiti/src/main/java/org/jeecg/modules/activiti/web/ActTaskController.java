package org.jeecg.modules.activiti.web;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricIdentityLink;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.vo.ComboModel;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.activiti.entity.*;
import org.jeecg.modules.activiti.service.Impl.ActBusinessServiceImpl;
import org.jeecg.modules.activiti.service.Impl.ActZprocessServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author pmc
 */
@Slf4j
@RestController
@RequestMapping("/actTask")
@Transactional
public class ActTaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private ManagementService managementService;

    @Autowired
    private ActZprocessServiceImpl actZprocessService;

    @Autowired
    private ActBusinessServiceImpl actBusinessService;
    @Autowired
    ISysBaseAPI sysBaseAPI;

    /*代办列表*/
    @RequestMapping(value = "/todoList" )
    public Result<Object> todoList(String name, String categoryId, Integer priority, HttpServletRequest request){
        List<TaskVo> list = new ArrayList<>();
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String userId = sysUser.getUsername();
        TaskQuery query = taskService.createTaskQuery().taskCandidateOrAssigned(userId);
        // 多条件搜索
        query.orderByTaskPriority().desc();
        query.orderByTaskCreateTime().desc();
        if(StrUtil.isNotBlank(name)){
            query.taskNameLike("%"+name+"%");
        }
        if(StrUtil.isNotBlank(categoryId)){
            query.taskCategory(categoryId);
        }
        if(priority!=null){
            query.taskPriority(priority);
        }
        String createTime_begin = request.getParameter("createTime_begin");
        String createTime_end = request.getParameter("createTime_end");
        if(StrUtil.isNotBlank(createTime_begin)&&StrUtil.isNotBlank(createTime_end)){
            Date start = DateUtil.parse(createTime_begin);
            Date end = DateUtil.parse(createTime_end);
            query.taskCreatedAfter(start);
            query.taskCreatedBefore(DateUtil.endOfDay(end));
        }

        List<Task> taskList = query.list();

        // 转换vo
        taskList.forEach(e -> {
            TaskVo tv = new TaskVo(e);

            // 关联委托人
            if(StrUtil.isNotBlank(tv.getOwner())){
                String realname = sysBaseAPI.getUserByName(tv.getOwner()).getRealname();
                tv.setOwner(realname);
            }
            List<IdentityLink> identityLinks = runtimeService.getIdentityLinksForProcessInstance(tv.getProcInstId());
            for(IdentityLink ik : identityLinks){
                // 关联发起人
                if("starter".equals(ik.getType())&&StrUtil.isNotBlank(ik.getUserId())){
                    tv.setApplyer(sysBaseAPI.getUserByName(ik.getUserId()).getRealname());
                }
            }
            // 关联流程信息
            ActZprocess actProcess = actZprocessService.getById(tv.getProcDefId());
            if(actProcess!=null){
                tv.setProcessName(actProcess.getName());
                tv.setRouteName(actProcess.getRouteName());
            }
            // 关联业务key
            ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(tv.getProcInstId()).singleResult();
            tv.setBusinessKey(pi.getBusinessKey());
            ActBusiness actBusiness = actBusinessService.getById(pi.getBusinessKey());
            if(actBusiness!=null){
                tv.setTableId(actBusiness.getTableId());
                tv.setTableName(actBusiness.getTableName());
            }

            list.add(tv);
        });
        return Result.ok(list);
    }
    /*获取可返回的节点*/
    @RequestMapping(value = "/getBackList/{procInstId}", method = RequestMethod.GET)
    public Result<Object> getBackList(@PathVariable String procInstId){

        List<HistoricTaskVo> list = new ArrayList<>();
        List<HistoricTaskInstance> taskInstanceList = historyService.createHistoricTaskInstanceQuery().processInstanceId(procInstId)
                .finished().list();

        taskInstanceList.forEach(e -> {
            HistoricTaskVo htv = new HistoricTaskVo(e);
            list.add(htv);
        });

        // 去重
        LinkedHashSet<String> set = new LinkedHashSet<String>(list.size());
        List<HistoricTaskVo> newList = new ArrayList<>();
        list.forEach(e->{
            if(set.add(e.getName())){
                newList.add(e);
            }
        });

        return Result.ok(newList);
    }
    /*任务节点审批驳回至发起人*/
    @RequestMapping(value = "/back", method = RequestMethod.POST)
    public Result<Object> back(@ApiParam("任务id") @RequestParam String id,
                               @ApiParam("流程实例id") @RequestParam String procInstId,
                               @ApiParam("意见评论") @RequestParam(required = false) String comment,
                               @ApiParam("是否发送站内消息") @RequestParam(defaultValue = "false") Boolean sendMessage,
                               @ApiParam("是否发送短信通知") @RequestParam(defaultValue = "false") Boolean sendSms,
                               @ApiParam("是否发送邮件通知") @RequestParam(defaultValue = "false") Boolean sendEmail){


        if(StrUtil.isBlank(comment)){
            comment = "";
        }
        taskService.addComment(id, procInstId, comment);
        ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(procInstId).singleResult();
        // 删除流程实例
        runtimeService.deleteProcessInstance(procInstId, "backed");
        ActBusiness actBusiness = actBusinessService.getById(pi.getBusinessKey());
        actBusiness.setStatus(ActivitiConstant.STATUS_FINISH);
        actBusiness.setResult(ActivitiConstant.RESULT_FAIL);
        actBusinessService.updateById(actBusiness);
        // 异步发消息
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        actZprocessService.sendMessage(sysUser,sysBaseAPI.getUserByName(actBusiness.getUserId()),ActivitiConstant.MESSAGE_BACK_CONTENT,
                String.format("您的 【%s】 申请已被驳回！",actBusiness.getTitle()),sendMessage, sendSms, sendEmail);
        // 记录实际审批人员
        actBusinessService.insertHI_IDENTITYLINK(IdUtil.simpleUUID(),
                ActivitiConstant.EXECUTOR_TYPE, sysUser.getUsername(), id, procInstId);
        return Result.ok("操作成功");
    }
    /*流程流转历史*/
    @RequestMapping(value = "/historicFlow/{id}", method = RequestMethod.GET)
    public Result<Object> historicFlow(@PathVariable String id){

        List<HistoricTaskVo> list = new ArrayList<>();

        List<HistoricTaskInstance> taskList = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(id).orderByHistoricTaskInstanceEndTime().asc().list();

        // 转换vo
        taskList.forEach(e -> {
            HistoricTaskVo htv = new HistoricTaskVo(e);
            List<Assignee> assignees = new ArrayList<>();
            // 关联分配人（委托用户时显示该人）
            if(StrUtil.isNotBlank(htv.getAssignee())){
                String assignee = sysBaseAPI.getUserByName(htv.getAssignee()).getRealname();
                String owner = sysBaseAPI.getUserByName(htv.getOwner()).getRealname();
                assignees.add(new Assignee(assignee+"(受"+owner+"委托)", true));
            }
            List<HistoricIdentityLink> identityLinks = historyService.getHistoricIdentityLinksForTask(e.getId());
            // 获取实际审批用户id
            String userId = actBusinessService.findUserIdByTypeAndTaskId(ActivitiConstant.EXECUTOR_TYPE, e.getId());
            for(HistoricIdentityLink hik : identityLinks){
                // 关联候选用户（分配的候选用户审批人）
                if("candidate".equals(hik.getType())&& StrUtil.isNotBlank(hik.getUserId())){
                    String username = sysBaseAPI.getUserByName(hik.getUserId()).getRealname();
                    Assignee assignee = new Assignee(username, false);
                    if(StrUtil.isNotBlank(userId)&&userId.equals(hik.getUserId())){
                        assignee.setIsExecutor(true);
                    }
                    assignees.add(assignee);
                }
            }
            htv.setAssignees(assignees);
            // 关联审批意见
            List<Comment> comments = taskService.getTaskComments(htv.getId(), "comment");
            if(comments!=null&&comments.size()>0){
                htv.setComment(comments.get(0).getFullMessage());
            }
            list.add(htv);
        });
        return Result.ok(list);
    }
    @RequestMapping(value = "/pass", method = RequestMethod.POST)
    @ApiOperation(value = "任务节点审批通过")
    public Result<Object> pass(@ApiParam("任务id") @RequestParam String id,
                               @ApiParam("流程实例id") @RequestParam String procInstId,
                               @ApiParam("下个节点审批人") @RequestParam(required = false) String assignees,
                               @ApiParam("优先级") @RequestParam(required = false) Integer priority,
                               @ApiParam("意见评论") @RequestParam(required = false) String comment,
                               @ApiParam("是否发送站内消息") @RequestParam(defaultValue = "false") Boolean sendMessage,
                               @ApiParam("是否发送短信通知") @RequestParam(defaultValue = "false") Boolean sendSms,
                               @ApiParam("是否发送邮件通知") @RequestParam(defaultValue = "false") Boolean sendEmail){

        if(StrUtil.isBlank(comment)){
            comment = "";
        }
        taskService.addComment(id, procInstId, comment);
        ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(procInstId).singleResult();
        Task task = taskService.createTaskQuery().taskId(id).singleResult();
        if(StrUtil.isNotBlank(task.getOwner())&&!("RESOLVED").equals(task.getDelegationState().toString())){
            // 未解决的委托任务 先resolve
            String oldAssignee = task.getAssignee();
            taskService.resolveTask(id);
            taskService.setAssignee(id, oldAssignee);
        }
        taskService.complete(id);
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(procInstId).list();
        // 判断下一个节点
        if(tasks!=null&&tasks.size()>0){
            ActBusiness actBusiness = actBusinessService.getById(pi.getBusinessKey());
            for(Task t : tasks){
                if(StrUtil.isBlank(assignees)){
                    // 如果下个节点未分配审批人为空 取消结束流程
                    List<LoginUser> users = actZprocessService.getNode(t.getTaskDefinitionKey()).getUsers();
                    if(users==null||users.size()==0){
                        runtimeService.deleteProcessInstance(procInstId, "canceled-审批节点未分配审批人，流程自动中断取消");
                        actBusiness.setStatus(ActivitiConstant.STATUS_CANCELED);
                        actBusiness.setResult(ActivitiConstant.RESULT_TO_SUBMIT);
                        actBusinessService.updateById(actBusiness);
                        break;
                    }else{
                        // 避免重复添加
                        List<String> list = actBusinessService.selectIRunIdentity(t.getId(), "candidate");
                        if(list==null||list.size()==0) {
                            // 分配了节点负责人分发给全部
                            for (LoginUser user : users) {
                                taskService.addCandidateUser(t.getId(), user.getId());
                                // 异步发消息
                                actZprocessService.sendActMessage(loginUser,user,actBusiness,task.getName(),  sendMessage, sendSms, sendEmail);
                            }
                            taskService.setPriority(t.getId(), task.getPriority());
                        }
                    }
                }else{
                    // 避免重复添加
                    List<String> list = actBusinessService.selectIRunIdentity(t.getId(), "candidate");
                    if(list==null||list.size()==0) {

                        for(String assignee : assignees.split(",")){
                            taskService.addCandidateUser(t.getId(), assignee);
                            // 异步发消息
                            LoginUser user = sysBaseAPI.getUserByName(assignee);
                            actZprocessService.sendActMessage(loginUser,user,actBusiness,task.getName(),  sendMessage, sendSms, sendEmail);
                            taskService.setPriority(t.getId(), priority);
                        }
                    }
                }
            }
        } else {
            ActBusiness actBusiness = actBusinessService.getById(pi.getBusinessKey());
            actBusiness.setStatus(ActivitiConstant.STATUS_FINISH);
            actBusiness.setResult(ActivitiConstant.RESULT_PASS);
            actBusinessService.updateById(actBusiness);
            // 异步发消息
            LoginUser user = sysBaseAPI.getUserByName(actBusiness.getUserId());
            actZprocessService.sendMessage(loginUser,user,ActivitiConstant.MESSAGE_PASS_CONTENT,
                    String.format("您的 【%s】 申请已通过！",actBusiness.getTitle()),sendMessage, sendSms, sendEmail);
        }
        // 记录实际审批人员
        actBusinessService.insertHI_IDENTITYLINK(IdUtil.simpleUUID(),
                ActivitiConstant.EXECUTOR_TYPE, loginUser.getUsername(), id, procInstId);
        return Result.ok("操作成功");
    }
    @RequestMapping(value = "/delegate", method = RequestMethod.POST)
    @ApiOperation(value = "委托他人代办")
    public Result<Object> delegate(@ApiParam("任务id") @RequestParam String id,
                                   @ApiParam("委托用户id") @RequestParam String userId,
                                   @ApiParam("流程实例id") @RequestParam String procInstId,
                                   @ApiParam("意见评论") @RequestParam(required = false) String comment,
                                   @ApiParam("是否发送站内消息") @RequestParam(defaultValue = "false") Boolean sendMessage,
                                   @ApiParam("是否发送短信通知") @RequestParam(defaultValue = "false") Boolean sendSms,
                                   @ApiParam("是否发送邮件通知") @RequestParam(defaultValue = "false") Boolean sendEmail){

        if(StrUtil.isBlank(comment)){
            comment = "";
        }
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        taskService.addComment(id, procInstId, comment);
        taskService.delegateTask(id, userId);
        taskService.setOwner(id, sysUser.getUsername());
        // 异步发消息
        actZprocessService.sendMessage(sysUser,sysBaseAPI.getUserByName(userId),ActivitiConstant.MESSAGE_DELEGATE_CONTENT,
                String.format("您有一个来自 %s 的委托需要处理！",sysUser.getRealname()),sendMessage, sendSms, sendEmail);
        return Result.ok("操作成功");
    }
    @RequestMapping(value = "/backToTask", method = RequestMethod.POST)
    @ApiOperation(value = "任务节点审批驳回至指定历史节点")
    public Result<Object> backToTask(@ApiParam("任务id") @RequestParam String id,
                                     @ApiParam("驳回指定节点key") @RequestParam String backTaskKey,
                                     @ApiParam("流程实例id") @RequestParam String procInstId,
                                     @ApiParam("流程定义id") @RequestParam String procDefId,
                                     @ApiParam("原节点审批人") @RequestParam(required = false) String assignees,
                                     @ApiParam("优先级") @RequestParam(required = false) Integer priority,
                                     @ApiParam("意见评论") @RequestParam(required = false) String comment,
                                     @ApiParam("是否发送站内消息") @RequestParam(defaultValue = "false") Boolean sendMessage,
                                     @ApiParam("是否发送短信通知") @RequestParam(defaultValue = "false") Boolean sendSms,
                                     @ApiParam("是否发送邮件通知") @RequestParam(defaultValue = "false") Boolean sendEmail){


        if(StrUtil.isBlank(comment)){
            comment = "";
        }
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        taskService.addComment(id, procInstId, comment);
        // 取得流程定义
        ProcessDefinitionEntity definition = (ProcessDefinitionEntity) repositoryService.getProcessDefinition(procDefId);
        // 获取历史任务的Activity
        ActivityImpl hisActivity = definition.findActivity(backTaskKey);
        // 实现跳转
        managementService.executeCommand(new JumpTask(procInstId, hisActivity.getId()));
        // 重新分配原节点审批人
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(procInstId).list();
        if(tasks!=null&&tasks.size()>0){
            tasks.forEach(e->{
                for(String assignee:assignees.split(",")){
                    taskService.addCandidateUser(e.getId(), assignee);
                    // 异步发消息
                    actZprocessService.sendMessage(loginUser,sysBaseAPI.getUserByName(assignee),ActivitiConstant.MESSAGE_TODO_CONTENT
                    ,"您有一个任务待审批，请尽快处理！",sendMessage, sendSms, sendEmail);
                }
                if(priority!=null){
                    taskService.setPriority(e.getId(), priority);
                }
            });
        }
        // 记录实际审批人员
        actBusinessService.insertHI_IDENTITYLINK(IdUtil.simpleUUID(),
                ActivitiConstant.EXECUTOR_TYPE, loginUser.getUsername(), id, procInstId);
        return Result.ok("操作成功");
    }

    public class JumpTask implements Command<ExecutionEntity> {

        private String procInstId;
        private String activityId;

        public JumpTask(String procInstId, String activityId) {
            this.procInstId = procInstId;
            this.activityId = activityId;
        }

        @Override
        public ExecutionEntity execute(CommandContext commandContext) {

            ExecutionEntity executionEntity = commandContext.getExecutionEntityManager().findExecutionById(procInstId);
            executionEntity.destroyScope("backed");
            ProcessDefinitionImpl processDefinition = executionEntity.getProcessDefinition();
            ActivityImpl activity = processDefinition.findActivity(activityId);
            executionEntity.executeActivity(activity);

            return executionEntity;
        }

    }
    /*已办列表*/
    @RequestMapping(value = "/doneList")
    public Result<Object> doneList(String name,
                                   String categoryId,
                                   Integer priority,
                                   HttpServletRequest req){

        List<HistoricTaskVo> list = new ArrayList<>();
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String userId = loginUser.getUsername();
        HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery().or().taskCandidateUser(userId).
                taskAssignee(userId).endOr().finished();

        // 多条件搜索
        query.orderByTaskCreateTime().desc();
        if(StrUtil.isNotBlank(name)){
            query.taskNameLike("%"+name+"%");
        }
        if(StrUtil.isNotBlank(categoryId)){
            query.taskCategory(categoryId);
        }
        if(priority!=null){
            query.taskPriority(priority);
        }
        List<HistoricTaskInstance> taskList = query.list();
        // 转换vo
        List<ComboModel> allUser = sysBaseAPI.queryAllUser();
        Map<String, String> userMap = allUser.stream().collect(Collectors.toMap(ComboModel::getUsername, ComboModel::getTitle));
        taskList.forEach(e -> {
            HistoricTaskVo htv = new HistoricTaskVo(e);
            // 关联委托人
            if(StrUtil.isNotBlank(htv.getOwner())){
                htv.setOwner(userMap.get(htv.getOwner()));
            }
            List<HistoricIdentityLink> identityLinks = historyService.getHistoricIdentityLinksForProcessInstance(htv.getProcInstId());
            for(HistoricIdentityLink hik : identityLinks){
                // 关联发起人
                if("starter".equals(hik.getType())&&StrUtil.isNotBlank(hik.getUserId())){
                    htv.setApplyer(userMap.get(hik.getUserId()));
                }
            }
            // 关联审批意见
            List<Comment> comments = taskService.getTaskComments(htv.getId(), "comment");
            if(comments!=null&&comments.size()>0){
                htv.setComment(comments.get(0).getFullMessage());
            }
            // 关联流程信息
            ActZprocess actProcess = actZprocessService.getById(htv.getProcDefId());
            if(actProcess!=null){
                htv.setProcessName(actProcess.getName());
                htv.setRouteName(actProcess.getRouteName());
            }
            // 关联业务key
            HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery().processInstanceId(htv.getProcInstId()).singleResult();
            htv.setBusinessKey(hpi.getBusinessKey());
            ActBusiness actBusiness = actBusinessService.getById(hpi.getBusinessKey());
            if(actBusiness!=null){
                htv.setTableId(actBusiness.getTableId());
                htv.setTableName(actBusiness.getTableName());
            }

            list.add(htv);
        });
        return Result.ok(list);
    }
    /*删除任务历史*/
    @RequestMapping(value = "/deleteHistoric/{ids}", method = RequestMethod.POST)
    public Result<Object> deleteHistoric( @PathVariable String ids){

        for(String id : ids.split(",")){
            historyService.deleteHistoricTaskInstance(id);
        }
        return Result.ok("操作成功");
    }
}
