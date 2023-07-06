package com.androidstrike.schoolprojects.securitygame.features.quiz

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.androidstrike.schoolprojects.securitygame.R
import com.androidstrike.schoolprojects.securitygame.models.Quiz

class QuizRulesAdapter(private val items: List<String>) : RecyclerView.Adapter<QuizRulesAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Define the views in the item layout
        val ruleTextView: TextView = itemView.findViewById(R.id.rule_text_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflate the item layout
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_rules_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Bind data to the views
        val item = items[position]
        holder.ruleTextView.text = item
    }

    override fun getItemCount(): Int {
        return items.size
    }
}
