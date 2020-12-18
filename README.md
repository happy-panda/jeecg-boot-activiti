


JEECG BOOT 低代码开发平台（前后端分离版本）
===============

当前最新版本： 2.4.0（发布日期：2020-12-01）


技术文档
-----------------------------------

- 技术官网：  [http://www.jeecg.com](http://www.jeecg.com)

- 开发文档：  [http://doc.jeecg.com](http://doc.jeecg.com)

- 微服务启动：  [单体升级为微服务启动文档2.4+](http://doc.jeecg.com/2043906)

- 在线演示 ： [http://boot.jeecg.com](http://boot.jeecg.com)

- 视频教程  ：[JeecgBoot入门视频](http://www.jeecg.com/doc/video)

- 常见问题：  [入门常见问题Q&A](http://jeecg.com/doc/qa)

- 更新日志：  [版本日志](http://www.jeecg.com/doc/log)


 
技术架构：
-----------------------------------
#### 开发环境

- 语言：Java 8

- IDE(JAVA)： IDEA / Eclipse安装lombok插件 

- IDE(前端)： WebStorm 或者 IDEA

- 依赖管理：Maven

- 数据库：MySQL5.7+  &  Oracle 11g & Sqlserver2017

- 缓存：Redis


#### 后端
- 基础框架：Spring Boot 2.3.5.RELEASE

- 微服务框架： Spring Cloud Alibaba 2.2.3.RELEASE

- 持久层框架：Mybatis-plus 3.4.1

- 安全框架：Apache Shiro 1.7.0，Jwt 3.11.0

- 微服务技术栈：Spring Cloud Alibaba、Nacos、Gateway、Sentinel、Skywarking

- 数据库连接池：阿里巴巴Druid 1.1.22

- 缓存框架：redis

- 日志打印：logback

- 其他：fastjson，poi，Swagger-ui，quartz, lombok（简化代码）等。


#### 前端
 
- [Vue 2.6.10](https://cn.vuejs.org/),[Vuex](https://vuex.vuejs.org/zh/),[Vue Router](https://router.vuejs.org/zh/)
- [Axios](https://github.com/axios/axios)
- [ant-design-vue](https://vuecomponent.github.io/ant-design-vue/docs/vue/introduce-cn/)
- [webpack](https://www.webpackjs.com/),[yarn](https://yarnpkg.com/zh-Hans/)
- [vue-cropper](https://github.com/xyxiao001/vue-cropper) - 头像裁剪组件
- [@antv/g2](https://antv.alipay.com/zh-cn/index.html) - Alipay AntV 数据可视化图表
- [Viser-vue](https://viserjs.github.io/docs.html#/viser/guide/installation)  - antv/g2 封装实现
- eslint，[@vue/cli 3.2.1](https://cli.vuejs.org/zh/guide)
- vue-print-nb - 打印





### 功能模块
```
├─系统管理
│  ├─用户管理
│  ├─角色管理
│  ├─菜单管理
│  ├─权限设置（支持按钮权限、数据权限）
│  ├─表单权限（控制字段禁用、隐藏）
│  ├─部门管理
│  ├─我的部门（二级管理员）
│  └─字典管理
│  └─分类字典
│  └─系统公告
│  └─职务管理
│  └─通讯录
│  └─多租户管理
├─消息中心
│  ├─消息管理
│  ├─模板管理
├─代码生成器(低代码)
│  ├─代码生成器功能（一键生成前后端代码，生成后无需修改直接用，绝对是后端开发福音）
│  ├─代码生成器模板（提供4套模板，分别支持单表和一对多模型，不同风格选择）
│  ├─代码生成器模板（生成代码，自带excel导入导出）
│  ├─查询过滤器（查询逻辑无需编码，系统根据页面配置自动生成）
│  ├─高级查询器（弹窗自动组合查询条件）
│  ├─Excel导入导出工具集成（支持单表，一对多 导入导出）
│  ├─平台移动自适应支持
├─系统监控
│  ├─Gateway路由网关
│  ├─性能扫描监控
│  │  ├─监控 Redis
│  │  ├─Tomcat
│  │  ├─jvm
│  │  ├─服务器信息
│  │  ├─请求追踪
│  │  ├─磁盘监控
│  ├─定时任务
│  ├─系统日志
│  ├─消息中心（支持短信、邮件、微信推送等等）
│  ├─数据日志（记录数据快照，可对比快照，查看数据变更情况）
│  ├─系统通知
│  ├─SQL监控
│  ├─swagger-ui(在线接口文档)
│─报表示例
│  ├─曲线图
│  └─饼状图
│  └─柱状图
│  └─折线图
│  └─面积图
│  └─雷达图
│  └─仪表图
│  └─进度条
│  └─排名列表
│  └─等等
│─大屏模板
│  ├─作战指挥中心大屏
│  └─物流服务中心大屏
│─常用示例
│  ├─自定义组件
│  ├─对象存储(对接阿里云)
│  ├─JVXETable示例（各种复杂ERP布局示例）
│  ├─单表模型例子
│  └─一对多模型例子
│  └─打印例子
│  └─一对多TAB例子
│  └─内嵌table例子
│  └─常用选择组件
│  └─异步树table
│  └─接口模拟测试
│  └─表格合计示例
│  └─异步树列表示例
│  └─一对多JEditable
│  └─JEditable组件示例
│  └─图片拖拽排序
│  └─图片翻页
│  └─图片预览
│  └─PDF预览
│  └─分屏功能
│─封装通用组件	
│  ├─行编辑表格JEditableTable
│  └─省略显示组件
│  └─时间控件
│  └─高级查询
│  └─用户选择组件
│  └─报表组件封装
│  └─字典组件
│  └─下拉多选组件
│  └─选人组件
│  └─选部门组件
│  └─通过部门选人组件
│  └─封装曲线、柱状图、饼状图、折线图等等报表的组件（经过封装，使用简单）
│  └─在线code编辑器
│  └─上传文件组件
│  └─验证码组件
│  └─树列表组件
│  └─表单禁用组件
│  └─等等
│─更多页面模板
│  ├─各种高级表单
│  ├─各种列表效果
│  └─结果页面
│  └─异常页面
│  └─个人页面
├─高级功能
│  ├─系统编码规则
│  ├─提供单点登录CAS集成方案
│  ├─提供APP发布方案
│  ├─集成Websocket消息通知机制
├─Online在线开发(低代码)
│  ├─Online在线表单 - 功能已开放
│  ├─Online代码生成器 - 功能已开放
│  ├─Online在线报表 - 功能已开放
│  ├─Online在线图表(暂不开源)
│  ├─Online图表模板配置(暂不开源)
│  ├─Online布局设计(暂不开源)
│  ├─多数据源管理 - 功能已开放
├─积木报表设计器(低代码)
│  ├─打印设计器
│  ├─数据报表设计
│  ├─图形报表设计（支持echart）
│  ├─大屏设计器(暂不开源)
│─流程模块功能 (暂不开源)
│  ├─流程设计器
│  ├─在线表单设计
│  └─我的任务
│  └─历史流程
│  └─历史流程
│  └─流程实例管理
│  └─流程监听管理
│  └─流程表达式
│  └─我发起的流程
│  └─我的抄送
│  └─流程委派、抄送、跳转
│  └─。。。
└─其他模块
   └─更多功能开发中。。
   
```

## 微服务整体解决方案(2.4+版本)


1、服务注册和发现 Nacos √

2、统一配置中心 Nacos  √

3、路由网关 gateway(三种加载方式) √

4、分布式 http feign √

5、熔断和降级 Sentinel √

6、分布式文件 Minio、阿里OSS √ 

7、统一权限控制 JWT + Shiro √

8、服务监控 SpringBootAdmin√

9、链路跟踪 Skywarking   [参考文档](https://www.kancloud.cn/zhangdaiscott/jeecgcloud/1771670)

10、消息中间件 RabbitMQ  √

11、分布式任务 xxl-job  √ 

12、分布式事务 Seata

13、分布式日志 elk + kafa

14、支持 docker-compose、k8s、jenkins

15、CAS 单点登录   √

16、路由限流   √

   
#### 微服务架构图
![微服务架构图](https://jeecgos.oss-cn-beijing.aliyuncs.com/files/jeecgboot-weifuwu-cloud.png "在这里输入图片标题")

### Jeecg Boot 产品功能蓝图
![功能蓝图](https://static.jeecg.com/upload/test/Jeecg-Boot-lantu202005_1590912449914.jpg "在这里输入图片标题")


后台开发环境和依赖
----
- java
- maven
- jdk8
- mysql
- redis
- 数据库脚本：jeecg-boot/db/jeecgboot-mysql-5.7.sql
- 默认登录账号： admin/123456


前端开发环境和依赖
----
- node
- yarn
- webpack
- eslint
- @vue/cli 3.2.1
- [ant-design-vue](https://github.com/vueComponent/ant-design-vue) - Ant Design Of Vue 实现
- [vue-cropper](https://github.com/xyxiao001/vue-cropper) - 头像裁剪组件
- [@antv/g2](https://antv.alipay.com/zh-cn/index.html) - Alipay AntV 数据可视化图表
- [Viser-vue](https://viserjs.github.io/docs.html#/viser/guide/installation)  - antv/g2 封装实现
- [jeecg-boot-angular 版本](https://gitee.com/dangzhenghui/jeecg-boot)

项目下载和运行
----

- 拉取项目代码
```bash
git clone https://github.com/zhangdaiscott/jeecg-boot.git
cd  jeecg-boot/ant-design-jeecg-vue
```

1. 安装node.js
2. 切换到ant-design-jeecg-vue文件夹下
```
# 安装yarn
npm install -g yarn

# 下载依赖
yarn install

# 启动
yarn run serve

# 编译项目
yarn run build

# Lints and fixes files
yarn run lint
```






其他说明
----

- 项目使用的 [vue-cli3](https://cli.vuejs.org/guide/), 请更新您的 cli

- 关闭 Eslint (不推荐) 移除 `package.json` 中 `eslintConfig` 整个节点代码

- 修改 Ant Design 配色，在文件 `vue.config.js` 中，其他 less 变量覆盖参考 [ant design](https://ant.design/docs/react/customize-theme-cn) 官方说明
```ecmascript 6
  css: {
    loaderOptions: {
      less: {
        modifyVars: {
          /* less 变量覆盖，用于自定义 ant design 主题 */

          'primary-color': '#F5222D',
          'link-color': '#F5222D',
          'border-radius-base': '4px',
        },
        javascriptEnabled: true,
      }
    }
  }
```



附属文档
----
- [Ant Design Vue](https://www.antdv.com/docs/vue/introduce-cn)

- [报表 viser-vue](https://viserjs.github.io/demo.html#/viser/line/basic-line)

- [Vue](https://cn.vuejs.org/v2/guide)

- [路由/菜单说明](https://gitee.com/jeecg/jeecg-boot/tree/v1.1/ant-design-jeecg-vue/src/router/README.md)

- [ANTD 默认配置项](https://gitee.com/jeecg/jeecg-boot/blob/v1.1/ant-design-jeecg-vue/src/defaultSettings.js)

- 其他待补充...


