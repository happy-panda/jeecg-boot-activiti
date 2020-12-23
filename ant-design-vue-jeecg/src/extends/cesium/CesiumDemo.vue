<template>
  <div>
    <div id="cesiumContainer"></div>
    <span>{{mouse_state.innerText}}</span><br>
    <span>{{pick2.id}}</span><br>
    <span>{{pick2.properties}}</span><br>
    <button @click="flyTo">flyTo</button>
    <button @click="lookAt">lookAt</button>
    <button @click="czmlDataSource">设形状</button>
    <button @click="area">区域点位</button>
    <button @click="area2">区域geojson</button>
    <button @click="entiti">addentiti闪动点</button>
    <button @click="river">river普通颜色河流</button>
    <button @click="drawWater">drawWater河流动画</button>
    <button @click="rotate(113,33)">围着点转</button>
  </div>
</template>

<script>
  import chinajson from '@/extends/cesium/china.json'
  import pointjson from '@/extends/cesium/point.json'
  import riverjson from '@/extends/cesium/river.json'
  import nsbdjson from '@/extends/cesium/nsbd.json'
  import waterNormalsjpg from '@/extends/cesium/waterNormals.jpg'
  import yellowgif from '@/extends/cesium/yellow.gif'
    export default {
      props: {
      },
      name: "CesiumDemo",
      data(){
        return {
          viewer:null,
          pick2: {},
          mouse_state:{innerText:''},
          rotates: false
        }
      },
      mounted() {
        const _this = this;
        //'GoogleAerial','BingAerial', 'EsriAerial', 'TianAerial',| 'OSMStandard', 'MapStreets, 'OSMCycle'
        const url = "http://118.190.55.100:8888/tileservice/imagery/OSMCycle/{z}/{x}/{y}";
        this.viewer = new Cesium.Viewer("cesiumContainer", {
          animation: false,        //是否创建动画小器件，左下角仪表
          timeline: false,         //是否显示时间线控件
          fullscreenButton: false, //右下角全屏按钮
          geocoder: false,         //是否显示地名查找控件
          baseLayerPicker: false,  //是否显示图层选择控件
          imageryProvider: new Cesium.UrlTemplateImageryProvider({url:url}),
          terrainProvider : Cesium.createWorldTerrain({ //立体
            requestVertexNormals: true
          })
        });
        //去cesium logo水印
        this.viewer.cesiumWidget.creditContainer.style.display = "none";
        // 默认弹窗去除
        document.querySelector('.cesium-infoBox').style.display = "none";
        //事件 https://www.cnblogs.com/-llf/p/10431978.html
        var handler = new Cesium.ScreenSpaceEventHandler(this.viewer.scene.canvas);
        handler.removeInputAction(Cesium.ScreenSpaceEventType.LEFT_CLICK);
        handler.setInputAction(function(e){
          console.log('左键单击事件：',e,e.position);
          var pick = _this.viewer.scene.pick(e.position);
          if (Cesium.defined(pick)){
            _this.pick2 = {
              id:  pick.id.id,
              name:  pick.id.name,
            }
            if (pick.id.properties) _this.pick2.properties = pick.id.properties.getValue();
            console.log('选中',_this.pick2)
          }
        },Cesium.ScreenSpaceEventType.LEFT_CLICK);
        handler.setInputAction(function(movement) {
          var ellipsoid = _this.viewer.scene.globe.ellipsoid;
          //通过指定的椭球或者地图对应的坐标系，将鼠标的二维坐标转换为对应椭球体三维坐标
          var cartesian = _this.viewer.camera.pickEllipsoid(movement.endPosition, ellipsoid);
          if (cartesian) {
            //将笛卡尔坐标转换为地理坐标
            var cartographic = ellipsoid.cartesianToCartographic(cartesian);
            //将弧度转为度的十进制度表示
            var longitudeString = Cesium.Math.toDegrees(cartographic.longitude).toFixed(3);
            var latitudeString = Cesium.Math.toDegrees(cartographic.latitude).toFixed(3);
            //获取相机高度
            var height = Math.ceil(_this.viewer.camera.positionCartographic.height).toFixed(3);
            _this.mouse_state.innerText = '移动：(' + longitudeString + ', ' + latitudeString + "," + height + ')';
          }else {
            _this.mouse_state.innerText = "";
          }
        }, Cesium.ScreenSpaceEventType.MOUSE_MOVE);
      },
      watch:{
      },
      methods: {
        getMapInstance: function(pick2){
          if(pick2 != null && pick2.name !=null && pick2.name.sdLon != null && pick2.name.sdLat != null){
            this.rotate(pick2.name.sdLon,pick2.name.sdLat);
          }else{
            var startTime = Cesium.JulianDate.fromDate(new Date());
            var stopTime = Cesium.JulianDate.addSeconds(startTime, 0.1, new Cesium.JulianDate());
            this.viewer.clock.stopTime = stopTime.clone();
          }
        },
        rotate: function (lng,lat) {
          var _this = this;
          // var viewer = new Cesium.Viewer('cesiumContainer');
          var options = {

            lng: lng,
            lat: lat,
            height: 11110.8,
            heading: 0.0,
            pitch: 0.0,
            roll: 0.0
          };
          var position = Cesium.Cartesian3.fromDegrees(options.lng, options.lat, options.height);
          // 相机看点的角度，如果大于0那么则是从地底往上看，所以要为负值，这里取-30度
          var pitch = Cesium.Math.toRadians(-20);
          // 给定飞行一周所需时间，比如10s, 那么每秒转动度数
          var angle = 210 / 30;
          // 给定相机距离点多少距离飞行，这里取值为5000m
          var distance = 5000;
          var startTime = Cesium.JulianDate.fromDate(new Date());

          // var stopTime = Cesium.JulianDate.addSeconds(startTime, 1, new Cesium.JulianDate());

          this.viewer.clock.startTime = startTime.clone();  // 开始时间
          // this.viewer.clock.stopTime = stopTime.clone();     // 结速时间
          this.viewer.clock.currentTime = startTime.clone(); // 当前时间
          this.viewer.clock.clockRange = Cesium.ClockRange.CLAMPED; // 行为方式
          this.viewer.clock.clockStep = Cesium.ClockStep.SYSTEM_CLOCK; // 时钟设置为当前系统时间; 忽略所有其他设置。
          // 相机的当前heading
          var initialHeading = this.viewer.camera.heading;
          var Exection = function TimeExecution() {
            // 当前已经过去的时间，单位s
            var delTime = Cesium.JulianDate.secondsDifference(_this.viewer.clock.currentTime, _this.viewer.clock.startTime);
            var heading = Cesium.Math.toRadians(delTime * angle) + initialHeading;
            _this.viewer.scene.camera.setView({
              destination :position, // 点的坐标
              orientation:{
                heading: heading,
                pitch : pitch,

              }
            });
            _this.viewer.scene.camera.moveBackward(distance);

            if (Cesium.JulianDate.compare(_this.viewer.clock.currentTime, _this.viewer.clock.stopTime) >= 0) {
              _this.viewer.clock.onTick.removeEventListener(Exection);
            }
          };

          this.viewer.clock.onTick.addEventListener(Exection);

          if(_this.rotates == true){
            _this.rotates = false
            _this.showGraph = true
            var stopTime = Cesium.JulianDate.addSeconds(startTime, 1, new Cesium.JulianDate());
            this.viewer.clock.stopTime = stopTime.clone();
            _this.flyTo();
            // _this.infoMap();
          }else{
            var stopTime = Cesium.JulianDate.addSeconds(startTime, 10000, new Cesium.JulianDate());
            this.viewer.clock.stopTime = stopTime.clone();
            _this.rotates = true
            _this.showGraph = false
          }
        },
        flyTo: function () {
          //lookAt(target, offect)
//target目标位置在世界坐标,offect以目标为中心的当地东北向参考系中的目标的偏移量。
          this.viewer.camera.flyTo({
            destination :  Cesium.Cartesian3.fromDegrees(114.6544496000,37.0705682300,3333331.0), // 设置位置
            orientation: {
              heading : Cesium.Math.toRadians(20.0), // 方向
              pitch : Cesium.Math.toRadians(-90.0),// 倾斜角度
              roll : 0
            },
            duration:5, // 设置飞行持续时间，默认会根据距离来计算
            complete: function () {
              // 到达位置后执行的回调函数
              console.log('到达位置');
            },
            cancle: function () {
              // 如果取消飞行则会调用此函数
              console.log('取消飞行')
            },
            // 如果摄像机飞越高于该值，则调整俯仰俯仰的俯仰角度，并将地球保持在视口中。
            pitchAdjustHeight: -90,
            maximumHeight:5000, // 相机最大飞行高度
            // flyOverLongitude: 100, // 如果到达目的地有2种方式，设置具体值后会强制选择方向飞过这个经度
          });
        },
        /*视角移过去后就不能缩放了，还是用 flyTo */
        lookAt: function(){
          var center = Cesium.Cartesian3.fromDegrees(114.6544496000,37.0705682300,3333331.0);
          var heading = Cesium.Math.toRadians(20.0);
          var pitch = Cesium.Math.toRadians(-180.0);
          var range = 5000.0;
          this.viewer.camera.lookAt(center, new Cesium.HeadingPitchRange(heading, pitch, range));


        },
        /*案例画出南水北调的省份*/
        area2:function(){
          var _this = this;
          // https://sandcastle.cesium.com/SampleData/simplestyles.geojson
          //可以直接调json链接，但是这样设置的name会无效，所以使用静态方式较好
          var datasname = 'china'
            var a = _this.viewer.dataSources.add(new Cesium.GeoJsonDataSource(datasname).load(chinajson, {
              stroke: Cesium.Color.fromCssColorString('#A4BF78'),// 折线和多边形轮廓的默认颜色
              fill: Cesium.Color.fromCssColorString('#A4BF78').withAlpha(0.3),// 内部填充色
              strokeWidth: 10,// 轮廓默认宽度
              markerSymbol: '?'
            }));
            _this.viewer.flyTo(a);
            // 删除方法
            // console.log(this.viewer.dataSources.remove(this.viewer.dataSources.getByName('j2')[0],true))

            console.log(_this.viewer.dataSources.getByName(datasname)[0])


        },
        /*案例画出南水北调线路，自定义颜色*/
        river: function (){
          var promise = Cesium.GeoJsonDataSource.load(riverjson, {
            stroke: Cesium.Color.fromCssColorString('#fff'),// 折线和多边形轮廓的默认颜色
            fill: Cesium.Color.fromCssColorString('#6BB0FF').withAlpha(0.8),// 内部填充色
            strokeWidth: 1,// 轮廓默认宽度
            markerSymbol: '?'
          });
          var _this = this;
          /*重写相关属性*/
          promise.then(function(dataSource) {
            _this.viewer.dataSources.add(dataSource);
            var entities = dataSource.entities.values;
            var colorHash = {};
            for (var i = 0; i < entities.length; i++) {
              var entity = entities[i];
              var name = entity.name;
              var color = Cesium.Color.fromRandom({
                alpha : 1.0
              });
              entity.polygon.material = color;
              entity.polygon.outline = false;
              entity.polygon.extrudedHeight =0;
            }
          });
          _this.viewer.flyTo(promise);
        },
        //案例南水北调线路图，绘制水面波浪效果
        drawWater: function(){
          var waterFace=[
            130.0, 30.0, 0,
            150.0, 30.0, 0,
            150.0, 10.0, 0,
            130.0, 10.0, 0];
          var _this = this;
            waterFace = []
            for (const coordinateElement of nsbdjson.coordinates[0]) {
              waterFace.push(coordinateElement[0])
              waterFace.push(coordinateElement[1])
              waterFace.push(coordinateElement[2])
            }
            _this.drawWaterByFace(waterFace)


        },
        drawWaterByFace: function(waterFace){
          //是否淹没效果
          this.viewer.scene.globe.depthTestAgainstTerrain = false;
          var waterPrimitive = new Cesium.Primitive({
            show:true,// 默认隐藏
            allowPicking:false,
            geometryInstances : new Cesium.GeometryInstance({
              geometry : new Cesium.PolygonGeometry({
                polygonHierarchy : new Cesium.PolygonHierarchy(Cesium.Cartesian3.fromDegreesArrayHeights(waterFace)),
                //extrudedHeight: 0,//注释掉此属性可以只显示水面
                //perPositionHeight : true//注释掉此属性水面就贴地了
              })
            }),
            // 可以设置内置的水面shader
            appearance : new Cesium.EllipsoidSurfaceAppearance({
              material : new Cesium.Material({
                translucent:true,
                fabric : {
                  type : 'Water',
                  uniforms : {
                    //baseWaterColor:new Cesium.Color(0.0, 0.0, 1.0, 0.5),
                    //blendColor: new Cesium.Color(0.0, 0.0, 1.0, 0.5),
                    //specularMap: 'gray.jpg',
                    //normalMap: '../assets/waterNormals.jpg',
                    normalMap: waterNormalsjpg,
                    frequency: 1000.0,
                    animationSpeed: 0.01,
                    amplitude: 10.0
                  }
                }
              }),
              // fragmentShaderSource:'varying vec3 v_positionMC;\nvarying vec3 v_positionEC;\nvarying vec2 v_st;\nvoid main()\n{\nczm_materialInput materialInput;\nvec3 normalEC = normalize(czm_normal3D * czm_geodeticSurfaceNormal(v_positionMC, vec3(0.0), vec3(1.0)));\n#ifdef FACE_FORWARD\nnormalEC = faceforward(normalEC, vec3(0.0, 0.0, 1.0), -normalEC);\n#endif\nmaterialInput.s = v_st.s;\nmaterialInput.st = v_st;\nmaterialInput.str = vec3(v_st, 0.0);\nmaterialInput.normalEC = normalEC;\nmaterialInput.tangentToEyeMatrix = czm_eastNorthUpToEyeCoordinates(v_positionMC, materialInput.normalEC);\nvec3 positionToEyeEC = -v_positionEC;\nmaterialInput.positionToEyeEC = positionToEyeEC;\nczm_material material = czm_getMaterial(materialInput);\n#ifdef FLAT\ngl_FragColor = vec4(material.diffuse + material.emission, material.alpha);\n#else\ngl_FragColor = czm_phong(normalize(positionToEyeEC), material);\gl_FragColor.a=0.5;\n#endif\n}\n'//重写shader，修改水面的透明度
            })
          });
          var a = this.viewer.scene.primitives.add(waterPrimitive);

          this.viewer.camera.flyTo({
            destination : Cesium.Cartesian3.fromDegrees(117.106488355488,
              34.1451075219894, 2500000.0),
            orientation : {
              heading: Cesium.Math.toRadians(0.0), //默认朝北0度，顺时针方向，东是90度
              pitch: Cesium.Math.toRadians(-90), //默认朝下看-90,0为水平看，
              roll: Cesium.Math.toRadians(0) //默认0
            }
          });

        },
        /*根据点位信息画区域，上面是根据geojson画*/
        area: function (){
          var _this = this
          var points = [
            112.82436,
            23.071506,
            112.82742,
            23.067512,
            112.828878,
            23.064659,
            112.830799,
            23.060947,
            112.832166,
            24.058329
          ]
          var positions=Cesium.Cartesian3.fromDegreesArray(points);
          var rangeModel = _this.viewer.entities.add({
            name: "rangeModel",
            polygon: {
              hierarchy: new Cesium.CallbackProperty(function() {
                return new Cesium.PolygonHierarchy(positions);
              }, false),
              clampToGround: true,
              show: true,
              fill: true,
              material: Cesium.Color.RED.withAlpha(0.5),
              width: 3,
              outlineColor: Cesium.Color.BLACK,
              outlineWidth: 1,
              outline: false
            }
          });
          _this.viewer.flyTo(rangeModel);

        },
        /*案例 点，动画闪动*/
        entiti: function (){
          var _this = this;
// 模型
          // let entity = viewer.entities.add({
          //     position: Cesium.Cartesian3.fromDegrees(106.325, 26.84, 200),
          //     model: {
          //         uri: '/static/cesium/Apps/SampleData/models/DracoCompressed/CesiumMilkTruck.gltf'
          //     }
          // });
          // entity.model.scale = 1000;
          // viewer.zoomTo(entity);
          //小原点
          // let citizensBankPark = viewer.entities.add({
          //     id: "11111111",
          //     name: '温度计1',
          //     position: Cesium.Cartesian3.fromDegrees(106.325, 26.84),
          //     // position: new Cesium.Cartesian3(-1610801.5773835022, 5457584.684552207, 2871437.1159677417),  //鼠标点击获取的，不准
          //     point: {
          //         pixelSize: 20,
          //         color: Cesium.Color.RED,
          //         //color: { alpha: 0.8, red: 255, green: 0, blue: 0 },
          //         outlineColor: Cesium.Color.WHITE,
          //         outlineWidth: 1
          //     },
          //     label: {
          //         text: 'P',
          //         font: '14pt',
          //         // style: Cesium.LabelStyle.FILL_AND_OUTLINE,
          //         style: Cesium.LabelStyle.FILL,
          //         // outlineWidth: 1,
          //         verticalOrigin: Cesium.VerticalOrigin.CENTER,
          //         pixelOffset: new Cesium.Cartesian2(0, 0),
          //         eyeOffset: new Cesium.Cartesian3(0, 0, -10)
          //     },
          // });
          //广告牌  //加地形后位置不固定?
          var e1 = {
            id: 'new Date().getSeconds()',
            position: Cesium.Cartesian3.fromDegrees(106.325, 26.84, 800),
            billboard: {
              disableDepthTestDistance: Number.POSITIVE_INFINITY,
              image: yellowgif,
              // width: 32,
              // height: 32,
            },
            height: 200,
            // label: {
            //     text: '温度计1',
            //     font: '12pt monospace',
            //     style: Cesium.LabelStyle.FILL_AND_OUTLINE,
            //     outlineWidth: 2,
            //     verticalOrigin: Cesium.VerticalOrigin.TOP,
            //     pixelOffset: new Cesium.Cartesian2(0, -32)
            // }
          }
          var citizensBankPark = this.viewer.entities.add(e1);
          citizensBankPark.addProperty("imageClock");
          var index=1,scale=0.05;
          citizensBankPark.imageClock=setInterval(function () {
            if(citizensBankPark!=null){
              scale+=0.01;
              citizensBankPark.billboard.image = yellowgif;
              index+=1;
              citizensBankPark.billboard.scale = scale;

              if(index>3){
                index=1;
              }
              if(scale >0.3){
                scale = 0.05
              }
            }
          }, 60);
          this.viewer.flyTo(citizensBankPark);
          //删除方式
          /* setTimeout(function () {
             _this.viewer.entities.removeById('new Date().getSeconds()')
           },5000)*/
        },
        /*立方体*/
        czmlDataSource: function () {
          var czml = [{
            "id" : "document",
            "name" : "box",
            "version" : "1.0"
          },{
            "id" : "shape2",
            "name" : "Red box with black outline",
            "position" : {
              "cartographicDegrees" : [114.6544496000,37.0705682300, 0.0]
            },
            "box" : {
              "dimensions" : {
                "cartesian": [40000.0, 30000.0, 50000.0]
              },
              "material" : {
                "solidColor" : {
                  "color" : {
                    "rgba" : [255, 0, 0, 128]
                  }
                }
              },
              "outline" : true,
              "outlineColor" : {
                "rgba" : [0, 0, 0, 255]
              }
            }
          }];

          var dataSourcePromise = Cesium.CzmlDataSource.load(czml);
          this.viewer.dataSources.add(dataSourcePromise);
          this.viewer.flyTo(dataSourcePromise);
          // this.viewer.zoomTo(dataSourcePromise);

          /*this.viewer.dataSources.add(Cesium.GeoJsonDataSource.load('modules/gis/nsbd.json', {
            clampToGround: true
          }));*/
        },
        }
    }
</script>

<style scoped>

</style>