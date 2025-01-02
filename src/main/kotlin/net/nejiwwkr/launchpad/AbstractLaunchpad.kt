package net.nejiwwkr.launchpad

import java.util.logging.Logger
import javax.sound.midi.*

val logger: Logger = Logger.getLogger("nejiwwkr.launchpad")

interface AbstractLaunchpad {
    fun init(name: String)
    fun sendShortMessage(message: IntArray)
    fun sendFeedbackMessage(type: LightType = LightType.STATIC, note: Int, color: Int = 0)
    fun sendFeedbackMessage(type: Int = 0x90, note: Int, color: Int = 0)
    fun getCurrentLight(note: Int): Int
}

abstract class BaseLaunchpad: AbstractLaunchpad {
    protected open var inputDevice: MidiDevice? = null
    protected open var outputDevice: MidiDevice? = null
    private var receiver: Receiver? = null
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
        receiver = outputDevice!!.receiver
    }

    /**
     * Send midi messages.
     * Midi message for launchpad is a byte array with 3 elements, including the channel, the location and the light color.
     * @param message the message to send
     */
    @Throws(InvalidMidiDataException::class)
    override fun sendShortMessage(message: IntArray) {
        val shortMessage = ShortMessage()
        shortMessage.setMessage(message[0], message[1], message[2])
        receiver!!.send(shortMessage, -1)
    }

    /**
     * Default Implement to get current light color.
     * @param note the location of light-on, from 11 to 99
     */
    override fun getCurrentLight(note: Int): Int = padLights[note]
}

enum class LightType(val channel: Int) {
    STATIC(0x90), FLASHING(0x91), PULSING(0x92);
    companion object {
        @JvmStatic
        fun fromInt(type: Int): LightType {
            return when(type) {
                0x90 -> STATIC
                0x91 -> FLASHING
                0x92 -> PULSING
                else -> throw IllegalStateException()
            }
        }
    }
}


fun MidiDevice.Info.matches(name: String, direction: String): Boolean {
    return this.name.contains(name) && this.name.contains(direction)
}