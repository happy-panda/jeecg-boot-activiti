package org.jeecg.modules.activiti.web;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ModelQuery;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.image.ProcessDiagramGenerator;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.activiti.entity.ActZprocess;
import org.jeecg.modules.activiti.service.Impl.ActZprocessServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/activiti/models")
@Slf4j
public class ActivitiModelController {
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private ProcessEngineConfiguration processEngineConfiguration;
    @Autowired
    private TaskService taskService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ActZprocessServiceImpl actZprocessService;

    @RequestMapping("/modelListData")
    @ResponseBody
    public Result modelListData( HttpServletRequest request){
        log.info("-------------模型列表-------------");
        ModelQuery modelQuery = repositoryService.createModelQuery();
        String keyWord = request.getParameter("keyWord");//搜索关键字
        if (StrUtil.isNotBlank(keyWord)){
            modelQuery.modelNameLike("%"+keyWord+"%");
        }
        List<Model> models = modelQuery.orderByCreateTime().desc().list();

        return Result.ok(models);
    }

    @RequestMapping("/create")
    public void newModel(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
       try {
           //初始化一个空模型
           Model model = repositoryService.newModel();
           //设置一些默认信息
           int revision = 1;
           String name = request.getParameter("name");
           String description = request.getParameter("description");
           String key = request.getParameter("key");
           if (StrUtil.isBlank(name)){
               name = "new-process";
           }
           if (StrUtil.isBlank(description)){
               description = "description";
           }
           if (StrUtil.isBlank(key)){
               key = "processKey";
           }


           ObjectNode modelNode = objectMapper.createObjectNode();
           modelNode.put(ModelDataJsonConstants.MODEL_NAME, name);
           modelNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, description);
           modelNode.put(ModelDataJsonConstants.MODEL_REVISION, revision);

           model.setName(name);
           model.setKey(key);
           model.setMetaInfo(modelNode.toString());

           repositoryService.saveModel(model);
           String id = model.getId();

           //完善ModelEditorSource
           ObjectNode editorNode = objectMapper.createObjectNode();
           editorNode.put("id", "canvas");
           editorNode.put("resourceId", "canvas");
           ObjectNode stencilSetNode = objectMapper.createObjectNode();
           stencilSetNode.put("namespace",
                   "http://b3mn.org/stencilset/bpmn2.0#");
           editorNode.put("stencilset", stencilSetNode);
           repositoryService.addModelEditorSource(id, editorNode.toString().getBytes("utf-8"));
           response.sendRedirect(request.getContextPath() + "/activiti/modeler.html?modelId=" + id);
       }catch (IOException e){
           e.printStackTrace();
           log.info("模型创建失败！");
       }

    }

    @RequestMapping("/delete/{id}")
    public @ResponseBody Result deleteModel(@PathVariable("id")String id){
        repositoryService.deleteModel(id);
        return Result.ok("删除成功。");
    }


    @RequestMapping("/deployment/{id}")
    public @ResponseBody Result deploy(@PathVariable("id")String id) {

        // 获取模型
        Model modelData = repositoryService.getModel(id);
        byte[] bytes = repositoryService.getModelEditorSource(modelData.getId());

        if (bytes == null) {
            return Result.error("模型数据为空，请先成功设计流程并保存");
        }

        try {
            JsonNode modelNode = new ObjectMapper().readTree(bytes);

            BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
            if(model.getProcesses().size()==0){
                return Result.error("模型不符要求，请至少设计一条主线流程");
            }
            byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(model);

            // 部署发布模型流程
            String processName = modelData.getName() + ".bpmn20.xml";
            Deployment deployment = repositoryService.createDeployment()
                    .name(modelData.getName())
                    .addString(processName, new String(bpmnBytes, "UTF-8"))
                    .deploy();
            String metaInfo = modelData.getMetaInfo();
            JSONObject metaInfoMap = JSON.parseObject(metaInfo);
            // 设置流程分类 保存扩展流程至数据库
            List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).list();
            for (ProcessDefinition pd : list) {
                ActZprocess actZprocess = new ActZprocess();
                actZprocess.setId(pd.getId());
                actZprocess.setName(modelData.getName());
                actZprocess.setProcessKey(modelData.getKey());
                actZprocess.setDeploymentId(deployment.getId());
                actZprocess.setDescription(metaInfoMap.getString(ModelDataJsonConstants.MODEL_DESCRIPTION));
                actZprocess.setVersion(pd.getVersion());
                actZprocess.setDiagramName(pd.getDiagramResourceName());
                actZprocessService.setAllOldByProcessKey(modelData.getKey());
                actZprocess.setLatest(true);
                actZprocessService.save(actZprocess);
            }
        }catch (Exception e){
            String err = e.toString();
            log.error(e.getMessage(),e);
            if (err.indexOf("NCName")>-1){
                return Result.error("部署失败：流程设计中的流程名称不能为空，不能为数字以及特殊字符开头！");
            }
            if (err.indexOf("PRIMARY")>-1){
                return Result.error("部署失败：该模型已发布，key唯一！");
            }
            return Result.error("部署失败！");
        }

        return Result.ok("部署成功");
    }
    /*获取高亮实时流程图*/
    @RequestMapping(value = "/getHighlightImg/{id}", method = RequestMethod.GET)
    public void getHighlightImg(@PathVariable String id, HttpServletResponse response){
        InputStream inputStream = null;
        ProcessInstance pi = null;
        String picName = "";
        // 查询历史
        HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery().processInstanceId(id).singleResult();
        if (hpi.getEndTime() != null) {
            // 已经结束流程获取原图
            ProcessDefinition pd = repositoryService.createProcessDefinitionQuery().processDefinitionId(hpi.getProcessDefinitionId()).singleResult();
            picName = pd.getDiagramResourceName();
            inputStream = repositoryService.getResourceAsStream(pd.getDeploymentId(), pd.getDiagramResourceName());
        } else {
            pi = runtimeService.createProcessInstanceQuery().processInstanceId(id).singleResult();
            BpmnModel bpmnModel = repositoryService.getBpmnModel(pi.getProcessDefinitionId());

            List<String> highLightedActivities = new ArrayList<String>();
            // 高亮任务节点
            List<Task> tasks = taskService.createTaskQuery().processInstanceId(id).list();
            for (Task task : tasks) {
                highLightedActivities.add(task.getTaskDefinitionKey());
            }

            List<String> highLightedFlows = new ArrayList<String>();
            ProcessDiagramGenerator diagramGenerator = processEngineConfiguration.getProcessDiagramGenerator();
            //"宋体"
            inputStream = diagramGenerator.generateDiagram(bpmnModel, "png", highLightedActivities, highLightedFlows,
                    "宋体", "宋体", "宋体",null, 1.0);
            picName = pi.getName()+".png";
        }
        try {
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(picName, "UTF-8"));
            byte[] b = new byte[1024];
            int len = -1;
            while ((len = inputStream.read(b, 0, 1024)) != -1) {
                response.getOutputStream().write(b, 0, len);
            }
            response.flushBuffer();
        } catch (IOException e) {
            log.error(e.toString());
            throw new JeecgBootException("读取流程图片失败");
        }
    }
    /**导出部署流程资源*/
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void exportResource(@RequestParam String id, HttpServletResponse response){

        ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(id).singleResult();

        String resourceName = pd.getDiagramResourceName();
        InputStream inputStream = repositoryService.getResourceAsStream(pd.getDeploymentId(),
                resourceName);

        try {
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(resourceName, "UTF-8"));
            byte[] b = new byte[1024];
            int len = -1;
            while ((len = inputStream.read(b, 0, 1024)) != -1) {
                response.getOutputStream().write(b, 0, len);
            }
            response.flushBuffer();
        } catch (IOException e) {
            log.error(e.toString());
        }
    }

}
