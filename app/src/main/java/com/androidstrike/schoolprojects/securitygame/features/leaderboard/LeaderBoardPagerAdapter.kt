/*
 * Copyright (c) 2023.
 * Richard Uzor
 * For Afro Connect Technologies
 * Under the Authority of Devstrike Digital Ltd.
 *
 */

@file:Suppress("DEPRECATION")

package com.androidstrike.schoolprojects.securitygame.features.leaderboard

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

@Suppress("DEPRECATION")
class LeaderBoardPagerAdapter (var context: FragmentActivity?,
                               fm: FragmentManager,
                               private var totalTabs: Int
) : FragmentPagerAdapter(fm) {
    override fun getCount(): Int {
        return totalTabs
    }

    val TAG = "ManagerOrdersLandingPagerAdapter"

    //when each tab is selected, define the fragment to be implemented
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                EasyLeaderBoard()
            }
            1 -> {
                MediumLeaderBoard()
            }
            2 -> {
                HardLeaderBoard()
            }
            else -> {
                getItem(position)

            }
        }
    }
}