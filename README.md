# SurfaceViewFrameAnimation
Use SurfaceView instead of Android AnimationDrawable to achieve Frame Animation that contains lots of pictures.
This way you can effectively avoid the Out Of Memory and Application is not responding while you decode many pictures from local.   
用SurfaceView来播放帧动画，避免在很多帧的情况下使用AnimationDrawable带来的OOM和卡顿问题。
## 使用

```
SurfaceViewAnimation surfaceViewAnimation=
                new SurfaceViewAnimation.Builder(mSurfaceView,file)
                .setRepeatMode(SurfaceViewAnimation.MODE_ONCE)
                .setFrameInterval(80)
                .build();
surfaceViewAnimation.start();
```
