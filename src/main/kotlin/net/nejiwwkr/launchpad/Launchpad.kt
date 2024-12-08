package net.nejiwwkr.launchpad

import java.util.*
import java.util.logging.Logger
import javax.sound.midi.*

typealias MidiMessageProcessor = (MidiMessage, Long) -> Unit
val logger: Logger = Logger.getLogger("nejiwwkr.launchpad")

class Launchpad {
    private var inputDevice: MidiDevice? = null
    private var outputDevice: MidiDevice? = null
    private var receiver: Receiver? = null
    val padLights: Array<Int> = Array(100){_ -> 0}

    /**
     * Initialize the launchpad device.
     * @param names the name of your launchpad name in system
     */
    @Throws(MidiUnavailableException::class)
    fun init(vararg names: String) {
        logger.info("Searching for devices...")
        val infos = MidiSystem.getMidiDeviceInfo()
        for (info in infos) {
            if (info.matches(names, "MIDIIN"))
                inputDevice = MidiSystem.getMidiDevice(info)
            if (info.matches(names, "MIDIOUT"))
                outputDevice = MidiSystem.getMidiDevice(info)
        }
        if (inputDevice == null || outputDevice == null) {
            throw MidiUnavailableException("未找到指定的 MIDI 设备")
        }
        inputDevice!!.open()
        outputDevice!!.open()
        println("成功连接" + inputDevice.toString())
        println("成功连接" + outputDevice.toString())
        receiver = outputDevice!!.receiver
    }

    /**
     * Process midi messages.
     * invoke repeatedly will overwrite the process before.
     * @param func the process
     */
    fun process(func: MidiMessageProcessor) {
        try {
            val transmitter = inputDevice!!.transmitter
            transmitter.receiver = object : Receiver {
                override fun send(message: MidiMessage, timeStamp: Long) = func(message, timeStamp)
                override fun close() {
                    transmitter.close()
                }
            }
        } catch (e: MidiUnavailableException) {
            logger.severe(e.stackTrace.contentToString())
            logger.severe(e.message)
        }
    }

    /**
     * Send midi messages.
     * Midi message for launchpad is a byte array with 3 elements, including the channel, the location and the light color.
     * @param message the message to send
     */
    @Throws(InvalidMidiDataException::class)
    fun sendShortMessage(message: ByteArray) {
        val shortMessage = ShortMessage()
        shortMessage.setMessage(message[0].toInt(), message[1].toInt(), message[2].toInt())
        receiver!!.send(shortMessage, -1)
    }

    /**
     * A shortcut to make lights of launchpad on
     * @param type the type of light-on
     * @param note the location of light-on, from 11 to 99
     * @param color the color of light-on, 0 for shutting lights
     * @see LightType
     */
    fun sendFeedbackMessage(type: LightType, note: Int, color: Int = 0) {
        try {
            val lightMessage = byteArrayOf(
                type.channel.toByte(),  // Note On, Channel 1
                note.toByte(),  // Pad 编号
                color.toByte() // 颜色
            )
            padLights[note] = color
            sendShortMessage(lightMessage)
        } catch (e: InvalidMidiDataException) {
            logger.severe(e.stackTrace.contentToString())
        }
    }
}

private fun MidiDevice.Info.matches(names: Array<out String>, direction: String): Boolean {
    return Arrays.stream(names)
        .anyMatch { name: String? -> this.name.contains(name!!) && this.name.contains(direction) }
}

enum class LightType(val channel: Int) {
    STATIC(0x90), FLASHING(0x91), PULSING(0x92)
}