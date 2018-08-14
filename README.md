## SilkyAnimation 
用SurfaceView来播放帧动画，避免在很多帧的情况下使用AnimationDrawable带来的OOM和卡顿问题。支持任意多帧的动画。
### 使用 Usage
#### Gradle引入
```
implementation 'com.yuyashuai.android:silkyanimation:1.1.6'
```
#### 用法
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
有任何问题，请直接[添加issues](https://github.com/yuyashuai/SilkyAnimation/issues/new)。  
If there is any problems, [add issues](https://github.com/yuyashuai/SilkyAnimation/issues/new) directly.  



