package com.moimemefutur.timestamp

import android.Manifest
import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.annotation.RequiresPermission
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.UseCase
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import com.moimemefutur.timestamp.databinding.DefaultTemplateBinding
import com.moimemefutur.timestamp.template.TemplateCollection
import com.moimemefutur.timestamp.template.TemplateInfo
import com.moimemefutur.timestamp.template.TemplateType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class TimestampCameraView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private lateinit var previewView: PreviewView
    private lateinit var defaultTemplateBinding: DefaultTemplateBinding

    private var currentTemplateId: String? = null
    private var currentTemplateBinding: ViewBinding? = null
    private val externalTemplates = mutableListOf<TemplateInfo>()

    private val viewScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val lifecycleOwner: LifecycleOwner?
        get() = when(val ctx = context) {
            is LifecycleOwner -> ctx
            else -> null
        }

    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    var debugMode: Boolean = false

    init {
        setUpView()
    }

    private fun setUpView() {
        previewView = PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
        defaultTemplateBinding = DefaultTemplateBinding.inflate(LayoutInflater.from(context), this, false)

        addView(previewView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        addView(defaultTemplateBinding.root, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    }

    @RequiresPermission(android.Manifest.permission.CAMERA)
    fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases()
            } catch(e: Exception) {
                log("카메라 초기화 실패: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun bindCameraUseCases() {
        val cameraProvider = cameraProvider ?: return
        val preview = createPreview()
        imageCapture = createImageCapture()
        bindCameraToLifecycle(cameraProvider, preview, imageCapture)
    }

    private fun createPreview(): Preview {
        return Preview.Builder()
            .build()
            .also {
                it.surfaceProvider = previewView.surfaceProvider
            }
    }

    private fun createImageCapture(): ImageCapture {
        return ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    private fun bindCameraToLifecycle(
        provider: ProcessCameraProvider,
        vararg useCases: UseCase?
    ) {
        val owner = lifecycleOwner ?: run {
            log("Context is not a LifecycleOwner: ${context.javaClass.simpleName}")
            return
        }
        try {
            provider.unbindAll()
            camera = provider.bindToLifecycle(
                owner,
                cameraSelector,
                *useCases
            )
        } catch (e: Exception) {
            log("카메라 바인딩 실패: ${e.message}")
        }
    }

    fun getCurrentTemplateId(): String? {
        return currentTemplateId
    }

    fun getExternalTemplates(): List<TemplateInfo> {
        return externalTemplates.toList()
    }

    fun clearCurrentTemplate() {
        currentTemplateBinding?.root?.let { templateView -> removeView(templateView) }
        currentTemplateId = null
        currentTemplateBinding = null
        showDefaultTemplate()
    }

    fun setTemplates(block: TemplateCollection.() -> Unit) {
        val collection = TemplateCollection()
        collection.block()
        val templates = collection.build()

        externalTemplates.clear()
        externalTemplates.addAll(templates)
    }

    fun selectTemplate(templateId: String) {
        val template = externalTemplates.find { it.id == templateId }
        if (template != null) {
            when (template.type) {
                TemplateType.VIEW_BINDING -> {
                     setTemplateWithBinding(template)
                }
                
                TemplateType.LAYOUT_RESOURCE -> {
                     setTemplate(template.layoutRes!!) { view ->
                         template.configureView?.invoke(view)
                     }
                }
            }
        } else {
            log("Template not found: $templateId")
        }
    }

    private fun setTemplateWithBinding(template: TemplateInfo) {
        if (template.type != TemplateType.VIEW_BINDING) return

        clearCurrentTemplate()
        hideDefaultTemplate()

        val bindingInflater = template.bindingInflater!!
        val configureBinding = template.configureBinding!!

        val binding = bindingInflater(LayoutInflater.from(context), this, false)
        configureBinding(binding)

        addView(binding.root)

        currentTemplateBinding = binding
        currentTemplateId = template.id
    }

    private fun setTemplate(template: TemplateInfo) {
        if (template.type != TemplateType.LAYOUT_RESOURCE) return

        clearCurrentTemplate()
        hideDefaultTemplate()

        val templateView = LayoutInflater.from(context).inflate(template.layoutRes!!, this, false)
        template.configureView?.let { it(templateView) }
        addView(templateView)

        currentTemplateBinding = null
        currentTemplateId = template.id

    }

    private fun setTemplate(@LayoutRes layoutRes: Int, config: (View) -> Unit = {}) {
        clearCurrentTemplate()
        hideDefaultTemplate()

        val templateView = LayoutInflater.from(context).inflate(layoutRes, this, false)
        config(templateView)

        addView(templateView)

        currentTemplateBinding = null
        currentTemplateId = null
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    fun switchCamera() {
        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
        startCamera()
    }

    private fun showDefaultTemplate() {
        defaultTemplateBinding.root.visibility = VISIBLE
    }

    private fun hideDefaultTemplate() {
        defaultTemplateBinding.root.visibility = GONE
    }

    private fun log(message: String) {
        if (debugMode) {
            Log.d(TAG, message)
        }
    }

    suspend fun captureToGallery(fileName: String): CaptureResult {
        return try {
            val filePath = captureToFile(fileName)
            val uri = MediaStoreRepository().saveToGallery(context, filePath)

            if (uri != null) {
                CaptureResult.Success(uri, filePath)
            } else {
                CaptureResult.Failure(CaptureError.STORAGE_ERROR, "Failed to save to gallery")
            }
        } catch (e: SecurityException) {
            CaptureResult.Failure(CaptureError.PERMISSION_DENIED, e.message ?: "Permission denied")
        } catch (e: IOException) {
            CaptureResult.Failure(CaptureError.STORAGE_ERROR, e.message ?: "Storage error")
        } catch (e: Exception) {
            log("Capture to gallery failed: ${e.message}")
            CaptureResult.Failure(CaptureError.COMPOSITE_FAILED, e.message ?: "Unknown error")
        }
    }

    private suspend fun captureToFile(fileName: String): String {
        return suspendCancellableCoroutine { continuation ->
            val imageCapture = imageCapture ?: run {
                continuation.resumeWithException(Exception("Image capture not initialized"))
                return@suspendCancellableCoroutine
            }

            val imageFile = File(
                context.getExternalFilesDir(null),
                fileName
            )

            val outputFileOptions = ImageCapture.OutputFileOptions.Builder(imageFile).build()

            imageCapture.takePicture(
                outputFileOptions,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        viewScope.launch {
                            try {
                                val resultPath = ImageCompositor().composite(this@TimestampCameraView, imageFile)
                                continuation.resume(resultPath)
                            } catch (e: Exception) {
                                continuation.resumeWithException(e)
                            }
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        continuation.resumeWithException(exception)
                    }
                }
            )
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        clearCurrentTemplate()
        viewScope.cancel("View detached")
        cameraProvider?.unbindAll()
    }

    sealed class CaptureResult {
        data class Success(val uri: Uri, val filePath: String): CaptureResult()
        data class Failure(val error: CaptureError, val message: String): CaptureResult()
    }

    enum class CaptureError {
        CAMERA_NOT_INITIALIZED,
        PERMISSION_DENIED,
        STORAGE_ERROR,
        COMPOSITE_FAILED,
        LIFECYCLE_ERROR
    }

    companion object {
        private const val TAG = "TimestampCameraView"
    }
}