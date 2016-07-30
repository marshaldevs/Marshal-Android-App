package com.basmach.marshal.ui;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.basmach.marshal.BuildConfig;
import com.basmach.marshal.R;
import com.basmach.marshal.ui.utils.LocaleUtils;
import com.basmach.marshal.ui.utils.ThemeUtils;

public class AboutActivity extends AppCompatActivity {
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.updateTheme(this);
        super.onCreate(savedInstanceState);
        LocaleUtils.updateLocale(this);
        setContentView(R.layout.activity_about);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.navigation_drawer_about);
        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        TextView aboutVersion = (TextView) findViewById(R.id.about_version_text);
        aboutVersion.setText(BuildConfig.VERSION_NAME);

        TextView aboutLibraries = (TextView) findViewById(R.id.activity_about_libraries);
        aboutLibraries.setMovementMethod(LinkMovementMethod.getInstance());

        Button aboutRate = (Button) findViewById(R.id.rate_app_button);
        aboutRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=" + getPackageName())));
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
                }
            }
        });

        TextView aboutLicense = (TextView)findViewById(R.id.about_app_license);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            aboutLicense.setText(Html.fromHtml(getString(R.string.about_app_license), Html.FROM_HTML_MODE_LEGACY));
        } else {
            aboutLicense.setText(Html.fromHtml(getString(R.string.about_app_license)));
        }
        aboutLicense.setMovementMethod(LinkMovementMethod.getInstance());

        TextView aboutBasmach = (TextView)findViewById(R.id.about_basmach);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            aboutBasmach.setText(Html.fromHtml(getString(R.string.about_basmach), Html.FROM_HTML_MODE_LEGACY));
        } else {
            aboutBasmach.setText(Html.fromHtml(getString(R.string.about_basmach)));
        }
        aboutBasmach.setMovementMethod(LinkMovementMethod.getInstance());
    }

}