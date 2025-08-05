# TimestampCameraView

[![](https://jitpack.io/v/JuhyeokLee97/Timestamp.svg)](https://jitpack.io/#JuhyeokLee97/Timestamp)
[![API](https://img.shields.io/badge/API-29%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=29)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A customizable camera view library with timestamp overlay templates for Android applications. Easily add professional timestamp overlays to your camera with flexible positioning and styling options.

## ✨ Features

- 📸 **Easy Integration**: Simple drop-in camera view powered by CameraX
- 🎨 **Flexible Templates**: Customizable timestamp templates with multiple positioning options  
- 🖼️ **ViewBinding Support**: Modern Android development with ViewBinding integration
- 📱 **Layout Resources**: Traditional XML layout support for custom designs
- 💾 **Auto Gallery Save**: Automatic image composition and gallery integration
- 🔄 **Camera Controls**: Built-in camera switching and lifecycle management
- 🎯 **Template Positions**: Pre-built templates for all corners (top-left, top-right, bottom-left, bottom-right)

## 🚀 Installation

### Step 1: Add JitPack repository

Add it in your root `build.gradle` at the end of repositories:

```gradle
allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

Or in `settings.gradle` (for newer Android projects):

```gradle
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

### Step 2: Add the dependency

```gradle
dependencies {
    implementation 'com.github.JuhyeokLee97:Timestamp:v1.0.0'
}
```

## 📱 Quick Start

### Basic Usage

#### 1. Add to your layout

```xml
<com.moimemefutur.timestamp.TimestampCameraView
    android:id="@+id/timestampCamera"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

#### 2. Initialize in your Activity

```kotlin
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Start camera (requires CAMERA permission)
        binding.timestampCamera.startCamera()
        
        // Capture photo to gallery
        lifecycleScope.launch {
            val result = binding.timestampCamera.captureToGallery("photo_${System.currentTimeMillis()}.jpg")
            when (result) {
                is TimestampCameraView.CaptureResult.Success -> {
                    Toast.makeText(this@MainActivity, "Saved to gallery!", Toast.LENGTH_SHORT).show()
                }
                is TimestampCameraView.CaptureResult.Failure -> {
                    Toast.makeText(this@MainActivity, "Failed: ${result.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        // Switch between front/back camera
        binding.timestampCamera.switchCamera()
    }
}
```

#### 3. Add permissions to AndroidManifest.xml

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

## 🎨 Custom Templates

### Using Pre-built Templates

```kotlin
binding.timestampCamera.setTemplates {
    template {
        id = "left_top"
        name = "Top Left Template"
        thumbnailRes = R.drawable.template_thumbnail
        configureBinding(
            inflater = { layoutInflater, parent, attachToParent ->
                TemplateLeftTopBinding.inflate(layoutInflater, parent, attachToParent)
            },
            bind = { binding ->
                binding.tvTimestamp.text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                binding.tvCompany.text = "Your Company"
            }
        )
    }
}

// Select template
binding.timestampCamera.selectTemplate("left_top")
```

### Using Layout Resources

```kotlin
binding.timestampCamera.setTemplates {
    template {
        id = "custom_template"
        name = "Custom Template"
        layoutRes = R.layout.my_custom_template
        configure { view ->
            view.findViewById<TextView>(R.id.tvTimestamp).text = getCurrentTimestamp()
        }
    }
}
```

### Create Your Own Templates

You can create templates for any position or design you want! The sample app demonstrates 4 common positions, but you're free to customize:

- Custom positioning and styling
- Multiple text elements  
- Company logos and branding
- Dynamic content updates
- Any layout design you need

## 📖 Documentation

- [Sample App](sample/) - Complete implementation example

## 🔧 Requirements

- **Minimum API Level**: 29 (Android 10)
- **Target SDK**: Latest
- **Dependencies**: CameraX, ViewBinding
- **Permissions**: CAMERA, WRITE_EXTERNAL_STORAGE

## 📸 Sample App

The repository includes a comprehensive sample app demonstrating:

- ✅ Camera permission handling
- ✅ Multiple template positions
- ✅ Template selection UI with RecyclerView
- ✅ Gallery saving with error handling
- ✅ Camera switching functionality
- ✅ Real-time timestamp updates

Run the sample:

```bash
git clone https://github.com/JuhyeokLee97/Timestamp.git
cd Timestamp
./gradlew sample:installDebug
```

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

```
MIT License

Copyright (c) 2024 JuhyeokLee97

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## 👨‍💻 Author

**JuhyeokLee97**

- GitHub: [@JuhyeokLee97](https://github.com/JuhyeokLee97)

## 🌟 Support

If this library helped you, please ⭐ star the repository!

For issues and feature requests, please use [GitHub Issues](https://github.com/JuhyeokLee97/Timestamp/issues).

---

<p align="center">Made with ❤️ for the Android community</p>
