## FrameAnimation 
用TextureView或SurfaceView 高性能播放帧动画，避免在很多帧的情况下使用AnimationDrawable带来的OOM和卡顿问题。

### 下载 Download
#### jar
[download jar](http://jcenter.bintray.com/com/yuyashuai/android/silkyanimation/1.1.6/silkyanimation-1.1.6-sources.jar)
### 使用 Usage

```
        TextureView textureView = findViewById(R.id.texture_view);
        frameAnimation = new FrameAnimation(textureView);
        frameAnimation.playAnimationFromAssets("zone");
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

```
#### issue
有问题[加issues](https://github.com/yuyashuai/SilkyAnimation/issues/new)。  



