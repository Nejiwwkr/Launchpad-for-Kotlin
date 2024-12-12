package net.nejiwwkr.launchpad

import java.util.concurrent.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import javax.sound.midi.*

typealias MidiListener = (Int, Int) -> Unit // (commandType, velocity) -> Unit

class ConcurrentLaunchpad: BaseLaunchpad() {
    private val listeners = ConcurrentHashMap<Int, MidiListener>()
    private val executorService: ExecutorService = Executors.newCachedThreadPool()
    private val padLightsLock = ReentrantReadWriteLock()

    init {
        Runtime.getRuntime().addShutdownHook(Thread { close() })
    }

    /**
     * Set a listener for a specific button position.
     */
    fun setOnListener(func: MidiListener, pos: Int) {
        listeners[pos] = func
    }

    /**
     * Send a MIDI message to the Launchpad.
     */
    @Throws(InvalidMidiDataException::class)
    override fun sendShortMessage(message: IntArray) {
        val shortMessage = ShortMessage()
        shortMessage.setMessage(message[0],message[1],message[2])
        receiver?.send(shortMessage, -1)
    }

    /**
     * Shortcut to turn on/off lights on the Launchpad.
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

    override fun sendFeedbackMessage(type: Int, note: Int, color: Int) {
        sendFeedbackMessage(phraseIntToLightType(type), note, color)
    }

    /**
     * Get current light color of a button.
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
    fun close() {
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