package projekt.cloud.piece.pic.ui.comic.comments

import android.view.View
import android.view.View.GONE
import androidx.core.widget.NestedScrollView
import androidx.core.widget.NestedScrollView.OnScrollChangeListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import projekt.cloud.piece.pic.MainViewModel
import projekt.cloud.piece.pic.api.common.LikeResponseBody
import projekt.cloud.piece.pic.base.BaseRecyclerViewAdapter.BaseRecyclerViewAdapterUtil.adapterInterface
import projekt.cloud.piece.pic.base.SnackLayoutCompat
import projekt.cloud.piece.pic.databinding.FragmentCommentsBinding
import projekt.cloud.piece.pic.util.AdapterInterface
import projekt.cloud.piece.pic.util.CoroutineUtil.default
import projekt.cloud.piece.pic.util.CoroutineUtil.ui
import projekt.cloud.piece.pic.util.FragmentUtil.setSupportActionBar
import projekt.cloud.piece.pic.util.LayoutSizeMode
import projekt.cloud.piece.pic.util.LayoutSizeMode.COMPACT
import projekt.cloud.piece.pic.util.LayoutSizeMode.EXPANDED
import projekt.cloud.piece.pic.util.LayoutSizeMode.MEDIUM
import projekt.cloud.piece.pic.util.ViewUtil.canScrollDown

abstract class CommentsLayoutCompat private constructor(
    protected val binding: FragmentCommentsBinding
): SnackLayoutCompat(), AdapterInterface {
    
    companion object CommentsLayoutCompatUtil {
        @JvmStatic
        fun FragmentCommentsBinding.getLayoutCompat(layoutSizeMode: LayoutSizeMode) = when (layoutSizeMode) {
            COMPACT -> CommentsLayoutCompatImpl(this)
            MEDIUM -> CommentsLayoutCompatW600dpImpl(this)
            EXPANDED -> CommentsLayoutCompatW1240dpImpl(this)
        }
        
        private const val INDEX_NOT_FOUND = -1
    }
    
    override val snackContainer: View
        get() = binding.coordinatorLayout
    
    private val top: RecyclerView
        get() = binding.recyclerViewTop
    private val normal: RecyclerView
        get() = binding.recyclerViewNormal
    
    private val linearProgressIndicator: LinearProgressIndicator
        get() = binding.linearProgressIndicator
    
    open fun setupActionBar(fragment: Fragment) = Unit
    
    open fun setupRecyclerViews(fragment: Fragment, mainViewModel: MainViewModel, commentsViewModel: CommentsViewModel) {
        val onClick: (String) -> Unit = {
            mainViewModel.account.value?.let { account ->
                if (account.isSignedIn) {
                    commentsViewModel.scopedPostLikeComic(account.token, it)
                }
            }
        }
        
        top.adapter = RecyclerViewAdapter(commentsViewModel.topCommentList, fragment, onClick)
        normal.adapter = RecyclerViewAdapter(commentsViewModel.commentList, fragment, onClick)
        
        // Set RecyclerView holder pool
        normal.setRecycledViewPool(top.recycledViewPool)
    }
    
    fun setupLoadingIndicator(fragment: Fragment, commentsViewModel: CommentsViewModel) {
        linearProgressIndicator.setVisibilityAfterHide(GONE)
        commentsViewModel.isUpdating.observe(fragment.viewLifecycleOwner) {
            when {
                it -> linearProgressIndicator.show()
                else -> linearProgressIndicator.hide()
            }
        }
    }
    
    fun updateCommentLike(fragment: Fragment, commentsViewModel: CommentsViewModel, id: String?, responseContent: String?) {
        fragment.lifecycleScope.ui {
            id ?: return@ui
            responseContent ?: return@ui
            val isLiked = withContext(default) {
                Json.decodeFromString<LikeResponseBody>(responseContent)
            }.isLiked
            updateCommentLike(commentsViewModel, id, isLiked)
        }
    }
    
    private suspend fun updateCommentLike(commentsViewModel: CommentsViewModel, id: String, isLiked: Boolean) {
        var index = withContext(default) {
            commentsViewModel.topCommentList.indexOfFirst { it.id == id }
        }
        if (index != INDEX_NOT_FOUND) {
            commentsViewModel.topCommentList[index].let { comment ->
                comment.isLiked = isLiked
                top.adapterInterface.notifyUpdate(index, comment)
            }
        }
        index = withContext(default) {
            commentsViewModel.commentList.indexOfFirst { it.id == id }
        }
        if (index != INDEX_NOT_FOUND) {
            commentsViewModel.commentList[index].let { comment ->
                comment.isLiked = isLiked
                normal.adapterInterface.notifyUpdate(index, comment)
            }
        }
    }
    
    override fun notifyClear() = Unit
    
    override fun notifyUpdate() {
        top.adapterInterface.notifyUpdate()
        normal.adapterInterface.notifyUpdate()
    }
    
    private class CommentsLayoutCompatImpl(binding: FragmentCommentsBinding): CommentsLayoutCompat(binding) {
    
        private val toolbar: MaterialToolbar
            get() = binding.materialToolbar!!
        
        private val nestedScrollView: NestedScrollView
            get() = binding.nestedScrollView
        
        override fun setupActionBar(fragment: Fragment) {
            fragment.setSupportActionBar(toolbar)
        }
    
        override fun setupRecyclerViews(fragment: Fragment, mainViewModel: MainViewModel, commentsViewModel: CommentsViewModel) {
            super.setupRecyclerViews(fragment, mainViewModel, commentsViewModel)
            nestedScrollView.setOnScrollChangeListener(OnScrollChangeListener { nestedScrollView, _, _, _, _ ->
                if (!nestedScrollView.canScrollDown && commentsViewModel.hasMorePage) {
                    mainViewModel.account.value?.let { account ->
                        if (account.isSignedIn) {
                            commentsViewModel.scopedObtainCommentsNewPage(account.token, fragment.lifecycleScope)
                        }
                    }
                }
            })
        }
        
    }
    
    private class CommentsLayoutCompatW600dpImpl(binding: FragmentCommentsBinding): CommentsLayoutCompat(binding)
    
    private class CommentsLayoutCompatW1240dpImpl(binding: FragmentCommentsBinding): CommentsLayoutCompat(binding)

}