package com.kukosin.app.playback

import android.content.Context
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import io.github.anilbeesetti.nextlib.media3ext.ffdecoder.NextRenderersFactory

@UnstableApi
class HiFiRenderersFactory(context: Context) : NextRenderersFactory(context) {
    init {
        setExtensionRendererMode(EXTENSION_RENDERER_MODE_PREFER)
        setEnableAudioFloatOutput(true)
        setEnableAudioTrackPlaybackParams(false)
    }

    override fun buildAudioSink(
        context: Context,
        enableFloatOutput: Boolean,
        enableAudioTrackPlaybackParams: Boolean,
    ): AudioSink {
        val config = AudioSettingsManager.configState.value
        val processors = ArrayList<AudioProcessor>()

        if (!config.bitPerfectEnabled && config.targetSampleRateHz > 0) {
            processors.add(SampleRateAudioProcessor(config.targetSampleRateHz))
        }

        val enableFloat = when (config.targetPcmFormat) {
            "PCM Int 16-bit", "PCM Int 24-bit" -> false
            else -> true
        }

        return DefaultAudioSink.Builder(context)
            .setEnableFloatOutput(enableFloat)
            .setEnableAudioTrackPlaybackParams(false)
            .setAudioProcessors(processors.toTypedArray())
            .build()
    }
}
