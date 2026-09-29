package com.example.ui.scanner

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlin.OptIn
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.data.remote.PublicBookResult
import com.example.ui.BookViewModel
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.camera.core.FocusMeteringAction
import com.example.ui.theme.DarkGrayBg
import com.example.ui.theme.DarkGraySurface
import com.example.ui.LookupUiState
import com.example.ui.theme.WarmAmber
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScannerScreen(
    viewModel: BookViewModel,
    onBookFound: (PublicBookResult) -> Unit,
    onManualEntry: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val lookupState by viewModel.barcodeLookupState.collectAsState()
    var manualIsbn by remember { mutableStateOf("") }
    var torchEnabled by remember { mutableStateOf(false) }
    var cameraRef by remember { mutableStateOf<Camera?>(null) }

    // Dialog handling for lookup results
    when (val state = lookupState) {
        is LookupUiState.Loading -> {
            AlertDialog(
                onDismissRequest = {},
                confirmButton = {},
                title = { Text("Consultando Bibliotecas") },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Text("Buscando dados do livro pelo código de barras...")
                    }
                }
            )
        }
        is LookupUiState.Success -> {
            AlertDialog(
                onDismissRequest = { viewModel.resetLookupState() },
                title = { Text("Livro Encontrado! 📖") },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = state.result.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (state.result.authors.isNotBlank()) {
                            Text(
                                text = "Autor: ${state.result.authors}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (state.result.publisher.isNotBlank()) {
                            Text(
                                text = "Editora: ${state.result.publisher}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (state.result.publishYear != null) {
                            Text(
                                text = "Ano: ${state.result.publishYear}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Fonte: ${state.result.source}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val res = state.result
                            viewModel.resetLookupState()
                            onBookFound(res)
                        },
                        modifier = Modifier.testTag("import_scanned_book_button")
                    ) {
                        Text("Catalogar Este Livro")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.resetLookupState() }) {
                        Text("Cancelar")
                    }
                }
            )
        }
        is LookupUiState.NotFound -> {
            AlertDialog(
                onDismissRequest = { viewModel.resetLookupState() },
                title = { Text("ISBN Não Encontrado") },
                text = {
                    Text("O código ${state.isbn} não foi localizado automaticamente nas bibliotecas públicas. Deseja cadastrar manualmente?")
                },
                confirmButton = {
                    Button(onClick = {
                        val isbn = state.isbn
                        viewModel.resetLookupState()
                        onManualEntry(isbn)
                    }) {
                        Text("Cadastrar Manualmente")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.resetLookupState() }) {
                        Text("Tentar Novamente")
                    }
                }
            )
        }
        is LookupUiState.Error -> {
            AlertDialog(
                onDismissRequest = { viewModel.resetLookupState() },
                title = { Text("Aviso") },
                text = { Text(state.message) },
                confirmButton = {
                    Button(onClick = { viewModel.resetLookupState() }) {
                        Text("OK")
                    }
                }
            )
        }
        LookupUiState.Idle -> {}
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escanear Código de Barras") },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                actions = {
                    if (hasCameraPermission && cameraRef != null) {
                        IconButton(
                            onClick = {
                                torchEnabled = !torchEnabled
                                cameraRef?.cameraControl?.enableTorch(torchEnabled)
                            }
                        ) {
                            Icon(
                                if (torchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                contentDescription = "Lanterna",
                                tint = if (torchEnabled) WarmAmber else Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkGraySurface,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(DarkGrayBg)
        ) {
            if (hasCameraPermission) {
                CameraPreviewWithAnalyzer(
                    onBarcodeScanned = { barcode ->
                        // Haptic feedback for tactile confirmation
                        try {
                            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator?.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
                            } else {
                                @Suppress("DEPRECATION")
                                vibrator?.vibrate(80)
                            }
                        } catch (_: Exception) {}
                        viewModel.lookupIsbn(barcode)
                    },
                    onCameraReady = { camera -> cameraRef = camera }
                )

                // Viewfinder scanning overlay
                ScanningOverlay(modifier = Modifier.fillMaxSize())

                // Samsung Galaxy large screen helper hint
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = DarkGraySurface.copy(alpha = 0.85f)
                ) {
                    Text(
                        text = "Toque para focar • Pinça para zoom",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            } else {
                // Permission request view
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Permissão da câmera necessária para leitura de código de barras",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        modifier = Modifier.testTag("request_camera_permission_button")
                    ) {
                        Text("Permitir Câmera")
                    }
                }
            }

            // Bottom card for manual ISBN input and emulator quick test chips
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Ou digite o código ISBN manualmente:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = manualIsbn,
                            onValueChange = { manualIsbn = it },
                            placeholder = { Text("Ex: 9788535914849") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("manual_isbn_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors()
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (manualIsbn.isNotBlank()) {
                                    viewModel.lookupIsbn(manualIsbn)
                                }
                            },
                            enabled = manualIsbn.isNotBlank(),
                            modifier = Modifier.testTag("search_isbn_button")
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Buscar")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Buscar")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Exemplos rápidos para teste (ISBNs de Clássicos):",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val testIsbns = listOf(
                        "9788535914849" to "Dom Casmurro",
                        "9788522031443" to "Pequeno Príncipe",
                        "9788535911695" to "Capitães da Areia",
                        "9780132350884" to "Clean Code"
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(testIsbns) { (isbn, label) ->
                            FilterChip(
                                selected = manualIsbn == isbn,
                                onClick = {
                                    manualIsbn = isbn
                                    viewModel.lookupIsbn(isbn)
                                },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalGetImage::class)
@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
fun CameraPreviewWithAnalyzer(
    onBarcodeScanned: (String) -> Unit,
    onCameraReady: (Camera) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    var isProcessing by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    AndroidView(
        factory = { ctx ->
            var activeCamera: Camera? = null
            val previewView = PreviewView(ctx).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }

            val scaleGestureDetector = ScaleGestureDetector(ctx, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    val camera = activeCamera ?: return false
                    val currentZoomRatio = camera.cameraInfo.zoomState.value?.zoomRatio ?: 1f
                    val delta = detector.scaleFactor
                    camera.cameraControl.setZoomRatio((currentZoomRatio * delta).coerceIn(1f, 5f))
                    return true
                }
            })

            previewView.setOnTouchListener { view, event ->
                scaleGestureDetector.onTouchEvent(event)
                if (event.action == MotionEvent.ACTION_UP && event.pointerCount == 1) {
                    val camera = activeCamera
                    if (camera != null) {
                        val factory = previewView.meteringPointFactory
                        val point = factory.createPoint(event.x, event.y)
                        val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF).build()
                        camera.cameraControl.startFocusAndMetering(action)
                    }
                    view.performClick()
                }
                true
            }

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val barcodeScanner = BarcodeScanning.getClient()

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage != null && !isProcessing) {
                        val image = InputImage.fromMediaImage(
                            mediaImage,
                            imageProxy.imageInfo.rotationDegrees
                        )
                        barcodeScanner.process(image)
                            .addOnSuccessListener { barcodes ->
                                for (barcode in barcodes) {
                                    val raw = barcode.rawValue
                                    if (!raw.isNullOrBlank() && (barcode.format == Barcode.FORMAT_EAN_13 || barcode.format == Barcode.FORMAT_UPC_A || barcode.format == Barcode.FORMAT_CODE_128)) {
                                        isProcessing = true
                                        onBarcodeScanned(raw)
                                        break
                                    }
                                }
                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                            }
                    } else {
                        imageProxy.close()
                    }
                }

                try {
                    cameraProvider.unbindAll()
                    val camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                    activeCamera = camera
                    onCameraReady(camera)
                } catch (exc: Exception) {
                    exc.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun ScanningOverlay(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "laser")
    val animatedProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_pos"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val boxWidth = width * 0.75f
        val boxHeight = boxWidth * 0.55f // Rectangular barcode aspect ratio

        val left = (width - boxWidth) / 2f
        val top = (height - boxHeight) / 2.5f

        // Draw semi-transparent dark backdrop
        drawRect(
            color = Color(0x77000000),
            size = size
        )

        // Clear target window inside
        drawRoundRect(
            color = Color.Transparent,
            topLeft = Offset(left, top),
            size = Size(boxWidth, boxHeight),
            cornerRadius = CornerRadius(12f, 12f),
            blendMode = androidx.compose.ui.graphics.BlendMode.Clear
        )

        // Draw modern viewfinder border corners
        val cornerLength = 36f
        val strokeWidth = 5f
        val cornerColor = Color(0xFFF59E0B) // Warm Amber

        // Top-left
        drawLine(cornerColor, Offset(left, top), Offset(left + cornerLength, top), strokeWidth)
        drawLine(cornerColor, Offset(left, top), Offset(left, top + cornerLength), strokeWidth)

        // Top-right
        drawLine(cornerColor, Offset(left + boxWidth, top), Offset(left + boxWidth - cornerLength, top), strokeWidth)
        drawLine(cornerColor, Offset(left + boxWidth, top), Offset(left + boxWidth, top + cornerLength), strokeWidth)

        // Bottom-left
        drawLine(cornerColor, Offset(left, top + boxHeight), Offset(left + cornerLength, top + boxHeight), strokeWidth)
        drawLine(cornerColor, Offset(left, top + boxHeight), Offset(left, top + boxHeight - cornerLength), strokeWidth)

        // Bottom-right
        drawLine(cornerColor, Offset(left + boxWidth, top + boxHeight), Offset(left + boxWidth - cornerLength, top + boxHeight), strokeWidth)
        drawLine(cornerColor, Offset(left + boxWidth, top + boxHeight), Offset(left + boxWidth, top + boxHeight - cornerLength), strokeWidth)

        // Subtle thin box outline
        drawRoundRect(
            color = Color(0x44FFFFFF),
            topLeft = Offset(left, top),
            size = Size(boxWidth, boxHeight),
            cornerRadius = CornerRadius(12f, 12f),
            style = Stroke(width = 1.5f)
        )

        // Animated laser scanning line
        val laserY = top + (boxHeight * animatedProgress)
        drawLine(
            color = Color(0xFFEF4444), // Bright red laser
            start = Offset(left + 10f, laserY),
            end = Offset(left + boxWidth - 10f, laserY),
            strokeWidth = 3f
        )
    }
}
