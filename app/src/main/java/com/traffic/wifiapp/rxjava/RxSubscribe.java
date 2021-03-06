package com.traffic.wifiapp.rxjava;

import android.content.Context;
import android.text.TextUtils;

import com.traffic.wifiapp.retrofit.ServerException;
import com.traffic.wifiapp.utils.L;
import com.traffic.wifiapp.utils.NetUtil;

import cn.pedant.SweetAlert.SweetAlertDialog;
import rx.Subscriber;

/**
 * Created by Jam on 16-7-21
 * Description: 自定义Subscribe
 */
public abstract class RxSubscribe<T> extends Subscriber<T> {
    private static final String TAG="RxSubscribe";
    private Context mContext;
    private SweetAlertDialog dialog;
    private String msg;


    public RxSubscribe() {
    }

    protected boolean showDialog() {
        if(mContext==null|| TextUtils.isEmpty(msg))return false;
        return true;
    }

    /**
     * @param context context
     * @param msg     dialog message
     */
    public RxSubscribe(Context context, String msg) {
        this.mContext = context;
        this.msg = msg;
    }
    public RxSubscribe(Context context, int msg) {
        this.mContext = context;
        this.msg = mContext.getString(msg);
    }


    /**
     * @param context context
     */
    public RxSubscribe(Context context) {
        this(context, "请稍后...");
    }

    @Override
    public void onCompleted() {
        if (showDialog())
            dialog.dismiss();
    }
    @Override
    public void onStart() {
        super.onStart();
        if (showDialog()) {
            dialog = new SweetAlertDialog(mContext, SweetAlertDialog.PROGRESS_TYPE)
                    .setTitleText(msg);
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);
            //点击取消的时候取消订阅
            dialog.setOnCancelListener(dialog1 -> {
                onCancel();
            });
            dialog.show();
        }
    }
    @Override
    public void onNext(T t) {
        _onNext(t);
    }
    @Override
    public void onError(Throwable e) {
        e.printStackTrace();
        if (!NetUtil.checkNetWork()) { //这里自行替换判断网络的代码
            _onError("网络不可用");
        } else if (e instanceof ServerException) {
            _onError(e.getMessage());
        } else {
            _onError("请求失败，请稍后再试...");
            L.v(TAG, e.toString());
        }
        if (showDialog())
            dialog.dismiss();
    }

    protected abstract void _onNext(T t);

    protected abstract void _onError(String message);

    /**
     * 取消的时候，取消对observable的订阅，同时也取消了http请求
     */
    public void onCancel() {
        if (!this.isUnsubscribed()) {
            this.unsubscribe();
        }
    }
}
