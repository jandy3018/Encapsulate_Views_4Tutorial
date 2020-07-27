package com.example.encapsulateviews4tutorial

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class AdapterRvTop(
    var context: Context,
    private var items: List<Int>?,
    private var itemsStr: List<String>?
) : RecyclerView.Adapter<AdapterRvTop.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_option_top, parent, false)
        return MyViewHolder(view)
    }


    override fun getItemCount(): Int {
        return items!!.size
    }

    override fun onBindViewHolder(myViewHolder: MyViewHolder, position: Int) {
        myViewHolder.ll_top.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this.context!!, items!![position]))
        myViewHolder.ll_center.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this.context!!, items!![position]))
        myViewHolder.tv_text.setText(itemsStr!!.get(position))
        // myViewHolder.ll_main.id= 1
    }


    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ll_center: LinearLayout = itemView.findViewById(R.id.ll_center)
        val ll_top: LinearLayout = itemView.findViewById(R.id.ll_top)
        val tv_text: TextView = itemView.findViewById(R.id.tv_text)
        val ll_main: LinearLayout = itemView.findViewById(R.id.ll_main)
    }


}