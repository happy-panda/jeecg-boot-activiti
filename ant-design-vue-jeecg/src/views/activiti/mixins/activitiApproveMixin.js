import { getAction, deleteAction, putAction, postAction, httpAction } from '@/api/manage'

export const activitiApproveMixin = {
  component(){},
  data(){
    return {
      url: {
        // 代办列表查询
        todoList: '/actTask/todoList',
        todoCounts: '/actTask/todoCounts',
        // 审批通过
        pass: '/actTask/pass',
        // 审批不通过
        back: '/actTask/back',
        // 审批打回 到某一个节点
        backToTask:'/actTask/backToTask',
        delegate:'/actTask/delegate',
        // 获取下一个节点 信息（审批人）
        getNextNode:'/activiti_process/getNextNode',
        // 获取节点
        getNode:'/activiti_process/getNode/',
        //
        getBackList:'/actTask/getBackList/',
        passAll:'/actTask/passAll/',
        backAll:'/actTask/backAll/',
      },
      // 查询参数
      param:{
        // 代办查询参数
        todo:{

        }
      },
    }
  },
  computed:{

  },
  methods:{
    // 查询代办数据
    searchTodoList() {
      let _this = this;
      getAction(this.url.todoList, this.param.todo).then(res => {
        // 处理代办查询结果
        _this.doTodoResult(res);
      }).catch(error => {
        console.log(error)
      }).catch(error => {
        console.log(error)
      })
    },
    searchTodoCounts(procDefKeys){
      return getAction(this.url.todoCounts, {"procDefIds":procDefKeys});
    },

    // 需要自定义覆盖的方法
    // 处理代办返回结果
    doTodoResult(result){

    }
  }
}