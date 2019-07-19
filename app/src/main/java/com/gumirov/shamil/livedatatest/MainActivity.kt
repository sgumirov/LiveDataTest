package com.gumirov.shamil.livedatatest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.gumirov.shamil.livedatatest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    val ids = (0..12).toList().map { it.toString() }
    val items = ArrayList<ItemWithId>().also {
        it.addAll(ids.map { id -> ItemWithId(id) })
    }

    //TODO try: item must be viewmodel itself!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        val viewModel = ViewModelProviders.of(this).get(TestViewModel::class.java)
        binding.viewmodel = viewModel

        val adapter = TestDataBoundItemAdaptor()
        binding.list.layoutManager = LinearLayoutManager(this)
        binding.list.adapter = adapter
        viewModel.id.observe(this, Observer {
            adapter.submitList(items)
        })
    }
}

