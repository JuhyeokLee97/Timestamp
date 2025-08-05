package com.moimemefutur.timestamp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.moimemefutur.timestamp.databinding.ActivityCameraViewBinding
import com.moimemefutur.timestamp.databinding.TemplateLeftBottomBinding
import com.moimemefutur.timestamp.databinding.TemplateLeftTopBinding
import com.moimemefutur.timestamp.databinding.TemplateRightBottomBinding
import com.moimemefutur.timestamp.databinding.TemplateRightTopBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CameraViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraViewBinding
    private lateinit var templateAdapter: TemplateAdapter

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("CameraViewActivity", "Camera permission granted")
            checkCameraPermission()
        } else {
            Toast.makeText(this, "Camera permission is required", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraViewBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        checkCameraPermission()
        setUpClickListener()
        setUpTemplateRecyclerView()
        setUpTemplates()
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                setUpCameraView()
            }

            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    private fun setUpCameraView() {
        binding.tsCamera.startCamera()
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    private fun setUpClickListener() = with(binding) {
        btnCapture.setOnClickListener {
            capture()
        }
        btnSwitchLens.setOnClickListener {
            switchLens()
        }
    }

    private fun capture() {
        val fileName = "sample_timestamp_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.jpg"
        lifecycleScope.launch {
            binding.tsCamera.captureToGallery(fileName)
        }
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    private fun switchLens() {
        binding.tsCamera.switchCamera()
    }


    private fun setUpTemplateRecyclerView() = with(binding.rvTemplate) {
        templateAdapter = TemplateAdapter { selectedTemplate ->
            selectTemplateById(selectedTemplate.id)
        }
        adapter = templateAdapter
        layoutManager = LinearLayoutManager(this@CameraViewActivity, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun selectTemplateById(templateId: String) {
        binding.tsCamera.selectTemplate(templateId)
        if (::templateAdapter.isInitialized) {
            templateAdapter.setSelectedTemplate(templateId)
        }
    }

    private fun setUpTemplates() {
        with(binding.tsCamera) {
            setTemplates {
                template {
                    id = "template_left_top"
                    name = "TEMPLATE_LEFT_TOP"
                    thumbnailRes = R.drawable.ic_template_left_top
                    configureBinding(
                        inflater = { layoutInflater, parent, attachToParent ->
                            TemplateLeftTopBinding.inflate(layoutInflater, parent, attachToParent)
                        },
                        bind = { binding ->
                            binding.tvTimestamp.text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                        }
                    )
                }
                template {
                    id = "template_left_bottom"
                    name = "TEMPLATE_LEFT_BOTTOM"
                    thumbnailRes = R.drawable.ic_template_left_bottom
                    configureBinding(
                        inflater = { layoutInflater, parent, attachToParent ->
                            TemplateLeftBottomBinding.inflate(layoutInflater, parent, attachToParent)
                        },
                        bind = { binding ->
                            binding.tvTimestamp.text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                        }
                    )
                }

                template {
                    id = "template_right_top"
                    name = "TEMPLATE_RIGHT_TOP"
                    thumbnailRes = R.drawable.ic_template_right_top
                    configureBinding(
                        inflater = { layoutInflater, parent, attachToParent ->
                            TemplateRightTopBinding.inflate(layoutInflater, parent, attachToParent)
                        },
                        bind = { binding ->
                            binding.tvTimestamp.text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                        }
                    )
                }

                template {
                    id = "template_right_bottom"
                    name = "TEMPLATE_RIGHT_BOTTOM"
                    thumbnailRes = R.drawable.ic_template_right_bottom
                    configureBinding(
                        inflater = { layoutInflater, parent, attachToParent ->
                            TemplateRightBottomBinding.inflate(layoutInflater, parent, attachToParent)
                        },
                        bind = { binding ->
                            binding.tvTimestamp.text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                        }
                    )
                }
            }
            val templates = binding.tsCamera.getExternalTemplates()

            if (::templateAdapter.isInitialized) {
                templateAdapter.submitList(templates)
                templateAdapter.setSelectedTemplate(templates.first().id)
            }
        }

    }
}