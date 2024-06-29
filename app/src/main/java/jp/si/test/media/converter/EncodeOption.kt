package jp.si.test.media.converter

import android.media.MediaFormat

abstract class EncodeOption {
    abstract val name: String

    abstract fun createEncodeFormat(inputFormat: MediaFormat): MediaFormat

    protected fun applyInteger(format: MediaFormat, input: MediaFormat, key: String, value: Int?, default: Int) {
        if (null != value) {
            format.setInteger(key, value)
        } else {
            format.setInteger(key, input.getInteger(key, default))
        }
    }
}