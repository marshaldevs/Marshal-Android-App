package com.basmapp.marshal.ui;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.basmapp.marshal.BaseActivity;
import com.basmapp.marshal.Constants;
import com.basmapp.marshal.R;
import com.basmapp.marshal.entities.Course;
import com.basmapp.marshal.entities.Cycle;
import com.basmapp.marshal.entities.MaterialItem;
import com.basmapp.marshal.entities.Rating;
import com.simplite.orm.interfaces.BackgroundTaskCallBack;
import com.basmapp.marshal.ui.adapters.CyclesGoogleFormsRecyclerAdapter;
import com.basmapp.marshal.ui.adapters.CyclesRecyclerAdapter;
import com.basmapp.marshal.util.ContentProvider;
import com.basmapp.marshal.util.LocaleUtils;
import com.basmapp.marshal.util.ThemeUtils;
import com.basmapp.marshal.util.AuthUtil;
import com.basmapp.marshal.util.DateHelper;
import com.basmapp.marshal.util.HashUtil;
import com.basmapp.marshal.util.MarshalServiceProvider;
import com.basmapp.marshal.util.TransitionUtils;
import com.basmapp.marshal.util.glide.CircleTransform;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import java.text.Bidi;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Response;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

public class CourseActivity extends BaseActivity {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private static final int RC_REVIEW_ACTIVITY = 123;

    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private Toolbar mToolbar;

    private Course mCourse;
    private Rating mUserRating;
    private ArrayList<Cycle> mCyclesWithGoogleForms;
    private Cycle mCurrentCycle;
    private ArrayList<Cycle> mCycles;

    private TextView mTextViewCourseName;
    private TextView mTextViewCourseCategory;
    private TextView mTextViewCourseMooc;
    private TextView mTextViewCourseCode;
    private TextView mTextViewGeneralDescription;
    private TextView mTextViewSyllabus;
    private TextView mTextViewPrerequisites;
    private TextView mTextViewDayTime;
    private TextView mTextViewDaysDuration;
    private TextView mTextViewHoursDuration;
    private TextView mTextViewComments;
    private ImageView mHeader;
    private TextView mTextViewRatingAverage;
    private TextView mTextViewRatingsAmount;
    private TextView mTextViewReviewHint;
    private TextView mTextViewReviewDate;
    private TextView mTextViewReviewText;
    private TextView mTextViewReviewEdited;
    private TextView mReviewAuthor;
    private RatingBar mReviewRating;
    private RatingBar mRatingBarAverage;
    private RatingBar mRatingBarUser;
    private RelativeLayout mReviewItemContainer;
    private LinearLayout mReviewEditButton;
    private ImageView mWishlistIcon;
    private Button mBtnReadAllReviews;
    private ImageView mReviewProfileImageView;

    private int contentColor = -1;
    private int scrimColor = -1;

    private FloatingActionButton mFabCycles;
    private RatingBar.OnRatingBarChangeListener mRatingBarUserOnChangeListener;
    private LinearLayout mRatingsFrame;
    private SharedPreferences mSharedPreferences;

    private MaterialTapTargetPrompt mFabPrompt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setSharedElementEnterTransition(TransitionInflater.from(this).inflateTransition(R.transition.course_transition));
            getWindow().setSharedElementReturnTransition(TransitionInflater.from(this).inflateTransition(R.transition.course_transition));
//            getWindow().setSharedElementsUseOverlay(false);
        }

        setContentView(R.layout.activity_course);

        supportPostponeEnterTransition();

        final View decor = getWindow().getDecorView();
        decor.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                decor.getViewTreeObserver().removeOnPreDrawListener(this);
                supportStartPostponedEnterTransition();
                return true;
            }
        });

        //Initialize Shared Preferences
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mCourse = getIntent().getParcelableExtra(Constants.EXTRA_COURSE);
        mCourse.Ctor(this);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                supportFinishAfterTransition();
            }
        });

        mCollapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        // hide toolbar expanded title
        mCollapsingToolbarLayout.setExpandedTitleColor(ContextCompat.getColor(getApplicationContext(), android.R.color.transparent));

        //Initialize Cycles FAB
        mFabCycles = (FloatingActionButton) findViewById(R.id.course_activity_fab_cycles);

        // Set wishlist menu item
        mWishlistIcon = (ImageView) findViewById(R.id.wishlist_button_icon);
        if (mCourse.getIsUserSubscribe()) {
            mWishlistIcon.setImageResource(R.drawable.ic_loyalty_added);
        } else {
            mWishlistIcon.setImageResource(R.drawable.ic_loyalty_add);
        }

        if (mCourse != null) {

            if (mCourse.getCycles() == null || mCourse.getCycles().size() == 0) {
                mFabCycles.setVisibility(View.GONE);
            } else {
                // Initialize Cycles FAB onClick event
                mCycles = new ArrayList<>(mCourse.getCycles());

                for (int index = 0; index < mCycles.size(); index++) {
                    if (mCycles.get(index).getStartDate() == null || mCycles.get(index).getEndDate() == null) {
                        mCycles.remove(index);
                    }
                }

                mFabCycles.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mCycles.size() > 0) {
                            CyclesRecyclerAdapter cyclesRecyclerAdapter =
                                    new CyclesRecyclerAdapter(CourseActivity.this, mCycles, mCourse);
                            showCyclesBottomSheet(cyclesRecyclerAdapter);
                        } else {
                            Toast.makeText(CourseActivity.this,
                                    getResources().getString(R.string.course_no_cycles_error),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                        && mSharedPreferences.getBoolean(Constants.PREF_COURSE_ACTIVITY_STARTED_SHARED, true)) {
                    if (getWindow().getSharedElementEnterTransition() != null) {
                        getWindow().getSharedElementEnterTransition().addListener(new TransitionUtils.TransitionListenerAdapter() {
                            @SuppressLint("NewApi")
                            @Override
                            public void onTransitionEnd(Transition transition) {
                                getWindow().getSharedElementEnterTransition().removeListener(this);
                                showFabTargetPrompt();
                                // After animation end, change boolean back to false
                                mSharedPreferences.edit().putBoolean(
                                        Constants.PREF_COURSE_ACTIVITY_STARTED_SHARED, false).apply();
                            }
                        });
                    }
                }
                // Target android version lower than lollipop or activity didn't start as shared element
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP
                        || !mSharedPreferences.getBoolean(Constants.PREF_COURSE_ACTIVITY_STARTED_SHARED, false)) {
                    showFabTargetPrompt();
                }
            }
            // Set the course title
            mCollapsingToolbarLayout.setTitle(mCourse.getName());

            mTextViewCourseName = (TextView) findViewById(R.id.course_content_textView_courseName);
            mTextViewCourseCategory = (TextView) findViewById(R.id.course_content_textView_courseCategory);
            mTextViewCourseMooc = (TextView) findViewById(R.id.mooc_badge);
            mTextViewCourseCode = (TextView) findViewById(R.id.course_content_textView_courseCode);
            mTextViewGeneralDescription = (TextView) findViewById(R.id.course_content_textView_description);
            mTextViewSyllabus = (TextView) findViewById(R.id.course_content_textView_syllabus);
            mTextViewPrerequisites = (TextView) findViewById(R.id.course_content_textView_prerequisites);
            mTextViewDayTime = (TextView) findViewById(R.id.course_content_textView_dayTime);
            mTextViewDaysDuration = (TextView) findViewById(R.id.course_content_textView_daysDuration);
            mTextViewHoursDuration = (TextView) findViewById(R.id.course_content_textView_hoursDuration);
            mTextViewComments = (TextView) findViewById(R.id.course_content_textView_comments);
            mTextViewReviewHint = (TextView) findViewById(R.id.review_hint);
            mReviewProfileImageView = (ImageView) findViewById(R.id.review_item_user_profile_image);

            // Set the course photo
            mHeader = (ImageView) findViewById(R.id.header);

            Glide.with(this)
                    .load(mCourse.getImageUrl())
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .priority(Priority.IMMEDIATE)
                    .into(new BitmapImageViewTarget(mHeader) {
                        @Override
                        public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                            super.onResourceReady(bitmap, glideAnimation);
                            final Bitmap bitmapDrawable = ((BitmapDrawable) mHeader.getDrawable()).getBitmap();
                            Palette.from(bitmapDrawable).generate(new Palette.PaletteAsyncListener() {
                                public void onGenerated(Palette palette) {
                                    contentColor = palette.getMutedColor(ThemeUtils.getThemeColor(CourseActivity.this, R.attr.colorPrimary));
                                    scrimColor = ContextCompat.getColor(getApplicationContext(), R.color.black_trans80);
                                    mCollapsingToolbarLayout.setStatusBarScrimColor(scrimColor);
                                    mCollapsingToolbarLayout.setContentScrimColor(contentColor);
                                }
                            });
                        }
                    });
            initializeTextViews();

            onSecondaryActionsClick();
        }

        mReviewItemContainer = (RelativeLayout)
                findViewById(R.id.review_item_container);

        mReviewEditButton = (LinearLayout)
                findViewById(R.id.review_item_action_container);

        mReviewRating = (RatingBar)
                findViewById(R.id.review_item_review_rating);

        mRatingBarAverage = (RatingBar)
                findViewById(R.id.summary_rating_bar);

        mRatingBarUser = (RatingBar)
                findViewById(R.id.course_content_ratingBar_user);

        mTextViewReviewHint = (TextView)
                findViewById(R.id.review_hint);

        mTextViewReviewDate = (TextView)
                findViewById(R.id.review_item_review_date);

        mTextViewReviewText = (TextView)
                findViewById(R.id.review_item_review_text);

        mTextViewReviewEdited = (TextView)
                findViewById(R.id.review_item_review_edited);

        mTextViewRatingsAmount = (TextView)
                findViewById(R.id.course_content_textView_ratingsAmount);

        mTextViewRatingAverage = (TextView)
                findViewById(R.id.course_content_textView_average_value);

        mReviewAuthor = (TextView)
                findViewById(R.id.review_item_author);

        mBtnReadAllReviews = (Button)
                findViewById(R.id.course_content_button_readAllReviews);

        mRatingsFrame = (LinearLayout)
                findViewById(R.id.course_content_ratingsFrame);

        if (mCourse != null) {
            setRatingViewsVisibility(View.VISIBLE);
            showRatingAverage();
            showRatingsCount();
            showUserRating();

            mBtnReadAllReviews.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mRatingBarAverage.getRating() == 0) {
                        Toast.makeText(CourseActivity.this, R.string.no_reviews_error, Toast.LENGTH_LONG).show();
                    } else {
                        Intent i = new Intent(CourseActivity.this, RatingsActivity.class);
                        i.putExtra(Constants.EXTRA_COURSE, mCourse);
                        i.putExtra(Constants.EXTRA_RATING_AMOUNT, mTextViewRatingsAmount.getText().toString());
                        i.putExtra(Constants.EXTRA_RATING_BAR_STARS, mRatingBarAverage.getRating());
                        i.putExtra(Constants.EXTRA_RATING_AVERAGE, mTextViewRatingAverage.getText().toString());
                        startActivity(i);
                    }
                }
            });

            mRatingBarUserOnChangeListener = new RatingBar.OnRatingBarChangeListener() {
                @Override
                public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                    if (MainActivity.sUserEmailAddress != null && ratingBar.getRating() != 0) {
                        Rating tempRating = new Rating(CourseActivity.this);
                        tempRating.setUserMailAddress("");
                        tempRating.setCourseID(0);
                        tempRating.setRating(rating);
                        tempRating.setComment("");
                        tempRating.setCreatedAt(new Date());
                        tempRating.setLastModified(new Date());

                        startReviewActivity(false, tempRating);
                    } else {
                        if (ratingBar.getRating() != 0) {
                            Toast.makeText(CourseActivity.this, R.string.please_log_in, Toast.LENGTH_SHORT).show();
                        }
                        ratingBar.setRating(0);
                    }
                }
            };

            mRatingBarUser.setOnRatingBarChangeListener(mRatingBarUserOnChangeListener);
            mReviewEditButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startReviewActivity(true, mUserRating);
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (mFabPrompt != null) {
            mFabPrompt.finish();
            mFabPrompt = null;
        } else {
            supportFinishAfterTransition();
        }
    }

    private void orderCyclesByAscending() {
        Collections.sort(mCycles, new Comparator<Cycle>() {
            public int compare(Cycle cycle1, Cycle cycle2) {
                if (cycle1.getStartDate() == null || cycle2.getStartDate() == null)
                    return 0;
                return cycle1.getStartDate().compareTo(cycle2.getStartDate());
            }
        });
    }

    private void setRatingViewsVisibility(int visibility) {
        if (mRatingsFrame != null) {
            mRatingsFrame.setVisibility(visibility);
        }
    }

    private void showFabTargetPrompt() {
        if (mSharedPreferences.getBoolean(Constants.SHOW_CYCLE_FAB_TAP_TARGET, true)) {
            mFabPrompt = new MaterialTapTargetPrompt.Builder(CourseActivity.this)
                    .setTarget(findViewById(R.id.course_activity_fab_cycles))
                    .setPrimaryText(R.string.cycle_fab_tip_title)
                    .setSecondaryText(R.string.cycle_fab_tip_subtitle)
                    .setBackgroundColour(ThemeUtils.getThemeColor(CourseActivity.this, R.attr.colorPrimary))
                    .setAnimationInterpolator(new FastOutSlowInInterpolator())
                    .setAutoDismiss(false)
                    .setAutoFinish(false)
                    .setCaptureTouchEventOutsidePrompt(true)
                    .setOnHidePromptListener(new MaterialTapTargetPrompt.OnHidePromptListener() {
                        @Override
                        public void onHidePrompt(MotionEvent event, boolean tappedTarget) {
                            if (tappedTarget) {
                                mFabPrompt.finish();
                                mFabPrompt = null;
                                // Storing a value so that this prompt is never shown again
                                mSharedPreferences.edit().putBoolean(
                                        Constants.SHOW_CYCLE_FAB_TAP_TARGET, false).apply();
                            }
                        }

                        @Override
                        public void onHidePromptComplete() {

                        }
                    })
                    .show();
        }
    }

    private void showUserRating() {
        if (MainActivity.sUserEmailAddress != null) {
            Rating.queryInBackground(Rating.class, CourseActivity.this, false,
                    new String[]{Rating.COL_COURSE_ID, Rating.COL_USER_MAIL_ADDRESS},
                    new String[]{String.valueOf(mCourse.getCourseID()), HashUtil.SHA(MainActivity.sUserEmailAddress)}, null,
                    new BackgroundTaskCallBack() {
                        @Override
                        public void onSuccess(String result, List<Object> data) {

                            if (data != null && data.size() > 0) {

                                mRatingBarUser.setVisibility(View.GONE);
                                mReviewItemContainer.setVisibility(View.VISIBLE);
                                mReviewAuthor.setText(MainActivity.sUserName);
                                Uri uri = MainActivity.sUserProfileImage;
                                if (uri != null) {
                                    Glide.with(CourseActivity.this)
                                            .load(uri)
                                            .transform(new CircleTransform(CourseActivity.this))
                                            .placeholder(R.drawable.ic_default_avatar)
                                            .into(mReviewProfileImageView);
                                }
                                mTextViewReviewHint.setVisibility(View.GONE);
                                mTextViewReviewText.setText(((Rating) (data.get(0))).getComment());
                                try {
                                    mTextViewReviewDate.setVisibility(View.VISIBLE);
                                    mTextViewReviewDate.setText(DateHelper.dateToString(((Rating) (data.get(0))).getLastModified()));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                mReviewRating.setRating((float) ((Rating) (data.get(0))).getRating());
                                mUserRating = (Rating) data.get(0);
                                if (mUserRating.getCreatedAt().before(mUserRating.getLastModified())) {
                                    mTextViewReviewEdited.setVisibility(View.VISIBLE);
                                }
                            } else {
                                initializeRatingViews();
                            }
                        }

                        @Override
                        public void onError(String error) {
                            initializeRatingViews();
                        }
                    });
        } else {
            initializeRatingViews();
        }
    }

    private void showRatingsCount() {
        Rating.countByColumnInBackground(Rating.class, CourseActivity.this,
                false, Rating.COL_COURSE_ID, mCourse.getCourseID(), new BackgroundTaskCallBack() {
                    @Override
                    public void onSuccess(String result, List<Object> data) {
                        if (data != null && data.size() > 0) {
                            try {
                                mTextViewRatingsAmount.setText(String.valueOf(data.get(0)));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(String error) {

                    }
                });
    }

    private void showRatingAverage() {
        Rating.getAverageByColumnInBackground(Rating.class, CourseActivity.this, false,
                Rating.COL_RATING, Rating.COL_COURSE_ID, mCourse.getCourseID(),
                new BackgroundTaskCallBack() {
                    @Override
                    public void onSuccess(String result, List<Object> data) {
                        if (data != null && data.size() > 0) {
                            try {
                                mTextViewRatingAverage.setText(String.valueOf(data.get(0)).substring(0, 3));
                                mRatingBarAverage.setRating((Float) data.get(0));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(String error) {

                    }
                });
    }

    private void startReviewActivity(boolean isEditMode, Rating rating) {
        if (!RateReviewActivity.isRunning) {
            Intent intent = new Intent(CourseActivity.this, RateReviewActivity.class);
            intent.putExtra(Constants.EXTRA_COURSE, mCourse);
            intent.putExtra(Constants.EXTRA_CONTENT_COLOR, contentColor);
            intent.putExtra(Constants.EXTRA_USER_RATING, rating);
            intent.putExtra(Constants.EXTRA_IS_EDIT_MODE, isEditMode);
            startActivityForResult(intent, RC_REVIEW_ACTIVITY);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RateReviewActivity.RESULT_CANCELED) {
            mRatingBarUser.setRating(0);
        } else if (resultCode == RateReviewActivity.RESULT_POST_SUCCESS) {
            Rating tempRating = data.getParcelableExtra(Constants.EXTRA_USER_RATING);
            mUserRating = new Rating(CourseActivity.this, tempRating);

            // Showing user review
            mTextViewReviewHint.setVisibility(View.GONE);
            mRatingBarUser.setVisibility(View.GONE);
            mReviewItemContainer.setVisibility(View.VISIBLE);
            mReviewAuthor.setText(MainActivity.sUserName);
            Uri uri = MainActivity.sUserProfileImage;
            if (uri != null) {
                Glide.with(CourseActivity.this)
                        .load(uri)
                        .placeholder(R.drawable.ic_default_avatar)
                        .into(mReviewProfileImageView);
            }
            mTextViewReviewEdited.setVisibility(View.GONE);

            mReviewRating.setRating(mRatingBarUser.getRating());
            try {
                mTextViewReviewDate.setVisibility(View.VISIBLE);
                mTextViewReviewDate.setText(DateHelper.dateToString(mUserRating.getLastModified()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            mTextViewReviewText.setText(mUserRating.getComment());
            showRatingChanges();
            Toast.makeText(CourseActivity.this, R.string.review_feedback_posted, Toast.LENGTH_LONG).show();
        } else if (resultCode == RateReviewActivity.RESULT_PUT_SUCCESS) {
            Rating tempRating = data.getParcelableExtra(Constants.EXTRA_USER_RATING);
            mUserRating.setComment(tempRating.getComment());
            mUserRating.setRating(tempRating.getRating());

            mTextViewReviewText.setVisibility(View.VISIBLE);
            showRatingChanges();
        } else if (resultCode == RateReviewActivity.RESULT_DELETE_SUCCESS) {
            initializeRatingViews();
            showRatingChanges();
        } else if (resultCode == RateReviewActivity.RESULT_POST_FAILED) {
            mRatingBarUser.setOnRatingBarChangeListener(null);
            mRatingBarUser.setRating(0);
            mRatingBarUser.setOnRatingBarChangeListener(mRatingBarUserOnChangeListener);
            Toast.makeText(CourseActivity.this, R.string.review_create_error, Toast.LENGTH_LONG).show();
        } else if (resultCode == RateReviewActivity.RESULT_PUT_FAILED) {
            Toast.makeText(CourseActivity.this, R.string.review_save_retry, Toast.LENGTH_LONG).show();
        } else if (resultCode == RateReviewActivity.RESULT_DELETE_FAILED) {
            Toast.makeText(CourseActivity.this, R.string.review_delete_retry, Toast.LENGTH_LONG).show();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showRatingChanges() {
        showRatingAverage();
        showRatingsCount();
        showUserRating();

        // Send broadcast for update the rating on the CardView
        ContentProvider.getInstance().notifyCourseRatingUpdated(getApplicationContext(), mCourse);
    }

    private void initializeRatingViews() {
        mTextViewReviewHint.setVisibility(View.VISIBLE);
        mReviewItemContainer.setVisibility(View.GONE);
        mRatingBarUser.setVisibility(View.VISIBLE);
        mRatingBarUser.setRating(0);
        mRatingBarUser.setIsIndicator(false);
        mRatingBarUser.setOnRatingBarChangeListener(mRatingBarUserOnChangeListener);
    }

    private void initializeTextViews() {

        boolean isAnyDataExist = false;

        if (mCourse.getIsMeetup()) {
            mTextViewReviewHint.setText(getString(R.string.rate_this_meetup));
        } else {
            mTextViewReviewHint.setText(getString(R.string.rate_this_course));
        }

        // Set course's Name
        if ((mCourse.getName() != null) &&
                (!mCourse.getName().equals(""))) {
            mTextViewCourseName.setText(mCourse.getName());
            isAnyDataExist = true;
        } else {
            findViewById(R.id.course_content_relativeLayout_name).setVisibility(View.GONE);
        }

        // Set course's Category
        //TODO: switch to generic implementation
        if (mCourse.getCategory() != null) {
            String categoryLocaleTitle = LocaleUtils.getCategoryLocaleTitle(mCourse.getCategory(), this);
            if (categoryLocaleTitle != null)
                mTextViewCourseCategory.setText(categoryLocaleTitle);
            if (mCourse.getIsMooc()) {
                mTextViewCourseMooc.setVisibility(View.VISIBLE);
            } else {
                mTextViewCourseMooc.setVisibility(View.GONE);
            }
        } else {
            findViewById(R.id.course_content_relativeLayout_category).setVisibility(View.GONE);
        }

        // Set course's Code
        if ((mCourse.getCourseCode() != null) &&
                (!mCourse.getCourseCode().equals(""))) {
            mTextViewCourseCode.setText(mCourse.getCourseCode());
            isAnyDataExist = true;
        } else {
            findViewById(R.id.course_content_relativeLayout_code).setVisibility(View.GONE);
        }

        // Set course's Description
        if ((mCourse.getDescription() != null) &&
                (!mCourse.getDescription().equals(""))) {
            mTextViewGeneralDescription.setText(mCourse.getDescription());
            isAnyDataExist = true;
        } else {
            findViewById(R.id.course_content_relativeLayout_description).setVisibility(View.GONE);
        }

        // Set course's Syllabus
        if ((mCourse.getSyllabus() != null) &&
                (!mCourse.getSyllabus().equals(""))) {
            mTextViewSyllabus.setText(mCourse.getSyllabus());
            isAnyDataExist = true;
        } else {
            findViewById(R.id.course_content_relativeLayout_syllabus).setVisibility(View.GONE);
        }

        // Set course's prerequisites
        if ((mCourse.getPrerequisites() != null) &&
                (!mCourse.getPrerequisites().equals(""))) {
            mTextViewPrerequisites.setText(mCourse.getPrerequisites());
            isAnyDataExist = true;
        } else {
            findViewById(R.id.course_content_relativeLayout_prerequisites).setVisibility(View.GONE);
        }

        // Set course's DayTime
        if ((mCourse.getDayTime() != null) &&
                (!mCourse.getDayTime().equals(""))) {
            mTextViewDayTime.setText(mCourse.getDayTime());
            isAnyDataExist = true;
        } else {
            findViewById(R.id.course_content_relativeLayout_dayTime).setVisibility(View.GONE);
        }

        // Set course's Days duration
        if (mCourse.getDurationInDays() != 0) {
            mTextViewDaysDuration.setText(String.valueOf(mCourse.getDurationInDays()));
            isAnyDataExist = true;
        } else {
            findViewById(R.id.course_content_relativeLayout_dayDuration).setVisibility(View.GONE);
        }

        // Set course's Hours duration
        if (mCourse.getDurationInHours() != 0) {
            mTextViewHoursDuration.setText(String.valueOf(mCourse.getDurationInHours()));
            isAnyDataExist = true;
        } else {
            findViewById(R.id.course_content_relativeLayout_hoursDuration).setVisibility(View.GONE);
        }

        // Set course's comments
        if ((mCourse.getComments() != null) &&
                (!mCourse.getComments().equals(""))) {
            mTextViewComments.setText(mCourse.getComments());
            isAnyDataExist = true;
        } else {
            findViewById(R.id.course_content_relativeLayout_comments).setVisibility(View.GONE);
        }
        if (!isAnyDataExist) {
            findViewById(R.id.course_content_textView_noDetailsMessage).setVisibility(View.VISIBLE);
        }
    }

    public void onSecondaryActionsClick() {

        findViewById(R.id.wishlist_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCourse.getIsUserSubscribe()) {
                    new SubscribeTask(SubscribeTask.TASK_TYPE_UNSUBSCRIBE, mWishlistIcon).execute();
                } else {
                    new SubscribeTask(SubscribeTask.TASK_TYPE_SUBSCRIBE, mWishlistIcon).execute();
                }
            }
        });
        findViewById(R.id.materials_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialItem.rawQueryInBackground(MaterialItem.getSelectCourseMaterialsQuery(mCourse.getCourseID()),
                        CourseActivity.this, MaterialItem.class, true, new BackgroundTaskCallBack() {
                            @Override
                            public void onSuccess(String result, List<Object> data) {
                                if (data != null && data.size() > 0) {
                                    try {
                                        ArrayList<MaterialItem> materialItems = (ArrayList) data;
                                        Intent i = new Intent(CourseActivity.this, CourseMaterialsActivity.class);
                                        i.putExtra(Constants.EXTRA_COURSE, mCourse);
                                        i.putParcelableArrayListExtra(Constants.EXTRA_COURSE_MATERIALS_LIST, materialItems);
                                        startActivity(i);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        Toast.makeText(CourseActivity.this, R.string.error_message, Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Toast.makeText(CourseActivity.this, R.string.no_materials_for_course, Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onError(String error) {
                                if (error != null) {
                                    Log.e("GET COURSE MATERIALS ", " ERROR:\n" + error);
                                } else {
                                    Log.e("GET COURSE MATERIALS ", " ERROR");
                                }
                                Toast.makeText(CourseActivity.this, R.string.error_message, Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });
        findViewById(R.id.share_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ShareCourseImageTask(CourseActivity.this, mCourse).execute();
            }
        });

        initCyclesGoogleForms();

        if (mTextViewGeneralDescription != null && !mTextViewGeneralDescription.getText().equals("")) {
            Bidi bidi = new Bidi(mTextViewGeneralDescription.getText().toString(),
                    Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT);
            if (Locale.getDefault().toString().toLowerCase().equals("en") && bidi.getBaseLevel() == 1) {
                // App language is english and description language is hebrew, show translate option
                findViewById(R.id.course_activity_google_translate_button).setVisibility(View.VISIBLE);
            } else if (Locale.getDefault().toString().toLowerCase().equals("iw") && bidi.getBaseLevel() == 0) {
                // App language is hebrew and description language is english, show translate option
                findViewById(R.id.course_activity_google_translate_button).setVisibility(View.VISIBLE);
            } else {
                // App language identical to description language, translate is not needed
                findViewById(R.id.course_activity_google_translate_button).setVisibility(View.GONE);
            }
        } else {
            // If description does not exist, don't show translate button
            findViewById(R.id.course_activity_google_translate_button).setVisibility(View.GONE);
        }

        findViewById(R.id.course_activity_google_translate_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text_input = mTextViewGeneralDescription.getText().toString();
                Bidi bidi = new Bidi(text_input, Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT);
                String language_from = (bidi.getBaseLevel() == 0) ? /* ltr */ "en" :  /* rtl */ "iw";
                String language_to = (bidi.getBaseLevel() == 0) ?  /* ltr */ "iw" : /* rtl */ "en";
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://translate.google.com/m/translate#"
                                + language_from + "/" + language_to + "/" + Uri.encode(text_input))));
            }
        });
    }

    private void initCyclesGoogleForms() {
        if (mCourse.getCycles() != null && mCourse.getCycles().size() > 0) {

            new AsyncTask<Void, Void, Cycle>() {
                @Override
                protected Cycle doInBackground(Void... voids) {
                    mCyclesWithGoogleForms = new ArrayList<>();
                    Cycle currentCycle = null;

                    for (Cycle cycle : mCourse.getCycles()) {
                        if (cycle.getGoogleFormUrl() != null &&
                                !cycle.getGoogleFormUrl().equals("")) {
                            mCyclesWithGoogleForms.add(cycle);
                        }
                        if (cycle.isRunningNow()) {
                            currentCycle = cycle;
                        }
                    }

                    if (mCyclesWithGoogleForms.size() > 0)
                        orderCycles();

                    return currentCycle;
                }

                @Override
                protected void onPostExecute(Cycle currentCycle) {
                    super.onPostExecute(currentCycle);

                    mCurrentCycle = currentCycle;

                    if (mCyclesWithGoogleForms.size() > 0) {
                        findViewById(R.id.course_activity_google_form_button).setVisibility(View.VISIBLE);
                        findViewById(R.id.course_activity_google_form_button).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (mCurrentCycle != null) {
                                    String message = String.format(CourseActivity.this
                                                    .getString(R.string.course_cycle_feedback_current_cycle_format),
                                            DateHelper.dateToString(mCurrentCycle.getStartDate()),
                                            DateHelper.dateToString(mCurrentCycle.getEndDate()));
                                    new AlertDialog.Builder(CourseActivity.this)
                                            .setMessage(message)
                                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    showGoogleFormsActivity(mCurrentCycle,
                                                            mCurrentCycle.getGoogleFormUrl());
                                                }
                                            })
                                            .setNegativeButton(R.string.other_cycle, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    showCycleFormsListDialog();
                                                }
                                            })
                                            .show();
                                } else {
                                    showCycleFormsListDialog();
                                }
                            }
                        });
                    }
                }

                private void showCycleFormsListDialog() {
                    CyclesGoogleFormsRecyclerAdapter cyclesGoogleFormsRecyclerAdapter =
                            new CyclesGoogleFormsRecyclerAdapter(CourseActivity.this, mCyclesWithGoogleForms);
                    showCyclesBottomSheet(cyclesGoogleFormsRecyclerAdapter);
                }

                private void showGoogleFormsActivity(Cycle cycle, String url) {
                    Intent intent = new Intent(CourseActivity.this, WebViewActivity.class);
                    intent.putExtra(Constants.EXTRA_WEB_VIEW_TITLE, cycle.toDatesRangeString(CourseActivity.this));
                    intent.putExtra(Constants.EXTRA_WEB_VIEW_URL, url);
                    startActivity(intent);
                }

                private void orderCycles() {
                    Collections.sort(mCyclesWithGoogleForms, new Comparator<Cycle>() {
                        public int compare(Cycle cycle1, Cycle cycle2) {
                            if (cycle1.getStartDate() == null || cycle2.getStartDate() == null)
                                return 0;
                            return cycle1.getStartDate().compareTo(cycle2.getStartDate());
                        }
                    });
                }

            }.execute();
        }
    }

    private void showCyclesBottomSheet (RecyclerView.Adapter adapter) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(CourseActivity.this);
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_cycles, null);
        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();
        try {
            orderCyclesByAscending();
            RecyclerView recyclerView = (RecyclerView)
                    sheetView.findViewById(R.id.cycle_activity_recyclerView);
            LinearLayoutManager linearLayoutManager =
                    new LinearLayoutManager(CourseActivity.this);
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(adapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class SubscribeTask extends AsyncTask<Void, Void, Boolean> {

        static final int TASK_TYPE_SUBSCRIBE = 1;
        static final int TASK_TYPE_UNSUBSCRIBE = 2;

        private int taskType;
        private ImageView wishlistIcon;

        SubscribeTask(int taskType, ImageView icon) {
            this.wishlistIcon = icon;
            this.taskType = taskType;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (taskType == TASK_TYPE_SUBSCRIBE) {
                Toast.makeText(CourseActivity.this,
                        getString(R.string.wishlist_adding), Toast.LENGTH_SHORT).show();
            } else if (taskType == TASK_TYPE_UNSUBSCRIBE) {
                Toast.makeText(CourseActivity.this,
                        getString(R.string.wishlist_removing), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                // Subscribe in Server
                Response serverResponse;
                if (taskType == TASK_TYPE_SUBSCRIBE) {
                    serverResponse = MarshalServiceProvider.getInstance(AuthUtil.getApiToken())
                            .subscribeCourse(AuthUtil.getHardwareId(getContentResolver()), mCourse.getCourseID()).execute();
                } else if (taskType == TASK_TYPE_UNSUBSCRIBE) {
                    serverResponse = MarshalServiceProvider.getInstance(AuthUtil.getApiToken())
                            .unsubscribeCourse(AuthUtil.getHardwareId(getContentResolver()), mCourse.getCourseID()).execute();
                } else return false;

                // Save the subscription locally IF successfully saved in the server
                if (serverResponse != null && serverResponse.isSuccessful()) {
                    if (taskType == TASK_TYPE_SUBSCRIBE) {
                        mCourse.setIsUserSubscribe(true);
                    } else if (taskType == TASK_TYPE_UNSUBSCRIBE) {
                        mCourse.setIsUserSubscribe(false);
                    }

                    mCourse.save();
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
                if (taskType == TASK_TYPE_SUBSCRIBE) {
                    if (wishlistIcon != null)
                        wishlistIcon.setImageResource(R.drawable.ic_loyalty_added);
                } else if (taskType == TASK_TYPE_UNSUBSCRIBE) {
                    if (wishlistIcon != null)
                        wishlistIcon.setImageResource(R.drawable.ic_loyalty_add);
                }

                ContentProvider.getInstance().notifyCourseSubscriptionUpdated(getApplicationContext(),
                        mCourse);
            } else {
                if (taskType == TASK_TYPE_SUBSCRIBE) {
                    Toast.makeText(CourseActivity.this, String.format(
                            getString(R.string.wishlist_add_error), mCourse.getName()),
                            Toast.LENGTH_LONG).show();
                } else if (taskType == TASK_TYPE_UNSUBSCRIBE) {
                    Toast.makeText(CourseActivity.this, String.format(
                            getString(R.string.wishlist_remove_error), mCourse.getName()),
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}