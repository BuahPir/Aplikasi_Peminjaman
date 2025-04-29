package com.mobile.aplikasipeminjaman

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class ShimmerAdapter(private val itemCount: Int = 6) :
    RecyclerView.Adapter<ShimmerAdapter.ShimmerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShimmerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_shimmer, parent, false)
        return ShimmerViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShimmerViewHolder, position: Int) {
        // shimmer animasi sudah otomatis
    }

    override fun getItemCount() = itemCount

    inner class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}