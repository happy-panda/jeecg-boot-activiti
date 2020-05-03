
Jeecg-Boot 快速开发平台（前后端分离版本 之 activiti）
===============
基于 https://github.com/zhangdaiscott/jeecg-boot 开源版的activiti流程扩展  
当前已同步到版本： 2.2.0  
>主干文档就不在这里展示了，大家都是从主干那里到我这来的，对吧！本fork跟踪主干更新，会有少量修改，通常是为了更好的体验。值得一提的是，本fork完全不会管online模块，因为本人不会去使用任何在线功能（原因不解释），可能会直接删掉一些相关文件。
如果介意，可自行copy我的代码，根据下述步骤自行整合到主干代码中。
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


>功能齐备，使用方法自己研究，正常开发者看看代码就应该会，看不会我解释起来也会挺费劲的。通过流程模块的七个菜单走查代码即可（看不见菜单，请先执行activiti模块包下的相关增量sql，再给菜单授权即可。），代码简单易读，一年经验的开发者读懂毫无问题！




## 捐赠 

如果觉得还不错，一毛两毛都是爱，一块两块都是情，土豪随意 ☺

![](https://images.gitee.com/uploads/images/2020/0503/232933_b1ee3efb_1406033.jpeg)
