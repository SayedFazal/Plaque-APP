package com.periocompliance.ai.ui.scan

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.periocompliance.ai.R
import com.periocompliance.ai.domain.model.ScanSummary
import com.periocompliance.ai.ui.auth.components.messageRes
import com.periocompliance.ai.ui.dashboard.components.IconChip
import com.periocompliance.ai.ui.theme.PerioTheme
import java.io.File
import java.util.concurrent.Executors

/**
 * Module 3 — the daily scan.
 *
 * A verified user captures a photo of their gums; submitting records today's scan, which is what
 * lights up the dashboard's streak and compliance. The screen is a small state machine: checking →
 * (already done | camera capture → review → submitting) → complete, with permission and error
 * branches. Navigation out is entirely via the hoisted callbacks.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyScanScreen(
    onDone: () -> Unit,
    onClose: () -> Unit,
    viewModel: DailyScanViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.scan_title)) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.scan_cd_close))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                state.checking -> LoadingState()

                state.isComplete -> ScanCompleteContent(
                    summary = state.summary,
                    alreadyDone = state.alreadyScannedToday && !state.submitted,
                    onDone = onDone,
                )

                else -> CaptureContent(
                    isSubmitting = state.isSubmitting,
                    errorMessageRes = state.error?.messageRes(),
                    onSubmit = viewModel::onSubmit,
                    onErrorDismissed = viewModel::onErrorDismissed,
                )
            }
        }
    }
}

// --- capture flow ------------------------------------------------------------

@Composable
private fun CaptureContent(
    isSubmitting: Boolean,
    errorMessageRes: Int?,
    onSubmit: (Uri?) -> Unit,
    onErrorDismissed: () -> Unit,
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    var requestedOnce by remember { mutableStateOf(false) }
    var capturedUri by remember { mutableStateOf<Uri?>(null) }
    var cameraError by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> hasPermission = granted }

    // Ask once automatically the first time the screen needs the camera.
    LaunchedEffect(Unit) {
        if (!hasPermission) {
            requestedOnce = true
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    when {
        !hasPermission -> CameraPermissionRequest(
            showSettings = requestedOnce,
            onGrant = { permissionLauncher.launch(Manifest.permission.CAMERA) },
            onOpenSettings = { context.openAppSettings() },
        )

        capturedUri != null -> CapturedReview(
            uri = capturedUri!!,
            isSubmitting = isSubmitting,
            errorMessageRes = errorMessageRes,
            onRetake = {
                capturedUri = null
                onErrorDismissed()
            },
            onSubmit = { onSubmit(capturedUri) },
        )

        cameraError -> CameraErrorState(onRetry = { cameraError = false })

        else -> CameraCapture(
            onCaptured = { capturedUri = it },
            onCameraError = { cameraError = true },
        )
    }
}

@Composable
private fun CameraCapture(
    onCaptured: (Uri) -> Unit,
    onCameraError: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val controller = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE)
        }
    }
    val executor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(lifecycleOwner) {
        controller.bindToLifecycle(lifecycleOwner)
        onDispose {
            controller.unbind()
            executor.shutdown()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.scan_instruction),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(PerioTheme.spacing.md),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = PerioTheme.spacing.screenMargin),
            contentAlignment = Alignment.Center,
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 4f),
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        this.controller = controller
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                },
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = PerioTheme.spacing.lg),
            contentAlignment = Alignment.Center,
        ) {
            ShutterButton(
                onClick = {
                    val file = File(context.cacheDir, "scan_${System.currentTimeMillis()}.jpg")
                    val output = ImageCapture.OutputFileOptions.Builder(file).build()
                    controller.takePicture(
                        output,
                        executor,
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(results: ImageCapture.OutputFileResults) {
                                onCaptured(results.savedUri ?: Uri.fromFile(file))
                            }

                            override fun onError(exception: ImageCaptureException) {
                                onCameraError()
                            }
                        },
                    )
                },
            )
        }
    }
}

@Composable
private fun ShutterButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.CameraAlt,
            contentDescription = stringResource(R.string.scan_cd_capture),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp),
        )
    }
}

@Composable
private fun CapturedReview(
    uri: Uri,
    isSubmitting: Boolean,
    errorMessageRes: Int?,
    onRetake: () -> Unit,
    onSubmit: () -> Unit,
) {
    val context = LocalContext.current
    val preview = remember(uri) { context.decodeSampled(uri) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(PerioTheme.spacing.screenMargin),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            if (preview != null) {
                Image(
                    bitmap = preview.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(PerioTheme.shapes.card),
                )
            }
        }

        Spacer(Modifier.height(PerioTheme.spacing.md))

        if (errorMessageRes != null) {
            Text(
                text = stringResource(errorMessageRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = PerioTheme.spacing.sm),
            )
        } else {
            Text(
                text = stringResource(R.string.scan_review_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = PerioTheme.spacing.sm),
            )
        }

        Button(
            onClick = onSubmit,
            enabled = !isSubmitting,
            shape = PerioTheme.shapes.button,
            modifier = Modifier
                .fillMaxWidth()
                .height(PerioTheme.spacing.minTouchTarget),
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp),
                )
            } else {
                Text(stringResource(R.string.scan_submit))
            }
        }

        Spacer(Modifier.height(PerioTheme.spacing.sm))

        OutlinedButton(
            onClick = onRetake,
            enabled = !isSubmitting,
            shape = PerioTheme.shapes.button,
            modifier = Modifier
                .fillMaxWidth()
                .height(PerioTheme.spacing.minTouchTarget),
        ) {
            Text(stringResource(R.string.scan_retake))
        }
    }
}

// --- permission / error / complete states ------------------------------------

@Composable
private fun CameraPermissionRequest(
    showSettings: Boolean,
    onGrant: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    CenteredMessage(
        icon = { IconChip(icon = Icons.Filled.CameraAlt, tint = MaterialTheme.colorScheme.primary) },
        title = stringResource(R.string.scan_permission_title),
        body = stringResource(R.string.scan_permission_body),
    ) {
        Button(
            onClick = onGrant,
            shape = PerioTheme.shapes.button,
            modifier = Modifier
                .fillMaxWidth()
                .height(PerioTheme.spacing.minTouchTarget),
        ) {
            Text(stringResource(R.string.scan_permission_grant))
        }
        if (showSettings) {
            Spacer(Modifier.height(PerioTheme.spacing.sm))
            OutlinedButton(
                onClick = onOpenSettings,
                shape = PerioTheme.shapes.button,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(PerioTheme.spacing.minTouchTarget),
            ) {
                Text(stringResource(R.string.scan_permission_settings))
            }
        }
    }
}

@Composable
private fun CameraErrorState(onRetry: () -> Unit) {
    CenteredMessage(
        icon = { IconChip(icon = Icons.Filled.CameraAlt, tint = MaterialTheme.colorScheme.error) },
        title = stringResource(R.string.scan_camera_error),
        body = null,
    ) {
        Button(
            onClick = onRetry,
            shape = PerioTheme.shapes.button,
            modifier = Modifier
                .fillMaxWidth()
                .height(PerioTheme.spacing.minTouchTarget),
        ) {
            Text(stringResource(R.string.scan_retry))
        }
    }
}

@Composable
private fun ScanCompleteContent(
    summary: ScanSummary?,
    alreadyDone: Boolean,
    onDone: () -> Unit,
) {
    val streak = summary?.streakDays ?: 0

    CenteredMessage(
        icon = {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = PerioTheme.colors.success,
                modifier = Modifier.size(64.dp),
            )
        },
        title = stringResource(
            if (alreadyDone) R.string.scan_already_title else R.string.scan_success_title,
        ),
        body = stringResource(R.string.scan_complete_body),
    ) {
        if (streak > 0) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PerioTheme.spacing.xs),
                modifier = Modifier.padding(bottom = PerioTheme.spacing.md),
            ) {
                Icon(
                    imageVector = Icons.Filled.LocalFireDepartment,
                    contentDescription = null,
                    tint = PerioTheme.colors.warning,
                )
                Text(
                    text = stringResource(R.string.scan_complete_streak, streak),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        } else {
            Text(
                text = stringResource(R.string.scan_complete_first),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = PerioTheme.spacing.md),
            )
        }

        Button(
            onClick = onDone,
            shape = PerioTheme.shapes.button,
            modifier = Modifier
                .fillMaxWidth()
                .height(PerioTheme.spacing.minTouchTarget),
        ) {
            Text(stringResource(R.string.scan_done))
        }
    }
}

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

/** Shared vertical layout for the permission / error / complete panels. */
@Composable
private fun CenteredMessage(
    icon: @Composable () -> Unit,
    title: String,
    body: String?,
    actions: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(PerioTheme.spacing.screenMargin),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        icon()
        Spacer(Modifier.height(PerioTheme.spacing.md))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        if (body != null) {
            Spacer(Modifier.height(PerioTheme.spacing.sm))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(Modifier.height(PerioTheme.spacing.lg))
        actions()
    }
}

// --- helpers -----------------------------------------------------------------

private fun Context.openAppSettings() {
    startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
    )
}

/** Decode the captured JPEG downscaled for preview, off the full-res bitmap that would jank the UI. */
private fun Context.decodeSampled(uri: Uri, reqWidth: Int = 1080): android.graphics.Bitmap? =
    runCatching {
        contentResolver.openInputStream(uri).use { bounds ->
            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(bounds, null, opts)
            var sample = 1
            while (opts.outWidth / sample > reqWidth) sample *= 2
            contentResolver.openInputStream(uri).use { stream ->
                BitmapFactory.decodeStream(
                    stream,
                    null,
                    BitmapFactory.Options().apply { inSampleSize = sample },
                )
            }
        }
    }.getOrNull()
