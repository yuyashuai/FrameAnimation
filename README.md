# SurfaceViewFrameAnimation
Use SurfaceView instead of Android AnimationDrawable to achieve Frame Animation that contains lots of pictures.
effectively avoid OOM and ANR while decode many pictures.
用SurfaceView来播放帧动画，避免在很多帧的情况下使用AnimationDrawable带来的OOM和卡顿问题。
## 使用 
#### 从文件目录中读取资源
```
SurfaceViewAnimation surfaceViewAnimation=
                new SurfaceViewAnimation.Builder(mSurfaceView,file)
                .setRepeatMode(SurfaceViewAnimation.MODE_ONCE)
                .setFrameInterval(80)
                .build();
surfaceViewAnimation.start();
```
#### 从assets目录中读取资源
如果传入的是assets下的一级目录，那么只需要传入文件夹名称，如果是二级目录，那么需要传入这个目录的完整路径
```
SurfaceViewAnimation surfaceViewAnimation=
                new SurfaceViewAnimation.Builder(mSurfaceView,"crow")
                .setRepeatMode(SurfaceViewAnimation.MODE_INFINITE)
                .build();
```
#### 帧动画命名
推荐以数字名称，并且必须严格按照顺序，且长度保持一致。

#### 更新 2017-11-24
增加了对帧图片缩放模式的支持,与ImageView的缩放模式一致，默认为FIT_CENTER
```
 new SurfaceViewAnimation.Builder(mSurfaceView,file)
                .setRepeatMode(SurfaceViewAnimation.MODE_ONCE)
                .setFrameInterval(80)
                .setScaleType(SurfaceViewAnimation.SCALE_TYPE_CENTER_INSIDE)
                .build();


```

