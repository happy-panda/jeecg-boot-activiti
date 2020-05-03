<style lang="less">
  @import '~@assets/less/common.less';

</style>
<template>
  <div class="search">
    <a-card>
      <div class="table-page-search-wrapper">
        <a-form layout="inline" @keyup.enter.native="handleSearch">
          <a-row :gutter="24">
            <a-col :md="6" :sm="8">
              <a-form-item label="流程名称" prop="name">
                <a-input
                  type="text"
                  v-model="searchForm.name"
                  placeholder="请输入"
                  clearable
                />
              </a-form-item>
            </a-col>
            <a-col :md="6" :sm="8">
              <a-form-item label="标识Key" prop="name">
                <a-input
                  type="text"
                  v-model="searchForm.key"
                  placeholder="请输入"
                  clearable
                />
              </a-form-item>
            </a-col>
            <a-col :md="6" :sm="8">
              <a-form-item label="结束时间">
                <a-range-picker
                  v-model="selectDate"
                  format="YYYY-MM-DD"
                  clearable
                  @change="selectDateRange"
                ></a-range-picker>
              </a-form-item>
            </a-col>
            <span style="float: left;overflow: hidden;" class="table-page-search-submitButtons">
            <a-col :md="6" :sm="12" >
              <a-button @click="handleSearch" type="primary" icon="search">搜索</a-button>
              <a-button @click="handleReset" style="margin-left: 10px;">重置</a-button>
            </a-col>
          </span>
          </a-row>
        </a-form>
      </div>
      <a-row>
        <a-table :scroll="{x:1680,y:innerHeight/2}" bordered
          :loading="loading"
          rowKey="id"
          :dataSource="data"
          :pagination="ipagination"
          @change="handleTableChange"
          ref="table"
        >
          <a-table-column title="#"  :width="50" fixed="left">
            <template slot-scope="t,r,i" >
              <span> {{i+1}} </span>
            </template>
          </a-table-column>
          <a-table-column title="流程实例ID" dataIndex="id" :width="150" fixed="left">
            <template slot-scope="t,r,i" >
              <span> {{t}} </span>
            </template>
          </a-table-column>
          <a-table-column title="流程名称" dataIndex="name" :width="150" fixed="left">
            <template slot-scope="t,r,i" >
              <span> {{t}} </span>
            </template>
          </a-table-column>
          <a-table-column title="申请人" dataIndex="applyer"  :ellipsis= "true" :width="150" fixed="left">
            <template slot-scope="t,r,i" >
              <span> {{t}} </span>
            </template>
          </a-table-column>
          <a-table-column title="标识Key" dataIndex="key" :width="150" >
            <template slot-scope="t,r,i" >
              <span> {{t}} </span>
            </template>
          </a-table-column>
          <a-table-column title="版本" dataIndex="version" :width="80">
            <template slot-scope="t,r,i" >
              <span> v.{{t}} </span>
            </template>
          </a-table-column>
          <a-table-column title="审批结果" dataIndex="result" :width="150"
                          key="result" :sorter="(a,b)=>a.result - b.result"
          >
            <template slot-scope="t,r,i" >
              <span v-if="t==4" style="color: #999999"> 发起人撤回 </span>
              <span v-else-if="t==5" style="color: orange"> 已删除 </span>
              <span v-else-if="t==2" style="color: green"> 已通过 </span>
              <span v-else-if="t==3" style="color: red"> 已驳回 </span>
              <span v-else > 未知 </span>
            </template>
          </a-table-column>
          <a-table-column title="原因详情" dataIndex="deleteReason" :width="150">
            <template slot-scope="t,r,i" >
             <j-ellipsis :value="t" :length="10"></j-ellipsis>
            </template>
          </a-table-column>
          <a-table-column title="总耗时" dataIndex="duration" :width="100"
                          key="duration" :sorter="(a,b)=>a.duration - b.duration"
          >
            <template slot-scope="t,r,i" >
              <span > {{millsToTime(t)}} </span>
            </template>
          </a-table-column>
          <a-table-column title="开始时间" dataIndex="startTime" :width="150">
            <template slot-scope="t,r,i" >
              <span > {{t}} </span>
            </template>
          </a-table-column>
          <a-table-column title="结束时间" dataIndex="endTime" :width="150">
            <template slot-scope="t,r,i" >
              <span > {{t}} </span>
            </template>
          </a-table-column>
          <a-table-column title="操作" dataIndex="" :width="250" fixed="right">
            <template slot-scope="t,r,i" >
              <a href="javascript:void(0);" style="color: green;" @click="history(r)" >审批历史</a>
              <a-divider type="vertical" />
              <a href="javascript:void(0);" style="color: blue;" @click="detail(r)" >表单数据</a>
              <a-divider type="vertical" />
              <a-popconfirm title="确定删除吗?" @confirm="() => remove(r)">
                <a style="color: red;">删除</a>
              </a-popconfirm>
            </template>
          </a-table-column>
        </a-table>
      </a-row>
    </a-card>
    <!---->
    <a-modal title="审批历史" v-model="modalLsVisible" :mask-closable="false" :width="'80%'" :footer="null">
      <div v-if="modalLsVisible">
        <component :is="historicDetail" :procInstId="procInstId"></component>
      </div>
    </a-modal>
    <!--流程表单-->
    <a-modal :title="lcModa.title" v-model="lcModa.visible" :footer="null" :maskClosable="false" width="80%">
      <component :disabled="lcModa.disabled" v-if="lcModa.visible" :is="lcModa.formComponent"
                 :processData="lcModa.processData" :isNew = "lcModa.isNew"
                 @close="lcModa.visible=false,lcModa.disabled = false"></component>
    </a-modal>
  </div>
</template>

<script>

import {activitiMixin} from "./mixins/activitiMixin";
import {JeecgListMixin} from "../../mixins/JeecgListMixin";
export default {
  mixins:[JeecgListMixin,activitiMixin],
  name: "process-finish-manage",
  data() {
    return {
      modalLsVisible: false,
      procInstId: '',
      lcModa: {
        title:'',
        disabled:false,
        visible:false,
        formComponent : null,
        isNew : false
      },
      openSearch: true,
      openTip: true,
      loading: true, // 表单加载状态
      selectCount: 0, // 多选计数
      selectList: [], // 多选数据
      selectDate: null, // 选择日期绑定modal
      searchForm: {
        // 搜索框对应data对象
        name: "",
        key: "",
        pageNumber: 1, // 当前页数
        pageSize: 10 // 页面大小
      },
      data: [], // 表单数据
      total: 0, // 表单数据总数
      deleteId: "",
      url: {
        getFinishedProcess:'/actProcessIns/getFinishedProcess',
        delHistoricIns:'/actProcessIns/delHistoricInsByIds/'
      }
    };
  },
  methods: {
    loadData(){},
    init() {
      this.getDataList();
    },
    selectDateRange(v) {
      this.searchForm.startDate = null;
      this.searchForm.endDate = null;
      if (v[0]) {
        this.searchForm.startDate = v[0].format("YYYY-MM-DD");
      }
      if (v[1]) {
        this.searchForm.endDate = v[1].format("YYYY-MM-DD");
      }
    },
    getDataList() {
      this.loading = true;
      this.getAction(this.url.getFinishedProcess,this.searchForm).then(res => {
        this.loading = false;
        if (res.success) {
          this.data = res.result;
        }else {
          this.$message.error(res.message)
        }
      });
    },
    handleSearch() {
      this.searchForm.pageNumber = 1;
      this.searchForm.pageSize = 10;
      this.getDataList();
    },
    handleReset() {
      this.selectDate = null;
      this.searchForm={};
      this.searchForm.pageNumber = 1;
      this.searchForm.pageSize = 10;
      // 重新加载数据
      this.getDataList();
    },
    handelCancel() {
      this.modalVisible = false;
    },
    detail(r) {
      if (!r.routeName) {
        this.$message.warning(
          "该流程信息未配置表单，请联系开发人员！"
        );
        return;
      }
      this.lcModa.disabled = true;
      this.lcModa.title = '查看流程业务信息：'+r.name;
      this.lcModa.formComponent = this.getFormComponent(r.routeName).component;
      this.lcModa.processData = r;
      this.lcModa.isNew = false;
      this.lcModa.visible = true;
    },
    history(v) {
      if (!v.id) {
        this.$message.error("流程实例ID不存在");
        return;
      }
      this.procInstId = v.id;
      this.modalLsVisible = true;
    },
    remove(v) {
      this.postFormAction(this.url.delHistoricIns+v.id).then(res => {
        if (res.success) {
          this.$message.success("操作成功");
          this.getDataList();
        }else {
          this.$message.error(res.message);
        }
      });
    },
  },
  mounted() {
    this.init();
  },
  watch: {
  }
};
</script>