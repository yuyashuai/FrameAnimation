## SilkyAnimation
Use SurfaceView instead of Android AnimationDrawable to achieve Frame Animation that contains lots of pictures.
effectively avoid OOM and ANR while decode many pictures.  
用SurfaceView来播放帧动画，避免在很多帧的情况下使用AnimationDrawable带来的OOM和卡顿问题。支持任意多帧的动画。
[CSDN](http://blog.csdn.net/qq_16445551/article/details/53367173)
### 下载 Download
#### jar
[download jar](http://jcenter.bintray.com/com/yuyashuai/android/silkyanimation/1.1.6/silkyanimation-1.1.6-sources.jar)
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
                //设置缩放类型, 默认fit center，与ImageView的缩放模式通用
                .setScaleType(SilkyAnimation.SCALE_TYPE_FIT_END)
                //设置动画开始结束状态监听
                .setAnimationListener(listener)
                //设置是否支持bitmap复用，默认为true
                .setSupportInBitmap(false)
                //设置循环模式, 默认不循环
                .setRepeatMode(SilkyAnimation.MODE_INFINITE)
                .build();
```
#### issue
如果使用中有任何问题，请直接[添加issues](https://github.com/yuyashuai/SilkyAnimation/issues/new),会及时回复。  
If there is any problems, [add issues](https://github.com/yuyashuai/SilkyAnimation/issues/new) directly.  



