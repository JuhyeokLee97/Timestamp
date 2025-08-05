package com.moimemefutur.timestamp.template

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding

enum class TemplateType {
    LAYOUT_RESOURCE,
    VIEW_BINDING
}

data class TemplateInfo(
    val id: String,
    val name: String,
    val description: String = "",
    val thumbnailUrl: String? = null,
    val thumbnailRes: Int? = null,
    @LayoutRes val layoutRes: Int? = null,
    val configureView: ((View) -> Unit)? = null,
    val configureBinding: ((ViewBinding) -> Unit)? = null,
    val bindingInflater: ((LayoutInflater, ViewGroup, Boolean) -> ViewBinding)? = null
) {
    val type: TemplateType
        get() = when {
            bindingInflater != null && configureBinding != null -> TemplateType.VIEW_BINDING
            layoutRes != null -> TemplateType.LAYOUT_RESOURCE
            else -> throw IllegalStateException("Template must have either layoutRes or bindingInflater with configureBinding")
        }
}

class TemplateConfig {
    var id: String = ""
    var name: String = ""
    var description: String = ""
    var thumbnailUrl: String? = null
    var thumbnailRes: Int? = null
    @LayoutRes var layoutRes: Int? = null
    private var configureView: ((View) -> Unit)? = null
    private var configureBinding: ((ViewBinding) -> Unit)? = null
    private var bindingInflater: ((LayoutInflater, ViewGroup, Boolean) -> ViewBinding)? = null

    fun configure(block: (View) -> Unit) {
        configureView = block
    }

    fun <T : ViewBinding> configureBinding(
        inflater: (LayoutInflater, ViewGroup, Boolean) -> T,
        bind: (T) -> Unit
    ) {
        bindingInflater = inflater
        configureBinding = { binding ->
            @Suppress("UNCHECKED_CAST")
            bind(binding as T)
        }
    }

    internal fun build(): TemplateInfo {
        require(id.isNotEmpty()) { "Template id is required" }
        require(name.isNotEmpty()) { "Template name is required" }
        require(layoutRes != null || bindingInflater != null) { "Template layoutRes or bindingInflater is required" }

        return TemplateInfo(
            id = id,
            name = name,
            description = description,
            thumbnailUrl = thumbnailUrl,
            thumbnailRes = thumbnailRes,
            layoutRes = layoutRes,
            configureView = configureView,
            configureBinding = configureBinding,
            bindingInflater = bindingInflater
        )
    }
}

class TemplateCollection {
    private val templates = mutableListOf<TemplateInfo>()

    fun template(block: TemplateConfig.() -> Unit) {
        val config = TemplateConfig()
        config.block()
        templates.add(config.build())
    }

    fun build(): List<TemplateInfo> = templates.toList()
}