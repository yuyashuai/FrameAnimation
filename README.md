## FrameAnimation 
用TextureView或SurfaceView 高性能播放帧动画，避免在很多帧的情况下使用AnimationDrawable带来的OOM和卡顿问题。

***华为 mate 20x 1920×1080 24bit color JPG 201frames 24fps 测试效果***

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
frameAnimation.setScaleType()//设置缩放类型，即时生效
frameAnimation.setRepeatMode()//设置循环播放模式，下次播放生效
frameAnimation.setFrameInterval()//设置帧间隔，默认42ms,如果设置过小，会以能达到的最快速度播放，及时生效
```
#### 已知问题&注意事项

* 由于 Bitmap reuse 问题，如果上个动画正在播放，有直接调用了`playAnimation`方法，务必保证两组动画的分辨率相同，或第二组动画图片占用内存的大小小于上组动画。否则请先调用 `stopAnimation()`停止后再播放。
* 如果帧动画的分辨率不一致，请设置`setSupportInBitmap(false)`关闭 bitmap 的复用，但是关闭复用后会造成频繁GC，因此最好使所有帧分辨率保持一致
* 请根据 View 的生命周期，及时停止动画的播放。比如 activity在 `onPause()`或者`onDestory()`中调用`stopAnimation()`, 接下来将会把动画封装在View 中。
* 关于动画的监听，现在只提供了动画的开始和结束监听，后续会补上progress 和 repeat 的监听。此外你也可以通过自定义[RepeatStrategy](https://github.com/yuyashuai/FrameAnimation/blob/master/frameanimation/src/main/java/com/yuyashuai/frameanimation/repeatmode/RepeatStrategy.kt) 将监听事件插入其中。
#### TextureView 还是 SurfaceView
[TextureView](https://developer.android.com/reference/android/view/TextureView)必须运行在支持硬件加速的上，与[SurfaceView](https://developer.android.com/reference/android/view/SurfaceView) 不同，不会单独创建 window，因此可以和常规 View 进行变换等操作，更多请参考官方[Wiki](https://developer.android.com/reference/android/view/TextureView). 
#### todolist
1. AnimationView
2. 完善动画监听
3. 抽离更多配置选项
#### issue

有问题[加issues](https://github.com/yuyashuai/SilkyAnimation/issues/new)。  

