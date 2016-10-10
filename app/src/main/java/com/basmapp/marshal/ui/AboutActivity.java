package com.basmapp.marshal.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.basmapp.marshal.BaseActivity;
import com.basmapp.marshal.BuildConfig;
import com.basmapp.marshal.R;
import com.basmapp.marshal.util.LocaleUtils;
import com.basmapp.marshal.util.URLSpanNoUnderline;

public class AboutActivity extends BaseActivity {
    private static final String KEY_SCROLL_X = "KEY_SCROLL_X";
    private static final String KEY_SCROLL_Y = "KEY_SCROLL_Y";
    private ScrollView mScrollView;
    private int mTapCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about);

        mScrollView = (ScrollView) findViewById(R.id.about_scrollview);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.navigation_drawer_about);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        findViewById(R.id.logo_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTapCount == 7) {
                    Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinatorLayout), "Easter Egg!!! " + ("\ud83d\udc83"), Snackbar.LENGTH_LONG);
                    TextView textView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
                    textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    snackbar.show();
                    mTapCount = 0;
                }
                mTapCount++;
            }
        });

        ((TextView) findViewById(R.id.about_version_text)).setText(
                String.format(getString(R.string.about_version),
                        BuildConfig.VERSION_NAME,
                        Integer.toString(BuildConfig.VERSION_CODE)));

        TextView librariesTextView = (TextView) findViewById(R.id.activity_about_libraries);
        removeUnderlinesFromLinks(librariesTextView);

        TextView appLicenseTextView = (TextView) findViewById(R.id.about_app_license);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            appLicenseTextView.setText(Html.fromHtml(getString(R.string.about_app_license), Html.FROM_HTML_MODE_LEGACY));
        } else {
            //noinspection deprecation
            appLicenseTextView.setText(Html.fromHtml(getString(R.string.about_app_license)));
        }
        removeUnderlinesFromLinks(appLicenseTextView);

        findViewById(R.id.rate_app_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(android.content.Intent.ACTION_VIEW)
                        .setData(Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
            }
        });

        TextView aboutBasmachText = (TextView) findViewById(R.id.about_basmach);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            aboutBasmachText.setText(Html.fromHtml(getString(R.string.about_basmach), Html.FROM_HTML_MODE_LEGACY));
        } else {
            //noinspection deprecation
            aboutBasmachText.setText(Html.fromHtml(getString(R.string.about_basmach)));
        }
        removeUnderlinesFromLinks(aboutBasmachText);

        makeEverythingClickable((ViewGroup) findViewById(R.id.about_container));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_SCROLL_X, mScrollView.getScrollX());
        outState.putInt(KEY_SCROLL_Y, mScrollView.getScrollY());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        final int x = savedInstanceState.getInt(KEY_SCROLL_X);
        final int y = savedInstanceState.getInt(KEY_SCROLL_Y);
        mScrollView.post(new Runnable() {
            @Override
            public void run() {
                mScrollView.scrollTo(x, y);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            if (LocaleUtils.isRtl(getResources())) {
                overridePendingTransition(R.anim.activity_close_enter,
                        R.anim.activity_close_exit_rtl);
            } else {
                overridePendingTransition(R.anim.activity_close_enter,
                        R.anim.activity_close_exit);
            }
        }
    }

    public static void removeUnderlinesFromLinks(@NonNull TextView textView) {
        CharSequence text = textView.getText();
        if (text instanceof Spanned) {
            Spannable spannable = new SpannableString(text);
            removeUnderlinesFromLinks(spannable, spannable.getSpans(0, spannable.length(), URLSpan.class));
            textView.setText(spannable);
        }
    }

    public static void removeUnderlinesFromLinks(@NonNull Spannable spannable,
                                                 @NonNull URLSpan[] spans) {
        for (URLSpan span : spans) {
            int start = spannable.getSpanStart(span);
            int end = spannable.getSpanEnd(span);
            spannable.removeSpan(span);
            span = new URLSpanNoUnderline(span.getURL());
            spannable.setSpan(span, start, end, 0);
        }
    }

    private void makeEverythingClickable(ViewGroup vg) {
        for (int i = 0; i < vg.getChildCount(); i++) {
            if (vg.getChildAt(i) instanceof ViewGroup) {
                makeEverythingClickable((ViewGroup) vg.getChildAt(i));
            } else if (vg.getChildAt(i) instanceof TextView) {
                TextView tv = (TextView) vg.getChildAt(i);
                tv.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }
    }
}
