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
        <a-table :scroll="scroll" bordered
          :loading="loading"
          rowKey="id"
          :dataSource="data"
          :pagination="ipagination"
          @change="handleTableChange"
          ref="table"
        >
          <a-table-column title="#"  :width="50">
            <template slot-scope="t,r,i" >
              <span> {{i+1}} </span>
            </template>
          </a-table-column>
          <a-table-column title="流程实例ID" dataIndex="id" :width="150">
            <template slot-scope="t,r,i" >
              <span> {{t}} </span>
            </template>
          </a-table-column>
          <a-table-column title="流程名称" dataIndex="name" :width="150">
            <template slot-scope="t,r,i" >
              <span> {{t}} </span>
            </template>
          </a-table-column>
          <a-table-column title="申请人" dataIndex="applyer" :width="100">
            <template slot-scope="t,r,i" >
              <span> {{t}} </span>
            </template>
          </a-table-column>
          <a-table-column title="标识Key" dataIndex="key" :width="150">
            <template slot-scope="t,r,i" >
              <span> {{t}} </span>
            </template>
          </a-table-column>
          <a-table-column title="版本" dataIndex="version" :width="150">
            <template slot-scope="t,r,i" >
              <span> v.{{t}} </span>
            </template>
          </a-table-column>
          <a-table-column title="当前环节" dataIndex="currTaskName" :width="150">
            <template slot-scope="t,r,i" >
              <span> {{t}} </span>
            </template>
          </a-table-column>
          <a-table-column title="状态" dataIndex="isSuspended" :width="110"
                    key="isSuspended" :sorter="(a,b)=>Boolean(a.isSuspended)?0:1 - Boolean(b.isSuspended)?0:1"
          >
            <template slot-scope="t,r,i" >
              <span v-if="t" style="color: red"> 已挂起 </span>
              <span v-else style="color: #2f54eb"> 已激活 </span>
            </template>
          </a-table-column>
          <a-table-column title="操作" dataIndex="action"  >
            <template slot-scope="t,r,i" >
              <template v-if="r.isSuspended">
                <a href="javascript:void(0);" style="color: green;" @click="editStatus(1,r)" >激活</a>
                <a-divider type="vertical" />
              </template>
              <template v-else>
                <a href="javascript:void(0);" style="color: orange;" @click="editStatus(0,r)" >挂起</a>
                <a-divider type="vertical" />
              </template>
              <a href="javascript:void(0);" style="color: blue;" @click="history(r)" >审批详情</a>
              <a-divider type="vertical" />
              <a href="javascript:void(0);" style="color: #999;" @click="detail(r)" >表单数据</a>
              <a-divider type="vertical" />
              <a-popconfirm title="确定删除吗?" @confirm="() => remove(r)">
                <a style="color: red;">删除</a>
              </a-popconfirm>
            </template>
          </a-table-column>
        </a-table>
      </a-row>
    </a-card>

    <a-modal
      :title="modalTitle"
      v-model="modalVisible"
      :mask-closable="false"
      :width="500"
      :styles="{top: '30px'}"
    >
      <a-form ref="form" :model="form" :label-width="70" :rules="formValidate">
        <a-form-item label="删除原因" prop="reason">
          <a-input type="textarea" v-model="form.reason" :rows="4" />
        </a-form-item>
      </a-form>
      <div slot="footer">
        <a-button type="text" @click="handelCancel">取消</a-button>
        <a-button type="primary" :loading="submitLoading" @click="handelSubmit">提交</a-button>
      </div>
    </a-modal>
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
  mixins:[activitiMixin,JeecgListMixin],
  name: "process-ins-manage",
  data() {
    return {
      openSearch: true,
      openTip: true,
      loading: true, // 表单加载状态
      selectCount: 0, // 多选计数
      selectList: [], // 多选数据
      searchForm: {
        // 搜索框对应data对象
        name: "",
        key: "",
        pageNumber: 1, // 当前页数
        pageSize: 10 // 页面大小
      },
      modalType: 0, // 添加或编辑标识
      modalVisible: false, // 添加或编辑显示
      modalTitle: "", // 添加或编辑标题
      form: {
        // 添加或编辑表单对象初始化数据
        reason: ""
      },
      formValidate: {
        // 表单验证规则
      },
      submitLoading: false, // 添加或编辑提交状态
      data: [], // 表单数据
      total: 0, // 表单数据总数
      deleteId: "",
      url: {
        getRunningProcess:'/actProcessIns/getRunningProcess',
        deleteProcessIns:'/actProcessIns/delInsByIds/',
        updateInsStatus:'/actProcessIns/updateInsStatus/',
      },
      modalLsVisible: false,
      procInstId: '',
      lcModa: {
        title:'',
        disabled:false,
        visible:false,
        formComponent : null,
        isNew : false
      },
    };
  },
  methods: {
    loadData(){},
    init() {
      this.getDataList();
    },
    getDataList() {
      this.loading = true;
      this.getAction(this.url.getRunningProcess,this.searchForm).then(res => {
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
      this.searchForm= {};
      this.searchForm.pageNumber = 1;
      this.searchForm.pageSize = 10;
      // 重新加载数据
      this.getDataList();
    },
    handelCancel() {
      this.modalVisible = false;
    },
    handelSubmit() {
      this.submitLoading = true;
      this.postFormAction(this.url.deleteProcessIns+this.deleteId, this.form).then(res => {
        this.submitLoading = false;
        if (res.success) {
          this.$message.success("操作成功");
          this.modalVisible = false;
          this.getDataList();
        }else {
          this.$message.error(res.message);
        }
      });
    },
    editStatus(status, v) {
      let operation = "";
      if (status == 0) {
        operation = "暂停挂起";
      } else {
        operation = "激活运行";
      }
      this.$confirm({
        title: "确认" + operation,
        content: `您确认要${operation}流程实例${v.name}?`,
        loading: true,
        onOk: () => {
          let params = {
            status: status,
            id: v.id
          };
          this.postFormAction(this.url.updateInsStatus,params).then(res => {
            if (res.success) {
              this.$message.success("操作成功");
              this.getDataList();
            }else {
              this.$message.error(res.message);
            }
          });
        }
      });
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
      if (!v.procInstId) {
        this.$message.error("流程实例ID不存在");
        return;
      }
      this.procInstId = v.procInstId;
      this.modalLsVisible = true;
    },
    remove(v) {
      this.modalTitle = `确认删除流程 ${v.name}`;
      // 单个删除
      this.deleteId = v.id;
      this.modalType = 0;
      this.modalVisible = true;
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
    }
  },
  mounted() {
    this.init();
  },
  watch: {
  }
};
</script>