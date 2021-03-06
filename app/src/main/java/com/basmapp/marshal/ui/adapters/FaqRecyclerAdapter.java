package com.basmapp.marshal.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.basmapp.marshal.R;
import com.basmapp.marshal.entities.FaqItem;
import com.basmapp.marshal.ui.MainActivity;
import com.basmapp.marshal.util.AuthUtil;
import com.basmapp.marshal.util.MarshalServiceProvider;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class FaqRecyclerAdapter extends RecyclerView.Adapter<FaqRecyclerAdapter.FaqVH> {

    private Context mContext;
    private ArrayList<FaqItem> mFaq;
    private SharedPreferences mSharedPreferences;
    private Boolean mIsDataFiltered = false;

    public FaqRecyclerAdapter(Context activity, ArrayList<FaqItem> faq) {
        this.mFaq = faq;
        this.mContext = activity;
        this.mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public Boolean getIsDataFiltered() {
        return mIsDataFiltered;
    }

    public void setIsDataFiltered(Boolean mIsDataFiltered) {
        this.mIsDataFiltered = mIsDataFiltered;
    }

    public void setItems(ArrayList<FaqItem> items) {
        this.mFaq = items;
        notifyDataSetChanged();
    }

    @Override
    public FaqVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.faq_card_view, parent, false);
        return new FaqVH(view);
    }

    @Override
    public void onBindViewHolder(final FaqVH holder, int position) {
        holder.questionContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean answerCollapsed = holder.answerContainer.getVisibility() == View.GONE;
                holder.expandAnswerArrow.clearAnimation();
                ViewCompat.animate(holder.expandAnswerArrow).rotation(answerCollapsed ? 180 : 0).start();
                if (answerCollapsed) {
                    holder.answerContainer.setVisibility(View.VISIBLE);
                    holder.answerTextView.setVisibility(mFaq.get(holder.getAdapterPosition())
                            .getAnswer() != null ? View.VISIBLE : View.GONE);
                    holder.answerLink.setVisibility(mFaq.get(holder.getAdapterPosition())
                            .getAnswerLink() != null ? View.VISIBLE : View.GONE);
                    holder.answerImageView.setVisibility(mFaq.get(holder.getAdapterPosition())
                            .getAnswerImageUrl() != null ? View.VISIBLE : View.GONE);
                    if (mFaq.get(holder.getAdapterPosition()).getAddressLatitude() != 0 &&
                            mFaq.get(holder.getAdapterPosition()).getAddressLongitude() != 0 &&
                            MainActivity.playServicesAvailable(mContext)) {
                        // We got coordinates and play service is installed - show google map preview
                        holder.mapView.setVisibility(View.VISIBLE);
                        holder.mapViewFab.setVisibility(View.VISIBLE);
                        holder.answerMoovit.setVisibility(View.VISIBLE);
                    } else if (mFaq.get(holder.getAdapterPosition()).getAnswerAddress() != null) {
                        // Show get directions icon
                        holder.answerAddress.setVisibility(View.VISIBLE);
                    }
                    holder.answerDial.setVisibility(mFaq.get(holder.getAdapterPosition())
                            .getAnswerPhoneNumber() != null ? View.VISIBLE : View.GONE);
                    holder.faqForm.setVisibility(mFaq.get(holder.getAdapterPosition())
                            .getIsRated() ? View.GONE : View.VISIBLE);
                } else {
                    holder.answerContainer.setVisibility(View.GONE);
                    holder.answerTextView.setVisibility(View.GONE);
                    holder.answerLink.setVisibility(View.GONE);
                    holder.answerImageView.setVisibility(View.GONE);
                    holder.mapView.setVisibility(View.GONE);
                    holder.mapViewFab.setVisibility(View.GONE);
                    holder.answerMoovit.setVisibility(View.GONE);
                    holder.answerAddress.setVisibility(View.GONE);
                    holder.answerDial.setVisibility(View.GONE);
                    holder.faqForm.setVisibility(View.GONE);
                }
            }
        });

        if (mFaq.get(position).getQuestion() != null) {
            holder.questionTextView.setText(mFaq.get(position).getQuestion());
        }
        if (mFaq.get(position).getAnswer() != null) {
            holder.answerTextView.setText(mFaq.get(position).getAnswer());
        }
        if (mFaq.get(position).getAnswerImageUrl() != null) {
            Glide.with(mContext)
                    .load(mFaq.get(position).getAnswerImageUrl())
                    .placeholder(R.drawable.inline_image_placeholder)
                    .into(holder.answerImageView);
        }
        if (mFaq.get(position).getAnswerLink() != null) {
            holder.answerLink.setText(mFaq.get(position).getAnswerLink());
        }

        if (holder.mapView != null && mFaq.get(holder.getAdapterPosition()).getAddressLatitude() != 0 &&
                mFaq.get(holder.getAdapterPosition()).getAddressLongitude() != 0) {
            final LatLng coordinates = new LatLng(mFaq.get(holder.getAdapterPosition()).getAddressLatitude(),
                    mFaq.get(holder.getAdapterPosition()).getAddressLongitude());

            // Moovit integration
            holder.answerMoovit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        // Assume that Moovit app exists. If not, exception will occur
                        PackageManager pm = mContext.getPackageManager();
                        pm.getPackageInfo("com.tranzmate", PackageManager.GET_ACTIVITIES);
                        String uri =
                        // Launch app call - scheme (with parameters)
                                "moovit://directions?dest_lat=" + coordinates.latitude +
                                        "&dest_lon=" + coordinates.longitude + "&dest_name=" +
                                        mFaq.get(holder.getAdapterPosition()).getAnswerAddress() +
                                        "&partner_id=com.basmapp.marshal";
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(uri));
                        mContext.startActivity(intent);
                    } catch (PackageManager.NameNotFoundException e) {
                        // Moovit not installed - send to store
                        String url = "http://app.appsflyer.com/com.tranzmate?pid=DL&c=com.basmapp.marshal";
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        mContext.startActivity(i);
                    }
                }
            });
            // End of Moovit integration
            holder.mapView.onCreate(null);
            holder.mapView.onResume();
            holder.mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(final GoogleMap googleMap) {
                    MapsInitializer.initialize(mContext.getApplicationContext());
                    holder.map = googleMap;
                    googleMap.addMarker(new MarkerOptions().position(coordinates));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 13));
                    // Save map coordinates after it's been set
                    holder.mapCenter = googleMap.getCameraPosition().target.toString();
                    googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
                        @Override
                        public void onCameraMove() {
                            // Recenter map on fab click with cool animation
                            holder.mapViewFab.setImageResource(R.drawable.ic_center_map);
                            holder.mapViewFab.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    holder.map.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 13), 2000, null);
                                }
                            });
                        }
                    });
                    googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                        @Override
                        public void onCameraIdle() {
                            if (holder.mapCenter.equals(holder.map.getCameraPosition().target.toString())) {
                                // Current camera view same as coordinates, show street view option
                                holder.mapViewFab.setImageResource(R.drawable.ic_streetview);
                                holder.mapViewFab.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        mContext.startActivity(new Intent(Intent.ACTION_VIEW,
                                                Uri.parse("google.streetview:cbll=" +
                                                        coordinates.latitude + "," + coordinates.longitude)));
                                    }
                                });
                            }
                        }
                    });
                    int currentNightMode = mContext.getResources().getConfiguration().uiMode
                            & Configuration.UI_MODE_NIGHT_MASK;
                    if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
                        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                                mContext, R.raw.night_style_map));
                    }
                }
            });
        }

        holder.answerAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mContext.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse(String.format("geo:0,0?q=%s",
                                Uri.encode(mFaq.get(holder.getAdapterPosition()).getAnswerAddress())))));
            }
        });

        holder.answerDial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mContext.startActivity(new Intent(Intent.ACTION_DIAL).setData(Uri.parse(
                        "tel:" + mFaq.get(holder.getAdapterPosition()).getAnswerPhoneNumber())));
            }
        });

        holder.faqFormPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SendFaqIsUsefulRequest("useful", mFaq.get(holder.getAdapterPosition()), holder.faqForm).execute();
            }
        });

        holder.faqFormNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SendFaqIsUsefulRequest("unuseful", mFaq.get(holder.getAdapterPosition()), holder.faqForm).execute();
            }
        });

        holder.progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onViewRecycled(FaqVH holder) {
        super.onViewRecycled(holder);
        if (holder.map != null) {
            // Clear the map and free up resources by changing the map type to none
            holder.map.clear();
            holder.map.setMapType(GoogleMap.MAP_TYPE_NONE);
        }
    }

    @Override
    public int getItemCount() {
        return mFaq.size();
    }

    public void animateTo(ArrayList<FaqItem> faqList) {
        applyAndAnimateRemovals(faqList);
        applyAndAnimateAdditions(faqList);
        applyAndAnimateMovedItems(faqList);
    }

    private void applyAndAnimateRemovals(ArrayList<FaqItem> newItems) {
        for (int i = mFaq.size() - 1; i >= 0; i--) {
            final FaqItem item = mFaq.get(i);
            if (!newItems.contains(item)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(ArrayList<FaqItem> newItems) {
        for (int i = 0, count = newItems.size(); i < count; i++) {
            final FaqItem item = newItems.get(i);
            if (!mFaq.contains(item)) {
                addItem(i, item);
            }
        }
    }

    private void applyAndAnimateMovedItems(ArrayList<FaqItem> newItems) {
        for (int toPosition = newItems.size() - 1; toPosition >= 0; toPosition--) {
            final FaqItem item = newItems.get(toPosition);
            final int fromPosition = mFaq.indexOf(item);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    public FaqItem removeItem(int position) {
        final FaqItem item = mFaq.remove(position);
        notifyItemRemoved(position);
        return item;
    }

    public void addItem(int position, FaqItem item) {
        mFaq.add(position, item);
        notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition) {
        final FaqItem item = mFaq.remove(fromPosition);
        mFaq.add(toPosition, item);
        notifyItemMoved(fromPosition, toPosition);
        notifyItemChanged(toPosition);
    }

    public class FaqVH extends RecyclerView.ViewHolder {

        CardView cardView;
        LinearLayout questionContainer;
        TextView questionTextView, answerTextView, answerLink;
        ImageButton expandAnswerArrow;
        ImageView answerImageView, answerAddress, answerDial, answerMoovit;
        LinearLayout faqForm, answerContainer;
        Button faqFormPositive;
        Button faqFormNegative;
        ProgressBar progressBar;
        MapView mapView;
        GoogleMap map;
        FloatingActionButton mapViewFab;
        String mapCenter = "";

        public FaqVH(View itemView) {
            super(itemView);

            cardView = (CardView) itemView.findViewById(R.id.faq_card);
            questionContainer = (LinearLayout) itemView.findViewById(R.id.faq_question_container);
            questionTextView = (TextView) itemView.findViewById(R.id.faq_question);
            expandAnswerArrow = (ImageButton) itemView.findViewById(R.id.faq_expand_arrow);
            answerContainer = (LinearLayout) itemView.findViewById(R.id.faq_answer_container);
            answerTextView = (TextView) itemView.findViewById(R.id.faq_answer_text);
            answerLink = (TextView) itemView.findViewById(R.id.faq_answer_link);
            answerImageView = (ImageView) itemView.findViewById(R.id.faq_answer_image);
            answerAddress = (ImageView) itemView.findViewById(R.id.card_action_address);
            answerDial = (ImageView) itemView.findViewById(R.id.card_action_dial);
            answerMoovit = (ImageView) itemView.findViewById(R.id.card_action_moovit);
            faqForm = (LinearLayout) itemView.findViewById(R.id.faq_form);
            faqFormPositive = (Button) itemView.findViewById(R.id.faq_helpful_positive);
            faqFormNegative = (Button) itemView.findViewById(R.id.faq_helpful_negative);
            progressBar = (ProgressBar) itemView.findViewById(R.id.faq_progressBar);
            mapView = (MapView) itemView.findViewById(R.id.lite_recycler_map_view);
            mapViewFab = (FloatingActionButton) itemView.findViewById(R.id.map_view_fab);
        }
    }

    private class SendFaqIsUsefulRequest extends AsyncTask<Void, Void, Boolean> {
        private String response;
        private FaqItem faqItem;
        private LinearLayout faqFormContainer;

        SendFaqIsUsefulRequest(String respond, FaqItem faqItem, LinearLayout faqFormContainer) {
            this.response = respond;
            this.faqItem = faqItem;
            this.faqFormContainer = faqFormContainer;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                String apiToken = AuthUtil.getApiToken();
                if (MarshalServiceProvider.getInstance(apiToken).
                        postIsUseful(response, faqItem).execute().isSuccessful()) {
                    faqItem.setIsRated(true);
                    faqItem.save();
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
                Toast.makeText(mContext, R.string.faq_helpful_complete_toast, Toast.LENGTH_LONG).show();
                faqFormContainer.setVisibility(View.GONE);
            } else {
                Toast.makeText(mContext, R.string.faq_helpful_failed_toast, Toast.LENGTH_LONG).show();
            }
        }
    }
}