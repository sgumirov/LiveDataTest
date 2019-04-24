package com.gumirov.shamil.livedatatest

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

class TestViewModel: ViewModel() {
    private val repo = TestRepo()

    val id = MutableLiveData<String> ()

    val data = Transformations.switchMap(id) { repo.load(it) }

    val name = Transformations.map(data) { it.name }
    val email = Transformations.map(data) { it.email }

    fun startLoad() {
        id.value = "1"
    }
}
