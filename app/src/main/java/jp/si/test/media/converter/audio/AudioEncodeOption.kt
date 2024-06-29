package jp.si.test.media.converter.audio

import jp.si.test.media.converter.EncodeOption
import jp.si.test.media.converter.MediaType

abstract class AudioEncodeOption : EncodeOption() {
    override val type: MediaType
        get() = MediaType.AUDIO
}