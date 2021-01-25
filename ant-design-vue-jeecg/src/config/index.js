/** init domain config */
import Vue from 'vue'
//设置全局API_BASE_URL
Vue.prototype.API_BASE_URL = process.env.VUE_APP_API_BASE_URL
window._CONFIG['domianURL'] = Vue.prototype.API_BASE_URL
//文件上传下载类型，默认使用 mongodb 附件组件
Vue.prototype.BASE_FileType = process.env.BASE_FileType || "mongodb"
window._CONFIG['BASE_FileType'] = Vue.prototype.BASE_FileType
//单点登录地址
window._CONFIG['casPrefixUrl'] = process.env.VUE_APP_CAS_BASE_URL
window._CONFIG['onlinePreviewDomainURL'] =  process.env.VUE_APP_ONLINE_BASE_URL
window._CONFIG['staticDomainURL'] = Vue.prototype.API_BASE_URL + '/sys/common/static'
window._CONFIG['pdfDomainURL'] = Vue.prototype.API_BASE_URL+ '/sys/common/pdf/pdfPreviewIframe'