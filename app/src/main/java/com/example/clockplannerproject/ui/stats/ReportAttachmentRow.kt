package com.example.clockplannerproject.ui.stats

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.clockplannerproject.R
import com.example.clockplannerproject.kit.core.ReportAttachment
import com.example.clockplannerproject.kit.core.ReportId
import com.example.clockplannerproject.kit.core.ReportMediaPaths
import java.io.File

/**
 * System pickers for a report attachment. Photo uses the photo picker;
 * file uses [ActivityResultContracts.OpenDocument]. Neither needs a storage permission.
 *
 * @param onPicked report id and content URI string
 * @since 1.0.0
 */
@Composable
fun rememberReportImportLaunchers(
    onPicked: (ReportId, String) -> Unit,
): ReportImportLaunchers {
    var pendingId by remember { mutableStateOf<ReportId?>(null) }
    val image = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        val id = pendingId
        pendingId = null
        if (id != null && uri != null) onPicked(id, uri.toString())
    }
    val file = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        val id = pendingId
        pendingId = null
        if (id != null && uri != null) onPicked(id, uri.toString())
    }
    return ReportImportLaunchers(
        pickImage = { id ->
            pendingId = id
            image.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
            )
        },
        pickFile = { id ->
            pendingId = id
            file.launch(arrayOf("*/*"))
        },
    )
}

/**
 * @param pickImage launches the photo picker for [ReportId]
 * @param pickFile launches the document picker for [ReportId]
 * @since 1.0.0
 */
class ReportImportLaunchers(
    val pickImage: (ReportId) -> Unit,
    val pickFile: (ReportId) -> Unit,
)

/**
 * Opens a copied attachment with a view intent. The URI is a [FileProvider]
 * URI for the file under `filesDir`, so it still resolves after process restart.
 *
 * @return false when the file is missing or no activity can view it
 * @since 1.0.0
 */
fun openReportAttachment(context: Context, attachment: ReportAttachment): Boolean {
    val file = ReportMediaPaths.resolve(context.filesDir, attachment.relativePath)
    if (!file.exists()) return false
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file,
    )
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, attachment.mimeType.ifBlank { "*/*" })
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    return try {
        context.startActivity(Intent.createChooser(intent, null))
        true
    } catch (_: ActivityNotFoundException) {
        false
    }
}

/**
 * Buttons and optional image preview for one report attachment.
 *
 * @since 1.0.0
 */
@Composable
fun ReportAttachmentRow(
    reportId: ReportId,
    attachment: ReportAttachment?,
    onPickImage: (ReportId) -> Unit,
    onPickFile: (ReportId) -> Unit,
    onRemove: (ReportId) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val attachImageCd = stringResource(R.string.cd_report_attach_image)
    val attachFileCd = stringResource(R.string.cd_report_attach_file)
    val openCd = stringResource(R.string.cd_report_open_attachment)
    val deleteCd = stringResource(R.string.cd_report_delete_attachment)
    val previewCd = stringResource(R.string.cd_report_attachment_preview)
    if (attachment != null) {
        AttachmentPreview(
            attachment = attachment,
            contentDescription = previewCd,
        )
        if (!LocalInspectionMode.current) {
            val file = ReportMediaPaths.resolve(context.filesDir, attachment.relativePath)
            if (!file.exists()) {
                Text(text = stringResource(R.string.report_attachment_missing))
            } else if (!attachment.isImage) {
                Text(text = attachment.displayName)
            }
        } else if (!attachment.isImage) {
            Text(text = attachment.displayName)
        }
    }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        TextButton(
            onClick = { onPickImage(reportId) },
            modifier = Modifier.semantics { contentDescription = attachImageCd },
        ) {
            Text(stringResource(R.string.report_attach_image))
        }
        TextButton(
            onClick = { onPickFile(reportId) },
            modifier = Modifier.semantics { contentDescription = attachFileCd },
        ) {
            Text(stringResource(R.string.report_attach_file))
        }
        if (attachment != null) {
            TextButton(
                onClick = { openReportAttachment(context, attachment) },
                modifier = Modifier.semantics { contentDescription = openCd },
            ) {
                Text(stringResource(R.string.report_open_attachment))
            }
            TextButton(
                onClick = { onRemove(reportId) },
                modifier = Modifier.semantics { contentDescription = deleteCd },
            ) {
                Text(stringResource(R.string.report_delete_attachment))
            }
        }
    }
}

@Composable
private fun AttachmentPreview(
    attachment: ReportAttachment,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    if (!attachment.isImage || LocalInspectionMode.current) return
    val context = LocalContext.current
    val file = ReportMediaPaths.resolve(context.filesDir, attachment.relativePath)
    val bitmap = remember(file.path, file.lastModified()) {
        decodeSampled(file, PREVIEW_MAX_PX)
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = modifier
                .heightIn(max = 96.dp)
                .size(width = 96.dp, height = 96.dp),
            contentScale = ContentScale.Crop,
        )
    }
}

private fun decodeSampled(file: File, maxPx: Int): android.graphics.Bitmap? {
    if (!file.exists()) return null
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.path, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
    var sample = 1
    while (bounds.outWidth / sample > maxPx || bounds.outHeight / sample > maxPx) {
        sample *= 2
    }
    val options = BitmapFactory.Options().apply { inSampleSize = sample }
    return BitmapFactory.decodeFile(file.path, options)
}

private const val PREVIEW_MAX_PX: Int = 512
