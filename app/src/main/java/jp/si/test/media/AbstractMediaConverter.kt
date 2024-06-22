package jp.si.test.media

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer

abstract class AbstractMediaConverter {
    protected lateinit var extractor: MediaExtractor
    protected lateinit var decoder: MediaCodec
    protected lateinit var encoder: MediaCodec
    protected lateinit var outputStream: FileOutputStream
    protected var sawInputEOS = false
    protected var sawOutputEOS = false

    @Throws(IOException::class)
    fun convert(inputFilePath: String, outputFilePath: String) {
        try {
            setupExtractor(inputFilePath)
            setupDecoder()
            setupEncoder(outputFilePath)

            while (!sawOutputEOS) {
                read()
                decode()
                encode()
            }
        } catch (e: IOException) {
            Log.e("AbstractMediaConverter", "Conversion failed: ${e.message}")
            throw e
        } finally {
            cleanup()
        }
    }

    @Throws(IOException::class)
    protected abstract fun setupExtractor(inputFilePath: String)

    @Throws(IOException::class)
    protected abstract fun setupDecoder()

    @Throws(IOException::class)
    protected abstract fun setupEncoder(outputFilePath: String)

    protected abstract fun read()
    protected abstract fun decode()

    @Throws(IOException::class)
    protected abstract fun encode()

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