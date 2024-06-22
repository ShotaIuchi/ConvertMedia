package jp.si.test.media

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import java.io.FileOutputStream
import java.io.IOException

class AudioConverter : AbstractMediaConverter() {
    @Throws(IOException::class)
    override fun setupExtractor(inputFilePath: String) {
        extractor = MediaExtractor()
        extractor.setDataSource(inputFilePath)
        val trackIndex = selectTrack(extractor, "audio/")
        if (trackIndex == -1) {
            throw RuntimeException("ファイルに音声トラックがありません。")
        }
        extractor.selectTrack(trackIndex)
    }

    @Throws(IOException::class)
    override fun setupDecoder() {
        val format = extractor.getTrackFormat(extractor.sampleTrackIndex)
        val codec = format.getString(MediaFormat.KEY_MIME)!!
        decoder = MediaCodec.createDecoderByType(codec)
        decoder.configure(format, null, null, 0)
        decoder.start()
    }

    @Throws(IOException::class)
    override fun setupEncoder(outputFilePath: String) {
        val inputFormat = extractor.getTrackFormat(extractor.sampleTrackIndex)
        val sampleRate = inputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        val channelCount = inputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        val bitRate = inputFormat.getInteger(MediaFormat.KEY_BIT_RATE)

        val format =
            MediaFormat.createAudioFormat("audio/mp4a-latm", sampleRate, channelCount).apply {
                setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
                setInteger(
                    MediaFormat.KEY_AAC_PROFILE,
                    MediaCodecInfo.CodecProfileLevel.AACObjectLC
                )
            }

        encoder = MediaCodec.createEncoderByType("audio/mp4a-latm")
        encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        encoder.start()

        outputStream = FileOutputStream(outputFilePath)
    }

    override fun read() {
        if (!sawInputEOS) {
            val inputBufferIndex = decoder.dequeueInputBuffer(10000)
            if (inputBufferIndex >= 0) {
                val inputBuffer = decoder.getInputBuffer(inputBufferIndex)!!
                val sampleSize = extractor.readSampleData(inputBuffer, 0)
                if (sampleSize < 0) {
                    decoder.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                    sawInputEOS = true
                } else {
                    decoder.queueInputBuffer(inputBufferIndex, 0, sampleSize, extractor.sampleTime, 0)
                    extractor.advance()
                }
            }
        }
    }

    override fun decode() {
        val bufferInfo = MediaCodec.BufferInfo()
        val outputBufferIndex = decoder.dequeueOutputBuffer(bufferInfo, 10000)
        if (outputBufferIndex >= 0) {
            val outputBuffer = decoder.getOutputBuffer(outputBufferIndex)!!
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
            decoder.releaseOutputBuffer(outputBufferIndex, false)
        }
    }

    @Throws(IOException::class)
    override fun encode() {
        val bufferInfo = MediaCodec.BufferInfo()
        val outputBufferIndex = encoder.dequeueOutputBuffer(bufferInfo, 10000)
        if (outputBufferIndex >= 0) {
            val outputBuffer = encoder.getOutputBuffer(outputBufferIndex)!!
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
        }
    }

    private fun selectTrack(extractor: MediaExtractor, mimePrefix: String): Int {
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
}