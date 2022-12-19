package projekt.cloud.piece.pic.ui.list

import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.view.MenuProvider
import androidx.core.view.doOnPreDraw
import androidx.databinding.ObservableArrayMap
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import androidx.recyclerview.widget.RecyclerView.State
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.platform.Hold
import com.google.android.material.transition.platform.MaterialContainerTransform
import projekt.cloud.piece.pic.ComicDetail
import projekt.cloud.piece.pic.Comics
import projekt.cloud.piece.pic.R
import projekt.cloud.piece.pic.api.ApiComics.ComicsResponseBody.Data.Comics.Doc
import projekt.cloud.piece.pic.api.CommonParam.ComicsSort
import projekt.cloud.piece.pic.api.CommonParam.ComicsSort.MORE_FAVOURITE
import projekt.cloud.piece.pic.api.CommonParam.ComicsSort.MORE_HEART
import projekt.cloud.piece.pic.api.CommonParam.ComicsSort.NEW_TO_OLD
import projekt.cloud.piece.pic.api.CommonParam.ComicsSort.OLD_TO_NEW
import projekt.cloud.piece.pic.base.BaseFragment
import projekt.cloud.piece.pic.databinding.FragmentListBinding
import projekt.cloud.piece.pic.util.CodeBook.AUTH_CODE_ERROR_ACCOUNT_INVALID
import projekt.cloud.piece.pic.util.CodeBook.AUTH_CODE_ERROR_CONNECTION
import projekt.cloud.piece.pic.util.CodeBook.AUTH_CODE_ERROR_NO_ACCOUNT
import projekt.cloud.piece.pic.util.CodeBook.AUTH_CODE_SUCCESS
import projekt.cloud.piece.pic.util.CodeBook.LIST_CODE_ERROR_CONNECTION
import projekt.cloud.piece.pic.util.CodeBook.LIST_CODE_ERROR_REJECTED
import projekt.cloud.piece.pic.util.CodeBook.LIST_CODE_PART_SUCCESS
import projekt.cloud.piece.pic.util.CodeBook.LIST_CODE_SUCCESS
import projekt.cloud.piece.pic.util.FragmentUtil.addMenuProvider
import projekt.cloud.piece.pic.util.FragmentUtil.setDisplayHomeAsUpEnabled
import projekt.cloud.piece.pic.util.FragmentUtil.setSupportActionBar
import projekt.cloud.piece.pic.util.RecyclerViewUtil.adapterAs
import projekt.cloud.piece.pic.util.StorageUtil.Account

class ListFragment: BaseFragment<FragmentListBinding>() {

    companion object {
        private const val GRID_SPAN = 2
    }
    
    private val toolbar: MaterialToolbar
        get() = binding.materialToolbar
    private val recyclerView: RecyclerView
        get() = binding.recyclerView

    private val comics: Comics by activityViewModels()
    private val comicDetail: ComicDetail by activityViewModels()

    private val comicList: ArrayList<Doc>
        get() = comics.comicList
    private val coverImages: ObservableArrayMap<String, Bitmap?>
        get() = comics.coverImages

    private var requireCaching = true

    private lateinit var navController: NavController
    
    private var snackbar: Snackbar? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navController = findNavController()
        sharedElementEnterTransition = MaterialContainerTransform()
        exitTransition = Hold()
    }
    
    override val containerTransitionName: String?
        get() = args.getString(getString(R.string.list_transition))
    
    override fun setViewModels(binding: FragmentListBinding) {
        binding.comics = comics
    }
    
    override fun setUpActionBar() {
        setSupportActionBar(toolbar)
        setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { navController.navigateUp() }
        addMenuProvider(object: MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.clear()
                menuInflater.inflate(R.menu.menu_sorting, menu)
                if (menu is MenuBuilder) {
                    @Suppress("RestrictedApi")
                    menu.setOptionalIconsVisible(true)
                }
                when (comics.sort) {
                    NEW_TO_OLD -> {
                        menu.findItem(R.id.menu_sort_new).isChecked = true
                    }
                    OLD_TO_NEW -> {
                        menu.findItem(R.id.menu_sort_old).isChecked = true
                    }
                    MORE_HEART -> {
                        menu.findItem(R.id.menu_sort_heart).isChecked = true
                    }
                    MORE_FAVOURITE -> {
                        menu.findItem(R.id.menu_sort_fav).isChecked = true
                    }
                }
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.menu_sort_new -> {
                        menuItem.isChecked = true
                        updateComicSort(NEW_TO_OLD)
                    }
                    R.id.menu_sort_old -> {
                        menuItem.isChecked = true
                        updateComicSort(OLD_TO_NEW)
                    }
                    R.id.menu_sort_heart -> {
                        menuItem.isChecked = true
                        updateComicSort(MORE_HEART)
                    }
                    R.id.menu_sort_fav -> {
                        menuItem.isChecked = true
                        updateComicSort(MORE_FAVOURITE)
                    }
                }
                return true
            }
            
            private fun updateComicSort(newSort: ComicsSort) {
                if (comics.sort != newSort) {
                    recyclerView.adapterAs<RecyclerViewAdapter>()
                        .notifyListReset()
                    comics.changeCategorySort(token, newSort)
                }
            }
            
        })
    }
    
    override fun setUpViews() {
        postponeEnterTransition()
        val recyclerViewAdapter = RecyclerViewAdapter(comicList, coverImages) { view, doc ->
            if (isAuthSuccess) {
                requireCaching = false
                comicDetail.setCover(coverImages[doc._id])
                comicDetail.requestComic(token, doc._id)
                navController.navigate(
                    ListFragmentDirections.actionListToComicDetail(doc._id, view.transitionName),
                    FragmentNavigatorExtras(view to view.transitionName)
                )
            }
        }
        recyclerView.adapter = recyclerViewAdapter
        recyclerView.layoutManager = StaggeredGridLayoutManager(GRID_SPAN, VERTICAL)
        recyclerView.doOnPreDraw { startPostponedEnterTransition() }
    
        val spacingInnerHor = resources.getDimensionPixelSize(R.dimen.md_spec_spacing_hor_8)
        val spacingOuterVer = resources.getDimensionPixelSize(R.dimen.md_spec_spacing_ver_8)
        var bottomInset = 0
        applicationConfigs.windowInsetBottom.value?.let {
            bottomInset = it
        }
    
        recyclerView.addItemDecoration(
            object : ItemDecoration() {
                override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: State) {
                    super.getItemOffsets(outRect, view, parent, state)
                    outRect.right = spacingInnerHor
                    val pos = parent.getChildAdapterPosition(view)
                    val itemCount = recyclerViewAdapter.itemCount
                    outRect.bottom = when {
                        itemCount % GRID_SPAN == 0 && pos >= itemCount - GRID_SPAN -> bottomInset
                        pos == itemCount - 1 -> bottomInset
                        else -> spacingOuterVer
                    }
                }
            }
        )
        
        recyclerView.addOnScrollListener(object: OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(1)) {
                    if (isAuthSuccess) {
                        comics.requestCategoryPage(token)
                    }
                }
            }
        })
        
        comics.taskReceipt.observe(viewLifecycleOwner) {
            snackbar?.dismiss()
            snackbar = null
            it?.let {
                when (it.code) {
                    LIST_CODE_SUCCESS -> { /** List content load success **/ }
                    LIST_CODE_PART_SUCCESS -> {
                        recyclerView.adapterAs<RecyclerViewAdapter>().notifyListUpdate()
                        recyclerView.invalidateItemDecorations()
                    }
                    LIST_CODE_ERROR_CONNECTION -> {
                        snackbar = sendSnack(getString(R.string.list_snack_login_connection_failed, it.message), resId = R.string.list_snack_request_action_retry) {
                            val category = comics.key
                            comics.clear()
                            comics.requestCategory(token, category)
                        }
                    }
                    LIST_CODE_ERROR_REJECTED -> {
                        snackbar = sendSnack(getString(R.string.list_snack_request_server_rejected, it.message), resId = R.string.list_snack_request_action_retry) {
                            val category = comics.key
                            comics.clear()
                            comics.requestCategory(token, category)
                        }
                    }
                    else -> {
                        snackbar = sendSnack(getString(R.string.list_snack_request_unknown_code, it.code, it.message), resId = R.string.list_snack_request_action_retry) {
                            val category = comics.key
                            comics.clear()
                            comics.requestCategory(token, category)
                        }
                    }
                }
            }
        }
    }
    
    override fun onAuthComplete(code: Int, codeMessage: String?, account: Account?) {
        super.onAuthComplete(code, codeMessage, account)
        val token = account?.token
        if (code != AUTH_CODE_SUCCESS || token == null) {
            when (code) {
                AUTH_CODE_ERROR_NO_ACCOUNT -> {
                    sendSnack(getString(R.string.list_snack_login_no_account))
                }
                AUTH_CODE_ERROR_ACCOUNT_INVALID -> {
                    sendSnack(getString(R.string.list_snack_login_invalid_account), resId = R.string.home_snack_action_retry) {
                        applicationConfigs.account.value?.let { requireAuth(it) }
                    }
                }
                AUTH_CODE_ERROR_CONNECTION -> {
                    sendSnack(getString(R.string.list_snack_login_connection_failed, codeMessage), resId = R.string.home_snack_action_retry) {
                        applicationConfigs.account.value?.let { requireAuth(it) }
                    }
                }
            }
            return
        }
    }

    override fun onDestroyView() {
        if (requireCaching) {
            comics.clear(viewLifecycleOwner)
        }
        super.onDestroyView()
    }
    
}