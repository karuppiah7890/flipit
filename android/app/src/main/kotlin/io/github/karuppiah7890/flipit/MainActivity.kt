package io.github.karuppiah7890.flipit

import android.net.Uri
import android.os.Bundle
import android.util.Log
import com.facebook.spectrum.*
import com.facebook.spectrum.image.EncodedImageFormat.JPEG
import com.facebook.spectrum.logging.SpectrumLogcatLogger
import com.facebook.spectrum.options.TranscodeOptions
import com.facebook.spectrum.requirements.EncodeRequirement
import com.facebook.spectrum.requirements.RotateRequirement
import io.flutter.app.FlutterActivity
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugins.GeneratedPluginRegistrant
import java.io.File
import java.io.IOException


class MainActivity : FlutterActivity() {
    private val _channel = "flutter.native/helper"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GeneratedPluginRegistrant.registerWith(this)
        SpectrumSoLoader.init(this)

        val mSpectrum = Spectrum.make(SpectrumLogcatLogger(Log.INFO),
                DefaultPlugins.get()) // JPEG, PNG and WebP plugins


        MethodChannel(flutterView, _channel).setMethodCallHandler { call, result ->
            if (call.method == "flip") {
                val inputFile = call.argument<String>("inputFile")
                val desiredOutputFile = call.argument<String>("outputFile")
                val flipHorizontally = call.argument<Boolean>("flipHorizontally")
                val flipVertically = call.argument<Boolean>("flipVertically")
                try {
                    if (inputFile != null && desiredOutputFile != null &&
                            flipHorizontally != null && flipVertically != null) {
                        flip(mSpectrum, desiredOutputFile, inputFile, flipHorizontally, flipVertically)
                        result.success("successfully flipped it!")
                    } else {
                        result.error("check your flip function parameters: \ninputFile: $inputFile\n" +
                                "outputFile: $desiredOutputFile\n" +
                                "flipHorizontally: $flipHorizontally\n" +
                                "flipVertically: $flipVertically", null, null)
                    }
                } catch (e: Exception) {
                    result.error("exception: $e", null, null)
                }
            }
        }
    }

    private fun flip(mSpectrum: Spectrum, outputFile: String, inputFile: String, flipHorizontally: Boolean, flipVertically: Boolean) {
        try {
            contentResolver.openInputStream(Uri.fromFile(File(inputFile))).use { inputStream ->
                val transcodeOptions = TranscodeOptions.Builder(EncodeRequirement(JPEG, 100))
                        .rotate(RotateRequirement(0, flipHorizontally, flipVertically, true))
                        .build()

                mSpectrum.transcode(
                        EncodedImageSource.from(inputStream),
                        EncodedImageSink.from(outputFile),
                        transcodeOptions,
                        "upload_flow_callsite_identifier")
            }
        } catch (e: IOException) {
            throw e
        } catch (e: SpectrumException) {
            throw e
        }
    }
}
