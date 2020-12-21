package org.jeecg.modules.mongodb.service.impl;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.mongodb.entity.SysFile;
import org.jeecg.modules.mongodb.mapper.SysFileMapper;
import org.jeecg.modules.system.service.ISysFileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsCriteria;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

/**
 * @author 游攀利
 * @version V1.0
 * @Description: 文件管理
 * @Date: 2020-06-10
 */
@Service
public class SysFileServiceImpl extends ServiceImpl<SysFileMapper, SysFile> implements ISysFileService {


    /**
     * 文件最大容量MB
     */
    @Value("${jeecg.file.maxSize}")
    private Long fileMaxSize;
    /**
     * 文件本地文件夹
     */
    @Value("${jeecg.path.upload}")
    private String fileRootPath;

    @Resource
    private GridFsOperations gridOperations;


    @Resource
    private GridFsTemplate gridFsTemplate;

    @Resource
    private GridFSBucket gridFSBucket;

    @Resource
    private SysFileMapper sysFileMapper;

    @Override
    public List<SysFile> getBusinessId(String businessId) {
        return sysFileMapper.getBusinessId(businessId);
    }

    @Override
    @Transactional
    public Map<String, Object> upload(String businessId, String fileType, String projectId, String moduleCode,
                                      HttpServletRequest request) {

        Map<String, Object> resultMap = new HashMap<String, Object>();
        StringBuffer uploadMsg = new StringBuffer("上传文件错误");
        resultMap.put("uploadMsg", uploadMsg);

        MultipartHttpServletRequest mRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = mRequest.getFileMap();
        Iterator<Map.Entry<String, MultipartFile>> it = fileMap.entrySet().iterator();
        //附件id数组
        List<String> ids = new ArrayList<String>();
        List<String> fileNames = new ArrayList<String>();
        List<Long> fileSizes = new ArrayList<Long>();

        while (it.hasNext()) {
            Map.Entry<String, MultipartFile> entry = it.next();
            MultipartFile mFile = entry.getValue();//获取文件

            String filename = mFile.getOriginalFilename();

            if (StrUtil.isNotBlank(filename)) {
                //验证文件是否合法
                boolean isSuccess = validateFile(mFile, uploadMsg, fileMaxSize);
                if (!isSuccess) {
                    return resultMap;
                }

                //存储文件的额外信息，比如用户ID,后面要查询某个用户的所有文件时就可以直接查询
                DBObject metaData = new BasicDBObject();
                metaData.put("moduleCode", moduleCode);
                metaData.put("businessId", businessId);
                metaData.put("fileType", fileType);
                metaData.put("projectId", projectId);
                if (null == projectId) {
                    projectId = "";
                }
                metaData.put("projectId", projectId);

                String contentType = mFile.getContentType();
                InputStream inputStream = null;
                try {
                    inputStream = mFile.getInputStream();
                    ObjectId gridFSFile = gridOperations.store(inputStream, filename, contentType, metaData);
                    String id = gridFSFile.toString();
                    SysFile sysFile = new SysFile();
                    sysFile.setId(id);
                    sysFile.setBusinessId(businessId);
                    sysFile.setFileType(fileType);
                    sysFile.setProjectId(projectId);
                    sysFile.setModuleCode(moduleCode);
                    sysFile.setFileName(filename);
                    sysFile.setFileLength(mFile.getSize());
                    this.save(sysFile);

                    ids.add(id);
                    fileNames.add(filename);
                    fileSizes.add(mFile.getSize());
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        resultMap.put("ids", ids);
        resultMap.put("fileNames", fileNames);
        resultMap.put("fileSizes", fileSizes);
        uploadMsg.delete(0, uploadMsg.length()).append("文件上传成功");
        resultMap.put("uploadMsg", uploadMsg);
        return resultMap;

    }
    /**
     * 文件下载
     *
     * @param id 文件唯一标示id
     */
    @Override
    public void downLoad(String id, HttpServletResponse response) {

//        Query query = Query.query(Criteria.where("_id").is(new ObjectId(id)));
        Query query = Query.query(GridFsCriteria.where("_id").is(new ObjectId(id)));
        GridFSFile gridFSFile = gridFsTemplate.findOne(query);

        if (null != gridFSFile) {

            GridFSDownloadStream in = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
            GridFsResource resource = new GridFsResource(gridFSFile, in);

            OutputStream outputStream = null;
            try {
                InputStream inputStream = resource.getInputStream();

                outputStream = new BufferedOutputStream(response.getOutputStream());

                response.setContentType("application/octet-stream");
                // 获取原文件名
                String originName = gridFSFile.getFilename();
//                String fileName = java.net.URLEncoder.encode(originName, "UTF-8");
                // 设置下载文件名
                response.addHeader("Content-Disposition", "attachment; filename=" + originName);
                response.addHeader("Content-Length", "" + gridFSFile.getLength());
                //下载
                byte[] bytes = new byte[1024];
                while (inputStream.read(bytes) > 0) {
                    outputStream.write(bytes);

                }
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    /**
     * 下载文件，返回静态文件路径
     * */
    @Override
    public Result downLoadStatic(String id) {
        try {
            Query query = Query.query(GridFsCriteria.where("_id").is(new ObjectId(id)));
            GridFSFile gridFSFile = gridFsTemplate.findOne(query);
            GridFSDownloadStream in = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
            GridFsResource resource = new GridFsResource(gridFSFile, in);
            // 获取原文件名
            String originName = gridFSFile.getFilename();
            // 文件流
            InputStream inputStream = resource.getInputStream();
            String bizPath = "mogodb";
            String separator = File.separator;
            File file = new File(fileRootPath + separator + bizPath + separator);
            if (!file.exists()) {
                file.mkdirs();// 创建文件根目录
            }
            String savePath = file.getPath() + separator + originName;
            File savefile = new File(savePath);
            IoUtil.copy(inputStream,new FileOutputStream(savefile));
            /* 返回给前端的文件路径 */
            String fileUrl = separator + bizPath + separator + originName;
            // 文件大小
            long length = gridFSFile.getLength();
            HashMap<String, Object> fileMap = Maps.newHashMap();
            fileMap.put("fileUrl",fileUrl);
            fileMap.put("name",originName);
            fileMap.put("length",length);
            return Result.ok(fileMap);
        } catch (Exception e) {
            log.error("downLoadStatic",e);
            return Result.error(e.getMessage());
        }
    }


    /**
     * 文件删除
     *
     * @param id 文件唯一标示id
     */
    @Override
    @Transactional
    public void delete(String id) {
        this.removeById(id);
        Query query = Query.query(GridFsCriteria.where("_id").is(new ObjectId(id)));
        gridFsTemplate.delete(query);
    }


    /**
     * 验证文件合法性
     * 1. 大小
     * 2. 类型
     */
    public boolean validateFile(MultipartFile mFile, StringBuffer rtnMsg, Long fileMaxSize) {
        long fileSize = mFile.getSize();
        if (fileSize > (fileMaxSize * 1024 * 1024)) {
            rtnMsg.delete(0, rtnMsg.length()).append("文件大小不能超过" + fileMaxSize + "MB");
            return false;
        }
        return true;
    }


}
