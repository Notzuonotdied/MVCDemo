# 博文地址：http://blog.csdn.net/notzuonotdied/article/details/70876654

# 前言

　　这篇博文转载修改自：[简述MVC框架模式以及在你（Android）项目中的应用](http://www.cnblogs.com/John-Chen/p/4458823.html)

----

>　　标题是阿里电话面试的问题，一直以为自己很清楚MVC模式，结果被问到时，居然没法将MVC和Android中各个组件对应起来，所以，面试肯定挂了，不过面试也是学习的一种方式，可以知道大公司看中什么，以及自己还有哪些知识漏洞，例如这次面试就学到了很多东西。
>　　大家可以在看下面的内容之前，也想想能否把MVC及与Android各个组件的对应关系讲清楚，看是否还有和我一样对MVC一知半解的。
>　　如果写的有问题的地方，欢迎讨论。转载请注明出处：http://www.cnblogs.com/John-Chen/p/4458823.html

　　学习中读了很多别人总结的文章，有几篇不错，推荐给大家：　

　　前端之Android入门(3):MVC模式：
　　http://isux.tencent.com/learn-android-from-zero-session3.html
　　http://isux.tencent.com/learn-android-from-zero-session4.html
　　http://isux.tencent.com/learn-android-from-zero-session5-html.html

　　The Activity Revisited:
　　http://www.therealjoshua.com/2012/07/android-architecture-part-10-the-activity-revisited/

　　谈谈UI架构设计的演化：
　　http://www.cnblogs.com/winter-cn/p/4285171.html

　　MVC，MVP 和 MVVM 的图示：
　　http://www.ruanyifeng.com/blog/2015/02/mvcmvp_mvvm.html

　　关于另一种框架模式MVP的实践：

　　http://www.imooc.com/wenda/detail/216700

# MVC
 
　　重新学习思考之后，再看自己项目中的某些实现，其实很多地方已经是遵循**MVC**的思想在实现，只是在设计和实现时，没提升到框架模式，只是根据以前的经验，以及一些基本的设计思想在做，所以被问到MVC模式时，也没想到项目中有用到的地方。我觉得不管了不了解什么框架模式以及设计模式，最主要的是得想办法做到解耦以及提升应用的稳定性。

　　首先说下我现在认识的**MVC**与Android的各个组件的对应关系：

　　**View**：自定义View或ViewGroup，负责将用户的请求通知Controller，并根据model更新界面；

　　**Controller**：Activity或者Fragment，接收用户请求并更新model；

　　**Model**：数据模型，负责数据处理相关的逻辑，封装应用程序状态，响应状态查询，通知View改变，对应Android中的datebase、SharePreference等。

　　![这里写图片描述](http://img.blog.csdn.net/20170427192224663?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvTm90enVvbm90ZGllZA==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

 - 备注：Android中，model通知view改变，可以通过消息总线实现。
 
 ----
 
　　下面以我做的项目中的一个模块来详细介绍Android中的MVC框架模式：

　　项目中有一个记录轨迹的功能，记录有几种状态：记录、暂停、停止：

　　![这里写图片描述](http://img.blog.csdn.net/20170427192013449?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvTm90enVvbm90ZGllZA==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

　　![这里写图片描述](http://img.blog.csdn.net/20170427192036317?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvTm90enVvbm90ZGllZA==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

　　接下来我把轨迹控制部分的逻辑提取出来，做了一个简单地demo，
　　![这里写图片描述](http://img.blog.csdn.net/20170427193459604?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvTm90enVvbm90ZGllZA==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
　　
　　Log如下：
``` Java
04-27 19:35:30.046 17066-17066/com.csq.mvcdemo I/mvc: view->control
04-27 19:35:30.056 17066-17066/com.csq.mvcdemo I/mvc: control->model
04-27 19:35:30.066 17066-17066/com.csq.mvcdemo I/mvc: model->view
04-27 19:35:30.076 17066-17066/com.csq.mvcdemo I/mvc: onEventMainThread : model->view
```
	
　　demo源码地址：https://github.com/John-Chen/BlogSamples/tree/master/MVCDemo

# 说明

　　该Demo实现MVC模式采用了EventBus来进行消息的接收和发送。
　　使用之前需要：（**备注**：EventBus的具体使用教程在下面的Github地址中有）
``` Gradle
compile 'org.greenrobot:eventbus:3.0.0'
```
　　EventBus的Github地址：[点我跳转～O(∩_∩)O哈哈~](https://github.com/greenrobot/EventBus)

# View实现

　　View层主要负责View控件的事件的绑定和View的重绘工作。
``` Java
package com.csq.mvcdemo.views;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.csq.mvcdemo.R;
import com.csq.mvcdemo.models.TrackRecordInfo;
import com.csq.mvcdemo.models.TrackRecordStatus;

/**
 * description : 轨迹控制View相关逻辑
 * Created by csq E-mail:csqwyyx@163.com
 * 15-4-26
 */
public class TrackCtrlView implements View.OnClickListener{

    // ------------------------ Constants ------------------------


    // ------------------------- Fields --------------------------

    private ImageView btnStartTrack, btnStopTrack, btnPauseTrack;
    private TrackCtrlViewListener listener;
    private TrackRecordInfo trackRecordInfo;


    // ----------------------- Constructors ----------------------

    /**
     * 实例化
     *
     * @param activity the activity
     * @param listener the listener
     */
    public TrackCtrlView(Activity activity, TrackCtrlViewListener listener){
        this.listener = listener;
        btnStartTrack = (ImageView) activity.findViewById(R.id.btnStartTrack);
        btnStopTrack = (ImageView) activity.findViewById(R.id.btnStopTrack);
        btnPauseTrack = (ImageView) activity.findViewById(R.id.btnPauseTrack);
        btnStartTrack.setOnClickListener(this);
        btnStopTrack.setOnClickListener(this);
        btnPauseTrack.setOnClickListener(this);
        btnPauseTrack.setOnClickListener(this);
    }


    // -------- Methods for/from SuperClass/Interfaces -----------

    /**
     * 将用户请求通知Controller
     */
    @Override
    public void onClick(View v) {
        Log.i("mvc", "view->control");
        switch(v.getId()){
            case R.id.btnStartTrack:
                if(listener != null){
                    listener.trackStatusRequest(TrackRecordStatus.Recording);
                }
                break;

            case R.id.btnStopTrack:
                if(listener != null){
                    listener.trackStatusRequest(TrackRecordStatus.Stoped);
                }
                break;

            case R.id.btnPauseTrack:
                if(listener != null){
                    if(trackRecordInfo.status == TrackRecordStatus.Paused){
                        listener.trackStatusRequest(TrackRecordStatus.Recording);
                    }else{
                        listener.trackStatusRequest(TrackRecordStatus.Paused);
                    }
                }
                break;

            default:

                break;
        }
    }

    // --------------------- Methods public ----------------------


    // --------------------- Methods private ---------------------
    /**
     * 更新View
     * */
    private void refreshView(){
        TrackRecordStatus trackStatus = trackRecordInfo == null ?
                TrackRecordStatus.Stoped : trackRecordInfo.status;
        if (trackStatus == TrackRecordStatus.Recording) {
            btnStartTrack.setVisibility(View.GONE);
            btnPauseTrack.setVisibility(View.VISIBLE);
            btnStopTrack.setVisibility(View.VISIBLE);
            btnPauseTrack.setImageResource(R.drawable.btn_track_ctrl_pause);

        } else if (trackStatus == TrackRecordStatus.Paused) {
            btnStartTrack.setVisibility(View.GONE);
            btnPauseTrack.setVisibility(View.VISIBLE);
            btnStopTrack.setVisibility(View.VISIBLE);
            btnPauseTrack.setImageResource(R.drawable.btn_track_ctrl_resume);

        } else {
            // TrackRecordStatus.Stoped
            btnStartTrack.setVisibility(View.VISIBLE);
            btnPauseTrack.setVisibility(View.GONE);
            btnStopTrack.setVisibility(View.GONE);
        }
    }

    // --------------------- Getter & Setter -----------------
    /**
     * 用于通知View更新
     *
     * @param trackRecordInfo the trackRecordInfo
     * */
    public void setTrackRecordInfo(@Nullable TrackRecordInfo trackRecordInfo) {
        this.trackRecordInfo = trackRecordInfo;
        refreshView();
    }


    // --------------- Inner and Anonymous Classes ---------------

    /**
     * The interface Track ctrl view listener.
     */
    public interface TrackCtrlViewListener{
        /**
         * 用户点击按钮
         */
        public void trackStatusRequest(@Nullable TrackRecordStatus newStatus);
    }

    // --------------------- logical fragments -----------------

}
```

# Model

　　Modle层用于数据的获取（网络连接）和数据的传递工作。
``` Java
/**
 * description : 轨迹记录信息
 * Created by csq E-mail:csqwyyx@163.com
 * 15-4-26
 */
package com.csq.mvcdemo.models;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.csq.mvcdemo.events.EventTrackRecordInfoChanged;
import com.csq.mvcdemo.utils.SpUtil;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

public class TrackRecordInfo {

    // ------------------------ Constants ------------------------


    // ------------------------- Fields --------------------------

    private static final Gson gson = new Gson();

    /**
     * 应该是保存轨迹数据库id，此demo中数据库操作不实现，暂时trackId一直为0
     */
    public int trackId;

    public TrackRecordStatus status;

    // ----------------------- Constructors ----------------------

    public TrackRecordInfo(int trackId, TrackRecordStatus status) {
        this.trackId = trackId;
        this.status = status;
    }


    // -------- Methods for/from SuperClass/Interfaces -----------


    // --------------------- Methods public ----------------------

    @NonNull
    public static TrackRecordInfo loadTrackRecordInfo(@NonNull Context context) {
        String pref = SpUtil.getString(context, SpUtil.KEY_TRACK_RECORD_INFO, "");
        if (!TextUtils.isEmpty(pref)) {
            return gson.fromJson(pref, TrackRecordInfo.class);
        }
        return null;
    }

    public static void changeTrackRecordInfo(@NonNull Context context, @Nullable TrackRecordInfo info) {
        SpUtil.saveString(context,
                SpUtil.KEY_TRACK_RECORD_INFO,
                info == null ? "" : gson.toJson(info));

        //model通过消息总线，通知View刷新
        Log.i("mvc", "model->view");
        EventBus.getDefault().post(new EventTrackRecordInfoChanged(info));
    }

    // --------------------- Methods private ---------------------


    // --------------------- Getter & Setter -----------------


    // --------------- Inner and Anonymous Classes ---------------


    // --------------------- logical fragments -----------------

}
```

# Controller

　　主要用于处理业务逻辑。
``` Java
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
```


