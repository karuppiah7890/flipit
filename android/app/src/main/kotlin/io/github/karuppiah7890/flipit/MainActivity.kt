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
      if (call.method == "flipHorizontally") {
        val inputFile = call.argument<String>("inputFile")
        val desiredOutputFile = call.argument<String>("outputFile")
        try {
          if (inputFile != null && desiredOutputFile != null) {
            flipHorizontally(inputFile, desiredOutputFile, mSpectrum)
            result.success(null)
          }
        } catch (e: Exception) {
          result.error("exception: $e", null, null)
        }

        result.error("input file path or output file path was null", null, null)
      }
    }
  }

  private fun flipHorizontally(inputFile: String, outputFile: String, mSpectrum: Spectrum) {
    try {
      contentResolver.openInputStream(Uri.fromFile(File(inputFile))).use { inputStream ->
        val transcodeOptions = TranscodeOptions.Builder(EncodeRequirement(JPEG, 100))
                .rotate(RotateRequirement(0, true, false, true))
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
