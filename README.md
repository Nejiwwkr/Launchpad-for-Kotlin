# Launchpad for kotlin

This API is mainly designed for _Novation Launchpad MK3 Mini_. But I suppose this could still work for other launchpads.  

[中文文档](README-zh.md)

# Using the API

Initial your launchpad and then process midi messages.  
__For Novation users, you need to turn your launchpad to _programme mode_ first.__

## Initialization

Passing the identity name of your launchpad device to ```Launchpad.init()```.

```kotlin
val launchpad = Launchpad()
launchpad.init("LPMiniMK3")
```

Tips: If you don't know the device name of your launchpad, try the code below.  

```kotlin
val infos = MidiSystem.getMidiDeviceInfo()
for (info in infos) {
    println("Name: ${info.name}, Vendor: ${info.vendor}, Description: ${info.description}")
}
```

## Processing message

Use ```Launchpad.process(func: (MidiMessage, Long) -> Unit)``` to get and process the hit on your launchpad.  

Use ```Launchpad.sendShortMessage(message: IntArray)``` to send a simple message。 
But be careful, the message of _Novation Launchpad MK3 Mini_ is basically an integer array with 3 elements, including the channel, the location and the light color.
The location code of _Novation Launchpad MK3 Mini_ is from 11 to 99, counted from the left bottom. The location of ```[i][j]``` is actually ij.
The color code of _Novation Launchpad MK3 Mini_ is 0~127, check it out on the [Programmers Reference Manual](https://fael-downloads-prod.focusrite.com/customer/prod/s3fs-public/downloads/Launchpad%20Mini%20-%20Programmers%20Reference%20Manual.pdf)  

But ```Launchpad.sendFeedbackMessage(type: LightType, note: Int, color: Int)``` is more recommended.

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