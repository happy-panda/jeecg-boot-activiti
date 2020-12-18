package org.jeecg.modules.activiti.web;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricIdentityLink;
import org.activiti.engine.history.HistoricTaskInstance;
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
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.api.ISysBaseAPI;
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
@Api(tags = "流程")
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
    @AutoLog(value = "流程-代办列表")
    @ApiOperation(value="流程-代办列表", notes="代办列表")
    @RequestMapping(value = "/todoList" ,method = RequestMethod.GET)
    public Result<Object> todoList(@ApiParam(value = "任务名称" )String name,
                                   @ApiParam(value = "任务分类" )String categoryId,
                                   @ApiParam(value = "优先级" )Integer priority,
                                   @ApiParam(value = "创建开始时间" )String createTime_begin,
                                   @ApiParam(value = "创建结束时间" )String createTime_end,
                                   HttpServletRequest request){
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
        if(StrUtil.isNotBlank(createTime_begin)){
            Date start = DateUtil.parse(createTime_begin);
            query.taskCreatedAfter(start);
        }
        if(StrUtil.isNotBlank(createTime_end)){
            Date end = DateUtil.parse(createTime_end);
            query.taskCreatedBefore(DateUtil.endOfDay(end));
        }
        //流程类型
        String type = request.getParameter("type");
        String proDefId = request.getParameter("proDefId");
        if(StringUtils.isNotEmpty(proDefId)){
            query.processDefinitionId(proDefId);
        } else if (StrUtil.isNotBlank(type)){
            List<String> deployment_idList = actBusinessService.getBaseMapper().deployment_idListByType(type);
            if (deployment_idList.size()==0){
                query.deploymentIdIn(Lists.newArrayList(""));
            }else {
                query.deploymentIdIn(deployment_idList);
            }
        }
        String searchVal = request.getParameter("searchVal");
        if (StrUtil.isNotBlank(searchVal)){
            //搜索标题、申请人
            List<LoginUser> usersByName = actBusinessService.getBaseMapper().getUsersByName(searchVal);
            List<String> uNames = null;
            if (usersByName.size()==0){
                uNames = Lists.newArrayList("");
            }else {
                uNames = usersByName.stream().map(u->u.getUsername()).collect(Collectors.toList());
            }
            List<ActBusiness> businessList = actBusinessService.list(new LambdaQueryWrapper<ActBusiness>()
                    .like(ActBusiness::getTitle, searchVal) //标题查询
                    .or().in(ActBusiness::getUserId,uNames)
            );
            if (businessList.size()>0){
                // 定义id
                List<String> pids = businessList.stream().filter(act -> act.getProcInstId()!=null).map(act -> act.getProcInstId()).collect(Collectors.toList());
                query.processInstanceIdIn(pids);
            }else {
                query.processInstanceIdIn(Lists.newArrayList(""));
            }
        }
        List<Task> taskList = query.list();
        // 是否需要业务数据
        String needData = request.getParameter("needData");
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
                tv.setTitle(actBusiness.getTitle());
                tv.setStatus(actBusiness.getStatus());
                tv.setResult(actBusiness.getResult());
                if (StrUtil.equals(needData,"true")){ // 需要业务数据
                    Map<String, Object> applyForm = actBusinessService.getApplyForm(actBusiness.getTableId(), actBusiness.getTableName());
                    tv.setDataMap(applyForm);
                }
            }
            list.add(tv);
        });
        return Result.ok(list);
    }

    /*查询当前登陆人的待办数量*/
    @AutoLog(value = "流程-待办条数")
    @ApiOperation(value="流程-待办条数", notes="待办列表")
    @RequestMapping(value = "/todoCounts" ,method = RequestMethod.GET)
    public Result<Object> todoList(@ApiParam(value = "流程定义key")@RequestParam(value = "procDefIds",defaultValue = "") String procDefIds,
                                   HttpServletRequest request){
        Map<String,Integer> todoCounts = new HashMap<>();
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String userId = sysUser.getUsername();
        TaskQuery query = taskService.createTaskQuery().taskCandidateOrAssigned(userId);
        if(procDefIds.length()==0){
            List<Task> list = query.list();
            for (Task task : list){
                Integer count = todoCounts.get(task.getProcessDefinitionId());
                if(count==null){
                    todoCounts.put(task.getProcessDefinitionId(),Integer.valueOf(1));
                }else{
                    todoCounts.put(task.getProcessDefinitionId(),++count);
                }
            }
        }else{
            String array[] = procDefIds.split(",");
            for (String procDefId : array) {
                List<Task> list = query.processDefinitionId(procDefId).list();
                todoCounts.put(procDefId,Integer.valueOf(0));
                if(list.size()>0){
                    todoCounts.put(procDefId,Integer.valueOf(list.size()));
                }
            }
        }

        return Result.ok(todoCounts);
    }

    /*获取可返回的节点*/
    @AutoLog(value = "流程-获取可返回的节点")
    @ApiOperation(value="流程-获取可返回的节点", notes="获取可返回的节点")
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
    /*任务节点审批 驳回至发起人*/
    @AutoLog(value = "流程-任务节点审批 驳回至发起人")
    @ApiOperation(value="流程-任务节点审批 驳回至发起人", notes="任务节点审批 驳回至发起人")
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
        actZprocessService.sendMessage(actBusiness.getId(),sysUser,sysBaseAPI.getUserByName(actBusiness.getUserId()),ActivitiConstant.MESSAGE_BACK_CONTENT,
                String.format("您的 【%s】 申请已被驳回！",actBusiness.getTitle()),sendMessage, sendSms, sendEmail);
        // 记录实际审批人员
        actBusinessService.insertHI_IDENTITYLINK(IdUtil.simpleUUID(),
                ActivitiConstant.EXECUTOR_TYPE_b, sysUser.getUsername(), id, procInstId);
        //修改业务表的流程字段
        actBusinessService.updateBusinessStatus(actBusiness.getTableName(), actBusiness.getTableId(),"驳回");
        return Result.ok("操作成功");
    }
    /*流程流转历史*/
    @AutoLog(value = "流程-流程流转历史")
    @ApiOperation(value="流程-流程流转历史", notes="流程流转历史")
    @RequestMapping(value = "/historicFlow/{id}", method = RequestMethod.GET)
    public Result<Object> historicFlow(@ApiParam("实例Id")@PathVariable String id){

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
            List<String> userIds_b = actBusinessService.findUserIdByTypeAndTaskId(ActivitiConstant.EXECUTOR_TYPE_b, e.getId());
            List<String> userIds_p = actBusinessService.findUserIdByTypeAndTaskId(ActivitiConstant.EXECUTOR_TYPE_p, e.getId());
            for(HistoricIdentityLink hik : identityLinks){
                // 关联候选用户（分配的候选用户审批人）
                if(ActivitiConstant.EXECUTOR_candidate.equals(hik.getType())&& StrUtil.isNotBlank(hik.getUserId())){
                    String username = sysBaseAPI.getUserByName(hik.getUserId()).getRealname();
                    Assignee assignee = new Assignee(username, false);
                    /*审批过的标记一下，前端标颜色用*/
                    if(CollectionUtil.contains(userIds_b,hik.getUserId()) || CollectionUtil.contains(userIds_p,hik.getUserId())){
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
    @AutoLog(value = "流程-任务节点审批通过")
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

        /**
         * 修改流程表单变量
         * 表单参数
         */
        LambdaQueryWrapper<ActBusiness> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActBusiness::getProcInstId,pi.getProcessInstanceId());
        ActBusiness one = actBusinessService.getOne(queryWrapper);
        Map<String, Object> busiData = actBusinessService.getBusiData(one.getTableId(), one.getTableName());

        /**
         * 获取所有流程变量 并修改
         */
        for(String key : busiData.keySet()){
            taskService.setVariable(id,key,busiData.get(key));
        }

        /*会签思路：
        act_hi_identitylink记录着审批历史 ActivitiConstant.EXECUTOR_TYPE_p 标识审批通过
        1、节点设置中增加人数字段，表示需要多少人通过这个任务节点才通过
        2、此处查询审批历史，查看当前节点的审批情况，符合预设的人数调用 taskService.complete(id); 完成该节点任务
        否则只记录审批数据，不完成该任务节点
        3、会有的问题：
            1、如此，审批过的人代办中还会看到这条任务，需要标识自己审批过，但是这条任务自己不能再审了  或 能再审，但是再审记得把之前审批过的记录删掉
            2、下一个节点的审批人只能最后通过的人选择才有效
            3、如果下一个节点是会签，指定下一节点的审批人的数量必须不小于节点预设数量
            其他问题，待暴露
          */
        /*完成任务*/
        taskService.complete(id);
        ActBusiness actBusiness = actBusinessService.getById(pi.getBusinessKey());
        //修改业务表的流程字段
        actBusinessService.updateBusinessStatus(actBusiness.getTableName(), actBusiness.getTableId(),"审批中-"+task.getTaskDefinitionKey()+"-"+task.getName());

        task.getName();
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(procInstId).list();
        // 判断下一个节点
        if(tasks!=null&&tasks.size()>0){
            for(Task t : tasks){
                if(StrUtil.isBlank(assignees)){
                    // 如果下个节点未分配审批人为空 取消结束流程
                    List<LoginUser> users = actZprocessService.getNode(t.getTaskDefinitionKey(),actBusiness.getTableName(),actBusiness.getTableId()).getUsers();
                    if(users==null||users.size()==0){
                        runtimeService.deleteProcessInstance(procInstId, "canceled-审批节点未分配审批人，流程自动中断取消");
                        actBusiness.setStatus(ActivitiConstant.STATUS_CANCELED);
                        actBusiness.setResult(ActivitiConstant.RESULT_TO_SUBMIT);
                        actBusinessService.updateById(actBusiness);
                        //修改业务表的流程字段
                        actBusinessService.updateBusinessStatus(actBusiness.getTableName(), actBusiness.getTableId(),"审批异常-"+task.getTaskDefinitionKey()+"-"+task.getName()+"-审批节点未分配审批人，流程自动中断取消");

                        break;
                    }else{
                        // 避免重复添加
                        List<String> list = actBusinessService.selectIRunIdentity(t.getId(), ActivitiConstant.EXECUTOR_candidate);
                        if(list==null||list.size()==0) {
                            // 分配了节点负责人分发给全部
                            for (LoginUser user : users) {
                                taskService.addCandidateUser(t.getId(), user.getUsername());
                                // 异步发消息
                                actZprocessService.sendActMessage(loginUser,user,actBusiness,task.getName(),  sendMessage, sendSms, sendEmail);
                            }
                            taskService.setPriority(t.getId(), task.getPriority());
                        }
                    }
                }else{
                    // 避免重复添加
                    List<String> list = actBusinessService.selectIRunIdentity(t.getId(), ActivitiConstant.EXECUTOR_candidate);
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
            actBusiness.setStatus(ActivitiConstant.STATUS_FINISH);
            actBusiness.setResult(ActivitiConstant.RESULT_PASS);
            actBusinessService.updateById(actBusiness);
            // 异步发消息
            LoginUser user = sysBaseAPI.getUserByName(actBusiness.getUserId());
            actZprocessService.sendMessage(actBusiness.getId(),loginUser,user,ActivitiConstant.MESSAGE_PASS_CONTENT,
                    String.format("您的 【%s】 申请已通过！",actBusiness.getTitle()),sendMessage, sendSms, sendEmail);
            //修改业务表的流程字段
            actBusinessService.updateBusinessStatus(actBusiness.getTableName(), actBusiness.getTableId(),"审批通过");

        }
        // 记录实际审批人员
        actBusinessService.insertHI_IDENTITYLINK(IdUtil.simpleUUID(),
                ActivitiConstant.EXECUTOR_TYPE_p, loginUser.getUsername(), id, procInstId);
        return Result.ok("操作成功");
    }
    @RequestMapping(value = "/delegate", method = RequestMethod.POST)
    @ApiOperation(value = "委托他人代办")
    @AutoLog(value = "流程-委托他人代办")
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
        ActBusiness actBusiness = actBusinessService.getOne(new LambdaQueryWrapper<ActBusiness>().eq(ActBusiness::getProcInstId, procInstId).last("limit 1"));
        // 异步发消息
        actZprocessService.sendMessage(actBusiness.getId(),sysUser,sysBaseAPI.getUserByName(userId),ActivitiConstant.MESSAGE_DELEGATE_CONTENT,
                String.format("您有一个来自 %s 的委托需要处理！",sysUser.getRealname()),sendMessage, sendSms, sendEmail);
        return Result.ok("操作成功");
    }
    @RequestMapping(value = "/backToTask", method = RequestMethod.POST)
    @ApiOperation(value = "任务节点审批驳回至指定历史节点")
    @AutoLog(value = "流程-任务节点审批驳回至指定历史节点")
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
        ActBusiness actBusiness = actBusinessService.getOne(new LambdaQueryWrapper<ActBusiness>().eq(ActBusiness::getProcInstId, procInstId).last("limit 1"));
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(procInstId).list();
        if(tasks!=null&&tasks.size()>0){
            tasks.forEach(e->{
                for(String assignee:assignees.split(",")){
                    taskService.addCandidateUser(e.getId(), assignee);
                    // 异步发消息
                    actZprocessService.sendMessage(actBusiness.getId(),loginUser,sysBaseAPI.getUserByName(assignee),ActivitiConstant.MESSAGE_TODO_CONTENT
                    ,"您有一个任务待审批，请尽快处理！",sendMessage, sendSms, sendEmail);
                }
                if(priority!=null){
                    taskService.setPriority(e.getId(), priority);
                }
            });
        }
        // 记录实际审批人员
        actBusinessService.insertHI_IDENTITYLINK(IdUtil.simpleUUID(),
                ActivitiConstant.EXECUTOR_TYPE_b, loginUser.getUsername(), id, procInstId);
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
    @AutoLog(value = "流程-已办列表")
    @ApiOperation(value="流程-已办列表", notes="已办列表")
    @RequestMapping(value = "/doneList" ,method = RequestMethod.GET)
    public Result<Object> doneList(String name,
                                   String categoryId,
                                   Integer priority,
                                   HttpServletRequest req){

        List<HistoricTaskVo> list = actBusinessService.getHistoricTaskVos(req, name, categoryId, priority);
        return Result.ok(list);
    }

    /*删除任务历史*/
    @AutoLog(value = "流程-删除任务历史")
    @ApiOperation(value="流程-删除任务历史", notes="删除任务历史")
    @RequestMapping(value = "/deleteHistoric/{ids}", method = RequestMethod.POST)
    public Result<Object> deleteHistoric( @PathVariable String ids){

        for(String id : ids.split(",")){
            historyService.deleteHistoricTaskInstance(id);
        }
        return Result.ok("操作成功");
    }
}
