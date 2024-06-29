package jp.si.test.media.converter.video

import jp.si.test.media.converter.EncodeOption
import jp.si.test.media.converter.MediaType

abstract class VideoEncodeOption : EncodeOption() {
    override val type: MediaType
        get() = MediaType.VIDEO
}
