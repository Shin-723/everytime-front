package com.wink.knockmate

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class Addschedule_invite_detail : Fragment() {
    lateinit var itemAdapter: Addschedule_invite_detail_Follower_Adapter
    var followerList = mutableListOf<UserModel>()
    lateinit var knockmate_recycler: RecyclerView

    lateinit var groupAdapter: Addschedule_invite_detail_group_Adapter
    var groupList = mutableListOf<UserModel>()
    lateinit var group_recycler: RecyclerView

    lateinit var invitedAdapter: Addschedule_invited_item_Adapter
    var inviteList = mutableListOf<UserModel>()
    lateinit var invited_recycler: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater!!.inflate(R.layout.addschedule_invite_detail, container, false)

        val okButton = view.findViewById<TextView>(R.id.invite_ok_button)
        knockmate_recycler = view.findViewById(R.id.invite_knockmate_recycler)
        invited_recycler = view.findViewById(R.id.to_invite_users)
        group_recycler = view.findViewById(R.id.invite_group_member_recycler)

        okButton.setOnClickListener {
            parentFragment?.childFragmentManager
                ?.beginTransaction()
                ?.replace(R.id.addschedule_frame, AddSchedule_detail())
                ?.addToBackStack(null)
                ?.commit()
        }

        initInvitedRecycler()
        initFollowerRecycler()
        initGroupRecycler()

        itemAdapter.setOnCheckBoxClickListener(object :
            Addschedule_invite_detail_Follower_Adapter.OnCheckBoxClickListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onCheckClick(v: CheckBox, data: UserModel, pos: Int) {
                //초대할 명단 추가하기
                followerList[pos].invite = v.isChecked
                if (followerList[pos].invite) {
                    AddScheduleInfo.invitersNumber++
                    inviteList.apply {
                        inviteList.add(followerList[pos])
                        invitedAdapter.datas = inviteList
                        invitedAdapter.notifyDataSetChanged()
                    }
                } else {
                    AddScheduleInfo.invitersNumber--
                    inviteList.apply {
                        inviteList.remove(followerList[pos])
                        invitedAdapter.datas = inviteList
                        invitedAdapter.notifyDataSetChanged()
                    }

                }
            }
        })

        return view
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initGroupRecycler() {
        groupAdapter = Addschedule_invite_detail_group_Adapter(requireParentFragment().requireContext())
        group_recycler.layoutManager =
            LinearLayoutManager(requireParentFragment().requireContext())
        group_recycler.adapter = invitedAdapter
        groupList.apply {

            groupAdapter.datas = inviteList
            groupAdapter.notifyDataSetChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initInvitedRecycler() {
        invitedAdapter = Addschedule_invited_item_Adapter(requireParentFragment().requireContext())
        invited_recycler.layoutManager =
            LinearLayoutManager(requireParentFragment().requireContext())
        invited_recycler.adapter = invitedAdapter
        inviteList.apply {
            for (i in 0 until followerList.size) {
                if (followerList[i].invite) {
                    inviteList.add(followerList[i])
                }
            }
            //groupList 버전도 추가
            invitedAdapter.datas = inviteList
            invitedAdapter.notifyDataSetChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initFollowerRecycler() {
        itemAdapter =
            Addschedule_invite_detail_Follower_Adapter(requireParentFragment().requireActivity().applicationContext)
        knockmate_recycler.layoutManager =
            LinearLayoutManager(requireParentFragment().requireActivity().applicationContext)
        knockmate_recycler.adapter = itemAdapter

        val client = OkHttpClient().newBuilder()
            .build()
        val request: Request = Request.Builder()
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .url("http://3.35.146.57:3000/myfollower?email=dy@test.com")
            .get()
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("log1", e.message.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                object : Thread() {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun run() {
                        if (response.code() == 200) {
                            val res = JSONObject(response.body()?.string())
                            val resTemp = res.getJSONArray("data")
                            followerList.apply {
                                for (i in 0 until resTemp.length()) {
                                    followerList.add(
                                        UserModel(
                                            resTemp.getJSONObject(i).getString("id"),
                                            resTemp.getJSONObject(i).getString("nickname"),
                                            true,
                                            resTemp.getJSONObject(i).getString("email"),
                                        )
                                    )
                                }
                            }
                        } else if (response.code() == 201) {
                        } else {
                        }
                    }
                }.run()
            }
        })

        itemAdapter.datas = followerList
        itemAdapter.notifyDataSetChanged()
    }
}