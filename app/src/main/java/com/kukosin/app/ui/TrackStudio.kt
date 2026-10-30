package com.kukosin.app.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kukosin.app.data.analysis.AudioAnalysis
import com.kukosin.app.data.analysis.AuthenticityVerdict
import com.kukosin.app.data.analysis.EmbeddedLyrics
import com.kukosin.app.ui.theme.Cream
import com.kukosin.app.ui.theme.CreamMuted
import com.kukosin.app.ui.theme.Gold
import com.kukosin.app.ui.theme.Ink
import com.kukosin.app.ui.theme.InkSoft

@Composable
fun LyricsPanel(
    lyrics: EmbeddedLyrics?,
    positionMs: Long,
    modifier: Modifier = Modifier,
    onLineClick: ((Long) -> Unit)? = null,
) {
    if (lyrics == null) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Sin letras embebidas",
                    color = Gold,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Este archivo no contiene letras en sus etiquetas ID3 (USLT/SYLT), Vorbis (LYRICS) o MP4 (©lyr).",
                    color = CreamMuted,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        return
    }

    val synced = lyrics.synced
    if (synced.isEmpty()) {
        LazyColumn(
            modifier = modifier.fillMaxWidth().padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Surface(
                    color = InkSoft,
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(
                        text = "Fuente: ${lyrics.source} (Texto sin sincronizar)",
                        style = MaterialTheme.typography.labelSmall,
                        color = Gold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
            }
            item {
                Text(
                    text = lyrics.plainText,
                    color = Cream,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 36.sp,
                )
            }
        }
        return
    }

    val active = synced.indexOfLast { it.timeMs <= positionMs }.coerceAtLeast(0)
    val listState = rememberLazyListState()
    LaunchedEffect(active) {
        listState.animateScrollToItem(active, scrollOffset = -220)
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Surface(
                color = InkSoft,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(bottom = 8.dp),
            ) {
                Text(
                    text = "Fuente: ${lyrics.source} · Sincronizada",
                    style = MaterialTheme.typography.labelSmall,
                    color = Gold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                )
            }
        }
        itemsIndexed(synced) { index, line ->
            val current = index == active
            val min = (line.timeMs / 1000) / 60
            val sec = (line.timeMs / 1000) % 60
            val timeLabel = "%d:%02d".format(min, sec)

            val alpha by animateFloatAsState(
                targetValue = if (current) 1.0f else 0.35f,
                label = "lyricAlpha",
            )
            val fontScale by animateFloatAsState(
                targetValue = if (current) 1.0f else 0.92f,
                label = "lyricScale",
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = onLineClick != null) { onLineClick?.invoke(line.timeMs) }
                    .background(
                        if (current) Gold.copy(alpha = 0.10f) else Color.Transparent,
                        shape = RoundedCornerShape(12.dp),
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = timeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (current) Gold else CreamMuted.copy(alpha = 0.4f),
                    modifier = Modifier.width(42.dp),
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = line.text,
                    color = if (current) Gold else Cream.copy(alpha = alpha),
                    fontWeight = if (current) FontWeight.ExtraBold else FontWeight.Medium,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 24.sp * fontScale,
                        lineHeight = 32.sp * fontScale,
                    ),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
fun AnalysisPanel(
    analysis: AudioAnalysis?,
    modifier: Modifier = Modifier,
    onAnalyzeClick: (() -> Unit)? = null,
    scrollable: Boolean = true,
) {
    if (analysis?.isAnalyzing == true) {
        Box(modifier = modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Gold)
                Spacer(Modifier.height(12.dp))
                Text("Decodificando PCM y analizando espectro FFT…", color = CreamMuted)
            }
        }
        return
    }

    val hasSpectrum = analysis?.spectrumDb?.isNotEmpty() == true
    val verdictColor = when (analysis?.verdict) {
        AuthenticityVerdict.AUTHENTIC_HIRES, AuthenticityVerdict.AUTHENTIC_CD -> Gold
        AuthenticityVerdict.AUTHENTIC_LOSSY -> Color(0xFF64B5F6)
        AuthenticityVerdict.UPSAMPLED, AuthenticityVerdict.LOSSY_TRANSCODE -> Color(0xFFE07A5F)
        else -> CreamMuted
    }

    @Composable
    fun AnalysisContent() {
        Surface(
            color = InkSoft,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.4f)),
        ) {
            Column(Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(if (hasSpectrum) verdictColor else Gold, RoundedCornerShape(5.dp)),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (hasSpectrum) analysis?.verdictTitle.orEmpty() else "Verificación de Autenticidad Hi-Res",
                            color = if (hasSpectrum) verdictColor else Gold,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    if (hasSpectrum) analysis?.verdictDetail.orEmpty()
                    else "Analiza las muestras PCM y el espectro de frecuencias FFT para verificar si el archivo es 100% auténtico, si fue upsampleado o si es un transcode de MP3/AAC.",
                    color = Cream,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { onAnalyzeClick?.invoke() },
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Ink),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Rounded.GraphicEq, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (hasSpectrum) "Re-analizar Espectro y Autenticidad" else "🔍 Verificar Autenticidad y Espectro",
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        if (hasSpectrum && analysis != null) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Espectro de Frecuencia (FFT 4096)", color = Gold, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Text(
                        "Corte: ${analysis.cutoffHz} Hz",
                        color = Color(0xFFE07A5F),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
                SpectrumChart(
                    magnitudes = analysis.spectrumDb,
                    frequencies = analysis.spectrumHz,
                    cutoffHz = analysis.cutoffHz.toFloat(),
                    nyquistHz = analysis.nyquistHz.toFloat(),
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Pico: ${"%.1f".format(analysis.peakDb)} dBFS", color = CreamMuted, style = MaterialTheme.typography.labelSmall)
                    Text("RMS: ${"%.1f".format(analysis.rmsDb)} dBFS", color = CreamMuted, style = MaterialTheme.typography.labelSmall)
                    Text("DR: ${"%.1f".format(analysis.dynamicRangeDb)} dB", color = Gold, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }

        Text("Parámetros Técnicos y Metadatos Extraídos", color = Gold, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)

        val technicalList = analysis?.technical?.ifEmpty { analysis.tags }.orEmpty()
        technicalList.forEach { (label, value) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(InkSoft.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(label, color = CreamMuted, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(0.42f))
                Text(value, color = Cream, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, modifier = Modifier.weight(0.58f))
            }
        }
    }

    if (scrollable) {
        LazyColumn(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { AnalysisContent() }
        }
    } else {
        Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            AnalysisContent()
        }
    }
}

@Composable
fun SpectrumChart(
    magnitudes: List<Float>,
    frequencies: List<Float>,
    cutoffHz: Float,
    nyquistHz: Float,
) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .background(Ink, RoundedCornerShape(12.dp))
            .padding(6.dp),
    ) {
        if (magnitudes.isEmpty()) return@Canvas
        val minDb = -90f
        val maxDb = magnitudes.max().coerceAtLeast(-10f)
        val barWidth = size.width / magnitudes.size

        magnitudes.forEachIndexed { index, db ->
            val h = ((db - minDb) / (maxDb - minDb)).coerceIn(0f, 1f) * size.height
            val color = if (frequencies.getOrNull(index)?.let { it <= cutoffHz } == true) {
                Gold.copy(alpha = 0.85f)
            } else {
                CreamMuted.copy(alpha = 0.35f)
            }
            drawRect(
                color = color,
                topLeft = Offset(index * barWidth, size.height - h),
                size = Size(barWidth * 0.85f, h),
            )
        }

        fun xForHz(hz: Float): Float {
            if (nyquistHz <= 0f) return 0f
            return (hz / nyquistHz).coerceIn(0f, 1f) * size.width
        }

        if (cutoffHz > 0f) {
            val x = xForHz(cutoffHz)
            drawLine(Color(0xFFE07A5F), Offset(x, 0f), Offset(x, size.height), strokeWidth = 3f)
        }

        if (nyquistHz > 20000f) {
            val x = xForHz(20000f)
            drawLine(CreamMuted.copy(alpha = 0.5f), Offset(x, 0f), Offset(x, size.height), strokeWidth = 1.5f)
        }
    }
}
