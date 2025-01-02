package com.thedigialex.questlog.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.thedigialex.questlog.R
import com.thedigialex.questlog.controllers.UserController

class ShopFragment(private val userController: UserController) : Fragment() {

    private lateinit var itemsListView: ListView
    private lateinit var tvShopCoinAmount: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_shop, container, false)
        itemsListView = view.findViewById(R.id.itemsListView)
        tvShopCoinAmount = view.findViewById(R.id.tvShopCoinAmount)
        tvShopCoinAmount.setText("Level: "+userController.currentUser.level + "\nCoins: "+userController.currentUser.coins)

        return view
    }
}