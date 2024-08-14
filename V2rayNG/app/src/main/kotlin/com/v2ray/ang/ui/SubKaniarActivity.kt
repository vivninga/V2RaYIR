package com.v2ray.ang.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.v2ray.ang.R
import com.v2ray.ang.databinding.ActivitySubKaniarBinding
import com.v2ray.ang.databinding.LayoutProgressBinding
import com.v2ray.ang.dto.SubscriptionItem
import com.v2ray.ang.extension.toast
import com.v2ray.ang.util.AngConfigManager
import com.v2ray.ang.util.MmkvManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SubKaniarActivity : BaseActivity() {
    private lateinit var binding: ActivitySubKaniarBinding

    var subscriptions: List<Pair<String, SubscriptionItem>> = listOf()
    //private val adapter by lazy { SubSettingRecyclerAdapter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubKaniarBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        title = getString(R.string.title_sub_kaniar)

        //binding.recyclerView.setHasFixedSize(true)
        //binding.recyclerView.layoutManager = LinearLayoutManager(this)
        //binding.recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        subscriptions = MmkvManager.decodeSubscriptions()
        //adapter.notifyDataSetChanged()
    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        //menuInflater.inflate(R.menu.action_sub_setting, menu)
//        return super.onCreateOptionsMenu(menu)
//    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.add_config -> {
            startActivity(Intent(this, SubEditActivity::class.java))
            true
        }

        else -> super.onOptionsItemSelected(item)

    }
}
