package com.androidstrike.schoolprojects.securitygame.features.home

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.androidstrike.schoolprojects.securitygame.R

class HomeBannerAdapter(itemView: View): RecyclerView.ViewHolder(itemView) {

    var bannerTextview: TextView
    var bannerTextviewHeading: TextView

    init {
        bannerTextview = itemView.findViewById(R.id.banner_text_view)
        bannerTextviewHeading = itemView.findViewById(R.id.banner_text_view_heading)
    }
}