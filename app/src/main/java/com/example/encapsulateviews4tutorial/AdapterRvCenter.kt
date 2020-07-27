package com.example.encapsulateviews4tutorial

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdapterRvCenter(var context: Context, private var itemsStr: List<String>?) :
    RecyclerView.Adapter<AdapterRvCenter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_card, parent, false)
        return MyViewHolder(view)
    }


    override fun getItemCount(): Int {
        return itemsStr!!.size
    }

    override fun onBindViewHolder(myViewHolder: MyViewHolder, position: Int) {
        myViewHolder.tv_text.setText(itemsStr!!.get(position))
    }


    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tv_text: TextView = itemView.findViewById(R.id.tv_text)
        val ll_main: LinearLayout = itemView.findViewById(R.id.ll_main)
    }


}