
Jeecg-Boot 快速开发平台（前后端分离版本 之 activiti）
===============
- 基于 https://github.com/zhangdaiscott/jeecg-boot 开源版的activiti流程扩展
- 为什么不继续维护fork仓库？
>码云上项目搜索直接默认屏蔽fork项目，另外本人只提交issue，作者也不随便接受pull，维护fork仓库显得很呆，故而干脆自己新启一个仓库

- 当前已同步到版本： v.2.4.0  --2020年12月18日17:57:32之前   
  I am busy with my work recently. I will update it in a month

>主干文档就不在这里展示了，大家都是从主干那里到我这来的，对吧！本fork跟踪主干更新，会有少量修改，通常是为了更好的体验。要注意的是，**本fork完全不会管online模块，因为本人不会去使用任何在线功能**（原因不解释），可能会直接删掉一些相关文件。
如果介意，可自行copy我的代码，根据下述步骤自行整合到主干代码中。

-  本项目是自己用的，取之于开源，回报以开源罢了。不是大佬，不建群，不交流，需要交流留言，有时间就来看看，另外可以加jeecg的交流群，jeecg团队就在群里，有什么框架问题问他们更好，主干如果没有什么不可兼容的大变化，本项目都会保持同步。

>整合文档已说明，使用文档无，必要的说明写在代码注释中，只要对jeecgboot熟悉，有一定工作经验的人自己看懂怎么用很容易。如果是初学者，个人不建议研究jeecgboot，相对来说此项目有一定技术经验门槛。建议初学者去学习一下若依，为什么若依粉丝量那么多，也是这个道理。



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

- **流程模块预览**

![流程相关菜单](https://images.gitee.com/uploads/images/2020/0612/160424_2624efb9_1406033.png "屏幕截图.png")


## 捐赠

如果觉得还不错（本人觉得是相当不错，jeecg-boot+me 完胜淘宝上三百五百的破玩意。）  
一毛两毛都是爱，一块两块都是情，土豪随意 ☺

![](https://images.gitee.com/uploads/images/2020/0503/233715_925e2dc6_1406033.jpeg)
