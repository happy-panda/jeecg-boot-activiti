###介绍
>封装百度echarts，只需专注于option，dom窗口大小变化图形自动刷新大小
####依赖
>yarn add echarts  
>yarn add element-resize-detector
###使用方式说明
- 引入组件  
`
import BEcharts from '@/components/append/BEcharts/BEcharts.vue'  
`
- 声明  
`
components: { BEcharts },
`
- html  
`<b-echarts :option="option" @click="click" style="height: 300px;width: 100%"></b-echarts>
     `
- 属性  
option ：百度echarts属性参数  
theme ：主题，默认 light

- 方法  
click ：点击事件 返回object