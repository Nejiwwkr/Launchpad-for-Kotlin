# Launchpad for kotlin

为 _Novation Launchpad MK3 Mini_ 设计的 API. 但对其他大部分厂商的 launchpad 应该仍然适用

[English Document](README.md)

# API 使用方法

先初始化launchpad，再处理midi消息。  
__对于 Novation 使用者, 你需要先将你的 launchpad 调至 _programme mode_。__

## 初始化 launchpad

将你的launchpad系统名传递给 ```Launchpad.init()```.

```kotlin
val launchpad = Launchpad()
launchpad.init("LPMiniMK3")
```

提示: 如果你不知道你的launchpad设备名称，你可以试试运行以下代码。  

```kotlin
val infos = MidiSystem.getMidiDeviceInfo()
for (info in infos) {
    println("Name: ${info.name}, Vendor: ${info.vendor}, Description: ${info.description}")
}
```


## 处理 midi 信息

使用 ```Launchpad.process(func: (MidiMessage, Long) -> Unit)``` 获取，处理midi信号的逻辑  

使用 ```Launchpad.sendShortMessage(message: IntArray)``` 发送简单信号，对 launchpad 而言是灯光信息。
但请注意，_Novation Launchpad MK3 Mini_ 需要的 midi 信息是一个包含三个数字的数组，分别表示通道，灯光位置，灯光颜色。
_Novation Launchpad MK3 Mini_ 的灯光位置索引从11到99, 左下角开始。 ```[i][j]``` 对应的位置的编码是 ij.
_Novation Launchpad MK3 Mini_ 的颜色值可取 0~127, 可以在 [Programmers Reference Manual](https://fael-downloads-prod.focusrite.com/customer/prod/s3fs-public/downloads/Launchpad%20Mini%20-%20Programmers%20Reference%20Manual.pdf) 上参考查看  

但其实更推荐这个方法：```Launchpad.sendFeedbackMessage(type: LightType, note: Int, color: Int)```

```kotlin
launchpad.process { message: MidiMessage, _: Long ->
    if (message is ShortMessage) {
        if (message.data2 > 0) {
            launchpad.sendFeedbackMessage(LightType.STATIC, message.data1, 5) //sending the red light
        } else {
            launchpad.sendFeedbackMessage(LightType.STATIC, message.data1) //removing light
        }
    }
}
```