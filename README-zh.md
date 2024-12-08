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

## 处理 midi 信息

使用 ```Launchpad.process(func: (MidiMessage, Long) -> Unit)``` 写入获取，处理midi信号的逻辑  
使用 ```Launchpad.sendShortMessage(message: ByteArray)``` 发送简单信号，对 launchpad 而言是灯光信息。
但请注意，_Novation Launchpad MK3 Mini_ 需要的 midi 信息是一个包含三元素的字节数组，分别表示通道，灯光位置，灯光颜色。
_Novation Launchpad MK3 Mini_ 的灯光位置索引从11到99, 左下角开始， ```[i][j]``` 对应的位置的编码是 ij.
_Novation Launchpad MK3 Mini_ 的颜色值可取 0~127, 可以在 [Programmers Reference Manual](https://fael-downloads-prod.focusrite.com/customer/prod/s3fs-public/downloads/Launchpad%20Mini%20-%20Programmers%20Reference%20Manual.pdf) 上参考查看
但其实更推荐这个方法：```Launchpad.sendFeedbackMessage(type: LightType, note: Int, color: Int)```

```kotlin
launchpad.process { message: MidiMessage, _: Long ->
    if (message is ShortMessage) {
        if (message.command == ShortMessage.NOTE_ON) {
            if (data2 > 0) {
                launchpad.sendFeedbackMessage(LightType.STATIC, message.data1, 5) //使按下的键位亮起红色灯光
                Thread.sleep(50L)
                launchpad.sendFeedbackMessage(LightType.STATIC, message.data1) //清除灯光
            }
        }
    }
}
```