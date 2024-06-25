package jp.si.test.media

import android.media.MediaCodec
import android.media.MediaCodecList
import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import java.io.FileOutputStream
import java.io.IOException

abstract class EncodeOption {
    abstract val name: String

    abstract fun createEncodeFormat(inputFormat: MediaFormat): MediaFormat

    fun defaultEncodeFormat(): MediaFormat? {
        val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
        for (codecInfo in codecList.codecInfos) {
            if (!codecInfo.isEncoder) {
                continue
            }
            val supportedTypes = codecInfo.supportedTypes
            for (type in supportedTypes) {
                if (name != type) {
                    continue
                }
                val capabilities = codecInfo.getCapabilitiesForType(type)
                if (capabilities != null) {
                    return capabilities.defaultFormat
                }
            }
        }
        return null
    }

    protected fun applyInteger(
        oFormat: MediaFormat,
        iFormat: MediaFormat,
        key: String,
        default: Int
    ) {
        oFormat.setInteger(key, iFormat.getInteger(key, default))
    }
}



fun getCodecInfo() {
    val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
    for (codecInfo in codecList.codecInfos) {
        if (codecInfo.isEncoder) {
            val supportedTypes = codecInfo.supportedTypes
            for (type in supportedTypes) {
                Log.d("CodecInfo", "=============== $codecInfo ====================")
                Log.d("CodecInfo", "Codec: ${codecInfo.name}")
                Log.d("CodecInfo", "Type: $type")
                val capabilities = codecInfo.getCapabilitiesForType(type)
                capabilities?.let {
//                    Log.d("CodecInfo", "encode: ${capabilities.encoderCapabilities.complexityRange}")
//                    Log.d("CodecInfo", "encode: ${capabilities.encoderCapabilities.qualityRange}")
                    Log.d("CodecInfo", "maxinstances: ${capabilities.maxSupportedInstances}")
                    Log.d("CodecInfo", "defaltformat: ${capabilities.defaultFormat}")
                    val audioCapabilities = capabilities.audioCapabilities
                    audioCapabilities?.let {
                        Log.d("CodecInfo", "=== AUDIO ===")
                        Log.d("CodecInfo", "Bit Rates: ${audioCapabilities.bitrateRange}")
                        Log.d("CodecInfo", "Sample Rates: ${audioCapabilities.supportedSampleRates}")
                        Log.d("CodecInfo", "Sample Rates: ${audioCapabilities.supportedSampleRateRanges}")
                        Log.d("CodecInfo", "channel: ${audioCapabilities.maxInputChannelCount}")
                        Log.d("CodecInfo", "channel: ${audioCapabilities.minInputChannelCount}")
                    }
                    val videoCapabilities = capabilities.videoCapabilities
                    videoCapabilities?.let {
                        val bitRates = videoCapabilities.bitrateRange
                        val frameRates = videoCapabilities.supportedFrameRates
                        val widths = videoCapabilities.supportedWidths
                        val heights = videoCapabilities.supportedHeights

                        Log.d("CodecInfo", "=== VIDEO ===")
                        Log.d("CodecInfo", "Bit Rates: $bitRates")
                        Log.d("CodecInfo", "Frame Rates: $frameRates")
                        Log.d("CodecInfo", "Widths: $widths")
                        Log.d("CodecInfo", "Heights: $heights")
                        Log.d("CodecInfo", "performancepoint: ${videoCapabilities.supportedPerformancePoints}")

                    }
                }
            }
        }
    }
}


abstract class AbstractMediaConverter(
    private val inputFilePath: String,
    private val outputFilePath: String,
    private val encode: EncodeOption,
) {
    private lateinit var extractor: MediaExtractor
    private lateinit var decoder: MediaCodec
    private lateinit var encoder: MediaCodec
    private lateinit var outputStream: FileOutputStream

    private var sawInputEOS = false
    private var sawOutputEOS = false

    @Throws(IOException::class)
    fun convert() {
        try {
            getCodecInfo()


            setupExtractor()
            setupDecoder()
            setupEncoder()
            setupOutStream()
            while (!sawOutputEOS) {
                read()
                decode()
                encode()
            }
        } catch (e: Exception) {
            Log.e("AbstractMediaConverter", "Conversion failed: ${e.message}")
            throw e
        } finally {
            cleanup()
        }
    }

    @Throws(Exception::class)
    fun setupExtractor() {
        extractor = MediaExtractor()
        extractor.setDataSource(inputFilePath)
        val trackIndex = selectTrack(extractor)
        if (trackIndex == -1) {
            throw RuntimeException("Not found track")
        }
        extractor.selectTrack(trackIndex)
    }

    @Throws(Exception::class)
    fun setupDecoder() {
        val format = extractor.getTrackFormat(extractor.sampleTrackIndex)
        val codec = format.getString(MediaFormat.KEY_MIME)!!
        decoder = MediaCodec.createDecoderByType(codec)
        decoder.configure(format, null, null, 0)
        decoder.start()
    }

    @Throws(Exception::class)
    fun setupEncoder() {
        val format = encode.createEncodeFormat(extractor.getTrackFormat(extractor.sampleTrackIndex))
//        var format = encode.defaultEncodeFormat()
//        if (format == null) {
//            format = encode.createEncodeFormat(extractor.getTrackFormat(extractor.sampleTrackIndex))
//        }

        val codec = format.getString(MediaFormat.KEY_MIME)!!
        encoder = MediaCodec.createEncoderByType(codec)
        encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        encoder.start()
    }

    @Throws(Exception::class)
    fun setupOutStream() {
        outputStream = FileOutputStream(outputFilePath)
    }

    @Throws(Exception::class)
    private fun read() {
        if (!sawInputEOS) {
            val inputBufferIndex = decoder.dequeueInputBuffer(10000)
            if (inputBufferIndex >= 0) {
                val inputBuffer = decoder.getInputBuffer(inputBufferIndex)!!
                val sampleSize = extractor.readSampleData(inputBuffer, 0)
                if (sampleSize <= 0) {
                    decoder.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                    sawInputEOS = true
                } else {
                    decoder.queueInputBuffer(inputBufferIndex, 0, sampleSize, extractor.sampleTime, 0)
                    extractor.advance()
                }
            }
        }
    }

    @Throws(Exception::class)
    private fun decode() {
        val bufferInfo = MediaCodec.BufferInfo()
        val outputBufferIndex = decoder.dequeueOutputBuffer(bufferInfo, 10000)
        if (outputBufferIndex >= 0) {
            val outputBuffer = decoder.getOutputBuffer(outputBufferIndex)
            if (outputBuffer == null) {
                decoder.releaseOutputBuffer(outputBufferIndex, false)

                val inputBufferIndex = encoder.dequeueInputBuffer(10000)
                if (inputBufferIndex >= 0) {
                    val inputBuffer = encoder.getInputBuffer(inputBufferIndex)!!
                    inputBuffer.clear()
                    encoder.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                }
                return
            }

            if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                decoder.releaseOutputBuffer(outputBufferIndex, false)
                return
            }

            if (bufferInfo.size != 0) {
                val decodedData = ByteArray(bufferInfo.size)
                outputBuffer.get(decodedData)
                outputBuffer.clear()

                val inputBufferIndex = encoder.dequeueInputBuffer(10000)
                if (inputBufferIndex >= 0) {
                    val inputBuffer = encoder.getInputBuffer(inputBufferIndex)!!
                    inputBuffer.clear()
                    inputBuffer.put(decodedData)
                    encoder.queueInputBuffer(inputBufferIndex, 0, decodedData.size, bufferInfo.presentationTimeUs, bufferInfo.flags)
                }
            }

            if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                val inputBufferIndex = encoder.dequeueInputBuffer(10000)
                if (inputBufferIndex >= 0) {
                    val inputBuffer = encoder.getInputBuffer(inputBufferIndex)!!
                    inputBuffer.clear()
                    encoder.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                }
            }

            decoder.releaseOutputBuffer(outputBufferIndex, false)
        }
    }

    @Throws(Exception::class)
    private fun encode() {
        val bufferInfo = MediaCodec.BufferInfo()
        val outputBufferIndex = encoder.dequeueOutputBuffer(bufferInfo, 10000)
        if (outputBufferIndex >= 0) {
            val outputBuffer = encoder.getOutputBuffer(outputBufferIndex)
            if (outputBuffer == null) {
                encoder.releaseOutputBuffer(outputBufferIndex, false)
                return
            }

            if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                encoder.releaseOutputBuffer(outputBufferIndex, false)
                return
            }

            if (bufferInfo.size != 0) {
                outputBuffer.position(bufferInfo.offset)
                outputBuffer.limit(bufferInfo.offset + bufferInfo.size)
                val encodedData = ByteArray(bufferInfo.size)
                outputBuffer.get(encodedData)
                outputStream.write(encodedData)
                outputBuffer.clear()
            }

            if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                sawOutputEOS = true
            }

            encoder.releaseOutputBuffer(outputBufferIndex, false)
        } else {
            sawOutputEOS = sawInputEOS
        }
    }

    protected fun selectTrack(extractor: MediaExtractor, mimePrefix: String): Int {
        val trackCount = extractor.trackCount
        for (i in 0 until trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (mime != null && mime.startsWith(mimePrefix)) {
                return i
            }
        }
        return -1
    }

    abstract fun selectTrack(extractor: MediaExtractor): Int

    @Throws(IOException::class)
    protected open fun cleanup() {
        try {
            outputStream.close()
        } catch (e: IOException) {
            Log.e("AbstractMediaConverter", "Failed to close output stream: ${e.message}")
        }

        decoder.stop()
        decoder.release()
        encoder.stop()
        encoder.release()
        extractor.release()
    }
}