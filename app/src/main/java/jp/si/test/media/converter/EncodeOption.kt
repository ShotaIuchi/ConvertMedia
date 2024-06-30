package jp.si.test.media.converter

import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaFormat
import android.util.Log

abstract class EncodeOption {
    abstract val type: MediaType
    abstract val mime: String

    abstract fun createEncodeFormat(inputFormat: MediaFormat): MediaFormat

    protected fun getInteger(input: MediaFormat, key: String, value: Int?, default: Int): Int =
        value ?: input.getInteger(key, default)

    protected fun applyInteger(format: MediaFormat, input: MediaFormat, key: String, value: Int?, default: Int) =
        if (null != value) {
            format.setInteger(key, value)
        } else {
            format.setInteger(key, input.getInteger(key, default))
        }

    protected fun capabilities(): MediaCodecInfo.CodecCapabilities? {
        val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
        for (codecInfo in codecList.codecInfos) {
            if (!codecInfo.isEncoder) {
                continue
            }
            val supportedTypes = codecInfo.supportedTypes
            for (type in supportedTypes) {
                if (mime != type) {
                    continue
                }
                return codecInfo.getCapabilitiesForType(type)
            }
        }
        return null
    }

//    protected fun defaultEncodeFormat(): MediaFormat? {
//        val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
//        for (codecInfo in codecList.codecInfos) {
//            if (!codecInfo.isEncoder) {
//                continue
//            }
//            val supportedTypes = codecInfo.supportedTypes
//            for (type in supportedTypes) {
//                Log.d("XXXXXXXXXX (------------------------)", type)
////                if (mime != type) {
////                    continue
////                }
//                val capabilities = codecInfo.getCapabilitiesForType(type)
//                if (capabilities != null) {
//                    Log.d("XXXXXXXXXX", capabilities.defaultFormat.toString())
//                    Log.d("XXXXXXXXXX", capabilities.colorFormats.toString())
//                    Log.d("XXXXXXXXXX", capabilities.profileLevels.toString())
////                    Log.d("XXXXXXXXXX", capabilities.bitrateRange.toString())
//                    Log.d("XXXXXXXXXX", capabilities.videoCapabilities?.toString() ?: "null")
//                    Log.d("XXXXXXXXXX", capabilities.audioCapabilities?.toString() ?: "null")
//                    Log.d("XXXXXXXXXX", capabilities.isFeatureSupported(MediaCodecInfo.CodecCapabilities.FEATURE_TunneledPlayback).toString())
//                    Log.d("XXXXXXXXXX", capabilities.isFeatureRequired(MediaCodecInfo.CodecCapabilities.FEATURE_TunneledPlayback).toString())
////                    Log.d("XXXXXXXXXX", capabilities.isFeatureSupported(MediaCodecInfo.CodecCapabilities.FEATURE_SecurePlayback).toString())
////                    Log.d("XXXXXXXXXX", capabilities.isFeatureRequired(MediaCodecInfo.CodecCapabilities.FEATURE_SecurePlayback).toString())
////                    Log.d("XXXXXXXXXX", capabilities.isFeatureSupported(MediaCodecInfo.CodecCapabilities.FEATURE_AdaptivePlayback).toString())
////                    Log.d("XXXXXXXXXX", capabilities.isFeatureRequired(MediaCodecInfo.CodecCapabilities.FEATURE_AdaptivePlayback).toString())
////                    Log.d("XXXXXXXXXX", capabilities.isFeatureSupported(MediaCodecInfo.CodecCapabilities.FEATURE_MultipleFrames).toString())
////                    Log.d("XXXXXXXXXX", capabilities.isFeatureRequired(MediaCodecInfo.CodecCapabilities.FEATURE_MultipleFrames).toString())
////                    Log.d("XXXXXXXXXX", capabilities.isFeatureSupported(MediaCodecInfo.CodecCapabilities.FEATURE_FrameParsing).toString())
////                    Log.d("XXXXXXXXXX", capabilities.isFeatureRequired(MediaCodecInfo.CodecCapabilities.FEATURE_FrameParsing).toString())
////                    Log.d("XXXXXXXXXX", capabilities.isFeatureSupported(MediaCodecInfo.CodecCapabilities.FEATURE_SecureWithCleartextPlayback).toString())
////                    Log.d("XXXXXXXXXX", capabilities.isFeatureRequired(MediaCodecInfo.CodecCapabilities.FEATURE_SecureWithCleartextPlayback).toString())
////                    Log.d("XXXXXXXXXX", capabilities.isFeatureSupported(MediaCodecInfo.CodecCapabilities.FEATURE_SecureWithSecureOutput).toString())
////                    Log.d("XXXXXXXXXX", capabilities.isFeatureRequired(MediaCodecInfo.CodecCapabilities.FEATURE_SecureWithSecureOutput).toString())
////                    Log.d("XXXXXXXXXX", capabilities.isFeatureSupported(MediaCodecInfo.CodecCapabilities.FEATURE_AdaptivePlayback).toString())
////                    Log.d("XXXXXXXXXX", capabilities.isFeatureRequired(MediaCodecInfo.CodecCapabilities.FEATURE_AdaptivePlayback).toString())
////                    Log.d("XXXXXXXXXX", capabilities.isFeatureSupported(MediaCodecInfo.CodecCapabilities.FEATURE_MultipleFrames).toString())
////                    Log.d("XXXXXXXXXX", capabilities.isFeatureRequired(MediaCodecInfo.CodecCapabilities.FEATURE_MultipleFrames).toString())
////                    Log.d("XXXXXXXXXX", capabilities.isFeatureSupported(MediaCodecInfo.CodecCapabilities.FEATURE_FrameParsing).toString())
//
////                    return capabilities.defaultFormat
//                }
//            }
//        }
//        return null
//    }
}