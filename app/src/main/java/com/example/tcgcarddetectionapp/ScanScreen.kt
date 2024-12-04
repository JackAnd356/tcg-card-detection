package com.example.tcgcarddetectionapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

var cards = arrayOf<CardData>()

enum class Stages(val title: Int) {
    Home(title = 0),
    Confirmation(title = 1),
    PostConfirmation(title = 2),
}

@Composable
fun ScanScreen(modifier: Modifier = Modifier, userid: String, collectionNavigate: () -> Unit, addToOverallCards: (CardData) -> Unit) {
    val stage = remember { mutableStateOf(Stages.Home) }

    when (stage.value) {
        Stages.Home -> {
            ScanHome(modifier, stage)
        }
        Stages.Confirmation -> {
            ScanConfirmation(modifier, userid, stage, addToOverallCards = addToOverallCards)
        }
        Stages.PostConfirmation -> {
            ScanPostConfirmation(modifier, stage, collectionNavigate)
        }
    }
}

@Composable
fun ScanHome(modifier: Modifier, stage: MutableState<Stages>) {
    val error = remember { mutableIntStateOf(0) }
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

        Box(
            Modifier
                .height(550.dp)
                .width(400.dp)
                .background(Color.Black)) {
            Text(text = "Make sure to include all of the card you are trying to scan", fontSize = 30.sp, color = Color.White)
        }

        ImageCaptureFromCamera(Modifier.align(Alignment.CenterHorizontally), stage = stage, err = error)

        if (error.intValue > 0) {
            Dialog(onDismissRequest = { error.intValue = 0 }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(375.dp)
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    if (error.intValue == 1) {
                        Text(text = "Cards could not be found within the image, try taking an image in better lighting or closer up")
                    } else {
                        Text(text = "There was a network error in processing your image, try again")
                    }
                }
            }
        }
    }
}

@Composable
fun ScanConfirmation(modifier: Modifier, userid: String, stage: MutableState<Stages>, addToOverallCards: (CardData) -> Unit) {
    Column(verticalArrangement = Arrangement.Top, modifier = modifier
        .fillMaxSize()
        .wrapContentWidth(Alignment.CenterHorizontally)) {
        if (cards.isEmpty()) {
            val sampleCard1 = CardData(cardid = "85106525", setcode = "MP24-EN133", game = "yugioh", cardname = "Bonfire",
                rarity = "Prismatic Secret Rare", quantity = 1, price = 8.24, userid = "1", subcollections = null, image = null)
            val sampleCard2 = CardData(cardid = "54693926", setcode = "SDAZ-EN030", game = "yugioh", cardname = "Dark Ruler No More",
                rarity = "Common", quantity = 1, price = 0.27, userid = "1", subcollections = null, image = null)
            val sampleCard3 = CardData(cardid = "0079", setcode = "FDN", game = "mtg", cardname = "Boltwave",
                rarity = "U", quantity = 1, price = 2.60, userid = "1", subcollections = null, image = null, cost = "R",
                attribute = "R", description = "Boltwave deals 3 damage to each opponent.", atk = "", def = "", type = "sorcery")
            cards = arrayOf(sampleCard2, sampleCard3, sampleCard1)
        }
        val map = cards.groupBy { it.game }
        map.forEach { entry ->
            val game = entry.key
            Text(text = game, fontSize = 32.sp)
            for (cardData in entry.value) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = cardData.added.value,
                        onCheckedChange = { cardData.added.value = it }
                    )
                    Text(
                        text = "x${cardData.quantity} ${cardData.cardname}"
                    )
                }
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = {
                stage.value = Stages.Home
            }) {
                Text(text = "Cancel")
            }

            Button(onClick = {
                cards.forEach { card ->
                    if (card.added.value) {
                        addToCollectionPost(userid = userid, card = card, addToOverallCards = addToOverallCards)
                    }
                }
                stage.value = Stages.PostConfirmation
            }) {
                Text(text = "Submit")
            }
        }
    }
}

@Composable
fun ScanPostConfirmation(modifier: Modifier, stage: MutableState<Stages>, collectionNavigate: () -> Unit) {
    Column(verticalArrangement = Arrangement.Top, modifier = modifier
        .fillMaxSize()
        .wrapContentWidth(Alignment.CenterHorizontally)) {

        Text(text = "Added Cards: ", fontSize = 36.sp)

        val map = cards.groupBy { it.game }
        map.forEach({ entry ->
            val game = entry.key
            Text(text = game, fontSize = 32.sp)
            var cardsFromGame = false
            for (cardData in entry.value) {
                if (cardData.added.value) {
                    cardsFromGame = true
                    Text(
                        text = "x${cardData.quantity} ${cardData.cardname}"
                    )
                }
            }
            if (!cardsFromGame) Text(text = "None")
        })

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { stage.value = Stages.Home }
            ) {
                Text(text = "Take Another Photo")
            }

            Button(onClick = collectionNavigate) {
                Text(text = "Go To Yu-Gi-Oh Collection")
            }
        }
    }
}

fun addToCollectionPost(userid: String, card: CardData, addToOverallCards: (CardData) -> Unit) {
    val retrofit = Retrofit.Builder()
        .baseUrl(api_url)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val retrofitAPI = retrofit.create(ApiService::class.java)

    val requestData = AddRemoveCardModel(userid = userid, game = card.game, cardid = card.cardid, setcode = card.setcode,
        cardname = card.cardname, price = card.price, quantity = card.quantity, level = card.level, attribute = card.attribute,
        type = card.type, atk = card.atk, def = card.def, description = card.description, cost = card.cost, attacks = card.attacks,
        weaknesses = card.weaknesses, hp = card.hp, retreat = card.retreat, rarity = card.rarity)

    retrofitAPI.addToCollection(requestData).enqueue(object:
        Callback<GenericSuccessErrorResponseModel> {
        override fun onResponse(
            call: Call<GenericSuccessErrorResponseModel>,
            response: Response<GenericSuccessErrorResponseModel>
        ) {
            val respData = response.body()
            if (respData != null) {
                println("Adding to overall cards")
                addToOverallCards(card)
            }
        }

        override fun onFailure(call: Call<GenericSuccessErrorResponseModel>, t: Throwable) {
            t.printStackTrace()
        }

    })
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

fun scanPhotoPost(imageFile: File, stage: MutableState<Stages>, err : MutableIntState) {
    val retrofit = Retrofit.Builder()
        .baseUrl(api_url)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val retrofitAPI = retrofit.create(ApiService::class.java)

    val requestFile = RequestBody.create(MediaType.parse("image/jpeg"), imageFile)
    val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)

    val call = retrofitAPI.getCardInfo(imagePart)


    call.enqueue(object : Callback<Array<CardData>> {
        override fun onResponse(
            call: Call<Array<CardData>>,
            response: Response<Array<CardData>>
        ) {
            val respData = response.body()
            if (respData != null) {
                cards = respData
                stage.value = Stages.Confirmation
                /*if (cards.isNotEmpty()) stage.value = Stages.Confirmation
                else err.intValue = 1*/
            } else {
                // Handle error response
                err.intValue = 2
                println("Upload failed: ${response.code()}")
            }
        }

        override fun onFailure(call: Call<Array<CardData>>, t: Throwable) {
            // Handle failure
            err.intValue = 2
            println("Upload error: ${t.message}")
        }
    })
}

@Composable
fun ImageCaptureFromCamera(modifier: Modifier, stage: MutableState<Stages>, err: MutableIntState) {
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
                scanPhotoPost(file, stage, err)
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
        ScanScreen(modifier = modifier, userid = "2", collectionNavigate = {}, addToOverallCards = {})
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