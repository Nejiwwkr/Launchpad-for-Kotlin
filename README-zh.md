# Launchpad for Kotlin

为 _Novation Launchpad MK3 Mini_ 设计的 API. 但对其他大部分厂商的 launchpad 应该仍然适用

[English Document](README.md)

## 简介

Launchpad for Kotlin 库为在 Kotlin 应用程序中通过 MIDI 控制 _Novation Launchpad MK3 Mini_ 提供了一个易于使用的接口。
该库抽象化了发送和接收 MIDI 消息的大部分复杂性，使开发人员能够专注于实现他们希望从 Launchpad 设备获得的行为。

## 特性摘要

- 初始化并连接到一个 Launchpad 设备。
- 发送 MIDI 消息以控制 Launchpad 的 LED 灯。
- 接收来自 Launchpad 按钮按下的 MIDI 消息。
- 支持 MIDI 事件的并发处理。

## API 使用方法提示

先初始化 launchpad。再处理 midi 消息。  
__对于 Novation 使用者, 你需要先将你的 launchpad 调至 _programme mode_。__

### 初始化 launchpad

将你的 launchpad 系统名传递给 `Launchpad.init()`.

```kotlin
val launchpad = Launchpad()
launchpad.init("LPMiniMK3")
```

提示: 如果你不知道你的 launchpad 设备名称，你可以试试运行以下代码。  

```kotlin
val infos = MidiSystem.getMidiDeviceInfo()
for (info in infos) {
    println("Name: ${info.name}, Vendor: ${info.vendor}, Description: ${info.description}")
}
```

### 处理 midi 信息

使用 `Launchpad.process(func: (MidiMessage, Long) -> Unit)` 获取，处理 midi 信号的逻辑  

使用 `Launchpad.sendShortMessage(message: IntArray)` 发送简单信号，对 launchpad 而言是灯光信息。
但请注意，_Novation Launchpad MK3 Mini_ 需要的 midi 信息是一个包含三个数字的数组，分别表示通道，灯光位置，灯光颜色。
_Novation Launchpad MK3 Mini_ 的灯光位置索引从11到99, 左下角开始。 `[i][j]` 对应的位置的编码是 ij.
_Novation Launchpad MK3 Mini_ 的颜色值可取0~127, 可以在 [Programmers Reference Manual](https://fael-downloads-prod.focusrite.com/customer/prod/s3fs-public/downloads/Launchpad%20Mini%20-%20Programmers%20Reference%20Manual.pdf) 上参考查看  

但其实更推荐这个方法：`Launchpad.sendFeedbackMessage(type: LightType, note: Int, color: Int)`

```kotlin
launchpad.process { message: MidiMessage, _: Long ->
    if (message is ShortMessage) {
        if (message.data2 > 0) {
            launchpad.sendFeedbackMessage(LightType.STATIC, message.data1, 5) //发送红色灯光信号
        } else {
            launchpad.sendFeedbackMessage(LightType.STATIC, message.data1) //移除灯光
        }
    }
}
```

### 并发处理

对于需要多个监听器或并发事件处理的场景，请使用 `ConcurrentLaunchpad` 类而不是 `Launchpad` 类。
此类支持为特定按钮位置注册监听器，并管理用于事件处理的线程。

```kotlin
val concurrentLaunchpad = ConcurrentLaunchpad()
concurrentLaunchpad.init("LPMiniMK3")
concurrentLaunchpad.setOnListener(11){ _: Int, velocity: Int -> // 指令类型，力度
    if (velocity > 0) {
        concurrentLaunchpad.sendFeedbackMessage(LightType.STATIC, 11, 5) //发送红色灯光信号
    } else {
        concurrentLaunchpad.sendFeedbackMessage(LightType.STATIC, 11) //移除灯光
    }
}
concurrentLaunchpad.start()
```

不妨尝试更加简洁的拓展方法：

```kotlin
val concurrentLaunchpad = ConcurrentLaunchpad()
concurrentLaunchpad.init("LPMiniMK3")
concurrentLaunchpad.setOnSingleClickListener(11, 5)
concurrentLaunchpad.start()
```

#### 释放资源

当你使用完 `ConcurrentLaunchpad` 后，记得关闭连接以释放资源：

```kotlin
concurrentLaunchpad.shutdown()
```