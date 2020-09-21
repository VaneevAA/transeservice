package com.example.ekotransservice_routemanager

import android.app.Application
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.ekotransservice_routemanager.DataBaseInterface.RouteRepository
import com.example.ekotransservice_routemanager.DataClasses.Region
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.lang.Exception

class RegionListAdapter(context: Context, val itemLayout: Int, var dataList: ArrayList<Region>?) : ArrayAdapter<Region>(
    context,
    itemLayout
)
{
    val routeRepository: RouteRepository = RouteRepository(context.applicationContext as Application)

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun publishResults(
                charSequence: CharSequence?,
                filterResults: Filter.FilterResults
            ) {
                dataList = if (filterResults.values != null) {
                    filterResults.values as ArrayList<Region>
                } else {
                    null
                }
                if (filterResults.count > 0) {
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }
                notifyDataSetChanged()
            }

            override fun performFiltering(charSequence: CharSequence?): Filter.FilterResults {
                val queryString = charSequence?.toString()?.toLowerCase()

                if (dataList!!.size == 0) {
                    GlobalScope.launch { loadRegion() }
                }

                val filterResults = Filter.FilterResults()
                filterResults.values = if (queryString==null || queryString.isEmpty())
                    dataList
                else
                    try {
                        dataList!!.filter {
                                it.toString().toLowerCase().contains(queryString)
                        }
                    }catch (e: Exception) {
                        Log.d("error", "er $e")
                    }
                return filterResults
            }
        }
    }

    private suspend fun loadRegion() {
        val serverList = GlobalScope.async {routeRepository.getRegionList()}
        val result = serverList.await()
        if (result != null){
            dataList = result
        }
    }

    override fun getCount(): Int {
        if (dataList != null){
            return dataList!!.size
        }
        return 0
    }

    override fun getItem(position: Int): Region? {
        return dataList!!.get(position)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        var itemView: View = LayoutInflater.from(context).inflate(itemLayout, parent, false)
        val textView: TextView = itemView.findViewById(com.example.ekotransservice_routemanager.R.id.textViewRegionItem);
        textView.setText(getItem(position).toString())
        return itemView
    }

    fun setList(dataList: ArrayList<Region>){
        /*this.dataList = dataList.value
        notifyDataSetChanged()*/
    }

}