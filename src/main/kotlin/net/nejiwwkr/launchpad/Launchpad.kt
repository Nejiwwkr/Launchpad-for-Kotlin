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
    private val padLights: Array<Int> = Array(100){_ -> 0}

    /**
     * Initialize the launchpad device.
     * @param name the name of your launchpad name in system
     */
    @Throws(MidiUnavailableException::class)
    fun init(name: String) {
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
        logger.info("Successfully Connected to:" + inputDevice.toString())
        logger.info("Successfully Connected to:" + outputDevice.toString())
        receiver = outputDevice!!.receiver
    }

    /**
     * Sets the processor for handling incoming MIDI messages.<br>
     * Calling this method multiple times will replace any previously set message processor with the new one.<br>
     * To retain multiple processing logics, implement composite logic within a single processor function.<p>
     *
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
    fun sendShortMessage(message: IntArray) {
        val shortMessage = ShortMessage()
        shortMessage.setMessage(message[0], message[1], message[2])
        receiver!!.send(shortMessage, -1)
    }

    /**
     * A shortcut to make lights of launchpad on
     * @param type the type of light-on
     * @param note the location of light-on, from 11 to 99
     * @param color the color of light-on, 0 for shutting lights
     * @see LightType
     */
    fun sendFeedbackMessage(type: Int = 0x90, note: Int, color: Int = 0) {
        try {
            val lightMessage = intArrayOf(
                type,  // Note On, Channel 1
                note,  // Location of light
                color // Color of light
            )
            padLights[note] = color
            sendShortMessage(lightMessage)
        } catch (e: InvalidMidiDataException) {
            logger.severe(e.stackTrace.contentToString())
        }
    }

    /**
     * A shortcut to make lights of launchpad on
     * @param type the type of light-on
     * @param note the location of light-on, from 11 to 99
     * @param color the color of light-on, 0 for shutting lights
     * @see LightType
     */
    fun sendFeedbackMessage(type: LightType = LightType.STATIC, note: Int, color: Int = 0) {
        sendFeedbackMessage(type.channel, note, color)
    }

    /**
     * Get current light color.
     * @param note the location of light-on, from 11 to 99
     */
    fun getCurrentLight(note: Int): Int = padLights[note]
}

private fun MidiDevice.Info.matches(name: String, direction: String): Boolean {
    return this.name.contains(name) && this.name.contains(direction)
}

enum class LightType(val channel: Int) {
    STATIC(0x90), FLASHING(0x91), PULSING(0x92)
}