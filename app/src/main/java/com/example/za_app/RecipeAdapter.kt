package com.example.za_app

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso

class CustomAdapter(private val context: Activity, private val lieux: ArrayList<lieu>)
    : BaseAdapter() {
    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val inflater = context.layoutInflater
        val rowView = inflater.inflate(R.layout.list_item_recipe, null)
        val imageView = rowView.findViewById<ImageView>(R.id.recipe_list_thumbnail)
        val title = rowView.findViewById<TextView>(R.id.recipe_list_title)
        val description= rowView.findViewById<TextView>(R.id.description)

        title.text = lieux[p0].nom
        description.text = lieux[p0].specialite

        var lieu = lieux[p0]!!.nom.replace(" ", "").trim();
        var path = "${lieu}${lieux[p0]!!.latitude}${lieux[p0]!!.longitude}"

        var number = 0

        Picasso.with(context)
            .load(
                "https://firebasestorage.googleapis.com/v0/b/zaapp-4771f.appspot.com/o/" +
                        "imagesLieux%2F" + path + "%2F" + number
                        + "?alt=media&token=dd682537-8e23-4150-99e9-7b12e3ec9d14"
            )
            .placeholder(R.drawable.logo)
            .into(imageView)

        return rowView
    }

    override fun getItem(p0: Int): Any {
        return lieux.get(p0)
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getCount(): Int {
        return lieux.size
    }

}
