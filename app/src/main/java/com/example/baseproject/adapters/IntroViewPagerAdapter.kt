package com.example.baseproject.adapters

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.baseproject.fragments.IntroFragment

class IntroViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    private val ARG_OBJECT = "position"

    override fun createFragment(position: Int): Fragment {
        val fragment: Fragment = IntroFragment()
        val args = Bundle()
        args.putInt(ARG_OBJECT, position)
        fragment.arguments = args
        return fragment
    }

    override fun getItemCount(): Int {
        return 3
    }
}