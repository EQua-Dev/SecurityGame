package com.androidstrike.schoolprojects.securitygame.features.leaderboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.androidstrike.schoolprojects.securitygame.R
import com.androidstrike.schoolprojects.securitygame.models.LeaderboardDetails
import com.androidstrike.schoolprojects.securitygame.models.Quiz

class LeaderBoardAdapter(private val items: List<LeaderboardDetails>) : RecyclerView.Adapter<LeaderBoardAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Define the views in the item layout
        val rankTextView: TextView = itemView.findViewById(R.id.leader_board_rank)
        val usernameTextView: TextView = itemView.findViewById(R.id.leader_board_user_name)
        val scoreTextView: TextView = itemView.findViewById(R.id.leader_board_score)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflate the item layout
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_leader_board_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Bind data to the views
        val item = items[position]
        holder.rankTextView.text = item.rank
        holder.usernameTextView.text = item.username
        holder.scoreTextView.text = item.score
    }

    override fun getItemCount(): Int {
        return items.size
    }
}
