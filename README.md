## SilkyAnimation
Use SurfaceView instead of Android AnimationDrawable to achieve Frame Animation that contains lots of pictures.
effectively avoid OOM and ANR while decode many pictures.
用SurfaceView来播放帧动画，避免在很多帧的情况下使用AnimationDrawable带来的OOM和卡顿问题。
## 使用 
#### 从文件目录中读取资源
```
SilkyAnimation mAnimation=
                new SilkyAnimation.Builder(mSurfaceView,file)
                .setRepeatMode(SilkyAnimation.MODE_ONCE)
                .setFrameInterval(80)
                .build();
mAnimation.start();
```
#### 从assets目录中读取资源
如果传入的是assets下的一级目录，那么只需要传入文件夹名称，如果是二级目录，那么需要传入这个目录的完整路径
```
SilkyAnimation mAnimation=
                new SilkyAnimation.Builder(mSurfaceView,"crow")
                .setRepeatMode(SilkyAnimation.MODE_INFINITE)
                .build();
```
#### 帧动画命名
推荐以数字名称，并且必须严格按照顺序，且长度保持一致。
#### issue
如果使用中有任务问题，请直接[添加issue](https://github.com/yuyashuai/SilkyAnimation/issues/new),会及时回复

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

