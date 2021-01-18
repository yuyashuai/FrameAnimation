## FrameAnimation

用TextureView或SurfaceView 高性能播放帧动画，避免在很多帧的情况下使用AnimationDrawable带来的OOM和卡顿问题。

***华为 mate 20X 1920×1080 24bit color JPG 201frames 24fps 测试效果***

![example](https://github.com/yuyashuai/PictureBed/blob/master/SVID_20190509_163330_1.gif?raw=true)

### download

#### use Gradle

1. project gradle

```groovy
//...
    repositories {
        //...
        jcenter()
    }
//...
```

2. module gradle

```groovy
 implementation 'com.yuyashuai.frameanimation:frameanimation:2.3.6'
```

### usage

#### xml

```xml
 <com.yuyashuai.frameanimation.FrameAnimationView
    android:id="@+id/animationView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"/>
```

**务必将`AnimationView`的生命周期与所在的`Activity`或`Fragment`绑定**

````kotlin
 override fun onPause() {
    animationView.onPause()
    super.onPause()
 }

 override fun onResume() {
    super.onResume()
    animationView.onResume()
 }
````

> 不绑定，在播放的过程中进行页面跳转，可能会导致crash。

#### 动画播放

##### 1. 从文件夹中读取

````kotlin
//传入文件路径
animationView.playAnimationFromFile("zone720p")
````

##### 2. 从assets目录中读取

```kotlin
//传入文件夹在assets中的路径
animationView.playAnimationFromAssets("zone720p")
```

##### 3. 自定义、组合播放等

通过调用`playAnimation(paths: MutableList<FrameAnimation.PathData>)`方法实现自定义资源的播放。

例：将assets中多个目录的动画合并为一个播放

```kotlin
val paths = FrameAnimationUtil.getPathList(applicationContext, "zone720p", "traffic720p")
animationView.playAnimation(paths)
```

#### 设置循环模式

通过`animationView.setRepeatMode()`，设置循环模式，内置了5种循环模式，你也可以参考[RepeatMode](https://github.com/yuyashuai/FrameAnimation/tree/master/frameanimation/src/main/java/com/yuyashuai/frameanimation/repeatmode)中的实现，通过实现`RepeatStrategy`接口来定义循环播放策略。

内置的5种循环播放模式，以**正常顺序为1, 2, 3, 4, 5为例**

1. `animationView.setRepeatMode(FrameAnimation.RepeatMode.INFINITE)`

   **重复播放：** 播放顺序：1, 2, 3, 4, 5, 1, 2, 3, 4, 5...

2. `animationView.setRepeatMode(FrameAnimation.RepeatMode.ONCE)`

   **单次播放：** 播放顺序：1, 2, 3, 4, 5

3. `animationView.setRepeatMode(FrameAnimation.RepeatMode.REVERSE_INFINITE)`

   **往复循环：** 播放顺序1, 2, 3, 4, 5, 4, 3, 2, 1, 2, 3, 4, 5...

4. `animationView.setRepeatMode(FrameAnimation.RepeatMode.REVERSE_ONCE)`

   **往复一次：** 播放顺序1, 2, 3, 4, 5, 4, 3, 2, 1

5. `animationView.setRepeatMode(RepeatTail(3))`
   从指定帧开始循环播放

   **尾部循环：** 播放顺序1, 2, 3, 4, 5, 3, 4, 5, 3, 4, 5...

> 由于没有对传入文件夹中的非图片文件进行过滤，请保证传入文件夹内皆为有效图片，否则会造成crash。

#### 其它设置

##### 设置缩放模式

`animationView.setScaleType()`

参考`ImageView`的缩放模式，该设置播放中立即生效，也可以通过`setMatrix(matrix)`直接设置变换矩阵。

##### 设置帧间隔

单位ms

`animationView.setFrameInterval(12)`

> 帧间隔默认为42ms≈24fps，具体播放速度受设备性能影响，如果设置过小，会以设备能达到的最快速度播放，该设置播放中立即生效

##### 设置自动恢复播放

`animationView.restoreEnable=true`

当View不可见时（包含页面跳转等情况），将自动停止播放并释放部分资源。

停止播放时会保留上次的播放记录，再次进入默认会自动恢复播放，如果不想自动恢复，可以设置`restoreEnable=false`。

##### 设置是否支持图片复用

> 图片复用能够避免内存抖动，但是要求所有图片大小（**分辨率与色位**）必须一致（或后一帧的大小总是小于前一帧），**因此强烈建议所有帧图片的大小都一致**。

`animationView.setSupportInBitmap(true)`

默认为ture，当设置为false时，将带来显著的内存抖动。**如果你的所有帧图片大小一致，则不用设置**，如果不一致请设置为false。

> Ps. 如果设置为ture但是图片大小不一致会导致复用失败，然后再次尝试以不复用的方式加载图片，所以图片大小不一致的情况下设置为false能带来性能的提升。

##### 设置播放后不清屏

`animationView.freezeLastFrame(true)`

动画播放结束后，将会保留最后一帧，默认清屏。

##### 设置动画播放监听

`animationView.setAnimationListener()`

##### 所有设置

参考 [AnimationController](https://github.com/yuyashuai/FrameAnimation/blob/master/frameanimation/src/main/java/com/yuyashuai/frameanimation/AnimationController.kt)

### 注意事项

* 不兼容`RecyclerView`或者`ListView`进行兼容。**不要在`RecyclerView`或者`ListView`中使用**
* **务必将`AnimationView`的生命周期与所在的`Activity`或`Fragment`绑定**
* 如果在`Dialog`或`PopupWindow`等拥有单独`window`的容器中播放，需设置`animationView.autoRelease=false`，以保证`dismiss`后可以再次播放。

### TextureView or SurfaceView

[TextureView](https://developer.android.com/reference/android/view/TextureView)必须运行在支持硬件加速的上，与[SurfaceView](https://developer.android.com/reference/android/view/SurfaceView) 不同，可以和常规View进行变换等操作，更多请参考官方[Wiki](https://developer.android.com/reference/android/view/TextureView).

### issue

有问题[加issue](https://github.com/yuyashuai/SilkyAnimation/issues/new)。
### 最后
对于复杂的动效，帧动画体积大、内场占用高、CPU占用高，可能是很差强人意的解决方案了。如果你们开发、设计资源允许的话，或许你可以采用更好的解决方案。比如
* [Spine](http://zh.esotericsoftware.com/)
* [Lottie](https://github.com/airbnb/lottie-android)
> 但是上面的解决方案对于设计来说的确又是一个考验，像lottie要求用必须用AE设计动画并导出（劝退了一波用Flash/Animator的人），而且要求AE素材中不能有非矢量图，不然多帧一样OOM（又劝退了一波用AE的，比如我们很多AE的素材直接来自PS而不是AI）。
