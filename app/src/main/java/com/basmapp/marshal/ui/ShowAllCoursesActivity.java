package com.basmapp.marshal.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.basmapp.marshal.BaseActivity;
import com.basmapp.marshal.Constants;
import com.basmapp.marshal.R;
import com.basmapp.marshal.entities.Course;
import com.basmapp.marshal.ui.adapters.CoursesRecyclerAdapter;
import com.basmapp.marshal.util.ContentProvider;

import java.util.ArrayList;

public class ShowAllCoursesActivity extends BaseActivity {

    private BroadcastReceiver mAdaptersBroadcastReceiver;

    RecyclerView mRecyclerView;
    GridLayoutManager mGridLayoutManager;
    CoursesRecyclerAdapter mAdapter;
    Toolbar mToolbar;

    String mCoursesType;
    ArrayList<Course> mCourses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_show_all_courses);

        mCoursesType = getIntent().getStringExtra(Constants.EXTRA_COURSE_CATEGORY);
        mCourses = getIntent().getParcelableArrayListExtra(Constants.EXTRA_COURSES_LIST);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        if (mCoursesType != null && mToolbar != null)
            mToolbar.setTitle(mCoursesType);

        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        if (mCourses != null) {
            if (mCourses.size() > 0) {
                mRecyclerView = (RecyclerView) findViewById(R.id.tab_recycler_view);
                mGridLayoutManager = new GridLayoutManager(ShowAllCoursesActivity.this,
                        getResources().getInteger(R.integer.course_card_columns));
                mRecyclerView.setLayoutManager(mGridLayoutManager);
                mRecyclerView.setItemAnimator(new DefaultItemAnimator());
                mRecyclerView.setHasFixedSize(true);
                mAdapter = new CoursesRecyclerAdapter(ShowAllCoursesActivity.this, mCourses,
                        CoursesRecyclerAdapter.LAYOUT_TYPE_GRID);
                mRecyclerView.setAdapter(mAdapter);
            }
        }
        mAdaptersBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
//                mAdapter.notifyDataSetChanged();
                if (intent.getAction().equals(ContentProvider.Actions.COURSE_RATING_UPDATED)) {
                    Course course = intent.getParcelableExtra(ContentProvider.Extras.COURSE);
                    int itemPosition = ContentProvider.Utils.getCoursePositionInList(mCourses, course);

                    if (itemPosition > -1)
                        mAdapter.notifyItemChanged(itemPosition);
                } else if (intent.getAction().equals(ContentProvider.Actions.COURSE_SUBSCRIPTION_UPDATED)) {
                    Course course = intent.getParcelableExtra(ContentProvider.Extras.COURSE);
                    mCourses.get(ContentProvider.Utils.getCoursePositionInList(mCourses,
                            course)).setIsUserSubscribe(course.getIsUserSubscribe());
                }
            }
        };

//        registerReceiver(mAdaptersBroadcastReceiver, new IntentFilter(CoursesRecyclerAdapter.ACTION_ITEM_DATA_CHANGED));
        registerReceiver(mAdaptersBroadcastReceiver, new IntentFilter(ContentProvider.Actions.COURSE_RATING_UPDATED));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mAdaptersBroadcastReceiver);
    }
}
