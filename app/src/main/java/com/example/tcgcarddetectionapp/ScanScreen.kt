package com.example.tcgcarddetectionapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import com.example.tcgcarddetectionapp.ui.theme.TCGCardDetectionAppTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.camera.core.Preview as CamPreview


@Composable
fun ScanScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var cameraPermission by remember { mutableStateOf(false) }
    var imgUri by remember { mutableStateOf<File?>(null)}

    LaunchedEffect(Unit) {
        // Check if camera permission is granted
        cameraPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    val cameraPermLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission(), onResult = { isGranted ->
        if (isGranted) {
            cameraPermission = true
        }
    })

    Column(verticalArrangement = Arrangement.Top, modifier = modifier
        .fillMaxSize()
        .wrapContentWidth(Alignment.CenterHorizontally)) {
        Text(
            text = "Scan Screen",
            fontSize = 40.sp,
            lineHeight = 46.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {
            if (!cameraPermission) {
                cameraPermLauncher.launch(Manifest.permission.CAMERA)
            }
        }) {
            Text(text = "Camera Permission $cameraPermission")
        }

        ImageCaptureFromCamera()
    }
}

// Create a temporary image file and return its URI
fun Context.createImageFile(): File {
    val timeStamp = SimpleDateFormat("yyyy_mm_dd_hh:mm:ss", Locale.US).format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    val image = File.createTempFile(imageFileName, ".jpg", externalCacheDir)

    return image
}

@Composable
fun ImageCaptureFromCamera() {
    val context = LocalContext.current
    val file = context.createImageFile()
    val uri = FileProvider.getUriForFile(
        context,
        context.packageName + ".provider",
        file
    )

    var capturedImageUri by remember {
        mutableStateOf<Uri>(Uri.EMPTY)
    }

    val takePictureLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicture()) {
        capturedImageUri = uri
    }

    val image = painterResource(R.drawable.photo_button)
    Button(onClick = {
        val permResult = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        if (permResult == PackageManager.PERMISSION_GRANTED) {
            takePictureLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Camera Permission Required", Toast.LENGTH_SHORT).show()
        }
    }) {
        Image(
            painter = image,
            contentDescription = "Take Photo Button"
        )
    }

    if (capturedImageUri.path?.isNotEmpty() == true) {
        val painter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .data(capturedImageUri)
                .build()
        )

        Image(
            painter = painter,
            modifier = Modifier,
            contentDescription = "Captured Image"
        )
    } else {
        CameraPreview(modifier = Modifier.fillMaxSize())
    }
}

@Preview(showBackground = true)
@Composable
fun ScanScreenPreview(modifier: Modifier = Modifier) {
    TCGCardDetectionAppTheme {
        ScanScreen(modifier = modifier)
    }
}

@Composable
fun CameraPreview(modifier: Modifier = Modifier) {
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }

            // Set up CameraProvider
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = CamPreview.Builder()
                    .build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                // Select Back Camera
                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build()

                // Bind the Camera to the Preview
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner, cameraSelector, preview
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = modifier
    )
}