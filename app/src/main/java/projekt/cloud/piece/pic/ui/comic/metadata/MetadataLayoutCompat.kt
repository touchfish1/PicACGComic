package projekt.cloud.piece.pic.ui.comic.metadata

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.CoroutineScope
import projekt.cloud.piece.pic.base.SnackLayoutCompat
import projekt.cloud.piece.pic.databinding.ChipBinding
import projekt.cloud.piece.pic.databinding.FragmentMetadataBinding
import projekt.cloud.piece.pic.util.CoroutineUtil.ui
import projekt.cloud.piece.pic.util.FragmentUtil.setSupportActionBar
import projekt.cloud.piece.pic.util.LayoutSizeMode
import projekt.cloud.piece.pic.util.LayoutSizeMode.COMPACT
import projekt.cloud.piece.pic.util.LayoutSizeMode.MEDIUM
import projekt.cloud.piece.pic.util.LayoutSizeMode.EXPANDED

abstract class MetadataLayoutCompat(protected val binding: FragmentMetadataBinding): SnackLayoutCompat() {

    companion object MetadataLayoutCompatUtil {
        @JvmStatic
        fun FragmentMetadataBinding.getLayoutCompat(layoutSizeMode: LayoutSizeMode) = when (layoutSizeMode) {
            COMPACT -> MetadataLayoutCompatImpl(this)
            MEDIUM -> MetadataLayoutCompatW600dpImpl(this)
            EXPANDED -> MetadataLayoutCompatW1240dpImpl(this)
        }
    }
    
    init {
        @Suppress("LeakingThis")
        binding.metadataLayoutCompat = this
    }
    
    private val category: ChipGroup
        get() = binding.chipGroupCategory
    
    private val tag: ChipGroup
        get() = binding.chipGroupTag
    
    open fun setupActionBar(fragment: Fragment) = Unit
    
    fun triggerShowOrGone(container: View, materialCheckBox: MaterialCheckBox) {
        container.visibility = when (container.visibility) {
            VISIBLE -> GONE
            else -> VISIBLE
        }
        materialCheckBox.isChecked = container.visibility != GONE
    }
    
    override val snackContainer: View
        get() = binding.root
    
    fun startUpdateCategoryAndTag(coroutineScope: CoroutineScope, context: Context, categoryList: List<String>, tagList: List<String>) {
        val layoutInflater = LayoutInflater.from(context)
        
        coroutineScope.ui {
            val onClick: (String) -> Unit = {
            }
            categoryList.forEach {
                category.addView(
                    createChip(category, layoutInflater, it, onClick)
                )
            }
        }
        coroutineScope.ui {
            val onClick: (String) -> Unit = {
            }
            tagList.forEach {
                tag.addView(createChip(tag, layoutInflater, it, onClick))
            }
        }
    }
    
    private fun createChip(
        container: ViewGroup,
        layoutInflater: LayoutInflater,
        textStr: String,
        onChipClick: (String) -> Unit
    ): View {
        return ChipBinding.inflate(layoutInflater, container, false).apply {
            onClick = onChipClick
            text = textStr
        }.root
    }
    
    private class MetadataLayoutCompatImpl(binding: FragmentMetadataBinding): MetadataLayoutCompat(binding) {
    
        private val toolbar: MaterialToolbar
            get() = binding.materialToolbar!!
        
        override fun setupActionBar(fragment: Fragment) {
            fragment.setSupportActionBar(toolbar)
        }
        
    }
    
    private class MetadataLayoutCompatW600dpImpl(binding: FragmentMetadataBinding): MetadataLayoutCompat(binding) {
        
        private val toolbar: MaterialToolbar
            get() = binding.materialToolbar!!
    
        override fun setupActionBar(fragment: Fragment) {
            fragment.setSupportActionBar(toolbar)
        }
        
    }
    
    private class MetadataLayoutCompatW1240dpImpl(binding: FragmentMetadataBinding): MetadataLayoutCompat(binding) {
    
    }

}