package org.jeecg.modules.mongodb.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.config.shiro.ShiroRealm;
import org.jeecg.modules.mongodb.entity.SysFile;
import org.jeecg.modules.system.service.ISysFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 游攀利
 * @version V1.0
 * @Description 文件管理
 * @Date 2020-06-10
 */
@Slf4j
@Api(tags = "文件管理")
@RestController
@RequestMapping("/system/sysFile")
public class SysFileController extends JeecgController<SysFile, ISysFileService> {
    @Autowired
    private ISysFileService sysFileService;
    @Autowired
    private ShiroRealm shiroRealm;
    /**
     * 分页列表查询
     *
     * @param sysFile
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "文件管理-分页列表查询")
    @ApiOperation(value = "文件管理-分页列表查询", notes = "文件管理-分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(SysFile sysFile,
                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                   HttpServletRequest req) {
        QueryWrapper<SysFile> queryWrapper = QueryGenerator.initQueryWrapper(sysFile, req.getParameterMap());
        Page<SysFile> page = new Page<SysFile>(pageNo, pageSize);
        IPage<SysFile> pageList = sysFileService.page(page, queryWrapper);
        return Result.ok(pageList);
    }

    /**
     * 添加
     *
     * @param sysFile
     * @return
     */
    @AutoLog(value = "文件管理-添加")
    @ApiOperation(value = "文件管理-添加", notes = "文件管理-添加")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody SysFile sysFile) {
        sysFileService.save(sysFile);
        return Result.ok("添加成功！");
    }

    /**
     * 编辑
     *
     * @param sysFile
     * @return
     */
    @AutoLog(value = "文件管理-编辑")
    @ApiOperation(value = "文件管理-编辑", notes = "文件管理-编辑")
    @PutMapping(value = "/edit")
    public Result<?> edit(@RequestBody SysFile sysFile) {
        sysFileService.updateById(sysFile);
        return Result.ok("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "文件管理-通过id删除")
    @ApiOperation(value = "文件管理-通过id删除", notes = "文件管理-通过id删除")
    @GetMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {

        sysFileService.delete(id);
        return Result.ok("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "文件管理-批量删除")
    @ApiOperation(value = "文件管理-批量删除", notes = "文件管理-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.sysFileService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.ok("批量删除成功！");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "文件管理-通过id查询")
    @ApiOperation(value = "文件管理-通过id查询", notes = "文件管理-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        SysFile sysFile = sysFileService.getById(id);
        return Result.ok(sysFile);
    }


    /**
     * 根据业务记录ID查询所有属于此记录的附件
     *
     * @param businessId 业务记录ID
     */
    @AutoLog(value = "文件管理-通过businessId查询")
    @ApiOperation(value = "文件管理-通过businessId查询", notes = "文件管理-通过businessId查询")
    @GetMapping("/queryByBusinessId")
    @ResponseBody
    public Result<?> queryByBusinessId(@RequestParam(name = "businessId", required = true) String businessId) {

        List<SysFile> sysFileList = sysFileService.getBusinessId(businessId);
        return Result.ok(sysFileList);
    }


    /**
     * 附件（包含图片）上传，moduleCode根据模块名称来确定
     *
     * @param moduleCode 附件类型：projectInfo ； XXX;
     * @param businessId 业务表ID（某条记录的ID）
     * @param projectId  项目ID
     */
    @ApiOperation(notes = "upload attachment ", httpMethod = "POST", value = "上传附件")
    @PostMapping("/upload")
    @ResponseBody
    public Result<?> upload(String businessId, String fileType, String projectId, String moduleCode,
                            HttpServletRequest request) {
        Map<String, Object> resultMap = new HashMap<String, Object>();

        resultMap = sysFileService.upload(businessId, fileType, projectId, moduleCode, request);

        return Result.ok(resultMap);
    }


    /**
     * 附件（包含图片）下载
     *
     * @param id 附件id
     */
    @ApiOperation(notes = "download attachment ", httpMethod = "GET", value = "下载附件")
    @GetMapping("/download")
    @ResponseBody
    public void download(@RequestParam(name = "id", required = true) String id, HttpServletResponse response) {
        sysFileService.downLoad(id, response);
    }
    /**
     * 附件（包含图片）下载
     *
     * @param id 附件id
     */
    @ApiOperation(notes = "download attachment ", httpMethod = "GET", value = "下载附件")
    @GetMapping("/opendownload")
    @ResponseBody
    public void opendownload(@RequestParam(name = "id", required = true) String id, HttpServletRequest request,HttpServletResponse response) {
        String token = request.getParameter("token");
        LoginUser user = null;
        if (StrUtil.isNotBlank(token)){
            user = shiroRealm.checkUserTokenIsEffect(token);
        }
        //验证权限  此处可定义其他条件，比如某些文件不用用户登录亦可访问

        if (user!=null&&StrUtil.isNotBlank(user.getId())){
            sysFileService.downLoad(id, response);
        }
    }
    /**
     * 附件（包含图片）下载 文件流
     *
     * @param id 附件id
     */
    @ApiOperation(notes = "download attachment ", httpMethod = "GET", value = "下载附件")
    @GetMapping("/downLoadStatic")
    @ResponseBody
    public Result downLoadStatic(@RequestParam(name = "id", required = true) String id, HttpServletResponse response) {
        return sysFileService.downLoadStatic(id);
    }


    /**
     * 导出excel  未使用
     *
     * @param request
     * @param sysFile
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, SysFile sysFile) {
        return super.exportXls(request, sysFile, SysFile.class, "文件管理");
    }

    /**
     * 通过excel导入数据   未使用
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, SysFile.class);
    }

}
