package com.docucraft.app

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.IntentSenderRequest
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Ink = Color(0xFF11110F)
private val SurfaceColor = Color(0xFF1C1D1A)
private val Lime = Color(0xFFD7FF64)
private val Muted = Color(0xFF9A9D91)
private val Paper = Color(0xFFF4F2E9)
private val Violet = Color(0xFFB9A5FF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(17, 17, 15)
        window.navigationBarColor = android.graphics.Color.rgb(17, 17, 15)
        setContent { DocuCraftApp() }
    }
}

private data class LocalDocument(val file: File) {
    val name: String get() = file.name
    val modified: Long get() = file.lastModified()
}

@Composable
private fun DocuCraftApp() {
    val context = LocalContext.current
    var documents by remember { mutableStateOf(loadDocuments(context)) }
    var search by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Your documents stay on this device.") }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris.isNotEmpty()) {
            val result = createPdfFromUris(context, uris)
            status = result.fold({ "PDF created: " + it.name }, { "Couldn't create PDF: " + (it.message ?: "unknown error") })
            documents = loadDocuments(context)
        }
    }
    val scannerOptions = remember { GmsDocumentScannerOptions.Builder().setGalleryImportAllowed(true).setPageLimit(50).setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_PDF, GmsDocumentScannerOptions.RESULT_FORMAT_JPEG).setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL).build() }
    val scanner = remember { GmsDocumentScanning.getClient(scannerOptions) }
    val scanLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val pdfUri = GmsDocumentScanningResult.fromActivityResultIntent(result.data)?.pdf?.uri
            if (pdfUri != null) {
                val saved = copyScannedPdf(context, pdfUri)
                status = saved.fold({ "Scan saved: " + it.name }, { "Couldn't save scan: " + (it.message ?: "unknown error") })
                documents = loadDocuments(context)
            } else status = "No PDF was returned by the scanner."
        } else status = "Scan cancelled."
    }
    val launchScanner = {
        scanner.getStartScanIntent(context as ComponentActivity)
            .addOnSuccessListener { sender -> scanLauncher.launch(IntentSenderRequest.Builder(sender).build()) }
            .addOnFailureListener { error -> status = "Scanner unavailable: " + (error.message ?: "check Google Play services") }
    }
    MaterialTheme(colorScheme = darkColorScheme(primary = Lime, onPrimary = Ink, background = Ink, surface = SurfaceColor, onSurface = Paper, secondary = Violet)) {
        Scaffold(containerColor = Ink, bottomBar = { BottomDock() }) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            Text("DOCUCRAFT", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.8.sp)
                            Spacer(Modifier.height(5.dp))
                            Text("Make it\nmake sense.", color = Paper, fontSize = 35.sp, lineHeight = 37.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-1.4).sp)
                        }
                        Box(Modifier.size(48.dp).clip(CircleShape).background(Lime), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "DocuCraft", tint = Ink)
                        }
                    }
                }
                item { HeroCard(documents.size, { launchScanner() }, { importLauncher.launch("image/*") }) }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("YOUR LIBRARY", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                            Text(documents.size.toString().padStart(2, '0') + " documents", color = Paper, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                        }
                        Icon(Icons.Default.Search, contentDescription = null, tint = Lime)
                    }
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(value = search, onValueChange = { search = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Find a document…", color = Muted) }, singleLine = true, shape = RoundedCornerShape(16.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Lime, unfocusedBorderColor = SurfaceColor, focusedTextColor = Paper, unfocusedTextColor = Paper, cursorColor = Lime))
                }
                val visible = documents.filter { it.name.contains(search, ignoreCase = true) }
                if (visible.isEmpty()) {
                    item { EmptyState { importLauncher.launch("image/*") } }
                } else {
                    items(visible, key = { it.name }) { doc ->
                        DocumentRow(doc, { sharePdf(context, doc.file) }, { doc.file.delete(); documents = loadDocuments(context); status = "Document deleted." })
                    }
                }
                item { Text(status, color = Muted, fontSize = 12.sp) }
            }
        }
    }
}

@Composable
private fun HeroCard(count: Int, onScan: () -> Unit, onImport: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) .985f else 1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "heroScale")
    Card(modifier = Modifier.fillMaxWidth().scale(scale), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = SurfaceColor)) {
        Column(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Color(0xFF30352A), SurfaceColor, Color(0xFF272235)))).padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = Lime.copy(alpha = .14f), shape = RoundedCornerShape(50)) { Text("  YOUR PAPER, REIMAGINED  ", Modifier.padding(vertical = 7.dp), color = Lime, fontSize = 9.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold) }
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Violet)
            }
            Spacer(Modifier.height(24.dp))
            Text("Capture the\nimportant stuff.", color = Paper, fontSize = 29.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold, letterSpacing = (-.8).sp)
            Spacer(Modifier.height(8.dp))
            Text("Turn pages and pictures into clean PDFs.", color = Muted, fontSize = 13.sp)
            Spacer(Modifier.height(22.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onScan, modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Lime, contentColor = Ink)) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Quick scan", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(onClick = onImport, modifier = Modifier.size(54.dp, 50.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Paper)) { Icon(Icons.Default.Add, contentDescription = "Import images") }
            }
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FolderOpen, contentDescription = null, tint = Violet, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(7.dp))
                Text(count.toString() + " saved documents", color = Muted, fontSize = 11.sp); Spacer(Modifier.weight(1f))
                Text("LOCAL-FIRST · PRIVATE", color = Muted, fontSize = 9.sp, letterSpacing = .8.sp)
            }
        }
    }
}

@Composable
private fun DocumentRow(doc: LocalDocument, onShare: () -> Unit, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = SurfaceColor), modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }) {
        Column(Modifier.padding(15.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(Violet.copy(alpha = .15f)), contentAlignment = Alignment.Center) { Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF", tint = Violet) }
                Spacer(Modifier.width(13.dp))
                Column(Modifier.weight(1f)) {
                    Text(doc.name, color = Paper, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(4.dp)); Text(formatDate(doc.modified) + " · " + formatSize(doc.file.length()), color = Muted, fontSize = 11.sp)
                }
                Icon(Icons.Default.Description, contentDescription = null, tint = Muted)
            }
            AnimatedVisibility(expanded, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                Row(Modifier.fillMaxWidth().padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onShare, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Icon(Icons.Default.IosShare, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text("Export") }
                    OutlinedButton(onClick = onDelete, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Icon(Icons.Default.DeleteOutline, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text("Delete") }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(onImport: () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(SurfaceColor).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Description, null, tint = Muted, modifier = Modifier.size(35.dp)); Spacer(Modifier.height(12.dp))
        Text("Nothing on your desk yet.", color = Paper, fontWeight = FontWeight.SemiBold); Spacer(Modifier.height(5.dp))
        Text("Scan a page or bring in an image to start.", color = Muted, fontSize = 12.sp); Spacer(Modifier.height(14.dp))
        TextButton(onClick = onImport) { Text("Import images", color = Lime) }
    }
}

@Composable
private fun BottomDock() {
    Surface(color = Ink, shadowElevation = 12.dp) {
        Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 28.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.clip(RoundedCornerShape(18.dp)).background(Lime.copy(alpha = .12f)).padding(horizontal = 18.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.AutoAwesome, null, tint = Lime, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Studio", color = Lime, fontWeight = FontWeight.SemiBold, fontSize = 12.sp) }
            Row(Modifier.padding(horizontal = 18.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.FolderOpen, null, tint = Muted, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Library", color = Muted, fontWeight = FontWeight.SemiBold, fontSize = 12.sp) }
        }
    }
}

private fun loadDocuments(context: Context): List<LocalDocument> =
    File(context.filesDir, "documents").listFiles()?.filter { it.isFile && it.extension.equals("pdf", true) }?.map { LocalDocument(it) }?.sortedByDescending { it.modified } ?: emptyList()

private fun copyScannedPdf(context: Context, uri: Uri): Result<File> = runCatching {
    val directory = File(context.filesDir, "documents").apply { mkdirs() }
    val stamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())
    val file = File(directory, "DocuCraft_$stamp.pdf")
    val input = context.contentResolver.openInputStream(uri)
    requireNotNull(input) { "The scanner returned an unreadable PDF." }
    input.use { source -> FileOutputStream(file).use { output -> source.copyTo(output) } }
    file
}

private fun createPdfFromUris(context: Context, uris: List<Uri>): Result<File> = runCatching {
    val images = uris.mapNotNull { uri -> context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) } }
    require(images.isNotEmpty()) { "No readable images were selected." }
    createPdfFromBitmaps(context, images)
}

private fun createPdfFromBitmaps(context: Context, images: List<Bitmap>): File {
    val directory = File(context.filesDir, "documents").apply { mkdirs() }
    val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val file = File(directory, "DocuCraft_" + stamp + ".pdf")
    val pdf = PdfDocument()
    try {
        images.forEachIndexed { index, bitmap ->
            val width = 595; val height = 842; val margin = 24f
            val page = pdf.startPage(PdfDocument.PageInfo.Builder(width, height, index + 1).create())
            page.canvas.drawColor(AndroidColor.WHITE)
            val ratio = minOf((width - margin * 2) / bitmap.width, (height - margin * 2) / bitmap.height)
            val w = bitmap.width * ratio; val h = bitmap.height * ratio
            val left = (width - w) / 2f; val top = (height - h) / 2f
            page.canvas.drawBitmap(bitmap, null, android.graphics.RectF(left, top, left + w, top + h), Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
            pdf.finishPage(page)
        }
        FileOutputStream(file).use { pdf.writeTo(it) }
    } finally { pdf.close(); images.forEach { if (!it.isRecycled) it.recycle() } }
    return file
}

private fun sharePdf(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(context, context.packageName + ".provider", file)
    val intent = Intent(Intent.ACTION_SEND).apply { type = "application/pdf"; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
    context.startActivity(Intent.createChooser(intent, "Export document"))
}

private fun formatDate(time: Long): String = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(time))
private fun formatSize(bytes: Long): String = if (bytes < 1024 * 1024) (bytes / 1024).toString() + " KB" else String.format(Locale.getDefault(), "%.1f MB", bytes / (1024f * 1024f))
