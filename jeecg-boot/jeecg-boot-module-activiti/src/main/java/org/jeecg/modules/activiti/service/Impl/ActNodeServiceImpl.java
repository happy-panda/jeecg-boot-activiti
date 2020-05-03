package org.jeecg.modules.activiti.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.activiti.entity.ActNode;
import org.jeecg.modules.activiti.entity.Department;
import org.jeecg.modules.activiti.entity.Role;
import org.jeecg.modules.activiti.mapper.ActNodeMapper;
import org.jeecg.modules.activiti.service.IActNodeService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description: 流程节点扩展表
 * @Author: pmc
 * @Date:   2020-03-30
 * @Version: V1.0
 */
@Service
public class ActNodeServiceImpl extends ServiceImpl<ActNodeMapper, ActNode> implements IActNodeService {
    public List<String> getRoleByUserName(String username) {
        return this.baseMapper.getRoleByUserName(username);
    }
    public void deleteByNodeId(String id) {
        this.remove(new LambdaQueryWrapper<ActNode>().eq(ActNode::getNodeId,id));
    }

    public List<LoginUser> findUserByNodeId(String nodeId) {
        List<LoginUser> users = this.baseMapper.findUserByNodeId(nodeId);
        if (users.size()==0) users = Lists.newArrayList();
        return users;
    }

    public List<Role> findRoleByNodeId(String nodeId) {
        return this.baseMapper.findRoleByNodeId(nodeId);
    }

    public List<Department> findDepartmentByNodeId(String nodeId) {
        return this.baseMapper.findDepartmentByNodeId(nodeId);
    }

    public Boolean hasChooseDepHeader(String nodeId) {
        List<ActNode> listNode = findByNodeIdAndType(nodeId, 4);
        if(listNode!=null&&listNode.size()>0){
            return true;
        }
        return false;
    }
    public Boolean hasChooseSponsor(String nodeId) {
        List<ActNode> listNode = findByNodeIdAndType(nodeId, 3);
        if(listNode!=null&&listNode.size()>0){
            return true;
        }
        return false;
    }

    public List<ActNode> findByNodeIdAndType(String nodeId, int type) {
        return list(new LambdaQueryWrapper<ActNode>().eq(ActNode::getNodeId,nodeId).eq(ActNode::getType,type));
    }

    public List<String> findDepartmentIdsByNodeId(String nodeId) {
        List<Department> departmentByNodeId = this.findDepartmentByNodeId(nodeId);
        return departmentByNodeId.stream().map(Department::getId).distinct().collect(Collectors.toList());
    }

    public List<LoginUser> queryAllUser() {
        return this.baseMapper.queryAllUser();
    }

    public List<LoginUser> findUserByRoleId(String id) {
        return this.baseMapper.findUserByRoleId(id);
    }

    public List<LoginUser> findUserDepartmentId(String id) {
        return this.baseMapper.findUserDepartmentId(id);
    }
}
