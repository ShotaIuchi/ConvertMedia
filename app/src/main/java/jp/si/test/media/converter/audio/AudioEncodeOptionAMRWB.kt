package jp.si.test.media.converter.audio

import android.media.MediaFormat

class AudioEncodeOptionAMRWB(
    override val bitRate: Int? = null,
    override val profile: Int? = null,
) : AudioEncodeOption() {
    override val mime: String
        get() = MediaFormat.MIMETYPE_AUDIO_AMR_WB
}