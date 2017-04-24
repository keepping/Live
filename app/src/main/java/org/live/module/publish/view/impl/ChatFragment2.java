package org.live.module.publish.view.impl;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.live.R;
import org.live.common.util.NetworkUtils;
import org.live.module.chat.service.ChatService;


import java.util.ArrayList;

/**
 * Created by KAM on 2017/4/24.
 */

public class ChatFragment2 extends Fragment {
    private static final String TAG = "Global";
    private ListView cRecordsListView; // 记录列表
    private ChatFragment2.ChatRecordAdapter adapter; // 聊天记录适配器
    private LinearLayout cUnreadMsgHintView; // 未读消息提示视图
    private TextView cUnreadMsgHintTextView; // 未读消息提示信息
    private EditText cMsgEditText; // 待发送消息文本框
    private Button cMsgSendButton; // 消息发送按钮
    private int unReadMsgCount = 0; // 未读信息数量
    private boolean scrollBottomFlag = true; // 滚动条标记，判断滚动条是否在底部
    private LinearLayout cLastChatRecordView; // 最后一条消息记录视图
    private TextView cLastChatRecordFromUser; // 最后一条消息记录来自用户
    private TextView cLastChatRecord; // 最后一条消息记录
    private ChatService chatService = null; // 聊天服务实体
    private View view; // 当前view
    private Typeface typeFace; // 字体样式
    private String wsUrl = null; // 服务器地址
    private LinearLayout cChatListLinearLayout; // 聊天记录
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_chat2, container, false);
        typeFace = Typeface.createFromAsset(getActivity().getAssets(), "fonts/AndroidClockMono-Thin.ttf"); // 外部字体目录
        initUIElements();
        return view;
    }

    /**
     * 初始化UI控件
     */
    private void initUIElements() {
        cRecordsListView = (ListView) view.findViewById(R.id.lv_chat_records);
        cUnreadMsgHintView = (LinearLayout) view.findViewById(R.id.ll_chat_hint);
        cUnreadMsgHintTextView = (TextView) view.findViewById(R.id.tv_chat_hint_msg);
        cLastChatRecordView = (LinearLayout) view.findViewById(R.id.ll_chat_record_last);
        cLastChatRecordFromUser = (TextView) view.findViewById(R.id.tv_chat_from_user_last);
        cLastChatRecord = (TextView) view.findViewById(R.id.tv_chat_record_last);
        cMsgEditText = (EditText) view.findViewById(R.id.et_chat_msg);
        cMsgSendButton = (Button) view.findViewById(R.id.btn_chat_send);
        cChatListLinearLayout = (LinearLayout) view.findViewById(R.id.ll_chat_record_list);

        cUnreadMsgHintTextView.setTypeface(typeFace);// 应用字体
        cLastChatRecordFromUser.setTypeface(typeFace);
        cLastChatRecord.setTypeface(typeFace);
//        cMsgEditText.setTypeface(typeFace);
/*        cChatListLinearLayout.getBackground().setAlpha(50);*/

        adapter = new ChatFragment2.ChatRecordAdapter(getActivity());
        cRecordsListView.setAdapter(adapter);
        addLastRecord("测试用户");

        /**
         * 绑定未读消息提示按钮
         */
        cUnreadMsgHintView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "提示消息被点击.. ");
                cRecordsListView.setSelection(adapter.getCount() - 1); // 设置listview滚动至底部
                cRecordsListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL); // 设自动滚动到底部
                scrollBottomFlag = true;
                unReadMsgCount = 0; // 清零未读消息数目
                cUnreadMsgHintView.setVisibility(View.GONE); // 清除未读消息提示视图
                cLastChatRecordView.setVisibility(View.VISIBLE); // 显示最后一条记录视图
            }
        });

        /** 绑定发送消息按钮 **/
/*
        cMsgSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkUtils.isConnected(getActivity()) && wsUrl != null) {
                    chatService.sendMsg(cMsgEditText.getText().toString()); // 发送消息
                } else {
                  //  showToastMsg("网络无法连接或请求服务地址有误，消息无法发送...", Toast.LENGTH_SHORT);
                    addRecord("测试用：" + cMsgEditText.getText().toString());
                }
                cMsgEditText.setText("");
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(cMsgEditText.getWindowToken(), 0); // 隐藏软键盘
            }
        });
*/

        /**
         * 设置滚动监听
         */
        cRecordsListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    // 当不滚动时
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        // 判断滚动到底部
                        if (cRecordsListView.getLastVisiblePosition() == (cRecordsListView.getCount() - 1)) {
                            Log.i(TAG, "滚动到底部");
                            cRecordsListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL); // 设自动滚动到底部
                            scrollBottomFlag = true;
                            unReadMsgCount = 0; // 清零未读消息数目
                            cUnreadMsgHintView.setVisibility(View.GONE); // 清除未读消息提示视图
                            cLastChatRecordView.setVisibility(View.VISIBLE); // 显示最后一条记录视图
                        } else {
                            Log.i(TAG, "不是在底部");
                            cRecordsListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL); // 取消自动滚动到底部
                            scrollBottomFlag = false;
                            cLastChatRecordView.setVisibility(View.GONE); // 清除最后一条记录视图
                        }
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });

    }

    /**
     * 追加消息记录
     */
    private void addRecord(String msg) {
        // 添加记录
        adapter.arr.add(msg);
        adapter.notifyDataSetChanged();
        if (!scrollBottomFlag) {
            unReadMsgCount++;
            cUnreadMsgHintTextView.setText("你有" + unReadMsgCount + "条消息");
            cUnreadMsgHintView.setVisibility(View.VISIBLE); // 显示视图
        }
    }

    /**
     * 添加最后一条消息记录
     */
    private void addLastRecord(String user) {
        cLastChatRecordFromUser.setText(user);
    }

    /**
     * 显示提示消息
     *
     * @param msg
     * @param lengthType
     */
    private void showToastMsg(String msg, int lengthType) {
        Toast.makeText(getActivity(), msg, lengthType).show();
    }


    /**
     * 聊天记录适配器
     */
    private class ChatRecordAdapter extends BaseAdapter {

        private Context context;
        private LayoutInflater inflater;
        public ArrayList<String> arr;

        public ChatRecordAdapter(Context context) {
            super();
            this.context = context;
            inflater = LayoutInflater.from(context);
            arr = new ArrayList<String>();
            arr.add("温馨提示：涉及色情、低俗、血腥、暴力、无版权等内容将被封停账号及追究法律责任，文明绿色直播从我做起！"); // 添加默认信息
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return arr.size();
        }

        @Override
        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
            return arg0;
        }

        @Override
        public long getItemId(int arg0) {
            // TODO Auto-generated method stub
            return arg0;
        }

        @Override
        public View getView(final int position, View view, ViewGroup arg2) {
            // TODO Auto-generated method stub
            if (view == null) {
                view = inflater.inflate(R.layout.item_chat_record, null);
            }
            final TextView userTextView = (TextView) view.findViewById(R.id.tv_chat_from_user);
            final TextView msgTextView = (TextView) view.findViewById(R.id.tv_chat_record);
            userTextView.setTypeface(typeFace);
            msgTextView.setTypeface(typeFace);
            userTextView.setText("测试用户:");
            msgTextView.setText(arr.get(position));
            msgTextView.setTextColor(getResources().getColor(R.color.colorWhite2));
            userTextView.setTextColor(getResources().getColor(R.color.colorShallowBlue));
            if (position == 0) {
                userTextView.setTextColor(Color.RED);
                userTextView.setText(arr.get(position));
                msgTextView.setText("");
            }
            return view;
        }
    }
}