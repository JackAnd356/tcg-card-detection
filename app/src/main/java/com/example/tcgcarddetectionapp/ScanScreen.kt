package com.example.tcgcarddetectionapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import android.util.Log
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
import androidx.compose.ui.graphics.vector.rememberVectorPainter
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
import com.example.tcgcarddetectionapp.ui.theme.TCGCardDetectionAppTheme
import java.io.File
import java.io.IOException
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

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { isSuccess: Boolean ->
            Toast.makeText(context, "Image capture: ${if(isSuccess) "Successful" else "Failed"}", Toast.LENGTH_SHORT)
                .show()
        }
    )

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

        if (cameraPermission) {
            CameraPreview(modifier = Modifier
                .fillMaxSize()
                .weight(1f))
        } else {
            Text("Camera permission is required.")
        }

        TakePictureButton(onClick = {
            if (cameraPermission) {
                val file = createImageFile(context)
                if (file != null) {
                    imgUri = file
                    val uri = FileProvider.getUriForFile(
                        context, "application_authority", file
                    )
                    takePictureLauncher.launch(uri)
                }
            } else {
                Toast.makeText(context, "Camera permission not granted", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

// Create a temporary image file and return its URI
fun createImageFile(context : Context): File? {
    return try {
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (storageDir?.exists() == false) storageDir.mkdirs() // Ensure directory exists
        File.createTempFile("temp", ".jpg", storageDir)
    } catch (e: IOException) {
        Log.e("ScanScreen", "File creation failed", e)
        Toast.makeText(context, "Failed to create image file", Toast.LENGTH_SHORT).show()
        null
    }
}

@Composable
fun TakePictureButton(onClick: () -> Unit) {
    val image = painterResource(R.drawable.photo_button)
    Button(onClick = { onClick()}) {
        Image(
            painter = image,
            contentDescription = "Take Photo Button"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ScanScreenPreview() {
    TCGCardDetectionAppTheme {
        ScanScreen()
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