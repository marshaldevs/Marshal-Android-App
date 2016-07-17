package com.basmach.marshal.ui.fragments;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.basmach.marshal.entities.Cycle;
import com.basmach.marshal.utils.DateHelper;
import com.lapism.searchview.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.basmach.marshal.R;
import com.basmach.marshal.entities.Course;
import com.basmach.marshal.localdb.DBConstants;
import com.basmach.marshal.ui.MainActivity;
import com.basmach.marshal.ui.ShowAllCoursesActivity;
import com.basmach.marshal.ui.adapters.CoursesRecyclerAdapter;
import com.basmach.marshal.ui.utils.AutoScrollViewPager;
import com.basmach.marshal.ui.utils.InkPageIndicator;
import com.basmach.marshal.ui.adapters.ViewPagerAdapter;

import java.lang.reflect.Field;
import java.util.ArrayList;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

public class CoursesFragment extends Fragment {
    private static final String EXTRA_COURSES_LIST = "extra_courses_list";
    private static final String EXTRA_LAST_VIEWPAGER_POSITION = "extra_last_viewpager_position";
    private static final String DRAWER_SHOWCASE_ID = "navigation_drawer_tutorial";
    private static final String SEARCH_SHOWCASE_ID = "search_tutorial";

    public static ArrayList<Course> mCoursesList = null;
    private ArrayList<Course> mViewPagerCourses = null;

    public static ArrayList<Course> mSoftwareCourses = null;
    public static ArrayList<Course> mCyberCourses = null;
    public static ArrayList<Course> mITCourses = null;
    public static ArrayList<Course> mToolsCourses = null;
    public static ArrayList<Course> mSystemCourses = null;

    private BroadcastReceiver mAdaptersBroadcastReceiver;

    private InkPageIndicator mInkPageIndicator;
    private AutoScrollViewPager mViewPager;
    private View mViewPagerOverlay;
    private LinearLayout mViewPagerTitle;

    private RecyclerView mRecyclerSoftware;
    private LinearLayoutManager mLinearLayoutManagerSoftware;
    private CoursesRecyclerAdapter mRecyclerAdapterSoftware;
    private LinearLayout mBtnShowAllSoftware;

    private RecyclerView mRecyclerCyber;
    private LinearLayoutManager mLinearLayoutManagerCyber;
    private CoursesRecyclerAdapter mRecyclerAdapterCyber;
    private LinearLayout mBtnShowAllCyber;

    private RecyclerView mRecyclerIT;
    private LinearLayoutManager mLinearLayoutManagerIT;
    private CoursesRecyclerAdapter mRecyclerAdapterIT;
    private LinearLayout mBtnShowAllIT;

    private RecyclerView mRecyclerTools;
    private LinearLayoutManager mLinearLayoutManagerTools;
    private CoursesRecyclerAdapter mRecyclerAdapterTools;
    private LinearLayout mBtnShowAllTools;

    private RecyclerView mRecyclerSystem;
    private LinearLayoutManager mLinearLayoutManagerSystem;
    private CoursesRecyclerAdapter mRecyclerAdapterSystem;
    private LinearLayout mBtnShowAllSystem;

    private View mRootView;

    private SearchView mSearchView;
    private TextView mViewPagerTitleTextView;
    private TextView mViewPagerSubtitleTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_courses, container, false);
        mCoursesList = null;
        mCyberCourses = null;
        mSoftwareCourses = null;
        mITCourses = null;
        mToolsCourses = null;
        mSystemCourses = null;

        setHasOptionsMenu(true);

        mSearchView = ((MainActivity)getActivity()).getSearchView(true, true);

        mViewPager = (AutoScrollViewPager) mRootView.findViewById(R.id.main_catalog_view_pager);

        if (savedInstanceState != null && mCoursesList == null) {
            mCoursesList = savedInstanceState.getParcelableArrayList(EXTRA_COURSES_LIST);
        }

        if (mCoursesList == null || mViewPagerCourses == null) {
            mViewPagerCourses = new ArrayList<>();
            mCoursesList = new ArrayList<>();

            new AsyncTask<Void, Void, Boolean>() {

                ProgressDialog progressDialog;

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    progressDialog = new ProgressDialog(getActivity());
                    progressDialog.setMessage(getActivity().getResources().getString(R.string.loading));
                    progressDialog.setCancelable(false);
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                }

                @Override
                protected Boolean doInBackground(Void... voids) {
                    try {
                        if (MainActivity.sAllCourses == null) {
                            mCoursesList = (ArrayList) Course.getAllByColumn(DBConstants.COL_IS_MEETUP,
                                    false, DBConstants.COL_ID, getActivity(), Course.class);
                            MainActivity.sAllCourses = mCoursesList;
                        } else {
                            mCoursesList = MainActivity.sAllCourses;
                        }

                        if (MainActivity.sViewPagerCourses == null) {
                            mViewPagerCourses = (ArrayList) Course.rawQuery(getActivity(),
                                    Course.getCloestCoursesSqlQuery(5, true), Course.class);

                            if (mViewPagerCourses == null || mViewPagerCourses.size() == 0)
                                mViewPagerCourses = (ArrayList) Course.rawQuery(getActivity(),
                                        Course.getCloestCoursesSqlQuery(5, false), Course.class);

                            MainActivity.sViewPagerCourses = mViewPagerCourses;
                        } else {
                            mViewPagerCourses = MainActivity.sViewPagerCourses;
                        }

                        if (mCoursesList.size() > 0) {
                            return true;
                        } else {
                            return false;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }

                @Override
                protected void onPostExecute(Boolean result) {
                    super.onPostExecute(result);
                    if (result) {
                        filterData();
                        showImagesViewPager();
                        showData();
                        initializeTutorial();
                    }

                    progressDialog.dismiss();
                }
            }.execute();

        } else {
            showImagesViewPager();
            showData();
            initializeTutorial();
        }

        mAdaptersBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mRecyclerAdapterCyber.notifyDataSetChanged();
                mRecyclerAdapterIT.notifyDataSetChanged();
                mRecyclerAdapterSoftware.notifyDataSetChanged();
                mRecyclerAdapterTools.notifyDataSetChanged();
                mRecyclerAdapterSystem.notifyDataSetChanged();
            }
        };

        getActivity().registerReceiver(mAdaptersBroadcastReceiver, new IntentFilter(CoursesRecyclerAdapter.ACTION_ITEM_DATA_CHANGED));

        return mRootView;
    }

    private void filterData() {
        if (mCyberCourses == null ||
                mITCourses == null ||
                mSoftwareCourses == null ||
                mToolsCourses == null ||
                mSystemCourses == null) {

            mCyberCourses = new ArrayList<>();
            mITCourses = new ArrayList<>();
            mSoftwareCourses = new ArrayList<>();
            mToolsCourses = new ArrayList<>();
            mSystemCourses = new ArrayList<>();

            for(Course course : mCoursesList) {
                if (course.getCategory() != null) {
                    switch (course.getCategory()) {
                        case Course.CATEGORY_SOFTWARE:
                            mSoftwareCourses.add(course);
                            break;
                        case Course.CATEGORY_CYBER:
                            mCyberCourses.add(course);
                            break;
                        case Course.CATEGORY_IT:
                            mITCourses.add(course);
                            break;
                        case Course.CATEGORY_TOOLS:
                            mToolsCourses.add(course);
                            break;
                        case Course.CATEGORY_SYSTEM:
                            mSystemCourses.add(course);
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mAdaptersBroadcastReceiver);
    }

    @Override
    public void onPause() {
        super.onPause();
        MainActivity.sLastCoursesViewPagerIndex = mViewPager.getCurrentItem();
        // stop auto scroll when onPause
        mViewPager.stopAutoScroll();
    }

    @Override
    public void onResume() {
        super.onResume();
        // start auto scroll when onResume
        mViewPager.startAutoScroll();
    }

    private void showImagesViewPager() {
        mViewPagerTitle = (LinearLayout) mRootView.findViewById(R.id.highlight_overlay_title);
        mViewPagerTitle.setVisibility(View.VISIBLE);
        mViewPagerTitleTextView = (TextView) mRootView.findViewById(R.id.viewpager_title);
        mViewPagerSubtitleTextView = (TextView) mRootView.findViewById(R.id.viewpager_subtitle);
        ViewPagerAdapter mViewPagerAdapter = new ViewPagerAdapter(getActivity(), mViewPagerCourses,
                mViewPagerTitleTextView, mViewPagerSubtitleTextView);
        mViewPager.setVisibility(View.VISIBLE);
        mViewPager.setAdapter(mViewPagerAdapter);
        mInkPageIndicator = (InkPageIndicator) mRootView.findViewById(R.id.main_catalog_indicator);
        mInkPageIndicator.setVisibility(View.VISIBLE);
        mViewPagerOverlay = mRootView.findViewById(R.id.gradient_overlay);
        mViewPagerOverlay.setVisibility(View.VISIBLE);
        mViewPager.setOffscreenPageLimit(5);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                mViewPagerTitleTextView.setText(mViewPagerCourses.get(position).getName());

                Cycle firstCycle = mViewPagerCourses.get(position).getFirstCycle();
                String sSubtitle = DateHelper.dateToString(firstCycle.getStartDate()) + " - " +
                        DateHelper.dateToString(firstCycle.getEndDate());
                mViewPagerSubtitleTextView.setText(sSubtitle);
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mViewPager.setCurrentItem(MainActivity.sLastCoursesViewPagerIndex);
        mInkPageIndicator.setViewPager(mViewPager);
        mViewPager.setInterval(5000);
        mViewPager.startAutoScroll();
    }

    private void showData() {
        initializeSoftwareComponents();
        initializeCyberComponents();
        initializeITComponents();
        initializeToolsComponents();
        initializeSystemComponents();
    }

    private void initializeTutorial() {
        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500); // half second between each showcase view

        final MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(getActivity());
        sequence.setConfig(config);

        try {
            Toolbar toolbar = null;
            if (getActivity() != null) {
                toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
            }
            Field navButtonField = Toolbar.class.getDeclaredField("mNavButtonView");
            navButtonField.setAccessible(true);
            View navigationView = null;
            if (toolbar != null) {
                navigationView = (View) navButtonField.get(toolbar);
            }
            if (navigationView != null) {
                sequence.addSequenceItem(
                        new MaterialShowcaseView.Builder(getActivity())
                                .setTarget(navigationView)
                                .setDismissText(R.string.got_it)
                                .setDismissOnTouch(false)
                                .setDismissOnTargetTouch(true)
                                .setTargetTouchable(false)
                                .setTitleText(R.string.navigation_drawer_tutorial_description)
//                                .setMaskColour(Color.argb(210, 0, 0, 0))
                                .singleUse(DRAWER_SHOWCASE_ID) // provide a unique ID used to ensure it is only shown once
                                .build()
                );
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                View searchView = null;
                if (getActivity() != null) {
                    searchView = getActivity().findViewById(R.id.menu_main_searchView);
                }
                if (searchView != null) {
                    sequence.addSequenceItem(
                            new MaterialShowcaseView.Builder(getActivity())
                                    .setTarget(searchView)
                                    .setDismissText(R.string.got_it)
                                    .setDismissOnTouch(false)
                                    .setDismissOnTargetTouch(true)
                                    .setTargetTouchable(false)
                                    .setTitleText(R.string.search_tutorial_description)
//                                    .setMaskColour(Color.argb(210, 0, 0, 0))
                                    .setShapePadding(24)
                                    .singleUse(SEARCH_SHOWCASE_ID) // provide a unique ID used to ensure it is only shown once
                                    .build()
                    );
                }
            }
        });

        sequence.start();
    }

    private void initializeSoftwareComponents() {
        mBtnShowAllSoftware = (LinearLayout) mRootView.findViewById(R.id.fragment_courses_software_see_all);
        mBtnShowAllSoftware.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ShowAllCoursesActivity.class);
                intent.putParcelableArrayListExtra(ShowAllCoursesActivity.EXTRA_COURSES_LIST,mSoftwareCourses);
                intent.putExtra(ShowAllCoursesActivity.EXTRA_COURSE_TYPE, getResources().getString(R.string.course_type_software));
                startActivityForResult(intent, MainActivity.RC_SHOW_ALL_ACTIVITY);
            }
        });
        mRecyclerSoftware = (RecyclerView) mRootView.findViewById(R.id.fragment_courses_software_recyclerView);
        mLinearLayoutManagerSoftware = new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
        mRecyclerSoftware.setLayoutManager(mLinearLayoutManagerSoftware);
        mRecyclerAdapterSoftware = new CoursesRecyclerAdapter(getActivity(),mSoftwareCourses,
                CoursesRecyclerAdapter.LAYOUT_TYPE_LIST);
        mRecyclerSoftware.setItemAnimator(new DefaultItemAnimator());
        mRecyclerSoftware.setAdapter(mRecyclerAdapterSoftware);
        mRootView.findViewById(R.id.fragment_courses_software_see_all).setVisibility(View.VISIBLE);
        mRootView.findViewById(R.id.fragment_courses_software_recyclerView).setVisibility(View.VISIBLE);
    }

    private void initializeCyberComponents() {
        mBtnShowAllCyber = (LinearLayout) mRootView.findViewById(R.id.fragment_courses_cyber_see_all);
        mBtnShowAllCyber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ShowAllCoursesActivity.class);
                intent.putParcelableArrayListExtra(ShowAllCoursesActivity.EXTRA_COURSES_LIST,mCyberCourses);
                intent.putExtra(ShowAllCoursesActivity.EXTRA_COURSE_TYPE, getResources().getString(R.string.course_type_cyber));
                startActivityForResult(intent, MainActivity.RC_SHOW_ALL_ACTIVITY);
            }
        });
        mRecyclerCyber = (RecyclerView) mRootView.findViewById(R.id.fragment_courses_cyber_recyclerView);
        mLinearLayoutManagerCyber = new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
        mRecyclerCyber.setLayoutManager(mLinearLayoutManagerCyber);
        mRecyclerAdapterCyber = new CoursesRecyclerAdapter(getActivity(),mCyberCourses,
                CoursesRecyclerAdapter.LAYOUT_TYPE_LIST);
        mRecyclerCyber.setItemAnimator(new DefaultItemAnimator());
        mRecyclerCyber.setAdapter(mRecyclerAdapterCyber);
        mRootView.findViewById(R.id.fragment_courses_cyber_see_all).setVisibility(View.VISIBLE);
        mRootView.findViewById(R.id.fragment_courses_cyber_recyclerView).setVisibility(View.VISIBLE);
    }

    private void initializeITComponents() {
        mBtnShowAllIT = (LinearLayout) mRootView.findViewById(R.id.fragment_courses_it_see_all);
        mBtnShowAllIT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ShowAllCoursesActivity.class);
                intent.putParcelableArrayListExtra(ShowAllCoursesActivity.EXTRA_COURSES_LIST, mITCourses);
                intent.putExtra(ShowAllCoursesActivity.EXTRA_COURSE_TYPE, getResources().getString(R.string.course_type_it));
                startActivityForResult(intent, MainActivity.RC_SHOW_ALL_ACTIVITY);
            }
        });
        mRecyclerIT = (RecyclerView) mRootView.findViewById(R.id.fragment_courses_it_recyclerView);
        mLinearLayoutManagerIT = new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
        mRecyclerIT.setLayoutManager(mLinearLayoutManagerIT);
        mRecyclerAdapterIT = new CoursesRecyclerAdapter(getActivity(),mITCourses,
                CoursesRecyclerAdapter.LAYOUT_TYPE_LIST);
        mRecyclerIT.setItemAnimator(new DefaultItemAnimator());
        mRecyclerIT.setAdapter(mRecyclerAdapterIT);
        mRootView.findViewById(R.id.fragment_courses_it_see_all).setVisibility(View.VISIBLE);
        mRootView.findViewById(R.id.fragment_courses_it_recyclerView).setVisibility(View.VISIBLE);
    }

    private void initializeToolsComponents() {
        mBtnShowAllTools = (LinearLayout) mRootView.findViewById(R.id.fragment_courses_tools_see_all);
        mBtnShowAllTools.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ShowAllCoursesActivity.class);
                intent.putParcelableArrayListExtra(ShowAllCoursesActivity.EXTRA_COURSES_LIST, mToolsCourses);
                intent.putExtra(ShowAllCoursesActivity.EXTRA_COURSE_TYPE, getResources().getString(R.string.course_type_tools));
                startActivityForResult(intent, MainActivity.RC_SHOW_ALL_ACTIVITY);
            }
        });
        mRecyclerTools = (RecyclerView) mRootView.findViewById(R.id.fragment_courses_tools_recyclerView);
        mLinearLayoutManagerTools = new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
        mRecyclerTools.setLayoutManager(mLinearLayoutManagerTools);
        mRecyclerAdapterTools = new CoursesRecyclerAdapter(getActivity(),mToolsCourses,
                CoursesRecyclerAdapter.LAYOUT_TYPE_LIST);
        mRecyclerTools.setItemAnimator(new DefaultItemAnimator());
        mRecyclerTools.setAdapter(mRecyclerAdapterTools);
        mRootView.findViewById(R.id.fragment_courses_tools_see_all).setVisibility(View.VISIBLE);
        mRootView.findViewById(R.id.fragment_courses_tools_recyclerView).setVisibility(View.VISIBLE);
    }

    private void initializeSystemComponents() {
        mBtnShowAllSystem = (LinearLayout) mRootView.findViewById(R.id.fragment_courses_system_see_all);
        mBtnShowAllSystem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ShowAllCoursesActivity.class);
                intent.putParcelableArrayListExtra(ShowAllCoursesActivity.EXTRA_COURSES_LIST, mSystemCourses);
                intent.putExtra(ShowAllCoursesActivity.EXTRA_COURSE_TYPE, getResources().getString(R.string.course_type_system));
                startActivityForResult(intent, MainActivity.RC_SHOW_ALL_ACTIVITY);
            }
        });
        mRecyclerSystem = (RecyclerView) mRootView.findViewById(R.id.fragment_courses_system_recyclerView);
        mLinearLayoutManagerSystem = new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
        mRecyclerSystem.setLayoutManager(mLinearLayoutManagerSystem);
        mRecyclerAdapterSystem = new CoursesRecyclerAdapter(getActivity(),mSystemCourses,
                CoursesRecyclerAdapter.LAYOUT_TYPE_LIST);
        mRecyclerSystem.setItemAnimator(new DefaultItemAnimator());
        mRecyclerSystem.setAdapter(mRecyclerAdapterSystem);
        mRootView.findViewById(R.id.fragment_courses_system_see_all).setVisibility(View.VISIBLE);
        mRootView.findViewById(R.id.fragment_courses_system_recyclerView).setVisibility(View.VISIBLE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        // Setup search button
        final MenuItem searchItem = menu.findItem(R.id.menu_main_searchView);
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
//        mSearchView = (SearchView) searchItem.getActionView();
//        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
//        mSearchView.setIconifiedByDefault(true);
//        mSearchView.setOnSuggestionListener(new SearchView.OnSuggestionListener()
//        {
//            @Override
//            public boolean onSuggestionClick(int position) {
//                mSearchView.clearFocus();
//                Cursor cursor = (Cursor) mSearchView.getSuggestionsAdapter().getItem(position);
//                String query = cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));
//                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
//                fragmentManager.beginTransaction().replace(R.id.content_frame,
//                        CoursesSearchableFragment.newInstance(query, mCoursesList, false)).commit();
//                return true;
//            }
//
//            @Override
//            public boolean onSuggestionSelect(int position) {
//                return false;
//            }
//        });
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                // Text has changed, apply filtering?
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                // Perform the final search
                mSearchView.clearFocus();
//                SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getActivity(),
//                        SuggestionProvider.AUTHORITY, SuggestionProvider.MODE);
//                suggestions.saveRecentQuery(query, null);
                ((MainActivity)getActivity()).addSearchHistory(query);
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
//                if (query.equals("*")) query = "";
                fragmentManager.beginTransaction().replace(R.id.content_frame,
                        CoursesSearchableFragment.newInstance(query, mCoursesList, false)).commit();
                return true;
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(EXTRA_COURSES_LIST,mCoursesList);
        outState.putInt(EXTRA_LAST_VIEWPAGER_POSITION, mViewPager.getCurrentItem());
    }
}