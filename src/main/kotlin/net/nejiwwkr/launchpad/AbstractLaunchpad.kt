package net.nejiwwkr.launchpad

import java.net.PortUnreachableException
import javax.sound.midi.MidiDevice
import javax.sound.midi.MidiSystem
import javax.sound.midi.MidiUnavailableException
import javax.sound.midi.Receiver

interface AbstractLaunchpad {
    fun init(name: String)
    fun sendShortMessage(message: IntArray)
    fun sendFeedbackMessage(type: LightType = LightType.STATIC, note: Int, color: Int = 0)
    fun sendFeedbackMessage(type: Int = 0x90, note: Int, color: Int = 0)
    fun getCurrentLight(note: Int): Int
}

abstract class BaseLaunchpad : AbstractLaunchpad {
    protected open var inputDevice: MidiDevice? = null
    protected open var outputDevice: MidiDevice? = null
    var receiver: Receiver? = null
    val padLights: Array<Int> = Array(100){_ -> 0}

    /**
     * Initialize the launchpad device.
     * @param name the name of your launchpad name in system
     */
    @Throws(MidiUnavailableException::class)
    override fun init(name: String) {
        logger.info("Searching for devices...")
        val infos = MidiSystem.getMidiDeviceInfo()
        for (info in infos) {
            if (info.matches(name, "MIDIIN"))
                inputDevice = MidiSystem.getMidiDevice(info)
            if (info.matches(name, "MIDIOUT"))
                outputDevice = MidiSystem.getMidiDevice(info)
        }
        if (inputDevice == null || outputDevice == null) {
            logger.severe("Can't find specific midi device.")
            throw MidiUnavailableException("Can't find specific midi device.")
        }
        inputDevice!!.open()
        outputDevice!!.open()
        logger.info("Successfully Connected to: ${inputDevice.toString()}")
        logger.info("Successfully Connected to: ${outputDevice.toString()}")
    }

    /**
     * Default Implement: Get current light color.
     * @param note the location of light-on, from 11 to 99
     */
    override fun getCurrentLight(note: Int): Int = padLights[note]
}

enum class LightType(val channel: Int) {
    STATIC(0x90), FLASHING(0x91), PULSING(0x92)
}

fun phraseIntToLightType(type: Int): LightType {
    return when(type) {
        0x90 -> LightType.STATIC
        0x91 -> LightType.FLASHING
        0x92 -> LightType.PULSING
        else -> throw PortUnreachableException()
    }
}

fun MidiDevice.Info.matches(name: String, direction: String): Boolean {
    return this.name.contains(name) && this.name.contains(direction)
}