package net.nejiwwkr.launchpad

/**
 * set a listener for cross pattern light.
 */
fun ConcurrentLaunchpad.setOnCrossPatternListener(pos: Int, color: Int, frequency: Long, internal: Long){
    this.setOnListener(pos) { _, velocity ->
        if (velocity <= 0) return@setOnListener

        val type = LightType.STATIC

        this.sendFeedbackMessage(type, pos, color)
        Thread.sleep(frequency)
        for (i in 1..8) {
            if (pos + 10 * i < 100)
                this.sendFeedbackMessage(type, pos + 10 * i, color)
            if (pos - 10 * i > 10)
                this.sendFeedbackMessage(type, pos - 10 * i, color)
            if ((pos + i) / 10 == pos / 10)
                this.sendFeedbackMessage(type, pos + i, color)
            if ((pos - i) / 10 == pos / 10)
                this.sendFeedbackMessage(type, pos - i, color)
            Thread.sleep(frequency)
        }

        Thread.sleep(internal)

        this.sendFeedbackMessage(0x90, pos, 0)
        Thread.sleep(frequency)
        for (i in 1..8) {
            if (pos + 10 * i < 100 && color == this.getCurrentLight(pos + 10 * i))
                this.sendFeedbackMessage(type, pos + 10 * i)
            if (pos - 10 * i > 10 && color == this.getCurrentLight(pos - 10 * i))
                this.sendFeedbackMessage(type, pos - 10 * i)
            if ((pos + i) / 10 == pos / 10 && color == this.getCurrentLight(pos + i))
                this.sendFeedbackMessage(type, pos + i)
            if ((pos - i) / 10 == pos / 10 && color == this.getCurrentLight(pos - i))
                this.sendFeedbackMessage(type, pos - i)
            Thread.sleep(frequency)
        }
    }
}

/**
 * set a listener for horizontal line pattern light.
 */
fun ConcurrentLaunchpad.setOnHorizontalLinePatternListener(pos: Int, color: Int, frequency: Long, interval: Long){
    this.setOnListener(pos) { _, velocity ->
        if (velocity <= 0) return@setOnListener

        val type = LightType.STATIC

        this.sendFeedbackMessage(type, pos, color)
        Thread.sleep(frequency)
        for (i in 1..8) {
            if (pos + 10 * i < 100)
                this.sendFeedbackMessage(type, pos + 10 * i, color)
            if (pos - 10 * i > 10)
                this.sendFeedbackMessage(type, pos - 10 * i, color)
            Thread.sleep(frequency)
        }

        Thread.sleep(interval)

        this.sendFeedbackMessage(0x90, pos, 0)
        Thread.sleep(frequency)
        for (i in 1..8) {
            if (pos + 10 * i < 100 && color == this.getCurrentLight(pos + 10 * i))
                this.sendFeedbackMessage(type, pos + 10 * i)
            if (pos - 10 * i > 10 && color == this.getCurrentLight(pos - 10 * i))
                this.sendFeedbackMessage(type, pos - 10 * i)
            Thread.sleep(frequency)
        }
    }
}

/**
 * set a listener for vertical line pattern light.
 */
fun ConcurrentLaunchpad.setOnVerticalLinePatternListener(pos: Int, color: Int, frequency: Long, interval: Long){
    this.setOnListener(pos) { _, velocity ->
        if (velocity <= 0) return@setOnListener

        val type = LightType.STATIC

        this.sendFeedbackMessage(type, pos, color)
        Thread.sleep(frequency)
        for (i in 1..8) {
            if ((pos + i) / 10 == pos / 10)
                this.sendFeedbackMessage(type, pos + i, color)
            if ((pos - i) / 10 == pos / 10)
                this.sendFeedbackMessage(type, pos - i, color)
            Thread.sleep(frequency)
        }

        Thread.sleep(interval)

        this.sendFeedbackMessage(0x90, pos, 0)
        Thread.sleep(frequency)
        for (i in 1..8) {
            if ((pos + i) / 10 == pos / 10 && color == this.getCurrentLight(pos + i))
                this.sendFeedbackMessage(type, pos + i)
            if ((pos - i) / 10 == pos / 10 && color == this.getCurrentLight(pos - i))
                this.sendFeedbackMessage(type, pos - i)
            Thread.sleep(frequency)
        }
    }
}

/**
 * set a listener for a long on-off single click
 */
fun ConcurrentLaunchpad.setOnLongClickListener(pos: Int, color: Int) {
    this.setOnListener(pos){ _: Int, velocity: Int -> // commandType, velocity
        if (velocity > 0) {
            this.sendFeedbackMessage(LightType.STATIC, pos, color) //sending the light
        } else {
            this.sendFeedbackMessage(LightType.STATIC, pos) //removing light
        }
    }
}

/**
 * set a listener for a single click
 */
fun ConcurrentLaunchpad.setOnSingleClickListener(pos: Int, color: Int, interval: Long) {
    this.setOnListener(pos){ _: Int, velocity: Int -> // commandType, velocity
        if (velocity > 0) {
            this.sendFeedbackMessage(LightType.STATIC, pos, color) //sending the light
            Thread.sleep(interval)
            this.sendFeedbackMessage(LightType.STATIC, pos) //removing light
        }
    }
}