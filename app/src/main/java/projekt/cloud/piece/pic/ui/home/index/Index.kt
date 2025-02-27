package projekt.cloud.piece.pic.ui.home.index

import android.util.Log
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import projekt.cloud.piece.pic.MainViewModel
import projekt.cloud.piece.pic.R
import projekt.cloud.piece.pic.base.BaseCallbackFragment
import projekt.cloud.piece.pic.databinding.FragmentIndexBinding
import projekt.cloud.piece.pic.ui.home.Home
import projekt.cloud.piece.pic.ui.home.index.IndexLayoutCompat.IndexLayoutCompatUtil.getLayoutCompat
import projekt.cloud.piece.pic.ui.home.index.IndexViewModel.IndexViewModelConstants.INDEX_COMPLETE
import projekt.cloud.piece.pic.ui.home.index.IndexViewModel.IndexViewModelConstants.INDEX_EMPTY_CONTENT
import projekt.cloud.piece.pic.ui.home.index.IndexViewModel.IndexViewModelConstants.INDEX_ERROR
import projekt.cloud.piece.pic.ui.home.index.IndexViewModel.IndexViewModelConstants.INDEX_EXCEPTION
import projekt.cloud.piece.pic.ui.home.index.IndexViewModel.IndexViewModelConstants.INDEX_INVALID_STATE_CODE
import projekt.cloud.piece.pic.ui.home.index.IndexViewModel.IndexViewModelConstants.INDEX_IO_EXCEPTION
import projekt.cloud.piece.pic.ui.home.index.IndexViewModel.IndexViewModelConstants.INDEX_REJECTED
import projekt.cloud.piece.pic.util.FragmentUtil.findParentAs
import projekt.cloud.piece.pic.util.LayoutSizeMode

class Index: BaseCallbackFragment<FragmentIndexBinding, IndexViewModel>() {
    
    companion object {
        private const val TAG = "Index"
    }

    private val home: Home
        get() = findParentAs()
    
    private lateinit var layoutCompat: IndexLayoutCompat
    
    private val mainViewModel: MainViewModel by activityViewModels()
    
    override fun onSetupLayoutCompat(binding: FragmentIndexBinding, layoutSizeMode: LayoutSizeMode) {
        layoutCompat = binding.getLayoutCompat(layoutSizeMode)
        layoutCompat.setNavController(home.findNavController())
    }
    
    override fun onBindData(binding: FragmentIndexBinding) {
        binding.viewModel = viewModel
        binding.mainViewModel = mainViewModel
    }

    override fun onSetupActionBar(binding: FragmentIndexBinding) {
        layoutCompat.setupActionBar(this)
    }
    
    override fun onSetupView(binding: FragmentIndexBinding) {
        layoutCompat.setupCollections(resources, viewModel.comicListA, viewModel.comicListB, this)
        layoutCompat.setupRandom(resources, viewModel.comicListRandom, this)
        mainViewModel.account.observe(viewLifecycleOwner) {
            Log.i(TAG, "account: $it ${it.isSignedIn}")
            when {
                it.isSignedIn -> obtainCollection(it.token)
                else -> mainViewModel.performSignIn(requireActivity())
            }
        }
    }
    
    private fun obtainCollection(token: String) {
        if (!viewModel.isCollectionsObtainComplete) {
            viewModel.scopedObtainComics(lifecycleScope, token)
        }
    }
    
    override fun onCallbackReceived(code: Int, message: String?, responseCode: Int?, errorCode: Int?, responseDetail: String?) {
        Log.i(TAG, "onCallbackReceived: code=$code message=$message responseCode=$responseCode errorCode=$errorCode responseDetail=$responseDetail")
        when (code) {
            INDEX_COMPLETE -> {
                // Complete
                layoutCompat.notifyUpdate()
                layoutCompat.completeLoading()
            }
            INDEX_IO_EXCEPTION -> {
                layoutCompat.indefiniteSnack(getString(R.string.request_io_exception, message))
            }
            INDEX_EXCEPTION -> {
                layoutCompat.indefiniteSnack(getString(R.string.request_unknown_exception, message))
            }
            INDEX_ERROR -> {
                layoutCompat.indefiniteSnack(getString(R.string.response_error, responseCode, code, message, responseDetail))
            }
            INDEX_EMPTY_CONTENT -> {
                layoutCompat.indefiniteSnack(getString(R.string.response_empty))
            }
            INDEX_REJECTED -> {
                layoutCompat.indefiniteSnack(getString(R.string.response_rejected))
            }
            INDEX_INVALID_STATE_CODE -> {
                layoutCompat.indefiniteSnack(getString(R.string.request_unexpected_state, message))
            }
        }
    }

}