package com.hnulab.sharebike.em.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.CycleInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.RelativeLayout;

import com.hnulab.sharebike.em.R;

import static razerdp.util.SimpleAnimUtil.getDefaultAlphaAnimation;

/**
 * Created by Administrator on 2017/4/24.
 */

public class RedpackageDialog extends DialogFragment {

    private static Activity activity;

    public static RedpackageDialog getInstance() {
        return FirstQuote.instance;
    }

    //在第一次被引用时被加载
    static class FirstQuote {
        private static RedpackageDialog instance = new RedpackageDialog();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(false);//点击背景不能Dialog不消失
        View view = inflater.inflate(R.layout.popup_redpackage, container);

        //设置红包抖动动画
        RelativeLayout relativeLayout = (RelativeLayout) view.findViewById(R.id.popup_anima);
        relativeLayout.setAnimation(setShowAnimation());
        return view;
    }

    private Animation setShowAnimation() {
        AnimationSet set=new AnimationSet(false);
        Animation shakeAnima=new RotateAnimation(0,15,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        shakeAnima.setInterpolator(new CycleInterpolator(5));
        shakeAnima.setDuration(600);
        set.addAnimation(getDefaultAlphaAnimation());
        set.addAnimation(shakeAnima);
        return set;
    }
}
