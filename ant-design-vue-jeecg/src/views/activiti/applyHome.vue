<template>
  <a-card :bordered="false">
    <!--流程申请选择-->
    <a-input-search style="margin-bottom: 10px;margin-right:10px;width: 200px" v-model="searchProcessKey"
                    placeholder="输入流程名称" @search="onSearchProcess" />
    <a-button @click="onSearchProcess(searchProcessKey)" type="primary">查询</a-button>
    <a-button @click="onSearchProcess('')">重置</a-button>
    <a-button @click="handleToApplyList" type="primary" style="float: right;">前往我的申请列表</a-button>
      <a-empty description="无流程可供选择" v-if="activeKeyAll.length==0" />
      <div v-else>
        <a-collapse v-model="activeKey">
          <a-collapse-panel v-for="(value, index)  in activeKeyAll" :header="filterDictText(dictOptions,value)||'未分类'" :key="value">
            <a-list :grid="{ gutter: 10,column:4}" :dataSource="processDataMap[value]">
              <a-list-item slot="renderItem" slot-scope="item">
                <a-card>
                  <div slot="title">
                    <a-row>
                      <a-col span="12" :title="item.name">{{item.name}} </a-col>
                      <a-col span="12" style="text-align: right;">
                        <a href="javascript:void (0)" @click="chooseProcess(item)">发起申请</a>
                      </a-col>
                    </a-row>
                  </div>
                  <b>版本：</b>v.{{item.version}}
                  <br/>
                  <b>说明：</b>{{item.description}}
                </a-card>
              </a-list-item>
            </a-list>
          </a-collapse-panel>
        </a-collapse>
      </div>
    <!--流程表单-->
    <a-modal :destroyOnClose="true" :title="lcModa.title" v-model="lcModa.visible" :footer="null" :maskClosable="false" width="80%">
      <component :disabled="lcModa.disabled" v-if="lcModa.visible" :is="lcModa.formComponent"
                 :processData="lcModa.processData" :isNew = "lcModa.isNew"
                 @afterSubmit="afterSub" @close="lcModa.visible=false,lcModa.disabled = false"></component>
    </a-modal>
  </a-card>

</template>

<script>
  import { activitiMixin } from '@/views/activiti/mixins/activitiMixin'
  import JEllipsis from '@/components/jeecg/JEllipsis'
  import JTreeSelect from '@/components/jeecg/JTreeSelect'
  import {initDictOptions, filterDictText} from '@/components/dict/JDictSelectUtil'
  import historicDetail from '@/views/activiti/historicDetail'
  export default {
    name: "applyHome",
    mixins:[activitiMixin],
    components: {
      JEllipsis
      ,JTreeSelect
      ,historicDetail
    },
    data () {
      return {
        description: '所有',
        dictOptions:[],
        url: {
          getProcessDataList: "/activiti_process/listData",
          getFirstNode:'/actProcessIns/getFirstNode',
          applyBusiness:'/actBusiness/apply',
        },
        // 查询条件
        queryParam: {
          createTimeRange:[],
          keyWord:'',
        },
        // 表头
        labelCol: {
          xs: { span: 4 },
          sm: { span: 4 },
        },
        wrapperCol: {
          xs: { span: 20 },
          sm: { span: 20 },
        },
        processModalVisible: null,
        activeKeyAll: [],
        activeKey: [],
        processDataMap: {},
        searchProcessKey: null,
        addApplyLoading: false,
        lcModa: {
          title:'',
          disabled:false,
          visible:false,
          formComponent : null,
          isNew : false
        },
      }
    },
    computed:{
    },
    mounted() {
      this.initDictConfig()
      this.getProcessList()
    },
    methods: {

      initDictConfig() {
        //初始化字典 - 流程分类
        initDictOptions('bpm_process_type').then((res) => {
          if (res.success) {
            this.dictOptions = res.result;
          }
        });
      },
      filterDictText(dictOptions, text) {
        if (dictOptions instanceof Array) {
          for (let dictItem of dictOptions) {
            if (text === dictItem.value) {
              return dictItem.text
            }
          }
        }
        return text||text=='null'?'':text
      },
      /*加载流程列表*/
      getProcessList() {
        this.addApplyLoading = true;
        this.getAction(this.url.getProcessDataList,{status:1,roles:true}).then(res => {
          this.activeKeyAll = [];
          if (res.success) {
            var result = res.result||[];
            if (result.length>0){
              let searchProcessKey = this.searchProcessKey;
              if (searchProcessKey){ //过滤条件
                result = _.filter(result, function(o) { return o.name.indexOf(searchProcessKey)>-1; });
              }
              this.processDataMap = _.groupBy(result,'categoryId');
              for (const categoryId in this.processDataMap) {
                this.activeKeyAll.push(categoryId)
              }
              this.activeKey = this.activeKeyAll;
            }
            this.processModalVisible = true;
          }else {
            this.$message.warning(res.message)
          }
        }).finally(()=>this.addApplyLoading = false);
      },
      onSearchProcess(value) {
        this.searchProcessKey = value;
        this.getProcessList()
      },
      chooseProcess(v) {
        if (!v.routeName) {
          this.$message.warning(
            "该流程信息未配置表单，请联系开发人员！"
          );
          return;
        }
        this.lcModa.formComponent = this.getFormComponent(v.routeName).component;
        this.lcModa.title = '发起流程：'+v.name;
        this.lcModa.isNew = true;
        this.lcModa.processData = v;
        this.lcModa.visible = true;
        console.log("发起",v)
      },
      /*提交成功申请后*/
      afterSub(formData){
          this.lcModa.visible = false;
          this.$message("请前往我的申请列表提交审批！")
      },
      /*前往我的申请页面*/
      handleToApplyList() {
        this.$router.push({path:'/activiti/applyList'})
      }
    }
  }
</script>
<style scoped>
  @import '~@assets/less/common.less';
</style>