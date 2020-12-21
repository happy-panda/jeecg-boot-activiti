package org.jeecg.modules.mongodb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.mongodb.entity.SysFile;

import java.util.List;

/**
 * @author 游攀利
 * @version V1.0
 * @Description: 文件管理
 * @Date: 2020-06-10
 */
public interface SysFileMapper extends BaseMapper<SysFile> {

    public List<SysFile> getBusinessId(String businessId);
}
