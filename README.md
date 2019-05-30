## FrameAnimation 
用TextureView或SurfaceView 高性能播放帧动画，避免在很多帧的情况下使用AnimationDrawable带来的OOM和卡顿问题。

***华为 mate 20X 1920×1080 24bit color JPG 201frames 24fps 测试效果***

![example](https://github.com/yuyashuai/PictureBed/blob/master/SVID_20190509_163330_1.gif?raw=true)

### download
#### use Gradle
1. project gradle
```groovy
...
    repositories {
        ...
        jcenter()
    }
...
```
2. module gradle
```groovy
 implementation 'com.yuyashuai.frameanimation:frameanimation:2.0.2'
```

### usage
**xml**

```xml
 <com.yuyashuai.frameanimation.FrameAnimationView
        android:id="@+id/animationView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>
```
**java**

```java
FrameAnimationView animationView = findViewById(R.id.animationView);
//从 assets 读取资源
animationView.playAnimationFromAssets("zone");
//从 file 读取资源
//animationView.playAnimationFromFile(filePath);
```
#### more settings
```                java
//设置缩放类型，播放中立即生效
animationView.setScaleType()
//设置循环播放模式，下次播放生效
animationView.setRepeatMode()
//设置帧间隔，默认42ms,如果设置过小，会以能达到的最快速度播放，播放中立即生效
animationView.setFrameInterval()
```
> 自定义播放顺序，循环模式，参考[RepeatMode](https://github.com/yuyashuai/FrameAnimation/tree/master/frameanimation/src/main/java/com/yuyashuai/frameanimation/repeatmode),实现自定义播放策略
#### known issues

* 切换动画时请**不要**调用 `stopAnimation()`停止动画，直接播放新动画即可。
* 不要在 RecyclerView 或者 ListView 中使用
#### TextureView or SurfaceView
[TextureView](https://developer.android.com/reference/android/view/TextureView)必须运行在支持硬件加速的上，与[SurfaceView](https://developer.android.com/reference/android/view/SurfaceView) 不同，不会单独创建 window，因此可以和常规 View 进行变换等操作，更多请参考官方[Wiki](https://developer.android.com/reference/android/view/TextureView). 

#### issue

有问题[加issue](https://github.com/yuyashuai/SilkyAnimation/issues/new)。  
