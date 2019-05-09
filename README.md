## FrameAnimation 
用TextureView或SurfaceView 高性能播放帧动画，避免在很多帧的情况下使用AnimationDrawable带来的OOM和卡顿问题。

***华为 mate 20x  图片1080p 24fps 测试效果***

![example](https://github.com/yuyashuai/PictureBed/blob/master/SVID_20190509_163330_1.gif?raw=true)

### 下载
[download aar](https://dl.bintray.com/yuyashuai/android/com/yuyashuai/android/frameanimation/2.0.0/:frameanimation-2.0.0.aar)

### 使用 

```
TextureView textureView = findViewById(R.id.texture_view);
FrameAnimation frameAnimation = new FrameAnimation(textureView);
frameAnimation.playAnimationFromAssets("zone");
```
##### 从文件目录中读取资源
```
frameAnimation.playAnimationFromFile(filePath);
```
##### 从assets目录中读取资源
如果传入的是assets下的*一级目录*，那么只需要传入文件夹名称，如果是*二级目录*，那么需要传入这个目录的完整路径。  
if your resources directory in the root directory of assets, just pass the file name, else pass the whole path. 

```
//assets 下一级目录 assets/bird
frameAnimation.playAnimationFromAssets("bird");
//assets 下二级目录 assets/bird/crow
frameAnimation.playAnimationFromAssets("bird/crow");
```
#### 更多设置 more setting
```                
frameAnimation.setScaleType()//设置缩放类型
frameAnimation.setRepeatMode()//设置循环播放模式
frameAnimation.setFrameInterval()//设置帧间隔，默认42ms,如果设置过小，会以能达到的最快速度播放
```
#### 已知问题&注意事项

* 由于 Bitmap reuse 问题，如果上个动画正在播放，有直接调用了`playAnimation`方法，务必保证两组动画的分辨率相同，或者 第二张动画图片decode进内存的大小小于上组动画。负责请先调用 `stopAnimation()`停止后再播放。
* 请根据 View 的生命周期，及时停止动画的播放。比如 activity在 `onPause()`或者`onDestory()`中调用`stopAnimation()`, 接下来将会把动画封装在View 中。

#### issue

有问题[加issues](https://github.com/yuyashuai/SilkyAnimation/issues/new)。  

