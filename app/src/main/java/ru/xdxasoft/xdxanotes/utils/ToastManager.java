package ru.xdxasoft.xdxanotes.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.Queue;

import ru.xdxasoft.xdxanotes.R;

public class ToastManager {

    private static final int MAX_TOASTS = 3;
    private static final Queue<View> toastQueue = new LinkedList<>();
    private static final Handler handler = new Handler(Looper.getMainLooper());
    private static LinearLayout toastContainer;

    public static void init(LinearLayout container) {
        toastContainer = container;
    }

    public static void showToast(Context context, String message, int iconResId, int backgroundColor, int textColor, int iconColor) {
        if (toastQueue.size() >= MAX_TOASTS) {

            View oldToast = toastQueue.poll();
            if (oldToast != null) {
                animateToastOut(oldToast);
                toastContainer.removeView(oldToast);
            }
        }

        View toastView = LayoutInflater.from(context).inflate(R.layout.custom_toast_layout, null);
        TextView toastMessage = toastView.findViewById(R.id.toastMessage);
        toastMessage.setText(message);
        toastMessage.setTextColor(textColor);

        ImageView toastIcon = toastView.findViewById(R.id.toastIcon);
        toastIcon.setImageResource(iconResId);

        toastIcon.setColorFilter(iconColor, android.graphics.PorterDuff.Mode.SRC_IN);

        GradientDrawable background = new GradientDrawable();
        background.setColor(backgroundColor);
        background.setCornerRadius(24f);

        toastView.setBackground(background);

        toastContainer.addView(toastView, 0);
        toastQueue.add(toastView);

        animateToastIn(toastView);

        if (toastContainer.getVisibility() == View.GONE) {
            toastContainer.setVisibility(View.VISIBLE);
        }

        setMargins(toastView, 0, 0, 0, 16);

        handler.postDelayed(() -> {
            animateToastOut(toastView);
        }, 3500);
    }




    private static void setMargins(View view, int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
        params.setMargins(left, top, right, bottom);
        view.setLayoutParams(params);
    }

    private static void animateToastIn(View toastView) {
        AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(750);
        toastView.startAnimation(animation);
    }

    private static void animateToastOut(View toastView) {
        AlphaAnimation animation = new AlphaAnimation(1.0f, 0.0f);
        animation.setDuration(750);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                toastContainer.removeView(toastView);
                toastQueue.remove(toastView);
                if (toastContainer.getChildCount() == 0) {
                    toastContainer.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        toastView.startAnimation(animation);
    }
}

