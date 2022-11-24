package projekt.cloud.piece.pic.ui.list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import projekt.cloud.piece.pic.api.ApiComics.ComicsResponseBody.Data.Comics.Doc
import projekt.cloud.piece.pic.api.CommonBody.bitmap
import projekt.cloud.piece.pic.databinding.LayoutRecyclerListBinding
import projekt.cloud.piece.pic.util.CoroutineUtil.io
import projekt.cloud.piece.pic.util.CoroutineUtil.ui

class RecyclerViewAdapter(private val onClick: (Doc) -> Unit):
    RecyclerView.Adapter<RecyclerViewAdapter.RecyclerViewHolder>() {

    inner class RecyclerViewHolder(private val binding: LayoutRecyclerListBinding):
        RecyclerView.ViewHolder(binding.root), OnClickListener {
        
        constructor(view: View): this(view.context)
        constructor(context: Context): this(LayoutInflater.from(context))
        constructor(inflater: LayoutInflater): this(LayoutRecyclerListBinding.inflate(inflater))
        
        private var job: Job? = null
        
        init {
            binding.root.setOnClickListener(this)
        }
        
        fun setDoc(doc: Doc) {
            binding.doc = doc
            job?.cancel()
            job = ui {
                binding.bitmap = withContext(io) {
                    doc.thumb.bitmap
                }
                job = null
            }
        }
    
        override fun onClick(v: View?) {
            binding.doc?.let(onClick)
        }
        
    }
    
    private var docSize = 0
    var docs: List<Doc>? = null
        set(value) {
            field = value
            docSize = field?.size ?: 0
            @Suppress("NotifyDataSetChanged")
            notifyDataSetChanged()
        }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        RecyclerViewHolder(parent)
    
    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        docs?.get(position)?.let {
            holder.setDoc(it)
        }
    }
    
    override fun getItemCount() = docSize
    
}