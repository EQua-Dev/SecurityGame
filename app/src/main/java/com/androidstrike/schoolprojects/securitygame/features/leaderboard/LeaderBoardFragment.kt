package com.androidstrike.schoolprojects.securitygame.features.leaderboard

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.androidstrike.schoolprojects.securitygame.R
import com.androidstrike.schoolprojects.securitygame.databinding.FragmentLeaderBoardBinding
import com.androidstrike.schoolprojects.securitygame.models.Leaderboard
import com.google.android.material.tabs.TabLayout

class LeaderBoardFragment : Fragment() {

    private var _binding: FragmentLeaderBoardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentLeaderBoardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding){
            //set the title to be displayed on each tab

            val tabTitles = resources.getStringArray(R.array.difficulties)
            for (title in tabTitles){
                val tab = leaderBoardTabLayout.newTab()
                tab.text = title
                leaderBoardTabLayout.addTab(tab)
            }
            leaderBoardTabLayout.tabGravity = TabLayout.GRAVITY_FILL

            val adapter = LeaderBoardPagerAdapter(
                activity,
                childFragmentManager,
                leaderBoardTabLayout.tabCount
            )
            leaderBoardViewPager.adapter = adapter
            leaderBoardViewPager.addOnPageChangeListener(
                TabLayout.TabLayoutOnPageChangeListener(
                    leaderBoardTabLayout
                )
            )

            //define the functionality of the tab layout
            leaderBoardTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    leaderBoardViewPager.currentItem = tab.position
                    leaderBoardTabLayout.setSelectedTabIndicatorColor(resources.getColor(R.color.primary))
                    leaderBoardTabLayout.setTabTextColors(
                        Color.BLACK,
                        resources.getColor(R.color.primary)
                    )
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {
                    leaderBoardTabLayout.setTabTextColors(Color.WHITE, Color.BLACK)
                }

                override fun onTabReselected(tab: TabLayout.Tab) {}
            })

        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}