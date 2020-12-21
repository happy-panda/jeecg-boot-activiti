package org.jeecg.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.mongodb.entity.SysFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * @author 游攀利
 * @version V1.0
 * @Description: 文件管理
 * @Date: 2020-06-10
 */
public interface ISysFileService extends IService<SysFile> {

    List<SysFile> getBusinessId(String businessId);

    Map<String, Object> upload(String businessId, String fileType, String projectId, String moduleCode,
                               HttpServletRequest request);


    void downLoad(String id, HttpServletResponse response);

    Result downLoadStatic(String id);

    void delete(String id);
}
