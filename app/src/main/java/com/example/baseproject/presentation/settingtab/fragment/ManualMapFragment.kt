package com.example.baseproject.presentation.settingtab.fragment


import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.baseproject.bases.BaseFragment
import com.example.baseproject.databinding.FragmentManualMapBinding
import com.example.baseproject.presentation.settingtab.adapter.PlacesAdapter
import com.example.baseproject.presentation.viewmodel.MapSettingViewModel
import com.example.baseproject.utils.gone
import com.example.baseproject.utils.visible
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import org.koin.androidx.viewmodel.ext.android.activityViewModel


class ManualMapFragment : BaseFragment<FragmentManualMapBinding>(FragmentManualMapBinding::inflate) {
    private lateinit var placesClient: PlacesClient
    private lateinit var placesAdapter: PlacesAdapter
    private val mapSettingViewModel: MapSettingViewModel by activityViewModel()

    override fun initData() {
        placesClient = Places.createClient(requireContext())
    }

    override fun initView() {
        setupRecyclerView()
    }

    override fun initActionView() {
        with(binding){
            edtSearch.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }
                override fun afterTextChanged(s: Editable?) {
                    val query = s.toString()
                    if(query.length >=2){
                        searchQuery(query)
                    }else{
                        recyclerView.gone()
                    }
                }
            })
            btnSearch.setOnClickListener {
                val query = binding.edtSearch.text.toString()
                if (query.isNotEmpty()) {
                    searchQuery(query)
                }
            }
        }
    }
    fun setupRecyclerView() {
        placesAdapter = PlacesAdapter{place->

        }
        with(binding){
            recyclerView.apply {
                adapter = placesAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }
        }
    }
    fun searchQuery(query: String) {
        val request = FindAutocompletePredictionsRequest.builder().setQuery(query).build()
        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                binding.recyclerView.visible()
                Log.d("ManualMapFragment", "Kết quả tìm kiếm: ${response.autocompletePredictions}")
                placesAdapter.submitList(response.autocompletePredictions)
            }
            .addOnFailureListener { exception ->
                binding.recyclerView.gone()
                Log.e("ManualMapFragment", "Lỗi tìm: $exception")
            }
    }
}