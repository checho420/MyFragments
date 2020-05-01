package com.checho.myfragment

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tabAdapter : TabsAdapter

        tabAdapter = TabsAdapter(supportFragmentManager)
        tabAdapter.addFragments(Fragment1(), "F1")
        tabAdapter.addFragments(Fragment2(), "F2")
        tabAdapter.addFragments(Fragment3(), "F3")


        viewPager.adapter = tabAdapter
        tabLayout.setupWithViewPager(viewPager)

    }


}
