import {filterObj} from '@/utils/util';
import {deleteAction, downFile, getAction} from '@/api/manage'
import Vue from 'vue'
import {ACCESS_TOKEN} from "@/store/mutation-types"
import JEllipsis from '@/components/jeecg/JEllipsis'

export const activitiMixin = {
  components: {
    JEllipsis
  },
  data(){
    return {
      //token header
      tokenHeader: {'X-Access-Token': Vue.ls.get(ACCESS_TOKEN)},
    }
  },
  computed:{
    /*todo 所有的流程表单，组件化注册，在此维护*/
    allFormComponent:function(){
      return [
          {
            text:'示例表单',
            routeName:'@/views/activiti/form/demoForm',
            component:() => import(`@/views/activiti/form/demoForm`),
            businessTable:'test_demo'
          }
      ]
    },
    historicDetail:function () {
      return () => import(`@/views/activiti/historicDetail`)
    }
  },
  methods:{
    getFormComponent(routeName){
      return _.find(this.allFormComponent,{routeName:routeName})||{};
    },
    millsToTime(mills) {
      if (!mills) {
        return "";
      }
      let s = mills / 1000;
      if (s < 60) {
        return s.toFixed(0) + " 秒"
      }
      let m = s / 60;
      if (m < 60) {
        return m.toFixed(0) + " 分钟"
      }
      let h = m / 60;
      if (h < 24) {
        return h.toFixed(0) + " 小时"
      }
      let d = h / 24;
      if (d < 30) {
        return d.toFixed(0) + " 天"
      }
      let month = d / 30
      if (month < 12) {
        return month.toFixed(0) + " 个月"
      }
      let year = month / 12
      return year.toFixed(0) + " 年"

    },
    handleTableChange(pagination, filters, sorter) {
      //分页、排序、筛选变化时触发
      //TODO 筛选
      if (Object.keys(sorter).length > 0) {
        this.isorter.column = sorter.field;
        this.isorter.order = "ascend" == sorter.order ? "asc" : "desc"
      }
      this.ipagination = pagination;
      // this.loadData();
    },
    //根据key 获取流程定义数据
    getProcessDefByKey(key){
      let procDef = null;
      let newestProcessList = JSON.parse(window.sessionStorage.getItem("newestProcessList"));
      newestProcessList.forEach(function(item) {
        if(item.processKey === key){
          procDef = item;
        }
      });
      return procDef;
    },
  }

}