
package com.example.weatherforecast


import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CityAdapter(private val city: MutableList<City>): RecyclerView.Adapter<CityAdapter.ViewHolder>() {

    private val limit = 9

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bind(city[position].name)
    }

    override fun getItemCount(): Int {

        return if(city.size > limit){
            limit
        } else {
            city.size
        }
    }

    fun addItem(name: String) {
        city.add(City(name))
        notifyItemInserted(city.size)
    }

    fun removeAt(position: Int) {
        city.removeAt(position)
        notifyItemRemoved(position)
    }

    class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.list_city, parent, false)) {

        fun bind(name: String) = with(itemView) {
            findViewById<TextView>(R.id.city_view).text = name
        }
    }
}