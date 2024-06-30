package jp.si.test.media.converter

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

open class MediaConverterEngineBuffer(
    private val inputFilePath: File,
    private val outputFilePath: File,
    override val encode: EncodeOption
): MediaConverterEngine(
) {
    private lateinit var extractor: MediaExtractor
    private lateinit var decoder: MediaCodec
    private lateinit var encoder: MediaCodec
    private lateinit var outputStream: FileOutputStream

    private var sawInputEOS = false
    private var sawOutputEOS = false

    companion object {
        private const val TIMEOUT_USEC = 10000L

        fun create(
            inputFilePath: File,
            outputFilePath: File,
            encode: EncodeOption
        ): MediaConverterEngineBuffer? {
            if (hasTrack(inputFilePath, encode.type)) {
                return MediaConverterEngineBuffer(inputFilePath, outputFilePath, encode)
            }
            return null
        }
    }

    @Throws(Exception::class)
    override fun convert() {
        try {
            setupExtractor()
            setupDecoder()
            setupEncoder()
            setupOutStream()
            while (!sawOutputEOS) {
                read()
                decode()
                encode()
            }
        } finally {
            cleanup()
        }
    }

    @Throws(Exception::class)
    fun setupExtractor() {
        extractor = MediaExtractor()
        extractor.setDataSource(inputFilePath.absolutePath)
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
        val codec = format.getString(MediaFormat.KEY_MIME)!!
        encoder = MediaCodec.createEncoderByType(codec)
        encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        encoder.start()
    }

    @Throws(Exception::class)
    fun setupOutStream() {
        outputStream = FileOutputStream(outputFilePath.absolutePath)
    }

    @Throws(Exception::class)
    private fun read() {
        if (!sawInputEOS) {
            val inputBufferIndex = decoder.dequeueInputBuffer(TIMEOUT_USEC)
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
        val outputBufferIndex = decoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC)
        if (outputBufferIndex >= 0) {
            val outputBuffer = decoder.getOutputBuffer(outputBufferIndex)
            if (outputBuffer == null) {
                decoder.releaseOutputBuffer(outputBufferIndex, false)

                val inputBufferIndex = encoder.dequeueInputBuffer(TIMEOUT_USEC)
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

                val inputBufferIndex = encoder.dequeueInputBuffer(TIMEOUT_USEC)
                if (inputBufferIndex >= 0) {
                    val inputBuffer = encoder.getInputBuffer(inputBufferIndex)!!
                    inputBuffer.clear()
                    inputBuffer.put(decodedData)
                    encoder.queueInputBuffer(inputBufferIndex, 0, decodedData.size, bufferInfo.presentationTimeUs, bufferInfo.flags)
                }
            }

            if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                val inputBufferIndex = encoder.dequeueInputBuffer(TIMEOUT_USEC)
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
        val outputBufferIndex = encoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC)
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

    @Throws(IOException::class)
    protected fun cleanup() {
        outputStream.close()
        decoder.stop()
        decoder.release()
        encoder.stop()
        encoder.release()
        extractor.release()
    }
}