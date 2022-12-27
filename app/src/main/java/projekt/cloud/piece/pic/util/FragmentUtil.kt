package projekt.cloud.piece.pic.util

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import projekt.cloud.piece.pic.util.ActivityUtil.defaultSharedPreference

object FragmentUtil {

    fun Fragment.setSupportActionBar(toolbar: Toolbar) {
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
    }
    
    val Fragment.defaultSharedPreference: SharedPreferences
        get() = requireActivity().defaultSharedPreference

}