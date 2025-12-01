package com.moaga.app.ui.screens.group.create

import android.content.Context
import android.net.Uri
import android.util.Size
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import android.util.Base64
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.gson.Gson
import com.moaga.app.data.api.IdApiClient
import com.moaga.app.data.api.IdRequest
import com.moaga.app.data.api.IdResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdCameraScreen(
    onBack: () -> Unit = {},
    onCaptured: (Uri, IdResponse?) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val hasPermission = rememberCameraPermission()          // 권한 처리
    val imageCapture = remember { ImageCapture.Builder()
        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
        .setTargetResolution(Size(1280, 720))
        .build()
    }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(Modifier.fillMaxWidth().padding(end = 48.dp), contentAlignment = Alignment.Center) {
                        Text("신분증 확인", color = Color.White)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF2F2F38))
            )
        },
        containerColor = Color(0xFF2F2F38)
    ) { inner ->
        if (!hasPermission) {
            // 권한 거부 시 가이드
            Box(Modifier.fillMaxSize().padding(inner), contentAlignment = Alignment.Center) {
                Text("카메라 권한이 필요합니다.", color = Color.White)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 미리보기 + 오버레이
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                val previewView = remember { PreviewView(context) }
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = {
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        bindCameraUseCases(context, lifecycleOwner, preview, imageCapture)
                        previewView
                    }
                )

                // 신분증 위치 가이드(흰색 라운드 박스)
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(0.9f)
                        .aspectRatio(1.6f) // 신분증 가로형 비율
                        .border(3.dp, Color.White, RoundedCornerShape(16.dp))
                )
            }

            Spacer(Modifier.height(12.dp))
            Text(
                "영역 안에 신분증이 꽉 차도록 배치 후\n하단 버튼을 누르면 촬영됩니다.",
                color = Color(0xFFEDEDED)
            )
            Spacer(Modifier.height(20.dp))

            // 셔터 버튼
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .background(Color(0xFF7C8BFF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        takePicture(
                            context = context,
                            scope = scope,
                            imageCapture = imageCapture,
                            onCaptured = onCaptured
                        )
                    },
                    shape = CircleShape,
                    modifier = Modifier.size(68.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5F72FF))
                ) {}
            }

            Spacer(Modifier.height(28.dp))
        }
    }
}

/* ---- helpers ---- */

private fun bindCameraUseCases(
    context: Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    preview: Preview,
    imageCapture: ImageCapture
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()
        try {
            cameraProvider.unbindAll()
            val selector = CameraSelector.DEFAULT_BACK_CAMERA
            cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview, imageCapture)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }, ContextCompat.getMainExecutor(context))
}

private fun takePicture(
    context: Context,
    scope: CoroutineScope,
    imageCapture: ImageCapture,
    onCaptured: (Uri, IdResponse?) -> Unit
) {
    val file = File(context.cacheDir, "id_${System.currentTimeMillis()}.jpg")
    val output = ImageCapture.OutputFileOptions.Builder(file).build()

    imageCapture.takePicture(
        output,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onError(exception: ImageCaptureException) {
                Log.e("OCR", "촬영 실패", exception)
                onCaptured(Uri.fromFile(file), null)
            }

            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                scope.launch {
                    try {
                        // 1) Base64 인코딩
                        val bytes = file.readBytes()
                        val base64 = Base64.encodeToString(bytes, Base64.DEFAULT)
                        val request = IdRequest(encrypted_image = base64)
                        Log.d("OCR", "보내는 Request 길이=${base64.length}")

                        val gson = Gson()
                        Log.d("OCR", "보내는 JSON: ${gson.toJson(request)}")
                        // 2) 서버 전송
                        val res = IdApiClient.api.sendIdCard(request)
                        Log.d("OCR", "서버 응답: $res")

                        // 3) 결과 전달
                        onCaptured(Uri.fromFile(file), res)

                    } catch (e: Exception) {
                        Log.e("OCR", "서버 전송 실패", e)
                        onCaptured(Uri.fromFile(file), null)
                    }
                }
            }
        }
    )
}

@Composable
private fun rememberCameraPermission(): Boolean {
    val context = LocalContext.current
    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted -> granted = isGranted }

    LaunchedEffect(Unit) {
        if (!granted) launcher.launch(Manifest.permission.CAMERA)
    }
    return granted
}

private fun encodeFileToBase64(file: File): String {
    val bytes = FileInputStream(file).use { it.readBytes() }
    return Base64.encodeToString(bytes, Base64.NO_WRAP)
}