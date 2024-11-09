package com.example.tcgcarddetectionapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import com.example.tcgcarddetectionapp.ui.theme.TCGCardDetectionAppTheme
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun ScanScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

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

        Box(Modifier.height(650.dp).width(400.dp).background(Color.Black)) {
            Text(text = "Make sure to include all of the card you are trying to scan", fontSize = 30.sp, color = Color.White)
        }

        ImageCaptureFromCamera(Modifier.align(Alignment.CenterHorizontally))
    }
}

// Create a temporary image file and return its URI
fun Context.createImageFile(): File {
    val timeStamp = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US).format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    val image = File.createTempFile(imageFileName, ".jpg", externalCacheDir)
    /*println("Path on Creation" + image.path)
    println("Abs Path On Creation" + image.absolutePath)*/
    return image
}

fun scanPhotoPost(imageFile: File) {
    val url = "http://10.0.2.2:5000"
    val retrofit = Retrofit.Builder()
        .baseUrl(url)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val retrofitAPI = retrofit.create(ApiService::class.java)

    val requestFile = RequestBody.create(MediaType.parse("image/jpeg"), imageFile)
    val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)

    val call = retrofitAPI.getCardInfo(imagePart)

    call.enqueue(object : Callback<ResponseBody> {
        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
            if (response.isSuccessful) {
                // Handle success
                println("Image uploaded successfully!")
            } else {
                // Handle error response
                println("Upload failed: ${response.code()}")
            }
        }

        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            // Handle failure
            println("Upload error: ${t.message}")
        }
    })
}

@Composable
fun ImageCaptureFromCamera(modifier: Modifier) {
    val context = LocalContext.current
    var cameraPermission by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        // Check if camera permission is granted
        cameraPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }
    val file by remember {
        mutableStateOf(context.createImageFile())
    }
    val uri = FileProvider.getUriForFile(
        context,
        context.packageName + ".provider",
        file
    )

    var picTaken by remember { mutableStateOf(false) }

    val takePictureLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicture(),
        onResult = { isPictureTaken ->
            if (isPictureTaken) {
                scanPhotoPost(file)
                picTaken = true
            }
        })

    val cameraPermLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission(), onResult = { isGranted ->
        if (isGranted) {
            cameraPermission = true
        }
    })

    val image = painterResource(R.drawable.photo_button)
    Button(modifier = modifier, onClick =  {
        if (cameraPermission) {
            takePictureLauncher.launch(uri)
        } else {
            cameraPermLauncher.launch(Manifest.permission.CAMERA)
        }
    }) {
        Image(
            painter = image,
            contentDescription = "Take Photo Button"
        )
    }

    if (picTaken) {
        println("Picture Taken")
        val painter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(context)
                .data(uri)
                .build()
        )

        Image(
            painter = painter,
            modifier = Modifier,
            contentDescription = "Captured Image"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ScanScreenPreview(modifier: Modifier = Modifier) {
    TCGCardDetectionAppTheme {
        ScanScreen(modifier = modifier)
    }
}

/*
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
}*/