package com.wink.knockmate

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class Addschedule_invite_item_Adapter :
    RecyclerView.Adapter<Addschedule_invite_item_Adapter.ViewHolder>() {
    var datas = mutableListOf<UserModel>()
    var imageboolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.addschedule_invite_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = datas.size


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val image = itemView.findViewById<ImageView>(R.id.profile_image)
        private val nameView = itemView.findViewById<TextView>(R.id.profile_name)

        @SuppressLint("SetTextI18n")
        fun bind(item: UserModel) {
            val client = OkHttpClient().newBuilder()
                .build()
            var tempQuery = ""
            tempQuery = if (item.email == null) {
                item.id + "@naver.com"
            } else {
                item.email!!
            }
            val request: Request = Request.Builder()
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .url("http://3.35.146.57:3000/picture/${tempQuery}")
                .get()
                .build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.d("log1", e.message.toString())
                }

                override fun onResponse(call: Call, response: Response) {
                    object : Thread() {
                        override fun run() {
                            if (response.code() == 200) {
                                Glide.with(itemView).load(JSONObject(response.body()?.string()))
                                    .transform(CenterCrop(), RoundedCorners(20))
                                    .into(image)
                                imageboolean = true
                            } else {
                                imageboolean = false
                            }
                        }
                    }.run()
                }
            })

            if (!imageboolean) {
                Glide.with(itemView).load(R.drawable.profile_default)
                    .into(image)
            }

            if (item.nickname != null && !item.user) {
                nameView.text = item.nickname + " (" + item.members.toString() + "명)"
            } else if (item.nickname == null && !item.user) {
                nameView.text =
                    item.id + " (" + item.members.toString() + "명)"
            }
            if (item.nickname != null && item.user) {
                nameView.text = item.nickname
            } else if (item.nickname == null && item.user) {
                nameView.text = item.id
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(datas[position])
    }


}