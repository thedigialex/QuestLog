package com.thedigialex.questlog.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.thedigialex.questlog.R
import com.thedigialex.questlog.models.Item

class ItemAdapter(
    private val context: android.content.Context,
    private val items: List<Item>,
    private val purchaseItem: (Item) -> Unit
) : android.widget.BaseAdapter() {

    override fun getCount(): Int = items.size

    override fun getItem(position: Int): Item = items[position]

    override fun getItemId(position: Int): Long = items[position].id.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_shop, parent, false)

        val item = getItem(position)

        val tvShopTitle = view.findViewById<TextView>(R.id.tvShopTitle)
        val tvShopType = view.findViewById<TextView>(R.id.tvShopType)
        val btnItemBuy = view.findViewById<Button>(R.id.btnItemBuy)
        val ivAvatarPreview = view.findViewById<ImageView>(R.id.ivAvatarPreview)
        ivAvatarPreview.visibility = View.GONE
        if(item.type == "Avatar"){
            ivAvatarPreview.visibility = View.VISIBLE
            ivAvatarPreview.setBackgroundResource(item.imageValue)
        }

        tvShopTitle.text = item.resource
        tvShopType.text = item.type

        btnItemBuy.setOnClickListener {
            purchaseItem(item)
        }

        return view
    }
}
