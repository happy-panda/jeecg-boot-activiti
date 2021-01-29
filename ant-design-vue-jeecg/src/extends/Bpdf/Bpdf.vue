<template>
  <div class="b-pdf">
    <div v-if="url!=''">
      <div class="tools">
        <a-button @click.stop="toPage(1)"><a-icon type="vertical-right" /> 首页</a-button>
        <a-button @click.stop="prePage"><a-icon type="left" /> 上一页</a-button>
        <span class="page">{{pageNum}}/{{pageTotalNum}} </span>
        跳转到: &nbsp;<a-input-number v-model="pageNum" @change="skip" style="width: 60px;margin-right: 4px;"></a-input-number>
        <a-button @click.stop="nextPage"><a-icon type="right" /> 下一页</a-button>
        <a-button @click.stop="toPage(pageTotalNum)"><a-icon type="vertical-left" /> 尾页</a-button>
        <span style="float: right">
          <a-button @click.stop="clock"><a-icon type="redo" /> 顺时针</a-button>
          <a-button @click.stop="counterClock"><a-icon type="undo" /> 逆时针</a-button>
          <a-button @click.stop="down"><a-icon type="arrow-down" />下载</a-button>
        </span>
      </div>
      <pdf ref="pdf"
           :src="url"
           :page="pageNum"
           :rotate="pageRotate"
           @progress="loadedRatio = $event"
           @page-loaded="pageLoaded($event)"
           @num-pages="pageTotalNum=$event"
           @error="pdfError($event)"
           @link-clicked="page = $event">
      </pdf>
    </div>
    <a-empty v-else :description="'未找到pdf文件'">
    </a-empty>
  </div>
</template>

<script>
  import Vue from 'vue'
  import { ACCESS_TOKEN } from "@/store/mutation-types"
  import pdf from 'vue-pdf'
  import {getFileAccessHttpUrl} from "@api/manage";
  export default {
    components: {
      pdf
    },
    name: "bPdf",
    props: {
      file: {type: String, default: null, required: false},
    },
    data () {
      return {
        url:'',
        id:"pdfPreviewIframe",
        headers:{},
        pageNum: 1,
        pageTotalNum: 1,
        pageRotate: 0,
        // 加载进度
        loadedRatio: 0,
        curPageNum: 0,
      }
    },
    created () {
      const token = Vue.ls.get(ACCESS_TOKEN);
      this.headers = {"X-Access-Token":token}
      if (this.file){
        this.url = getFileAccessHttpUrl(this.file)
      }
    },
    computed:{

    },
    mounted(){
    },
    methods: {
      onChange(pageNumber) {
        console.log('Page: ', pageNumber);
      },
      // 上一页函数，
      prePage() {
        var page = this.pageNum
        page = page > 1 ? page - 1 : this.pageTotalNum
        this.pageNum = page
      },
      // 下一页函数
      nextPage() {
        var page = this.pageNum
        page = page < this.pageTotalNum ? page + 1 : 1
        this.pageNum = page
      },
      // 页面顺时针翻转90度。
      clock() {
        this.pageRotate += 90
      },
      // 页面逆时针翻转90度。
      counterClock() {
        this.pageRotate -= 90
      },
      // 页面加载回调函数，其中e为当前页数
      pageLoaded(e) {
        this.curPageNum = e
      },
      // 其他的一些回调函数。
      pdfError(error) {
        console.error(error)
      },
      toPage(pageNum) {
        this.pageNum = pageNum
      },
      skip() {
        let pageNum = this.pageNum;
        if (!Number(pageNum)){ pageNum = 1 }
        if (pageNum<1) pageNum = 1
        if (pageNum>this.pageTotalNum) pageNum = this.pageTotalNum
        this.pageNum = Math.floor(pageNum)
      },
      down() {
        window.open(this.url)
      }
    },
  }
</script>

<style scoped lang="less">
.tools{
  .ant-btn{
    margin: 4px 2px;
  }
  .page{
    background-color: #fcfcfc;
    padding: 2px 6px;
    font-size: 16px;
    margin: 0 4px;
  }
}
</style>