package com.gumirov.shamil.livedatatest

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

/**
 * Test for flow: user sets id, observed by repo loader which sets data. Data is observed to provide name and email.
 *
 *     [id] -switchmap-> repo loads [data] --map-> [name]
 *                                        \-map-> [email]
 */
class TestViewModel: ViewModel() {
    private val repo = TestRepo()

    val id = MutableLiveData<String> ()
    fun startLoad() {
        id.value = "1"
    }

    val data = Transformations.switchMap(id) { id -> repo.load(id) }
    val anotherData = Transformations.switchMap(data) { if (it != null) repo.load(it.id) else null }

    val name = Transformations.map(data) { it?.name ?: "NONE" }
    val email = Transformations.map(data) { it?.email ?: "NONE" }
    val uid = Transformations.map(data) { transcode(it?.id) }
    val anotherName = Transformations.map(anotherData) { it?.name ?: "NONE" }

    private fun transcode(id: String?): String? {
        id ?: return null
        return "<$id>"
    }
}
