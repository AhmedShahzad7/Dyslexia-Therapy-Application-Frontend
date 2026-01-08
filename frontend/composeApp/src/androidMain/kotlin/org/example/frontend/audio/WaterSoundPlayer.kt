import android.content.Context
import android.media.SoundPool
import org.example.frontend.R

class WaterSoundPlayer(context: Context) {

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(1)
        .build()

    private val soundId = soundPool.load(context, R.raw.water_sound, 1)
    private var streamId: Int? = null

    fun start() {
        if (streamId == null) {
            streamId = soundPool.play(
                soundId,
                1f, 1f, // left & right volume
                1,
                -1,     // loop forever
                1f
            )
        }
    }

    fun stop() {
        streamId?.let {
            soundPool.stop(it)
            streamId = null
        }
    }

    fun release() {
        soundPool.release()
    }
}
