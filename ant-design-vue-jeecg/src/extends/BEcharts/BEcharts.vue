<template>
    <div class="b-echarts">
    </div>
</template>

<script>
  import echart from 'echarts';
  import ResizeListener from 'element-resize-detector';
    export default {
      props: {
        option: {
          type: Object,
          default: {}
        },
        /* 主题 default light dark*/
        theme: {
          type: String,
          default: 'light'
        }
      },
      data() {
        return {
          chart: null
        };
      },
      watch:{
        option(){
          this.updateChartView();
        }
      },
      mounted() {
        const _this = this;
        this.chart = echart.init(this.$el, this.theme);
        this.updateChartView();
        this.chart.on('click',(params)=>{
          _this.$emit('click',params);
        })
        window.addEventListener('resize', this.handleWindowResize);
        this.addChartResizeListener();
      },
      beforeDestroy() {
        window.removeEventListener('resize', this.handleWindowResize);
      },
      methods: {
        /**
         * 更新echart视图
         */
        updateChartView() {
          if (!this.chart) return;
          let option = this.option;
          this.chart.setOption(option, true);
        },
        /**
         * 对chart元素尺寸进行监听，当发生变化时同步更新echart视图
         */
        addChartResizeListener() {
          const instance = ResizeListener({
            strategy: 'scroll',
            callOnAdd: true
          });

          instance.listenTo(this.$el, () => {
            if (!this.chart) return;
            this.chart.resize();
          });
        },
        /**
         * 当窗口缩放时，echart动态调整自身大小
         */
        handleWindowResize() {
          if (!this.chart) return;
          this.chart.resize();
        }
      }
    };
</script>
<style scoped lang="less">
  .b-echarts{
    width: 100%;
    height: 100%;
  }
</style>