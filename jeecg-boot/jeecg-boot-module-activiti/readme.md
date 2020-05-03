<h3>activiti 模块说明</h3>  ------- from pmc 
  
>使用**activiti5.22.0**版本
+ JeecgApplication.java   
修改注解  
```
@SpringBootApplication(exclude = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration.class
,org.activiti.spring.boot.SecurityAutoConfiguration.class
})
```


+ jeecg-boot/jeecg-boot-module-system/pom.xml 添加依赖
```xml
<dependency>
    <groupId>org.jeecgframework.boot</groupId>
    <artifactId>jeecg-boot-module-activiti</artifactId>
    <version>${jeecgboot.version}</version>
</dependency>
```
+ jeecg-boot/pom.xml  添加 
> <module>jeecg-boot-module-activiti</module>
```xml
<modules>
    <module>jeecg-boot-base-common</module>
    <module>jeecg-boot-module-system</module>
    <module>jeecg-boot-module-activiti</module>
</modules>
```
+ jeecg-boot/jeecg-boot-module-activiti/db/ 文件夹内数据库初始化文件
> 其他，可网上搜索activiti 5.22.0 版本

+ ShiroConfig.java  
添加过滤路径
```
//activiti
filterChainDefinitionMap.put("/activiti/**", "anon");
filterChainDefinitionMap.put("/diagram-viewer/**", "anon");
filterChainDefinitionMap.put("/editor-app/**", "anon");
```
+ 启动


>功能齐备，使用方法自己研究，正常开发者看看代码就应该会，看不会我解释起来也费劲。


