package com.griffith.androidproject_3144757.pages

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.AtomicFile
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.griffith.androidproject_3144757.R
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

private const val JOURNAL_FRAME_COUNT = 4

@Composable
fun PhotoJournalScreen(plantImageResource: Int) {
    val context = LocalContext.current
    val journalDirectory = remember(context) { File(context.filesDir, "plant_journal") }
    val scope = rememberCoroutineScope()
    var photos by remember { mutableStateOf<List<Bitmap?>>(List(JOURNAL_FRAME_COUNT) { null }) }
    var loaded by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf(false) }

    LaunchedEffect(journalDirectory) {
        photos = withContext(Dispatchers.IO) {
            List(JOURNAL_FRAME_COUNT) { index -> loadJournalPhoto(journalDirectory, index) }
        }
        loaded = true
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = stringResource(R.string.track_growth),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        if (saveError) {
            Text(
                text = stringResource(R.string.journal_save_error),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        JournalFrames(photos = photos, enabled = loaded && !saving) { index ->
            saving = true
            saveError = false
            scope.launch {
                try {
                    val bitmap = withContext(Dispatchers.IO) {
                        saveJournalPhoto(context, journalDirectory, index, plantImageResource)
                    }
                    photos = photos.toMutableList().apply { this[index] = bitmap }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    saveError = true
                } finally {
                    saving = false
                }
            }
        }
    }
}

private fun loadJournalPhoto(directory: File, index: Int): Bitmap? {
    val photoFile = File(directory, "photo_$index.png")
    if (!photoFile.isFile) return null

    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(photoFile.absolutePath, bounds)
    val options = BitmapFactory.Options().apply {
        inSampleSize = 1
        while (bounds.outWidth / (inSampleSize * 2) >= 512 &&
            bounds.outHeight / (inSampleSize * 2) >= 512
        ) {
            inSampleSize *= 2
        }
    }
    return BitmapFactory.decodeFile(photoFile.absolutePath, options)
}

private fun saveJournalPhoto(
    context: android.content.Context,
    directory: File,
    index: Int,
    plantImageResource: Int
): Bitmap {
    require(index in 0 until JOURNAL_FRAME_COUNT)
    if (!directory.isDirectory && !directory.mkdirs()) {
        throw IOException("Unable to create the photo journal")
    }
    val file = AtomicFile(File(directory, "photo_$index.png"))
    if (file.baseFile.exists()) throw IOException("Journal frame is already occupied")
    val output = file.startWrite()
    try {
        context.resources.openRawResource(plantImageResource).use { input ->
            input.copyTo(output)
        }
        file.finishWrite(output)
    } catch (error: Exception) {
        file.failWrite(output)
        throw error
    }
    return loadJournalPhoto(directory, index) ?: throw IOException("Unable to read journal photo")
}
