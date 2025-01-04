package com.thedigialex.questlog.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.thedigialex.questlog.R
import com.thedigialex.questlog.adapters.ItemAdapter
import com.thedigialex.questlog.controllers.UserController
import com.thedigialex.questlog.models.Item

class ShopFragment(private val userController: UserController) : Fragment() {

    private lateinit var itemsListView: ListView
    private lateinit var tvShopCoinAmount: TextView
    private lateinit var items: List<Item>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_shop, container, false)
        itemsListView = view.findViewById(R.id.itemsListView)
        tvShopCoinAmount = view.findViewById(R.id.tvShopCoinAmount)
        return view
    }

    override fun onResume() {
        super.onResume()
        loadItems()
    }

    private fun loadItems() {
        items = userController.dbHelper.getItems(0)
        userController.currentUser = userController.dbHelper.getUser()
        tvShopCoinAmount.text = "Level: "+userController.currentUser.level + "\nCoins: "+userController.currentUser.coins

        if (items.isEmpty()) {
            Toast.makeText(requireContext(), "No tasks with the selected status.", Toast.LENGTH_SHORT).show()
            itemsListView.adapter = null
        } else {
            val adapter = ItemAdapter(requireContext(), items) { item ->
                purchaseItem(item)
            }
            itemsListView.adapter = adapter
        }
    }

    private fun purchaseItem(item: Item){
        if(userController.currentUser.coins > item.cost){
            userController.currentUser.coins -= item.cost
            userController.dbHelper.saveItem(item, false)
            loadItems()
        }
    }
}