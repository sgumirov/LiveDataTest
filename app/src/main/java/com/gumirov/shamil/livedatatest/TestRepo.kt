package com.gumirov.shamil.livedatatest

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class TestRepo {
    fun load(id: String): LiveData<TestData> {
        val res = MutableLiveData<TestData>()
        Thread(Runnable {
            Thread.sleep(1000)
            res.postValue(TestData("Name", "email@b.com"))
        }).start()
        return res
    }
}
