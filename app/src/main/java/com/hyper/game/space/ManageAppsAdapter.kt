package com.hyper.game.space

import android.content.Context
import android.content.pm.ApplicationInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hyper.game.space.util.InstalledGamesManager

class ManageAppsAdapter(
    private val context: Context,
    private var appsList: List<ApplicationInfo>,
    private val onToggle: (ApplicationInfo, Boolean) -> Unit
) : RecyclerView.Adapter<ManageAppsAdapter.AppViewHolder>() {

    private val pm = context.packageManager
    private var selectedPackages: Set<String> = emptySet()

    fun updateData(newApps: List<ApplicationInfo>, selected: Set<String>) {
        appsList = newApps
        selectedPackages = selected
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_manage_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val appInfo = appsList[position]
        holder.bind(appInfo)
    }

    override fun getItemCount(): Int = appsList.size

    inner class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivAppIcon: ImageView = itemView.findViewById(R.id.ivAppIcon)
        private val tvAppName: TextView = itemView.findViewById(R.id.tvAppName)
        private val switchToggle: Switch = itemView.findViewById(R.id.switchToggle)

        fun bind(appInfo: ApplicationInfo) {
            tvAppName.text = appInfo.loadLabel(pm)
            ivAppIcon.setImageDrawable(appInfo.loadIcon(pm))

            // Temporarily remove listener to avoid triggering on bind
            switchToggle.setOnCheckedChangeListener(null)

            val isOn = InstalledGamesManager.isGame(appInfo) || selectedPackages.contains(appInfo.packageName)
            switchToggle.isChecked = isOn

            switchToggle.setOnCheckedChangeListener { _, isChecked ->
                onToggle(appInfo, isChecked)
            }
        }
    }
}
