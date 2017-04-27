package com.csq.mvcdemo.controllers;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.csq.mvcdemo.R;
import com.csq.mvcdemo.events.EventTrackRecordInfoChanged;
import com.csq.mvcdemo.models.TrackRecordInfo;
import com.csq.mvcdemo.models.TrackRecordStatus;
import com.csq.mvcdemo.views.TrackCtrlView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class MainActivity extends ActionBarActivity implements TrackCtrlView.TrackCtrlViewListener {

    private TrackCtrlView trackCtrlView;
    private TrackRecordInfo trackRecordInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        trackCtrlView = new TrackCtrlView(this, this);

        EventBus.getDefault().register(this);

        // model
        trackRecordInfo = TrackRecordInfo.loadTrackRecordInfo(this);
        // View
        trackCtrlView.setTrackRecordInfo(trackRecordInfo);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }


    @Override
    public void trackStatusRequest(@Nullable TrackRecordStatus newStatus) {
        if (newStatus == TrackRecordStatus.Recording) {
            int trackId = 0;  //在数据库创建一条轨迹，并获取到数据库id
            trackRecordInfo = new TrackRecordInfo(trackId, TrackRecordStatus.Recording);

        } else if (newStatus == TrackRecordStatus.Paused) {
            if (trackRecordInfo != null) {
                trackRecordInfo.status = newStatus;
            }

        } else {
            trackRecordInfo = null;
        }
        Log.i("mvc", "control->model");
        TrackRecordInfo.changeTrackRecordInfo(this, trackRecordInfo);
    }

    // 这里获取到了Model传递过来的消息，通知View做出改变
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventTrackRecordInfoChanged event) {
        trackRecordInfo = event.info;
        trackCtrlView.setTrackRecordInfo(trackRecordInfo);
        Log.i("mvc", "onEventMainThread : model->view");
    }

}
