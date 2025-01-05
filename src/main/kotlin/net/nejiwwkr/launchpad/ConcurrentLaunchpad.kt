package net.nejiwwkr.launchpad

import java.util.concurrent.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import javax.sound.midi.*

/**
 * function for (commandType, velocity) -> Process
 */
typealias MidiListener = (Int, Int) -> Unit

class ConcurrentLaunchpad: BaseLaunchpad() {
    private val listeners = ConcurrentHashMap<Int, MidiListener>()
    private val executorService: ExecutorService = Executors.newCachedThreadPool()
    private val padLightsLock = ReentrantReadWriteLock()

    init {
        Runtime.getRuntime().addShutdownHook(Thread { shutdown() } )
    }

    /**
     * Set a listener for a specific button position.
     * @param func a function of [MidiListener] as the listener
     * @param pos the certain position
     * @see setOnCrossPatternListener
     * @see setOnLongClickListener
     */
    fun setOnListener(pos: Int, func: MidiListener) {
        listeners[pos] = func
    }

    /**
     * A shortcut to make lights of launchpad on, always thread safe
     * @param type the type of light-on
     * @param note the location of light-on, from 11 to 99
     * @param color the color of light-on, 0 for shutting lights
     * @see LightType
     */
    override fun sendFeedbackMessage(type: LightType, note: Int, color: Int) {
        padLightsLock.writeLock().lock()
        try {
            sendShortMessage(intArrayOf(type.channel, note, color))
            padLights[note] = color
        } finally {
            padLightsLock.writeLock().unlock()
        }
    }

    /**
     * A shortcut to make lights of launchpad on
     * @param type the type of light-on
     * @param note the location of light-on, from 11 to 99
     * @param color the color of light-on, 0 for shutting lights
     * @see LightType
     */
    override fun sendFeedbackMessage(type: Int, note: Int, color: Int) {
        sendFeedbackMessage(LightType.fromInt(type), note, color)
    }

    /**
     * A thread safe version to get current light color of a button.
     * @param note the location of light-on, from 11 to 99
     */
    override fun getCurrentLight(note: Int): Int {
        padLightsLock.readLock().lock()
        try {
            return padLights[note]
        } finally {
            padLightsLock.readLock().unlock()
        }
    }

    /**
     * Shutdown the service and close devices.
     */
    fun shutdown() {
        executorService.shutdownNow()
        inputDevice?.close()
        outputDevice?.close()
        logger.info("Shutting down MIDI devices...")
    }

    fun start() {
        val transmitter = inputDevice!!.transmitter
        transmitter.receiver = object : Receiver {
            override fun send(message: MidiMessage, timeStamp: Long) {
                if (message is ShortMessage) {
                    val commandType = message.command
                    val note = message.data1
                    val velocity = message.data2
                    listeners[note]?.let { listener ->
                        executorService.submit {
                            listener(commandType, velocity)
                        }
                    }
                }
            }

            override fun close() {
                transmitter.close()
            }
        }
    }
}