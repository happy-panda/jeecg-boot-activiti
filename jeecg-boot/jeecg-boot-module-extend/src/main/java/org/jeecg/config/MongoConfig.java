package org.jeecg.config;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;


/**
 * @author 游攀利
 * @description: shiro 配置类
 * @Date: 2020-06-10
 */

@Slf4j
@Configuration
public class MongoConfig {
    //
    //@Resource
    //private MongoDbFactory mongoDbFactory;
//    @Resource
//    private GridFSBucket gridFSBucket;
@Autowired
private MongoTemplate mongoTemplate;

    @Bean
    public GridFSBucket getGridFSBuckets() {
        //MongoDatabase db = mongoDbFactory.getDb();
        MongoDatabase db1 = mongoTemplate.getDb();
        return GridFSBuckets.create(db1);
    }


}
