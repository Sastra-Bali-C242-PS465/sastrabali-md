package com.example.sabi.ui.fragment.dictionary

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sabi.R
import com.example.sabi.data.repository.DictionaryRepository
import com.example.sabi.data.retrofit.ApiConfig
import com.example.sabi.data.store.SessionPreferences
import com.example.sabi.ui.adapter.DictionaryAdapter
import com.example.sabi.ui.activity.dictionary.DetailDictionaryActivity
import com.example.sabi.ui.viewmodel.DictionaryViewModel
import com.example.sabi.utils.DictionaryViewModelFactory
import kotlinx.coroutines.launch

class DictionaryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var noDataTextView: View
    private val dictionaryViewModel: DictionaryViewModel by viewModels {
        DictionaryViewModelFactory(DictionaryRepository(ApiConfig.getApiService()))
    }

    private lateinit var dictionaryAdapter: DictionaryAdapter
    private lateinit var sessionPreferences: SessionPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dictionary, container, false)

        sessionPreferences = SessionPreferences(requireContext())

        recyclerView = view.findViewById(R.id.rv_dictionary)
        noDataTextView = view.findViewById(R.id.tv_no_data)

        recyclerView.layoutManager = GridLayoutManager(context, 2)
        dictionaryAdapter = DictionaryAdapter(emptyList()) { dictionary ->
            val intent = Intent(requireContext(), DetailDictionaryActivity::class.java)
            intent.putExtra("DICTIONARY_ID", dictionary.id)
            startActivity(intent)
        }
        recyclerView.adapter = dictionaryAdapter

        fetchDictionaries()

        return view
    }

    private fun fetchDictionaries() {
        lifecycleScope.launch {
            sessionPreferences.token.collect { token ->
                if (!token.isNullOrEmpty()) {
                    observeDictionaries(token)
                } else {
                    noDataTextView.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                }
            }
        }
    }

    private fun observeDictionaries(token: String) {
        dictionaryViewModel.getDictionaries(token).observe(viewLifecycleOwner) { response ->
            val data = response.data.readings
            if (data.isNullOrEmpty()) {
                noDataTextView.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                noDataTextView.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                dictionaryAdapter = DictionaryAdapter(data) { dictionary ->
                    Log.d("DictionaryFragment", "Clicked Dictionary ID: ${dictionary.id}")
                    val intent = Intent(requireContext(), DetailDictionaryActivity::class.java)
                    intent.putExtra("DICTIONARY_ID", dictionary.id)  // Pass the dictionary ID to the detail activity
                    startActivity(intent)
                }
                recyclerView.adapter = dictionaryAdapter
            }
        }
    }
}
