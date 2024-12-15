package net.nejiwwkr.launchpad

/**
 * set a listener for cross pattern light.
 */
fun ConcurrentLaunchpad.setOnCrossPatternListener(pos: Int, color: Int){
    this.setOnListener(pos) { _, velocity ->
        if (velocity > 0) {
            val type = LightType.STATIC
            this.sendFeedbackMessage(0x90, pos, color)
            Thread.sleep(50L)
            for (i in 1..8) {
                if (pos + 10 * i < 100)
                    this.sendFeedbackMessage(type, pos + 10 * i, color)
                if (pos - 10 * i >= 11)
                    this.sendFeedbackMessage(type, pos - 10 * i, color)
                if ((pos + i) - (pos + i) % 10 == pos - pos % 10)
                    this.sendFeedbackMessage(type, pos + i, color)
                if ((pos - i) - (pos - i) % 10 == pos - pos % 10)
                    this.sendFeedbackMessage(type, pos - i, color)
                Thread.sleep(50L)
            }

            this.sendFeedbackMessage(0x90, pos, 0)
            Thread.sleep(50L)
            for (i in 1..8) {
                if (pos + 10 * i < 100 && color == this.getCurrentLight(pos + 10 * i))
                    this.sendFeedbackMessage(type, pos + 10 * i)
                if (pos - 10 * i >= 11 && color == this.getCurrentLight(pos - 10 * i))
                    this.sendFeedbackMessage(type, pos - 10 * i)
                if ((pos + i) - (pos + i) % 10 == pos - pos % 10 && color == this.getCurrentLight(pos + i))
                    this.sendFeedbackMessage(type, pos + i)
                if ((pos - i) - (pos - i) % 10 == pos - pos % 10 && color == this.getCurrentLight(pos - i))
                    this.sendFeedbackMessage(type, pos - i)
                Thread.sleep(50L)
            }
        }
    }
}

/**
 * set a listener for single click
 */
fun ConcurrentLaunchpad.setOnSingleClickListener(pos: Int, color: Int) {
    this.setOnListener(pos){ _: Int, velocity: Int -> // commandType, velocity
        if (velocity > 0) {
            this.sendFeedbackMessage(LightType.STATIC, pos, color) //sending the light
        } else {
            this.sendFeedbackMessage(LightType.STATIC, pos) //removing light
        }
    }
}