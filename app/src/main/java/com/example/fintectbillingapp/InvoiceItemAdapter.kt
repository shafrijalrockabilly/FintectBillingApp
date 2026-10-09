package com.example.fintectbillingapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fintectbillingapp.databinding.ItemInvoiceProductBinding

class InvoiceItemAdapter(
    private val items: List<InvoiceItem>
) : RecyclerView.Adapter<InvoiceItemAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemInvoiceProductBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemInvoiceProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context
        with(holder.binding) {
            tvItemName.text = item.itemName
            tvItemQtyPrice.text = context.getString(
                R.string.item_qty_price_format,
                item.qty,
                item.unitPrice.toRupiah()
            )
            tvItemSubtotal.text = item.subtotal.toRupiah()
        }
    }

    override fun getItemCount(): Int = items.size
}
