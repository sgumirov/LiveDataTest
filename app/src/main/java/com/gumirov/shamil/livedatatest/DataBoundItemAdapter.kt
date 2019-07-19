package com.gumirov.shamil.livedatatest

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.OnRebindCallback
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.*
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gumirov.shamil.livedatatest.databinding.ItemBinding

class TestDataBoundItemAdaptor: DataBoundItemAdapter<ItemWithId, TestDataBoundViewHolder>(IdItemDiffCallback()) {

    override fun createDataBoundViewHolder(binding: ViewDataBinding) =
        TestDataBoundViewHolder(binding)

    override fun createBinding(parent: ViewGroup, viewType: Int): ItemBinding {
        val binding = DataBindingUtil.inflate<ItemBinding>(LayoutInflater.from(parent.context), R.layout.item, parent, false)
        return binding
    }

    override fun bind(viewHolder: TestDataBoundViewHolder, item: ItemWithId) =
        viewHolder.bind(item)
}

/**
 * Contains rebindCallback which is important for updating items that was changed due to updates in
 * viewModels. Effectively it's creates a feedback loop VM -> RecyclerView.
 */
abstract class DataBoundItemAdapter<T: ItemWithId, DBVH : DataBoundViewHolder>(diffCallback: DiffUtil.ItemCallback<T>):
    ListAdapter<T, DBVH>(AsyncDifferConfig.Builder<T>(diffCallback).build()) {

    //needed for rebindCallback
    private var recyclerView: RecyclerView? = null

    abstract fun createBinding(parent: ViewGroup, viewType: Int): ViewDataBinding
    abstract fun bind(viewHolder: DBVH, item: T)
    abstract fun createDataBoundViewHolder(binding: ViewDataBinding): DBVH

    private fun log(m: String) = Log.i("ZZZ_", m)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DBVH {
        val binding = createBinding(parent, viewType)
        val viewHolder = createDataBoundViewHolder(binding)
        binding.addOnRebindCallback(rebindCallback)
        binding.lifecycleOwner = viewHolder
        log("create holder="+viewHolder)
        return viewHolder
    }

    override fun onBindViewHolder(holder: DBVH, position: Int) {
        bind(holder, getItem(position))
        log("bind pos=$position holder[id="+getItem(position).id+"]="+holder)
    }
    override fun onViewAttachedToWindow(holder: DBVH) {
        super.onViewAttachedToWindow(holder)
        holder.markAttach()
        log("attached holder="+holder)
    }
    override fun onViewDetachedFromWindow(holder: DBVH) {
        super.onViewDetachedFromWindow(holder)
        holder.markDetach()
        log("detached holder="+holder)
    }
    override fun onViewRecycled(holder: DBVH) {
        super.onViewRecycled(holder)
        log("recycled holder="+holder)
    }

    // recyclerview-acquiring machinery below:
    private val rebindCallback: OnRebindCallback<ViewDataBinding> = object: OnRebindCallback<ViewDataBinding>() {
        override fun onPreBind(binding: ViewDataBinding): Boolean {
            return recyclerView?.let { recyclerView ->
                val childAdapterPosition = recyclerView.getChildAdapterPosition(binding.root)
                (recyclerView.isComputingLayout || childAdapterPosition == RecyclerView.NO_POSITION).also {
                    if (!it) {
                        notifyItemChanged(childAdapterPosition, DB_PAYLOAD)
                    }
                }
            } ?: true
        }

    }
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        //this.recyclerView = recyclerView
    }
    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = null
    }

    companion object {
        private val DB_PAYLOAD = Any()
    }
}

class TestDataBoundViewHolder(binding: ViewDataBinding): DataBoundViewHolder(binding) {
    companion object {
        var _hid = 0
    }
    val hid = _hid++

    val viewModel by lazy { ItemViewModel() }
    private var idForLogging = ""

    override fun markAttach() {
        super.markAttach()
        binding.executePendingBindings()
    }

    override fun log() =
        Log.i("ZZZ_", "[hid=$hid] viewModel=$viewModel state["+idForLogging+"] = ${lifecycleRegistry.currentState}")

    fun bind(item: ItemWithId) {
        Log.i("ZZZY", "[hid=$hid] [state=${lifecycleRegistry.currentState}] holder.setId(): viewModel=$viewModel id=${item.id}")
        viewModel.setItem(item)
        binding.setVariable(BR.item, viewModel)
        idForLogging = item.id
    }

    override fun toString() = "ViewHolder[hid=$hid]"
}

abstract class DataBoundViewHolder(val binding: ViewDataBinding):
    RecyclerView.ViewHolder(binding.root), LifecycleOwner {

    protected val lifecycleRegistry = LifecycleRegistry(this)

    abstract fun log(): Int

    init {
        lifecycleRegistry.currentState = Lifecycle.State.INITIALIZED
    }

    open fun markAttach() {
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
        log()
    }

    open fun markDetach() {
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        log()
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }
}

class IdItemDiffCallback: DiffUtil.ItemCallback<ItemWithId>() {
    override fun areItemsTheSame(oldItem: ItemWithId, newItem: ItemWithId) =
        oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: ItemWithId, newItem: ItemWithId) =
        oldItem.getData().value == newItem.getData().value
}

class ItemWithId(val id: String) {
    private val liveData: MutableLiveData<String> = MutableLiveData()

    override fun equals(other: Any?) = other is ItemWithId && id.equals(other.id) &&
        other.liveData.value.equals(liveData.value)

    fun setData(data: String) = this.liveData.postValue(data)

    fun getData(): LiveData<String> = liveData
}
