package projekt.cloud.piece.pic.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.search.SearchBar
import com.google.android.material.search.SearchView
import com.google.android.material.transition.platform.MaterialContainerTransform
import projekt.cloud.piece.pic.R
import projekt.cloud.piece.pic.base.BaseFragment
import projekt.cloud.piece.pic.databinding.FragmentSearchBinding
import projekt.cloud.piece.pic.util.FragmentUtil.setSupportActionBar

class SearchFragment: BaseFragment<FragmentSearchBinding>() {

    private lateinit var navController: NavController
    
    private val searchBar: SearchBar
        get() = binding.searchBar
    private val searchView: SearchView
        get() = binding.searchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navController = findNavController()
        sharedElementEnterTransition = MaterialContainerTransform()
    }
    
    override fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentSearchBinding.inflate(inflater, container, false)
    
    override fun setViewModels(binding: FragmentSearchBinding) {
        binding.applicationConfigs = applicationConfigs
        binding.lifecycleOwner = viewLifecycleOwner
    }
    
    override fun setUpToolbar() {
        setSupportActionBar(searchBar)
        searchBar.setNavigationIcon(R.drawable.ic_round_arrow_back_24)
        searchBar.setNavigationOnClickListener {
            navController.navigateUp()
        }
    }
    
    override fun setUpViews() {
    }
    
    override fun onBackPressed() = when {
        searchView.isShowing -> {
            searchView.hide()
            false
        }
        else -> super.onBackPressed()
    }

}