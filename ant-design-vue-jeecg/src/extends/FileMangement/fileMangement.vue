<template>
  <a-upload-dragger
    :fileList="fileList"
    :multiple="isMultiple"
    @change="changeFile"
    class="upload-list-inline"
    :customRequest="customRequest"
    @download="downloadFile"
    @preview="previewFile"
    @remove="removeFile"
    :before-upload="beforeUpload"
  >
    <p class="ant-upload-drag-icon">
      <a-icon type="inbox" />
    </p>
    <p class="ant-upload-text">
      单击或拖动文件到此区域以上载
    </p>
    <p v-if="isMultiple" class="ant-upload-hint">
      支持单次或批量上传。
    </p>
  </a-upload-dragger>
</template>

<script>
  import {downloadFile, getAction, postAction} from '@/api/manage';
    export default {
      props: {
        uuId:{
            type: String,
            default: '',
            required: false
          },
        fileType:{
          type: String,
          default: '',
          required: false
        },
        projectId:{
          type: String,
          default: '',
          required: false
        },
        moduleCode:{
          type: String,
          default: '',
          required: false
        },
        isMultiple:{
          type:Boolean,
          required:false,
          default: true
        },
      },
        name: "fileMangement",
        data(){
          return {
            fileList:[],
            url: {
              queryByBusinessId:"/system/sysFile/queryByBusinessId",
            }
          }
        },
      created() {
        const _this = this;
        const uuId = this.uuId;
        this.intFile();
      },
      watch:{
        uuId(newVal,oldVal){
          this.uuId = newVal;
          this.intFile();
        }
      },
        methods: {
          //上传文件
          upload (params){return postAction("/system/sysFile/upload",params)},
          download (params){return getAction("/system/sysFile/download",params)},
          deleteFile (params){return getAction("/system/sysFile/delete",params)},
           intFile(){
             let _this = this;
             //修改数据回显
             const uuId = this.uuId;
             this.getAction(this.url.queryByBusinessId,{
               businessId:uuId,
             }).then((res)=>{
               if(res.code == 200){
                 let sysFiles = res.result;
                 if(sysFiles != null && sysFiles.length >0){
                   for(let i = 0;i<sysFiles.length; i++){
                     let file = sysFiles[i];
                     sysFiles[i].uid = sysFiles[i].id;
                     sysFiles[i].name = sysFiles[i].fileName;
                     sysFiles[i].status = 'done';
                   }
                 }else{
                   sysFiles = []
                 }
                 _this.fileList = sysFiles;
                 console.log('_this.$emit("afterIntFiles",_this.fileList)')
                 _this.$emit("afterIntFiles",_this.fileList)
               }
              });
           },
          /*上传文件 */
          changeFile(info){
            const _this = this;
            if (info.file.status === 'uploading') {
              let fileId = this.fileId;
              let fileList = [...info.fileList];
              fileList = fileList.map(file => {
                if (file.response) {
                  file.url = file.response.url;
                }
                return file;
              });
              this.fileList = fileList;
              console.log('this.fileList',this.fileList);
              _this.$emit("changeFile",_this.fileList,info.file.status)
            }
            if(info.file.status === "removed"){
              let fileId = info.file.id;
              const formData = {id:fileId}
              this.deleteFile(formData);
              this.fileList = info.fileList;
              _this.$emit("changeFile",_this.fileList,info.file.status)
            }
            if (info.file.status === 'done') {
              this.$message.success(`${info.file.name} 上传成功`);
            } else if (info.file.status === 'error') {
              this.$message.error(`${info.file.name} 上传失败.`);
            }
          },
          customRequest(data){ // 上传提交
            const formData = new FormData()
            formData.append('file', data.file)
            formData.append('businessId',this.uuId)
            formData.append('fileType',this.fileType)
            formData.append('projectId',this.projectId?this.projectId:0)
            formData.append('moduleCode',this.moduleCode)
            this.saveFile(formData)
          },
          saveFile (formData) {
            const _this = this;
            _this.upload(formData).then((res) => {
              console.log("--->");
              console.log(res);
              if(res.code == 200){
                _this.intFile();
              }else{
                const fileList = _this.fileList;
                const fileLast = fileList[fileList.length-1];
                const fileId  = res.result.ids[0];
                fileList[fileList.length-1].status = 'error';
              }
            }).finally(() => {
              this.loading = false
            })
          },
          downloadFile(formData){
            formData = {id:formData.id}
            this.download(formData);
          },
          previewFile(formData){
            let data = {id:formData.id}
            downloadFile('/system/sysFile/download',formData.name,data);
          },
          removeFile(file){
            console.log("file",file);
          },
          beforeUpload(file) {
            if (this.fileType==='img'){
              const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png';
              if (!isJpgOrPng) {
                this.$message.error('请选择图片文件上传!');
              }
              const isLt2M = file.size / 1024 / 1024 < 2;
              if (!isLt2M) {
                this.$message.error('上传的文件请小于 2MB!');
              }
              return isJpgOrPng && isLt2M;
            }
            return true;
          },
        }
    }
</script>

<style scoped>

</style>