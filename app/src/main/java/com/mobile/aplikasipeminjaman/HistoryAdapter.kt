package com.mobile.aplikasipeminjaman

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    private val data = mutableListOf<PeminjamanBuku>()

    fun submitList(newData: List<PeminjamanBuku>) {
        data.clear()
        data.addAll(newData)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val judul: TextView = itemView.findViewById(R.id.textJudul)
        val status: TextView = itemView.findViewById(R.id.textStatus)
        val tanggal: TextView = itemView.findViewById(R.id.textTanggal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.judul.text = item.informasi_buku.judul
        holder.status.text = item.status.uppercase()
        holder.tanggal.text = "Dipinjam: ${item.tanggal_pinjam} • Kembali: ${item.tanggal_kembali ?: "-"}"
    }
}
@kotlinx.serialization.Serializable
data class PeminjamanBuku(
    val id: Int,
    val tanggal_pinjam: String,
    val tanggal_kembali: String? = null,
    val status: String,
    val informasi_buku: InformasiBuku1
)

@kotlinx.serialization.Serializable
data class InformasiBuku1(
    val judul: String
)
