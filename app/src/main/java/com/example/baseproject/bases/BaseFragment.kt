package com.example.baseproject.bases

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

typealias Inflater<T> = (LayoutInflater, ViewGroup?, Boolean) -> T
abstract class BaseFragment<viewBinding : ViewBinding>(val inflate: Inflater<viewBinding>) : Fragment() {

    private var _binding : viewBinding ?= null
    val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initData()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = inflate.invoke(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initActionView()
    }

    abstract fun initData()

    abstract fun initView()

    abstract fun initActionView()

}