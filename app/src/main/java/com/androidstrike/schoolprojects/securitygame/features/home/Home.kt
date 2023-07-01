package com.androidstrike.schoolprojects.securitygame.features.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.androidstrike.schoolprojects.securitygame.R
import com.androidstrike.schoolprojects.securitygame.databinding.FragmentHomeBinding
import com.androidstrike.schoolprojects.securitygame.models.SecurityTips
import com.androidstrike.schoolprojects.securitygame.utils.Common.securityTipsCollectionRef
import com.androidstrike.schoolprojects.securitygame.utils.toast
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import java.util.Timer
import java.util.TimerTask

class Home : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var securityTipsAdapter: FirestoreRecyclerAdapter<SecurityTips, HomeBannerAdapter>? =
        null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getRealtimeSecurityTips()

    }

    private fun getRealtimeSecurityTips() {

        val securityTips =
            securityTipsCollectionRef
        val options = FirestoreRecyclerOptions.Builder<SecurityTips>()
            .setQuery(securityTips, SecurityTips::class.java).build()
        try {
            securityTipsAdapter = object :
                FirestoreRecyclerAdapter<SecurityTips, HomeBannerAdapter>(options) {
                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ): HomeBannerAdapter {
                    val itemView = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_security_tips_layout, parent, false)
                    return HomeBannerAdapter(itemView)
                }

                override fun onBindViewHolder(
                    holder: HomeBannerAdapter,
                    position: Int,
                    model: SecurityTips
                ) {
                    holder.bannerTextviewHeading.text = model.heading
                    holder.bannerTextview.text = model.text

                }
            }

        } catch (e: Exception) {
            requireActivity().toast(e.message.toString())
        }
        securityTipsAdapter?.startListening()
        binding.securityTipsBanner.adapter = securityTipsAdapter

        val slideTimer = Timer()
        val slideRunnable = object : TimerTask() {
            override fun run() {
                // Increment the current item index or reset to 0
                val currentItem = binding.securityTipsBanner.currentItem
                val nextItem = if (currentItem == securityTipsAdapter!!.itemCount - 1) 0 else currentItem + 1
                binding.securityTipsBanner.setCurrentItem(nextItem, true)
            }
        }

// Schedule the slideRunnable to run every few seconds
        slideTimer.scheduleAtFixedRate(slideRunnable, 3000, 3000)


    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}