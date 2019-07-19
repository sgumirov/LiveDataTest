package com.gumirov.shamil.livedatatest

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

class ItemViewModel: ViewModel() {
    companion object {
        var _vmid = 0
    }
    val vmid = _vmid++

    private val item = MutableLiveData<ItemWithId>()

    val theName = Transformations.switchMap(item) { it.getData() }
    val id = Transformations.map(item) {it.id}

    fun setItem(item: ItemWithId?) {
        Log.i("ZZZY", "[vmid=$vmid] setId = id=${item?.id}")
        this.item.value = item
        if (item == null) return
        Thread {
            val id = item.id
            for (i in 0..4) {
                Thread.sleep(800)
                if (this.item.value?.id != id) return@Thread
                val item2 = this@ItemViewModel.item.value
                item2?.setData("[$i] "+item.id)
                this@ItemViewModel.item.postValue(item2)
            }
        }.start()
    }

    override fun toString(): String {
        return "ItemViewModel@"+hashCode()
    }
}
