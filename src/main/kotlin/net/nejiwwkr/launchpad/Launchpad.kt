package net.nejiwwkr.launchpad

import java.util.*
import java.util.logging.Logger
import javax.sound.midi.*

typealias MidiMessageProcessor = (MidiMessage, Long) -> Unit
val logger: Logger = Logger.getLogger("nejiwwkr.launchpad")

class Launchpad: BaseLaunchpad() {
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
    override fun sendShortMessage(message: IntArray) {
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
    override fun sendFeedbackMessage(type: Int, note: Int, color: Int) {
        sendFeedbackMessage(phraseIntToLightType(type), note, color)
    }

    /**
     * A shortcut to make lights of launchpad on
     * @param type the type of light-on
     * @param note the location of light-on, from 11 to 99
     * @param color the color of light-on, 0 for shutting lights
     * @see LightType
     */
    override fun sendFeedbackMessage(type: LightType, note: Int, color: Int) {
        try {
            val lightMessage = intArrayOf(
                type.channel,  // Note On, Channel 1
                note,  // Location of light
                color // Color of light
            )
            padLights[note] = color
            sendShortMessage(lightMessage)
        } catch (e: InvalidMidiDataException) {
            logger.severe(e.stackTrace.contentToString())
        }
    }


}