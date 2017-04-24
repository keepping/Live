package org.live.module.publish.view.impl;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.appyvet.rangebar.RangeBar;

import org.live.R;

import com.bumptech.glide.Glide;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;
import com.suke.widget.SwitchButton;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.ui.TXCloudVideoView;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;
import net.steamcrafted.materialiconlib.MaterialIconView;

import org.live.common.constants.LiveConstants;
import org.live.common.listener.BackHandledFragment;
import org.live.common.listener.NoDoubleClickListener;
import org.live.common.util.NetworkUtils;
import org.live.common.util.ResponseModel;
import org.live.module.anchor.view.AnchorInfoView;
import org.live.module.home.constants.HomeConstants;
import org.live.module.home.domain.AppAnchorInfo;
import org.live.module.home.presenter.LivePresenter;
import org.live.module.home.presenter.LiveRoomPresenter;
import org.live.module.home.view.custom.AnchorInfoDialogView;
import org.live.module.home.view.impl.HomeActivity;
import org.live.module.login.domain.MobileUserVo;
import org.live.module.publish.presenter.PublishPresenter;
import org.live.module.publish.presenter.impl.PublishPresenterImpl;
import org.live.module.publish.util.constant.PublishConstant;
import org.live.module.publish.view.PublishView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.wasabeef.glide.transformations.CropCircleTransformation;


/**
 * 推流模块
 * Created by KAM on 2017/2/27.
 */

public class PublishFragment extends BackHandledFragment implements PublishView {
    private static final String TAG = "PublishFragment";
    private View view = null;
    private IconButtonOnClickListener listener = null;
    private static final int ALPHHA_DEFAULT_VALUE = 50; // 默认透明度
    private TXCloudVideoView iPreviewVideoView = null; // 直播预览视图
    private PublishPresenter recorderPresenter = null;
    private String rtmpUrl = null; // 测试用例
    private DialogPlus dialog = null; // 对话框

    private String pDefinitionInfos[] = {"标清", "高清", "超清"};

    private boolean isRecording = false; // 正在直播
    private boolean isFrontCamera = true; // 开启前置摄像头
    private boolean isFlashOn = false; // 开启闪光灯

    private MaterialIconView iRecordingStatusButton = null; // 直播状态转换按钮
    private MaterialIconView iCameraSwitchButton = null; // 摄像头转换按钮
    private MaterialIconView iBeautyButton = null; // 美颜按钮
    private MaterialIconView iflashStatusButton = null; // 闪光灯转台转换按钮
    private MaterialIconView iVolumeSettingsButton = null; // 麦克风转台转换按钮
    private MaterialIconView iRecordSettingsButton = null; // 直播设置按钮
    private MaterialIconView iRecordCloseButton = null; // 主播界面关闭按钮

    private RangeBar iBeautySettingRangeBar = null; // 磨皮设置拉杆
    private RangeBar iWhiteningSettingRangeBar = null; // 美白设置拉杆
    private RangeBar iMicroPhoneSettingRangeBar = null; // 麦克风音量拉杆
    private RangeBar iVolumeSettingRangeBar = null; // 背景音量设置拉杆

    private SwitchButton iVolumeOffSwitchButton = null; // 静音开关
    private SwitchButton iTouchFocusSwitchButton = null; // 手动对焦开关

    private Spinner pDefinitionSpinner = null; // 清晰度下拉框
    private RelativeLayout pLiveRoomInfoRelativeLayout; // 直播间信息视图
    private MobileUserVo mobileUserVo; // 用户信息
    private ImageView pHeadImgImageView; // 用户头像视图
    private TextView pLiveRoomName; // 直播间名称
    private TextView pOnlineCount; // 在线人数
    private LinearLayout pBlockListLinearLayout; // 黑名单按钮
    private List<Map<String, Object>> data;

    private Handler handler ;

    private LiveRoomPresenter liveRoomPresenter ;
    private AnchorInfoDialogView anchorInfoDialogView;     //主播信息弹出框的包裹view

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_recorder, container);
        listener = new IconButtonOnClickListener(); // 初始化图标按钮监听事件
        recorderPresenter = new PublishPresenterImpl(this, getActivity());
        PublishActivity publishActivity = (PublishActivity) getActivity();
        this.rtmpUrl = publishActivity.getRtmpUrl(); // 推流地址
        this.mobileUserVo = HomeActivity.mobileUserVo; // 用户信息

        initUIElements(); // 初始化ui控件
        recorderPresenter.startCameraPreview(); // 开始预览

        newHanderInstance() ;
        liveRoomPresenter = new LiveRoomPresenter(getActivity(), handler) ;

        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        recorderPresenter.refreshPreferences(); // 刷新参数并持久化
    }

    /**
     * 初始化ui控件
     */
    private void initUIElements() {
        /** 各控件含义如上成员变量所释 **/
        iRecordingStatusButton = (MaterialIconView) view.findViewById(R.id.btn_recording_status);
        iRecordingStatusButton.getBackground().setAlpha(ALPHHA_DEFAULT_VALUE);
        iCameraSwitchButton = (MaterialIconView) view.findViewById(R.id.btn_camera_switch);
        iCameraSwitchButton.getBackground().setAlpha(ALPHHA_DEFAULT_VALUE);
        iBeautyButton = (MaterialIconView) view.findViewById(R.id.btn_beauty);
        iBeautyButton.getBackground().setAlpha(ALPHHA_DEFAULT_VALUE);
        iflashStatusButton = (MaterialIconView) view.findViewById(R.id.btn_flash_status);
        iflashStatusButton.getBackground().setAlpha(ALPHHA_DEFAULT_VALUE);
        iVolumeSettingsButton = (MaterialIconView) view.findViewById(R.id.btn_volume_settings);
        iVolumeSettingsButton.getBackground().setAlpha(ALPHHA_DEFAULT_VALUE);
        iRecordCloseButton = (MaterialIconView) view.findViewById(R.id.btn_record_close);
        iRecordCloseButton.getBackground().setAlpha(ALPHHA_DEFAULT_VALUE);
        iRecordSettingsButton = (MaterialIconView) view.findViewById(R.id.btn_record_settings);
        iRecordSettingsButton.getBackground().setAlpha(ALPHHA_DEFAULT_VALUE);
        pLiveRoomInfoRelativeLayout = (RelativeLayout) view.findViewById(R.id.rl_recorder_live_room_info);
        pLiveRoomInfoRelativeLayout.getBackground().setAlpha(ALPHHA_DEFAULT_VALUE); // 设置透明度*/

        iRecordingStatusButton.setOnClickListener(listener);
        iCameraSwitchButton.setOnClickListener(listener);
        iBeautyButton.setOnClickListener(listener);
        iflashStatusButton.setOnClickListener(listener);
        iVolumeSettingsButton.setOnClickListener(listener);
        iRecordCloseButton.setOnClickListener(listener);
        iRecordSettingsButton.setOnClickListener(listener);

        iPreviewVideoView = (TXCloudVideoView) view.findViewById(R.id.vv_preview);
        pHeadImgImageView = (ImageView) view.findViewById(R.id.iv_recorder_head_img);
        pLiveRoomName = (TextView) view.findViewById(R.id.tv_recorder_live_room_name);
        pOnlineCount = (TextView) view.findViewById(R.id.tv_recrder_online_count);

        Glide.with(getActivity()).load(LiveConstants.REMOTE_SERVER_HTTP_IP + mobileUserVo.getHeadImgUrl())
                .bitmapTransform(new CropCircleTransformation(getActivity()))
                .into(pHeadImgImageView); // 设置头像
        pLiveRoomName.setText(mobileUserVo.getLiveRoomVo().getRoomName());

        pBlockListLinearLayout = (LinearLayout) view.findViewById(R.id.ll_recorder_black_list);
        pBlockListLinearLayout.getBackground().setAlpha(150);
        pBlockListLinearLayout.setOnClickListener(new NoDoubleClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                DialogPlus dialog = DialogPlus.newDialog(getActivity()).setContentBackgroundResource(R.color.colorWhite)
                        .setContentHolder(new ViewHolder(R.layout.dialog_black_list))
                        .setContentHeight(650)
                        .setGravity(Gravity.CENTER)
                        .create();
                dialog.show();
                View dialogView = dialog.getHolderView();
                ListView blackList = (ListView) dialogView.findViewById(R.id.lv_black_list);
                getData(); // 获取数据
                SimpleAdapter adapter = new SimpleAdapter(getActivity(), data, R.layout.item_black_list, new String[]{"nickname"}, new int[]{R.id.tv_black_list_nickname});
                blackList.setAdapter(adapter);
            }
        }); // 黑名单按钮点击

        pLiveRoomInfoRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 liveRoomPresenter.loadAnchorInfoData(mobileUserVo.getUserId(), mobileUserVo.getLiveRoomVo().getRoomId()) ;
            }
        });
    }

    private void newHanderInstance() {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == HomeConstants.LOAD_ANCHOR_INFO_SUCCESS_FLAG) {   //加载主播信息成功
                    ResponseModel<AppAnchorInfo> dataModel = (ResponseModel) msg.obj ;
                    if(dataModel.getStatus() == 1) {
                        AppAnchorInfo info = dataModel.getData() ;
                        showAnchorInfoDialog(info) ;
                    }
                }
            }
        } ;
    }

    /**
     * 弹出主播信息的弹出框
     * @param info
     */
    private void showAnchorInfoDialog(AppAnchorInfo info) {
        if (anchorInfoDialogView == null)
            anchorInfoDialogView = new AnchorInfoDialogView(getActivity());
        anchorInfoDialogView.getReportView().setVisibility(View.GONE) ;     //隐藏举报
        anchorInfoDialogView.getAttentionHold().setVisibility(View.GONE) ;  //隐藏关注按钮
        anchorInfoDialogView.setValueAndShow(info) ;
    }


        /**
         * 获得数据
         */
    public void getData() {
        String[] nicknames = {"黑名单1", "黑名单2", "黑名单3", "黑名单1", "黑名单1", "黑名单1"};
        List<Map<String, Object>> data = new ArrayList<>();
        for (int i = 0; i < nicknames.length; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("nickname", nicknames[i]);
            data.add(item);
        }
        this.data = data;
    }

    /**
     * 显示直播暂停图标
     */
    @Override
    public void onShowPauseIconView() {
        iRecordingStatusButton.setIcon(MaterialDrawableBuilder.IconValue.PAUSE);
        isRecording = true;
    }

    /**
     * 显示直播开始图标
     */
    @Override
    public void onShowPlayIconView() {
        iRecordingStatusButton.setIcon(MaterialDrawableBuilder.IconValue.PLAY);
        isRecording = false;
    }

    /**
     * 显示提示信息
     *
     * @param msg
     * @param lengthType
     */
    @Override
    public void onShowToastMessage(String msg, Integer lengthType) {
        Toast.makeText(getActivity(), msg, lengthType).show();
    }

    /**
     * 显示闪光灯关闭图标
     */
    @Override
    public void onShowFlashOffIconView() {
        iflashStatusButton.setIcon(MaterialDrawableBuilder.IconValue.FLASH_OFF);
    }

    /**
     * 显示闪光灯开启图标
     */
    @Override
    public void onShowFlashIconView() {
        iflashStatusButton.setIcon(MaterialDrawableBuilder.IconValue.FLASH);
    }

    /**
     * 显示美颜设置对话框
     *
     * @param beauty
     * @param whitening
     */
    @Override
    public void onShowBeautyRangeBarView(Integer beauty, Integer whitening) {
        if (dialog != null) {
            dialog.dismiss();
        }
        dialog = DialogPlus.newDialog(getActivity()).setBackgroundColorResId(R.color.colorWall)
                .setContentHolder(new ViewHolder(R.layout.dialog_beauty_settings))
                .setExpanded(true, 375).setGravity(Gravity.BOTTOM)
                .create();
        dialog.show();
        initBeautySettingsRangeBar(dialog.getHolderView(), beauty, whitening); // 监听并初始化拉杆

    }

    /**
     * 获取直播预览视图控件
     *
     * @return
     */
    @Override
    public TXCloudVideoView getPreviewVideoView() {
        return iPreviewVideoView;
    }

    /**
     * 显示音量设置对话框
     *
     * @param microphone 麦克风音量
     * @param volume     背景音量
     */
    @Override
    public void onShowVolumeSettingsView(Float microphone, Float volume, boolean isVolumeOff) {
        if (dialog != null) {
            dialog.dismiss();
        }
        dialog = DialogPlus.newDialog(getActivity())
                .setBackgroundColorResId(R.color.colorWall)
                .setContentHolder(new ViewHolder(R.layout.dialog_volume_settings))
                .setExpanded(true, 550).setGravity(Gravity.BOTTOM)
                .create();
        dialog.show();
        initVolumeSettingsView(dialog.getHolderView(), microphone, volume, isVolumeOff);
    }

    @Override
    public void onRefreshVolumeOffSwitchVal(boolean isVolumeOff) {
        View currentDialogView = dialog.getHolderView();
        iVolumeOffSwitchButton = (SwitchButton) currentDialogView.findViewById(R.id.btn_volume_off_switch);
        iVolumeOffSwitchButton.setChecked(isVolumeOff);
    }

    @Override
    public void onShowPublishSettingsView(Map<String, Object> config) {
        if (dialog != null) {
            dialog.dismiss();
        }
        dialog = DialogPlus.newDialog(getActivity())
                .setBackgroundColorResId(R.color.colorWall)
                .setContentHolder(new ViewHolder(R.layout.dialog_publish_settings))
                .setExpanded(true, 350).setGravity(Gravity.BOTTOM)
                .create();
        dialog.show();
        initPublishSettingsView(dialog.getHolderView(), config);
    }

    /**
     * 刷新在线人数
     *
     * @param count
     */
    @Override
    public void refreshOnlineCount(int count) {

    }

    @Override
    public boolean onBackPressed() {
        if (isRecording) {
            onShowToastMessage("正在直播，请先关闭直播...", Toast.LENGTH_SHORT);
            return true;
        }
        return false;
    }


    /**
     * 图标按钮点击事件监听
     */
    private class IconButtonOnClickListener extends NoDoubleClickListener {
        @Override
        protected void onNoDoubleClick(View v) {
            switch (v.getId()) {
                case R.id.btn_recording_status:
                    if (NetworkUtils.isConnected(getActivity())) {
                        if (!isRecording) {
                            if (rtmpUrl != null) {
                                recorderPresenter.startPusher(rtmpUrl);
                            } else {
                                onShowToastMessage("地址出错！", Toast.LENGTH_SHORT);
                            }
                        } else {
                            recorderPresenter.stopRtmpPublish();
                        }
                    } else {
                        onShowToastMessage("网络无法连接", Toast.LENGTH_SHORT);
                    }
                    break;
                case R.id.btn_camera_switch:
                    isFrontCamera = recorderPresenter.switchCamera(isFrontCamera);
                    if (isFrontCamera && isFlashOn) {
                        isFlashOn = false;
                        onShowFlashIconView();
                    }
                    break;
                case R.id.btn_beauty:
                    recorderPresenter.getBeautyAndWhiteningVal();
                    break;
                case R.id.btn_flash_status:
                    // 闪光灯状态
                    isFlashOn = recorderPresenter.switchFlashLight(isFlashOn);
                    break;
                case R.id.btn_volume_settings:
                    recorderPresenter.getVolumeVal();
                    break;
                case R.id.btn_record_close:
                    if (isRecording) {
                        onShowToastMessage("正在直播，不能退出当前界面！", Toast.LENGTH_SHORT);
                    } else {
                        getActivity().finish();
                    }
                    break;
                case R.id.btn_record_settings:
                    recorderPresenter.getPushConfig();
                    break;
                default:
                    break;
            }
        }
    }


    /**
     * 初始化美颜相关拉杆控件和绑定拉杆滑动事件监听
     *
     * @param rangeBarView 拉杆所在对话框视图
     * @param beauty       磨皮级别
     * @param whitening    美白级别
     */
    private void initBeautySettingsRangeBar(View rangeBarView, Integer beauty, Integer whitening) {
        iBeautySettingRangeBar = (RangeBar) rangeBarView.findViewById(R.id.rb_beauty_setting);
        iWhiteningSettingRangeBar = (RangeBar) rangeBarView.findViewById(R.id.rb_whitening_setting);

        iBeautySettingRangeBar.setSeekPinByIndex(beauty); // 设置磨皮默认值
        iWhiteningSettingRangeBar.setSeekPinByIndex(whitening); // 设置美白默认值
        iBeautySettingRangeBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex, int rightPinIndex, String leftPinValue, String rightPinValue) {
                Integer beautyVal = Integer.parseInt(rightPinValue);
                recorderPresenter.setBeautyFilter(beautyVal, null); // 设置磨皮
            }
        });
        iWhiteningSettingRangeBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex, int rightPinIndex, String leftPinValue, String rightPinValue) {
                Integer whiteningVal = Integer.parseInt(rightPinValue);
                recorderPresenter.setBeautyFilter(null, whiteningVal);// 设置美白
            }
        });
    }

    /**
     * 初始化音量设置对话框并绑定拉杆滑动和滑动按钮滑动事件
     *
     * @param volumeSettingsView 音量设置所在的对话框视图
     * @param microPhone         麦克风音量
     * @param volume             背景音量
     */
    private void initVolumeSettingsView(View volumeSettingsView, Float microPhone, Float volume, boolean isVolumeOff) {
        iVolumeOffSwitchButton = (SwitchButton) volumeSettingsView.findViewById(R.id.btn_volume_off_switch);
        iMicroPhoneSettingRangeBar = (RangeBar) volumeSettingsView.findViewById(R.id.rb_microphone_setting);
        iVolumeSettingRangeBar = (RangeBar) volumeSettingsView.findViewById(R.id.rb_volume_setting);
        float microPhoneVal = microPhone;
        float volumeVal = volume;
        iMicroPhoneSettingRangeBar.setSeekPinByIndex((int) microPhoneVal); // 设置麦克风音量默认值
        iVolumeSettingRangeBar.setSeekPinByIndex((int) volumeVal);// 设置背景音量默认值
        iVolumeOffSwitchButton.setChecked(isVolumeOff);
        iMicroPhoneSettingRangeBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex, int rightPinIndex, String leftPinValue, String rightPinValue) {
                float microPhoneVal = Float.parseFloat(rightPinValue);
                recorderPresenter.setVolumeVal(microPhoneVal / 10, null); // 设置麦克风音量
            }
        });
        iVolumeSettingRangeBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex, int rightPinIndex, String leftPinValue, String rightPinValue) {
                float volumeVal = Float.parseFloat(rightPinValue);
                recorderPresenter.setVolumeVal(null, volumeVal / 10); // 设置背景音量
            }
        });
        iVolumeOffSwitchButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                recorderPresenter.setVolumeOff(isChecked); // 静音开关
            }
        });
    }

    /**
     * 初始化推流参数设置界面
     *
     * @param publishSettingsView
     * @param config
     */
    public void initPublishSettingsView(View publishSettingsView, Map<String, Object> config) {
        pDefinitionSpinner = (Spinner) publishSettingsView.findViewById(R.id.spin_pulish_definition);
        iTouchFocusSwitchButton = (SwitchButton) publishSettingsView.findViewById(R.id.btn_touch_focu_switch);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, pDefinitionInfos); // 建立Adapter并且绑定数据源
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        pDefinitionSpinner.setAdapter(adapter); //绑定 Adapter到控件

        iTouchFocusSwitchButton.setChecked((boolean) config.get(PublishConstant.CONFIG_TYPE_TOUCH_FOCUS)); // 设置手动对焦按钮状态
        pDefinitionSpinner.setSelection(mapperSpinnerSelectionPosition((Integer) config.get(PublishConstant.CONFIG_TYPE_VIDEO_QUALITY))); // 设置当前选项
        pDefinitionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                recorderPresenter.setPushConfig(PublishConstant.CONFIG_TYPE_VIDEO_QUALITY, mapperVideoQuality(pos)); // 设置视频清晰度
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        }); // 监听下拉框
        iTouchFocusSwitchButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                recorderPresenter.setPushConfig(PublishConstant.CONFIG_TYPE_TOUCH_FOCUS, isChecked); // 设置手动对焦
            }
        }); // 监听手动对焦按钮
    }

    /**
     * 视频清晰度对应下拉框位置
     *
     * @param videoQuality 视频清晰度
     * @return
     */
    private int mapperSpinnerSelectionPosition(int videoQuality) {
        int defalutPosition = 1;
        switch (videoQuality) {
            case TXLiveConstants.VIDEO_QUALITY_STANDARD_DEFINITION:
                defalutPosition = 0;
                break;
            case TXLiveConstants.VIDEO_QUALITY_HIGH_DEFINITION:
                defalutPosition = 1;
                break;
            case TXLiveConstants.VIDEO_QUALITY_SUPER_DEFINITION:
                defalutPosition = 2;
                break;
            default:
                break;
        }
        return defalutPosition;
    }

    /**
     * 下拉框位置对应的视频清晰度
     *
     * @param position 下拉框位置
     * @return
     */
    private int mapperVideoQuality(int position) {
        int definition = TXLiveConstants.VIDEO_QUALITY_HIGH_DEFINITION;
        switch (position) {
            case 0:
                definition = TXLiveConstants.VIDEO_QUALITY_STANDARD_DEFINITION;
                break;
            case 1:
                definition = TXLiveConstants.VIDEO_QUALITY_HIGH_DEFINITION;
                break;
            case 2:
                definition = TXLiveConstants.VIDEO_QUALITY_SUPER_DEFINITION;
                break;
            default:
                break;
        }
        return definition;
    }

    /**
     * 监听系统参数变化
     *
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        int mobileRotation = this.getActivity().getWindowManager().getDefaultDisplay().getRotation();
        recorderPresenter.onDisplayRotationChanged(mobileRotation);
    }

}
