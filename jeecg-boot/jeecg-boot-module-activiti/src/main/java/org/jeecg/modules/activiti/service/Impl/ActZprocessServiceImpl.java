package org.jeecg.modules.activiti.service.Impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.*;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.dto.message.BusMessageDTO;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.modules.activiti.entity.*;
import org.jeecg.modules.activiti.mapper.ActZprocessMapper;
import org.jeecg.modules.activiti.service.IActZprocessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: 流程定义扩展表
 * @Author: pmc
 * @Date:   2020-03-22
 * @Version: V1.0
 */
@Service
public class ActZprocessServiceImpl extends ServiceImpl<ActZprocessMapper, ActZprocess> implements IActZprocessService {
    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private IdentityService identityService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private TaskService taskService;
    @Autowired
    private ActNodeServiceImpl actNodeService;
    @Autowired
    private ActBusinessServiceImpl actBusinessService;
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private ISysBaseAPI sysBaseAPI;

    /**
     * 通过key设置所有版本为旧
     * @param processKey
     */
    public void setAllOldByProcessKey(String processKey) {
        List<ActZprocess> list = this.list(new LambdaQueryWrapper<ActZprocess>().eq(ActZprocess::getProcessKey,processKey));
        if(list==null||list.size()==0){
            return;
        }
        list.forEach(item -> {
            item.setLatest(false);
        });
        this.updateBatchById(list);
    }

    /**
     * 更新最新版本的流程
     * @param processKey
     */
    public void setLatestByProcessKey(String processKey) {
        ActZprocess actProcess = this.findTopByProcessKeyOrderByVersionDesc(processKey);
        if(actProcess==null){
            return;
        }
        actProcess.setLatest(true);
        this.updateById(actProcess);
    }

    private ActZprocess findTopByProcessKeyOrderByVersionDesc(String processKey) {
        List<ActZprocess> list = this.list(new LambdaQueryWrapper<ActZprocess>().eq(ActZprocess::getProcessKey, processKey)
                .orderByDesc(ActZprocess::getVersion)
        );
        if (CollUtil.isNotEmpty(list)){
            return list.get(0);
        }
        return null;
    }

    public String startProcess(ActBusiness actBusiness) {
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        // 启动流程用户
        identityService.setAuthenticatedUserId(loginUser.getUsername());
        // 启动流程 需传入业务表id变量
        Map<String, Object> params = actBusiness.getParams();
        params.put("tableId", actBusiness.getTableId());
        ActBusiness act = actBusinessService.getById(actBusiness.getId());
        String tableName = act.getTableName();
        String tableId = act.getTableId();
        if (StrUtil.isBlank(tableId)||StrUtil.isBlank(tableName)){
            throw new JeecgBootException("没有业务表单数据");
        }
        /*表单数据写入*/
        Map<String, Object> busiData = actBusinessService.getBusiData(tableId, tableName);
        for (String key : busiData.keySet()) {
            params.put(key,busiData.get(key));
        }
        ProcessInstance pi = runtimeService.startProcessInstanceById(actBusiness.getProcDefId(), actBusiness.getId(), params);
        // 设置流程实例名称
        runtimeService.setProcessInstanceName(pi.getId(), actBusiness.getTitle());
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
        for(Task task : tasks){
            if(actBusiness.getFirstGateway()){
                // 网关类型
                List<LoginUser> users = getNode(task.getTaskDefinitionKey(),tableName,tableId).getUsers();
                // 如果下个节点未分配审批人为空 取消结束流程
                if(users==null||users.size()==0){
                    throw new RuntimeException("任务节点未分配任何候选审批人，发起流程失败");
                }else{
                    // 分配了节点负责人分发给全部
                    for(LoginUser user : users){
                        taskService.addCandidateUser(task.getId(), user.getUsername());
                        // 异步发消息
                        sendActMessage(loginUser,user,actBusiness,task.getName(), actBusiness.getSendMessage(),
                                actBusiness.getSendSms(), actBusiness.getSendEmail());
                    }
                }
            }else {
                // 分配第一个任务用户
                String assignees = actBusiness.getAssignees();
                for (String assignee : assignees.split(",")) {
                    taskService.addCandidateUser(task.getId(), assignee);
                    // 异步发消息
                    LoginUser user = sysBaseAPI.getUserByName(assignee);
                    sendActMessage(loginUser,user,actBusiness,task.getName(), actBusiness.getSendMessage(),
                            actBusiness.getSendSms(), actBusiness.getSendEmail());
                }
            }
            // 设置任务优先级
            taskService.setPriority(task.getId(), actBusiness.getPriority());
        }
        return pi.getId();
    }

    /**
     * 发送流程信息
     * @param fromUser 发送人
     * @param toUser 接收人
     * @param act 流程
     * @param taskName
     * @param sendMessage 系统消息
     * @param sendSms 短信消息
     * @param sendEmail 邮件消息
     */
    public void sendActMessage(LoginUser fromUser, LoginUser toUser, ActBusiness act, String taskName, Boolean sendMessage, Boolean sendSms, Boolean sendEmail) {
        String title = String.format("您有一个新的审批任务："+act.getTitle());
        Map<String, String> msgMap = Maps.newHashMap();
                        /*流程名称：  ${bpm_name}
催办任务：  ${bpm_task}
催办时间 :    ${datetime}
催办内容 :    ${remark}*/
        msgMap.put("bpm_name",act.getTitle());
        msgMap.put("bpm_task",taskName);
        msgMap.put("datetime", DateUtils.now());
        msgMap.put("remark", "请进入待办栏，尽快处理！");
        /*流程催办模板*/
        //String msgText = sysBaseAPI.parseTemplateByCode("bpm_cuiban", msgMap);
        String msgText = String.format("您有一个新的审批任务："+act.getTitle());
        this.sendMessage(act.getId(),fromUser,toUser,title,msgText,sendMessage,sendSms,sendEmail);
    }

    /**
     * 发消息
     * @param actBusId 流程业务id
     * @param fromUser 发送人
     * @param toUser 接收人
     * @param title 标题
     * @param msgText 信息内容
     * @param sendMessage 系统消息
     * @param sendSms 短信
     * @param sendEmail 邮件
     */
    public void sendMessage(String actBusId,LoginUser fromUser, LoginUser toUser,String title,String msgText,  Boolean sendMessage, Boolean sendSms, Boolean sendEmail) {
        if (sendMessage!=null&&sendMessage){
            BusMessageDTO messageDTO = new BusMessageDTO(fromUser.getUsername(),toUser.getUsername(),title,msgText,"2","bpm",actBusId);
            sysBaseAPI.sendBusAnnouncement(messageDTO);
        }
        //todo 以下需要购买阿里短信服务；设定邮件服务账号
        if (sendSms!=null&&sendSms&& StrUtil.isNotBlank(toUser.getPhone())){
            //DySmsHelper.sendSms(toUser.getPhone(), obj, DySmsEnum.REGISTER_TEMPLATE_CODE)
        }
        if (sendEmail!=null&&sendEmail&& StrUtil.isNotBlank(toUser.getEmail())){
            JavaMailSender mailSender = (JavaMailSender) SpringContextUtils.getBean("mailSender");
            SimpleMailMessage message = new SimpleMailMessage();
// 设置发送方邮箱地址
//            message.setFrom(emailFrom);
            message.setTo(toUser.getEmail());
            //message.setSubject(es_title);
            message.setText(msgText);
            mailSender.send(message);
        }
    }

    public ProcessNodeVo getNode(String nodeId,String tableName,String tableId) {

        ProcessNodeVo node = new ProcessNodeVo();
        // 设置关联用户
        List<LoginUser> users = getNodetUsers(nodeId,tableName,tableId);
        node.setUsers(removeDuplicate(users));
        return node;
    }
    /**
     * 设置节点审批人
     * @param nodeId
     */
    public List<LoginUser> getNodetUsers(String nodeId,String tableName,String tableId){
        ActBusiness business = actBusinessService.getOne(new LambdaQueryWrapper<ActBusiness>().eq(ActBusiness::getTableId, tableId).eq(ActBusiness::getTableName, tableName));
        String procDefId = business.getProcDefId();
        // 直接选择人员
        List<LoginUser> users = actNodeService.findUserByNodeId(nodeId,procDefId);
        // 根据角色选择
        List<Role> roles = actNodeService.findRoleByNodeId(nodeId,procDefId);
        for(Role r : roles){
            List<LoginUser> userList = actNodeService.findUserByRoleId(r.getId());
            users.addAll(userList);
        }
        // 部门
        List<Department> departments = actNodeService.findDepartmentByNodeId(nodeId,procDefId);
        for (Department d : departments){
            List<LoginUser> userList = actNodeService.findUserDepartmentId(d.getId());
            users.addAll(userList);
        }
        // 部门负责人
        List<Department> departmentManages = actNodeService.findDepartmentManageByNodeId(nodeId,procDefId);
        for (Department d : departmentManages){
            List<LoginUser> userList = actNodeService.findUserDepartmentManageId(d.getId());
            users.addAll(userList);
        }
        // 发起人部门负责人
        if(actNodeService.hasChooseDepHeader(nodeId,procDefId)){
            List<LoginUser> allUser = actNodeService.queryAllUser();
            //申请人的部门负责人
            String createBy = getCreateBy(tableName,tableId);
            List<String> departIds = sysBaseAPI.getDepartIdsByUsername(createBy);

            for (String departId : departIds) {
                List<LoginUser> collect = allUser.stream().filter(u -> u.getDepartIds() != null && u.getDepartIds().indexOf(departId) > -1).collect(Collectors.toList());
                users.addAll(collect);
            }
        }
        // 发起人
        if(actNodeService.hasChooseSponsor(nodeId,procDefId)){
            String createBy = getCreateBy(tableName,tableId);
            LoginUser userByName = sysBaseAPI.getUserByName(createBy);
            users.add(userByName);
        }
        // 表单变量
        if(actNodeService.hasFormVariable(nodeId,procDefId)){
            List<String> variableNames = actNodeService.findFormVariableByNodeId(nodeId,procDefId);
            if(!variableNames.isEmpty()){
                Map<String, Object> applyForm = actBusinessService.getApplyForm(tableId, tableName);
                for(String variable : variableNames){
                    // 变量类型
                    String type = "user";
                    String paramName = variable;

                    int i = variable.indexOf(":");
                    if(i>0){
                        type = variable.substring(i + 1);
                        paramName = variable.substring(0,i);
                    }
                    // 获取表单变量的值
                    String paramVal = (String)applyForm.get(paramName);

                    if(StringUtils.isNotEmpty(paramVal)){
                        for(String val : StringUtils.split(paramVal,',')) {
                            if(StringUtils.equalsIgnoreCase(type,"role")){
                                List<LoginUser> roleUsers = actNodeService.findUserByRoleId(val);
                                users.addAll(roleUsers);
                            }else if(StringUtils.equalsIgnoreCase(type,"user")){
                                LoginUser user = sysBaseAPI.getUserByName(val);
                                if(user!=null){
                                    users.add(user);
                                }
                            }else if(StringUtils.equalsIgnoreCase(type,"dept")){
                                List<LoginUser> depUsers = actNodeService.findUserDepartmentId(val);
                                users.addAll(depUsers);
                            }else if(StringUtils.equalsIgnoreCase(type,"deptManage")){
                                List<LoginUser> depManageUsers = actNodeService.findUserDepartmentManageId(val);
                                users.addAll(depManageUsers);
                            }
                        }

                    }


                }
            }
        }

        // 过滤掉删除用户
        users = users.stream().filter(u->StrUtil.equals("0",u.getDelFlag()+"")).collect(Collectors.toList());
        return users;
    }

    /**
     * 根据节点id获取申请人
     * @param nodeId
     * @return
     */
    public String getUserByNodeid(String nodeId) {
        ActNode actNode = actNodeService.getOne(new LambdaQueryWrapper<ActNode>().eq(ActNode::getNodeId, nodeId).last("limit 1"));
        String procDefId = actNode.getProcDefId();
        ActBusiness actBusiness = actBusinessService.getOne(new LambdaQueryWrapper<ActBusiness>().eq(ActBusiness::getProcDefId, procDefId).last("limit 1"));
        return actBusiness.getCreateBy();
    }

    /**
     * 获取表单的创建人
     * @param tableName
     * @param tableId
     * @return
     */
    public String getCreateBy(String tableName,String tableId) {
        Map<String, Object> applyForm = actBusinessService.getApplyForm(tableId, tableName);
        return applyForm.get("createBy").toString();
    }

    /**
     * 去重
     * @param list
     * @return
     */
    private List<LoginUser> removeDuplicate(List<LoginUser> list) {

        LinkedHashSet<LoginUser> set = new LinkedHashSet<>(list.size());
        set.addAll(list);
        list.clear();
        list.addAll(set);
        entityManager.clear();
        list.forEach(u -> {
            u.setPassword(null);
        });
        return list;
    }

    public ProcessNodeVo getFirstNode(String procDefId,String tableName,String tableId) {
        BpmnModel bpmnModel = repositoryService.getBpmnModel(procDefId);

        ProcessNodeVo node = new ProcessNodeVo();

        List<Process> processes = bpmnModel.getProcesses();
        Collection<FlowElement> elements = processes.get(0).getFlowElements();
        // 流程开始节点
        StartEvent startEvent = null;
        for (FlowElement element : elements) {
            if (element instanceof StartEvent) {
                startEvent = (StartEvent) element;
                break;
            }
        }
        FlowElement e = null;
        // 判断开始后的流向节点
        SequenceFlow sequenceFlow = startEvent.getOutgoingFlows().get(0);
        for (FlowElement element : elements) {
            if(element.getId().equals(sequenceFlow.getTargetRef())){
                if(element instanceof UserTask){
                    e = element;
                    node.setType(1);
                    break;
                }else if(element instanceof ExclusiveGateway){
                    e = element;
                    node.setType(3);
                    break;
                }else if(element instanceof ParallelGateway){
                    e = element;
                    node.setType(4);
                    break;
                }else{
                    throw new RuntimeException("流程设计错误，开始节点后只能是用户任务节点、排他网关、并行网关");
                }
            }
        }
        // 排他、平行网关直接返回
        if(e instanceof ExclusiveGateway || e instanceof ParallelGateway){
            return node;
        }
        node.setTitle(e.getName());
        // 设置关联用户
        List<LoginUser> users = getNodetUsers(e.getId(),tableName,tableId);
        node.setUsers(removeDuplicate(users));
        return node;
    }

    public ProcessNodeVo getNextNode(String procDefId, String currActId,String procInsId) {
        // 根据流程实例id获取业务表单数据
        ActBusiness actBusiness = actBusinessService.getOne(new LambdaQueryWrapper<ActBusiness>().eq(ActBusiness::getProcInstId, procInsId));

        ProcessNodeVo node = new ProcessNodeVo();
        // 当前执行节点id
        ProcessDefinitionEntity dfe = (ProcessDefinitionEntity) ((RepositoryServiceImpl)repositoryService).getDeployedProcessDefinition(procDefId);
        // 获取所有节点
        List<ActivityImpl> activitiList = dfe.getActivities();
        // 判断出当前流程所处节点，根据路径获得下一个节点实例
        for(ActivityImpl activityImpl : activitiList){
            if (activityImpl.getId().equals(currActId)) {
                // 获取下一个节点
                List<PvmTransition> pvmTransitions = activityImpl.getOutgoingTransitions();

                PvmActivity pvmActivity = pvmTransitions.get(0).getDestination();

                String type = pvmActivity.getProperty("type").toString();
                if("userTask".equals(type)){
                    // 用户任务节点
                    node.setType(ActivitiConstant.NODE_TYPE_TASK);
                    node.setTitle(pvmActivity.getProperty("name").toString());
                    // 设置关联用户
                    List<LoginUser> users = getNodetUsers(pvmActivity.getId(),actBusiness.getTableName(),actBusiness.getTableId());
                    node.setUsers(removeDuplicate(users));
                }else if("exclusiveGateway".equals(type)){
                    // 排他网关
                    node.setType(ActivitiConstant.NODE_TYPE_EG);
                    ActivityImpl pvmActivity1 = (ActivityImpl) pvmActivity;
                    /*定义变量*/
                    Map<String, Object> vals = Maps.newHashMap();

                    LambdaQueryWrapper<ActBusiness> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(ActBusiness::getProcInstId,procInsId);
                    ActBusiness one = actBusinessService.getOne(wrapper);
                    vals = actBusinessService.getApplyForm(one.getTableId(), one.getTableName());

                    TaskDefinition taskDefinition = actNodeService.nextTaskDefinition(pvmActivity1, pvmActivity1.getId(), vals, procInsId);
                    List<LoginUser> users = getNodetUsers(taskDefinition.getKey(),actBusiness.getTableName(),actBusiness.getTableId());
                    node.setUsers(removeDuplicate(users));
                }else if("parallelGateway".equals(type)){
                    // 平行网关
                    node.setType(ActivitiConstant.NODE_TYPE_PG);
                }else if("endEvent".equals(type)){
                    // 结束
                    node.setType(ActivitiConstant.NODE_TYPE_END);
                }else{
                    throw new JeecgBootException("流程设计错误，包含无法处理的节点");
                }
                break;
            }
        }

        return node;
    }

    @Override
    public List<ActZprocess> queryNewestProcess(String processKey) {
        return baseMapper.selectNewestProcess(processKey);
    }
}
