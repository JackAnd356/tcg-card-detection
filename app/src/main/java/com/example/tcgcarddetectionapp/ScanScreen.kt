package com.example.tcgcarddetectionapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.camera.core.Preview as CameraPreview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.tcgcarddetectionapp.models.AddRemoveCardModel
import com.example.tcgcarddetectionapp.models.CardData
import com.example.tcgcarddetectionapp.models.GenericSuccessErrorResponseModel
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
            CameraPreviewScreen(modifier, stage)
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
fun CameraPreviewScreen(modifier: Modifier, stage: MutableState<Stages>) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val err = remember { mutableIntStateOf(0) }

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    val previewView = remember { PreviewView(context) }

    var showPhotoTipsDialog by remember { mutableStateOf(true) }

    if (showPhotoTipsDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoTipsDialog = false },
            title = { Text(stringResource(R.string.scan_screen_tips_title)) },
            text = {
                Text(stringResource(R.string.scan_screen_tips_1) + "\n" +
                        stringResource(R.string.scan_screen_tips_2) + "\n" +
                        stringResource(R.string.scan_screen_tips_3) + "\n" +
                        stringResource(R.string.scan_screen_tips_4) + "\n" +
                        stringResource(R.string.scan_screen_tips_5))
            },
            confirmButton = {
                TextButton(onClick = { showPhotoTipsDialog = false }) {
                    Text(stringResource(R.string.confirm_tips_button_label))
                }
            }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(factory = {
            val preview = CameraPreview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )
                } catch (e: Exception) {
                    Log.e("CameraX", "Use case binding failed", e)
                }
            }, ContextCompat.getMainExecutor(context))

            previewView
        }, modifier = Modifier.fillMaxSize())

        // Overlay corners
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cornerLength = 40.dp.toPx()
            val strokeWidth = 4.dp.toPx()
            val padding = 32.dp.toPx()

            val left = padding
            val top = padding
            val right = size.width - padding
            val bottom = size.height - padding

            drawRectGuideCorners(left, top, right, bottom, cornerLength, strokeWidth)
        }
    }

    // Floating Action Button to capture image
    Box(modifier = Modifier.fillMaxSize()) {
        Button(onClick = {
            val photoFile = context.createImageFile()
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

            imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        scanPhotoPost(photoFile, stage, err)
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Log.e("CameraX", "Photo capture failed: ${exception.message}", exception)
                    }
                }
            )
        },
            modifier = modifier.align(Alignment.BottomCenter)
                .padding(bottom = 200.dp) // Adjust this value to fine-tune the vertical position
                .size(125.dp)) {
            Text(stringResource(R.string.photo_button_label))
        }
    }
}

@Composable
fun ScanHome(modifier: Modifier, stage: MutableState<Stages>) {
    val screenHeight = LocalConfiguration.current.screenHeightDp

    val error = remember { mutableIntStateOf(0) }
    Column(verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier
            .padding(horizontal = 10.dp)
            .fillMaxSize()
            .wrapContentWidth(Alignment.CenterHorizontally)) {
        Text(
            text = stringResource(R.string.scan_screen_heading),
            style = appTypography.headlineLarge,
            modifier = Modifier.padding(16.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Box(modifier = Modifier
            .height((screenHeight / 1.7).dp)
            .padding(horizontal = 10.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(colorResource(R.color.textLightGrey))) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(modifier = Modifier.padding(5.dp),
                    text = stringResource(R.string.user_photo_instructions_header),
                    style = appTypography.headlineLarge,
                    textAlign = TextAlign.Center
                )
                Text(modifier = Modifier.padding(10.dp),
                    text = stringResource(R.string.user_photo_instructions),
                    style = appTypography.bodyLarge
                )
            }
        }

        Spacer(modifier= Modifier.height(20.dp))
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
                        Text(
                            text = stringResource(R.string.no_card_error),
                            style = appTypography.headlineMedium
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.scan_network_error),
                            style = appTypography.headlineMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScanConfirmation(modifier: Modifier, userid: String, stage: MutableState<Stages>, addToOverallCards: (CardData) -> Unit) {
    if (cards.isEmpty()) {
        val sampleCard1 = CardData(cardid = "85106525", setcode = "MP24-EN133", game = "yugioh", cardname = "Bonfire",
            quantity = 1, price = 8.24, userid = "1", subcollections = null, image = null, level="0", attribute = "spell", type = "none", atk = "0", def = "0",
            description = "Add 1 Level 4 or lower Pyro monster from your Deck to your hand. You can only activate 1 \"Bonfire\" once per turn", possRarities = arrayOf("Common", "Ultra Rare"))
        val sampleCard2 = CardData(cardid = "54693926", setcode = "SDAZ-EN030", game = "yugioh", cardname = "Dark Ruler No More",
            rarity = "Common", quantity = 1, price = 0.27, userid = "1", subcollections = null, image = null, level="0", attribute = "spell", type = "none", atk = "0", def = "0",
            description = "Negate the effects of all face-up monsters your opponent currently controls, until the end of this turn, also, for the rest of this turn after this card resolves, your opponent takes no damage. Neither player can activate monster effects in response to this card's activation")
        val sampleCard3 = CardData(cardid = "8d1ec351-5e70-4eb2-b590-6bff94ef8178", setcode = "FDN", game = "mtg", cardname = "Boltwave",
            rarity = "U", quantity = 1, price = 2.60, userid = "1", subcollections = null, image = null, cost = "R",
            attribute = "R", description = "Boltwave deals 3 damage to each opponent.", atk = "", def = "", type = "sorcery")
        cards = arrayOf(sampleCard2, sampleCard3, sampleCard1)
    }
    val context = LocalContext.current
    val rarityPopups = remember { mutableStateListOf(*Array(cards.size) { false }) }
    var counter = 0
    Column(verticalArrangement = Arrangement.Top, modifier = modifier
        .fillMaxSize()
        .wrapContentWidth(Alignment.CenterHorizontally)) {
        Column(verticalArrangement = Arrangement.Top, modifier = modifier
            .padding(16.dp)
            .background(Color.Gray)
            .padding(16.dp)) {
            val map = cards.groupBy { it.game }
            map.forEach { entry ->
                val game = entry.key
                Text(
                    text = mapGameToFullName(game),
                    style = appTypography.displaySmall
                )
                for (cardData in entry.value) {
                    if (cardData.possRarities != null) cardData.added.value = false
                    val isChecked = remember { cardData.added }
                    val index = counter
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .background(
                                color = if (cardData.possRarities != null) Color(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.extremelyLightRed
                                    )
                                )
                                else if (isChecked.value) Color(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.extremelyLightBlue
                                    )
                                )
                                else Color(ContextCompat.getColor(context, R.color.gray)),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(
                                width = 2.dp,
                                color = if (cardData.possRarities != null) Color(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.buttonRedBorder
                                    )
                                )
                                else if (isChecked.value) Color(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.buttonBlueBorder
                                    )
                                )
                                else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                if (cardData.possRarities != null) {
                                    rarityPopups[index] = true
                                } else {
                                    isChecked.value = !isChecked.value
                                }
                            }
                            .padding(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "x${cardData.quantity} ${cardData.cardname}",
                                style = appTypography.labelMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Checkbox(
                                checked = isChecked.value,
                                onCheckedChange = {
                                    isChecked.value = it

                                }
                            )
                        }
                    }
                    if (rarityPopups[index]) {
                        Dialog(onDismissRequest = {
                            rarityPopups[index] = !rarityPopups[index]
                        }) {
                            ScanSetCodeChoicePopup(modifier, cardData, {rarityPopups[index] = !rarityPopups[index]})
                        }
                    }
                    counter++
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = {
                    stage.value = Stages.Home
                }) {
                    Text(
                        text = stringResource(R.string.cancel_button_label),
                        style = appTypography.labelLarge
                    )
                }

                Button(onClick = {
                    cards.forEach { cardData ->
                        if (cardData.added.value) {
                            addToCollectionPost(userid = userid, card = cardData, addToOverallCards = addToOverallCards)
                            cardImagePost(cardData.cardid, cardData.game, {image, bitmap ->
                                cardData.image = image
                                cardData.imageBitmap = bitmap
                            })
                        }
                    }
                    stage.value = Stages.PostConfirmation
                }) {
                    Text(
                        text = stringResource(R.string.submit),
                        style = appTypography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
fun ScanSetCodeChoicePopup(modifier: Modifier, card: CardData, dismissPopup: () -> Unit) {
    val context = LocalContext.current
    val selectedRarity = remember { mutableStateOf<String?>(null) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.rarity_choice_text),
                style = appTypography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(card.possRarities.orEmpty()) { rarity ->
                    val placeholderPair = mapRarityToPlaceholder(rarity)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(8.dp)
                            .clickable { selectedRarity.value = rarity }
                    ) {
                        Text(
                            text = rarity,
                            style = appTypography.headlineSmall
                        )
                        Image(
                            painter = painterResource(placeholderPair.first),
                            contentDescription = stringResource(R.string.card_image_not_loaded_context),
                            modifier = Modifier
                                .size(120.dp, 200.dp)
                                .border(
                                    width = if (selectedRarity.value == rarity) 4.dp else 2.dp,
                                    color = if (selectedRarity.value == rarity) Color.Blue else Color.Gray,
                                    shape = RoundedCornerShape(8.dp)
                                )
                        )
                        Text(
                            text = stringResource(placeholderPair.second),
                            color = if (selectedRarity.value == rarity) Color.Blue else Color.Black,
                            style = appTypography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
            Row {
                Button(onClick = {
                    if (selectedRarity.value != null) {
                        card.rarity = selectedRarity.value!!
                        card.possRarities = null
                        dismissPopup()
                    } else {
                        Toast.makeText(context, context.getString(R.string.select_rarity_error), Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text(
                        text = stringResource(R.string.submit),
                        style = appTypography.labelLarge,
                        )
                }
                Button(onClick = {
                    dismissPopup()
                }) {
                    Text(
                        text = stringResource(R.string.ignore_card),
                        style = appTypography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
fun ScanPostConfirmation(modifier: Modifier, stage: MutableState<Stages>, collectionNavigate: () -> Unit) {
    Column(verticalArrangement = Arrangement.Top, modifier = modifier
        .fillMaxSize()
        .wrapContentWidth(Alignment.CenterHorizontally)) {

        Text(text = stringResource(R.string.added_cards),
            style = appTypography.displaySmall
        )

        val map = cards.groupBy { it.game }
        map.forEach({ entry ->
            val game = entry.key
            Text(
                text = game,
                style = appTypography.headlineLarge
            )
            var cardsFromGame = false
            for (cardData in entry.value) {
                if (cardData.added.value) {
                    cardsFromGame = true
                    Text(
                        text = "x${cardData.quantity} ${cardData.cardname}",
                        style = appTypography.labelMedium
                    )
                }
            }
            if (!cardsFromGame) {
                Text(
                    text = stringResource(R.string.none),
                    style = appTypography.labelMedium
                )
            }
        })

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { stage.value = Stages.Home }
            ) {
                Text(
                    text = stringResource(R.string.take_another_photo_label),
                    style = appTypography.labelLarge
                )
            }

            Button(onClick = collectionNavigate) {
                Text(
                    text = stringResource(R.string.go_to_collection_label),
                    style = appTypography.labelLarge
                )
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

    val image = painterResource(R.drawable.takepicture)
    Button(modifier = modifier
        .width(125.dp)
        .height(125.dp), onClick =  {
        if (cameraPermission) {
            takePictureLauncher.launch(uri)
        } else {
            cameraPermLauncher.launch(Manifest.permission.CAMERA)
        }
    }) {
        Icon(painter = image, contentDescription = stringResource(R.string.photo_button_label))
    }
}

fun DrawScope.drawRectGuideCorners(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    cornerLength: Float,
    strokeWidth: Float,
    inset: Float = 10f,      // Bring corners closer to the edge
    bottomOffset: Float = 900f // Pull bottom corners upward
) {
    val color = Color.White
    val adjustedLeft = left + inset
    val adjustedTop = top + inset
    val adjustedRight = right - inset
    val adjustedBottom = bottom - inset - bottomOffset

    // Top-left
    drawLine(
        start = Offset(adjustedLeft, adjustedTop),
        end = Offset(adjustedLeft + cornerLength, adjustedTop),
        color = color,
        strokeWidth = strokeWidth
    )
    drawLine(
        start = Offset(adjustedLeft, adjustedTop),
        end = Offset(adjustedLeft, adjustedTop + cornerLength),
        color = color,
        strokeWidth = strokeWidth
    )

    // Top-right
    drawLine(
        start = Offset(adjustedRight, adjustedTop),
        end = Offset(adjustedRight - cornerLength, adjustedTop),
        color = color,
        strokeWidth = strokeWidth
    )
    drawLine(
        start = Offset(adjustedRight, adjustedTop),
        end = Offset(adjustedRight, adjustedTop + cornerLength),
        color = color,
        strokeWidth = strokeWidth
    )

    // Bottom-left
    drawLine(
        start = Offset(adjustedLeft, adjustedBottom),
        end = Offset(adjustedLeft + cornerLength, adjustedBottom),
        color = color,
        strokeWidth = strokeWidth
    )
    drawLine(
        start = Offset(adjustedLeft, adjustedBottom),
        end = Offset(adjustedLeft, adjustedBottom - cornerLength),
        color = color,
        strokeWidth = strokeWidth
    )

    // Bottom-right
    drawLine(
        start = Offset(adjustedRight, adjustedBottom),
        end = Offset(adjustedRight - cornerLength, adjustedBottom),
        color = color,
        strokeWidth = strokeWidth
    )
    drawLine(
        start = Offset(adjustedRight, adjustedBottom),
        end = Offset(adjustedRight, adjustedBottom - cornerLength),
        color = color,
        strokeWidth = strokeWidth
    )
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