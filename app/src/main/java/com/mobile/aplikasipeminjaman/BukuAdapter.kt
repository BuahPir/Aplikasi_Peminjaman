package com.mobile.aplikasipeminjaman
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class BukuAdapter(
    private val onItemClick: (InformasiBuku) -> Unit,
    private val onPinjamBukuClick: (InformasiBuku) -> Unit,
    private val onKembalikanBukuClick: (InformasiBuku) -> Unit
) : ListAdapter<InformasiBuku, BukuAdapter.BukuViewHolder>(DIFF_CALLBACK) {

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<InformasiBuku>() {
            override fun areItemsTheSame(oldItem: InformasiBuku, newItem: InformasiBuku) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: InformasiBuku, newItem: InformasiBuku) = oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BukuViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_buku, parent, false)
        return BukuViewHolder(view)
    }

    override fun onBindViewHolder(holder: BukuViewHolder, position: Int) {
        val buku = getItem(position)
        holder.bind(buku)
    }

    inner class BukuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textJudul: TextView = itemView.findViewById(R.id.textJudul)
        private val textPenulis: TextView = itemView.findViewById(R.id.textPenulis)
        private val textPenerbit: TextView = itemView.findViewById(R.id.penerbit)
        private val textTahun: TextView = itemView.findViewById(R.id.tahunTerbit)
        private val imageCover: ImageView = itemView.findViewById(R.id.imageCover)
        private val buttonPinjam: Button = itemView.findViewById(R.id.buttonPinjamBuku)
        private val buttonKembalikan: Button = itemView.findViewById(R.id.buttonKembalikanBuku)

        fun bind(buku: InformasiBuku) {
            textJudul.text = buku.judul
            textPenulis.text = "oleh ${buku.penulis}"
            textPenerbit.text = buku.penerbit ?: "-"
            textTahun.text = buku.tahun_terbit.toString() ?: "-"

            Glide.with(itemView.context)
                .load(buku.cover_url)
                .placeholder(R.drawable.ic_launcher_background)
                .into(imageCover)

            if (buku.status == "dipinjam") {
                buttonPinjam.visibility = View.GONE
                buttonKembalikan.visibility = View.VISIBLE
                buttonKembalikan.isEnabled = true

                buttonKembalikan.setOnClickListener {
                    onKembalikanBukuClick(buku)
                }

                itemView.setOnClickListener {
                    onItemClick(buku)
                }
            } else {
                buttonPinjam.visibility = View.VISIBLE
                buttonKembalikan.visibility = View.GONE
                buttonPinjam.isEnabled = true
                buttonPinjam.text = "Pinjam Buku"

                buttonPinjam.setOnClickListener {
                    onPinjamBukuClick(buku)
                }

                itemView.setOnClickListener(null)
                itemView.isClickable = false
            }
        }
    }
}
