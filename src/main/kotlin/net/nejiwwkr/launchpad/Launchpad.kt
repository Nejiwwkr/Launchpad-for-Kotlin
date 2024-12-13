package net.nejiwwkr.launchpad

import javax.sound.midi.*

typealias MidiMessageProcessor = (MidiMessage, Long) -> Unit

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