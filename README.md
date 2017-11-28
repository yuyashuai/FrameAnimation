## SilkyAnimation
Use SurfaceView instead of Android AnimationDrawable to achieve Frame Animation that contains lots of pictures.
effectively avoid OOM and ANR while decode many pictures.  
用SurfaceView来播放帧动画，避免在很多帧的情况下使用AnimationDrawable带来的OOM和卡顿问题。支持任意多帧的动画。
### 下载 Download
#### gradle   
`compile 'com.yuyashuai.android:silkyanimation:1.1.4'`  
#### maven  
```
<dependency>
  <groupId>com.yuyashuai.android</groupId>
  <artifactId>silkyanimation</artifactId>
  <version>1.1.4</version>
  <type>pom</type>
</dependency>
```
#### jar
[download jar](http://jcenter.bintray.com/com/yuyashuai/android/silkyanimation/1.1.4/silkyanimation-1.1.4-sources.jar)
### 使用 Usage

```
SilkyAnimation mAnimation=
                new SilkyAnimation.Builder(mSurfaceView)
                .build();
```
##### 从文件目录中读取资源 get resources from a directory
```
//file为资源文件的目录 file is your resources directory
File file=new FIle(Environment.getExternalStorageDirectory() + File.separator + "bird")
mAnimation.start(file);

```
##### 从assets目录中读取资源 get resources from assets
如果传入的是assets下的*一级目录*，那么只需要传入文件夹名称，如果是*二级目录*，那么需要传入这个目录的完整路径。  
if your resources directory in the root directory of assets, just pass the file name, else pass the whole path. 

```
//一级目录 assets/bird
String asssetsPath="bird";
//二级目录 assets/bird/crow
String assetsPath="bird/crow";

mAnimation.start(assetsPath);

```
#### 更多设置 more setting
```                
new SilkyAnimation.Builder(mSurfaceView)
                //设置常驻内存的缓存数量, 默认5. 
                .setCacheCount(8)
                //设置帧间隔, 默认100
                .setFrameInterval(80)
                //设置缩放类型, 默认fit center
                .setScaleType(SilkyAnimation.SCALE_TYPE_FIT_END)
                //设置动画开始结束状态监听
                .setAnimationListener(listener)
                //设置循环模式, 默认不循环
                .setRepeatMode(SilkyAnimation.MODE_INFINITE)
                .build();
```
#### issue
如果使用中有任何问题，请直接[添加issues](https://github.com/yuyashuai/SilkyAnimation/issues/new),会及时回复。  
If there is any problem with this, [add issues](https://github.com/yuyashuai/SilkyAnimation/issues/new) directly.  

#### 更新 2017-11-24
增加了对帧图片缩放模式的支持,与ImageView的缩放模式一致，默认为`FIT_CENTER`，也可以通过`setMatrix()`方法来指定任意形变。
setMatrix()和setScaleType()不能同时生效
```
 new SilkyAnimation.Builder(mSurfaceView,file)
                .setRepeatMode(SilkyAnimation.MODE_ONCE)
                .setFrameInterval(80)
                .setScaleType(SilkyAnimation.SCALE_TYPE_CENTER_INSIDE)
                .build();


```

