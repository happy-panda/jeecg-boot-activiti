package org.jeecg.modules.activiti.service.Impl;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.activiti.entity.ActBusiness;
import org.jeecg.modules.activiti.mapper.ActBusinessMapper;
import org.jeecg.modules.activiti.service.IActBusinessService;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @Description: 流程业务扩展表
 * @Author: pmc
 * @Date:   2020-03-30
 * @Version: V1.0
 */
@Service
public class ActBusinessServiceImpl extends ServiceImpl<ActBusinessMapper, ActBusiness> implements IActBusinessService {

    public List<ActBusiness> findByProcDefId(String id) {
       return this.list(new LambdaQueryWrapper<ActBusiness>().eq(ActBusiness::getProcDefId,id));
    }
    /**保存业务表单数据到数据库表
     * <br>该方法相对通用，复杂业务单独定制，套路类似
     * @param tableId 业务表中的数据id
     * */
    public void saveApplyForm(String tableId, HttpServletRequest request) {
        String tableName = request.getParameter("tableName");
        String filedNames = request.getParameter("filedNames");
        Map<String, Object> busiData = this.baseMapper.getBusiData(tableId, tableName);
        String[] fileds = filedNames.split(",");
        if (MapUtil.isEmpty(busiData)){ //没有，新增逻辑
            StringBuilder filedsB = new StringBuilder("id");
            StringBuilder filedsVB = new StringBuilder("'"+tableId+"'");
            for (String filed : fileds) {
                filedsB.append(","+filed);
                filedsVB.append(",'"+request.getParameter(filed)+"'");
            }
            this.baseMapper.insertBusiData(String.format("INSERT INTO %s (%s) VALUES (%s)",tableName,filedsB.toString(),filedsVB.toString()));
        }else { //有，修改
            StringBuilder setSql = new StringBuilder();
            for (String filed : fileds) {
                String parameter = request.getParameter(filed);
                if (parameter==null){
                    setSql.append(String.format("%s = null,",filed));
                }else {
                    setSql.append(String.format("%s = '%s',",filed, parameter));
                }
            }
            String substring = setSql.substring(0, setSql.length()-1);//去掉最后一个,号
            this.baseMapper.insertBusiData(String.format("update %s set %s where id = '%s'",tableName,substring,tableId));
        }

    }

    public Map<String, Object> getApplyForm(String tableId, String tableName) {
        return this.baseMapper.getBusiData(tableId, tableName);
    }

    public void deleteBusiness(String tableName, String tableId) {
        this.baseMapper.deleteBusiData(tableId,tableName);
    }
    /**
     *通过类型和任务id查找用户id
     * */
    public String findUserIdByTypeAndTaskId(String type, String taskId) {
        return baseMapper.findUserIdByTypeAndTaskId(type, taskId);
    }

    public void insertHI_IDENTITYLINK(String id, String type, String userId, String taskId, String procInstId) {
        this.baseMapper.insertHI_IDENTITYLINK(id, type, userId, taskId, procInstId);
    }

    public List<String> selectIRunIdentity(String taskId, String type) {
       return baseMapper.selectIRunIdentity(taskId,type);
    }
}
