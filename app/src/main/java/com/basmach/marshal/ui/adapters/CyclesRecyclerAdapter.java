package com.basmach.marshal.ui.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.CalendarContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.provider.CalendarContract.Events;
import android.widget.Toast;

import com.basmach.marshal.R;
import com.basmach.marshal.entities.Cycle;
import com.basmach.marshal.utils.DateHelper;

import java.util.ArrayList;
import java.util.GregorianCalendar;

public class CyclesRecyclerAdapter extends RecyclerView.Adapter<CyclesRecyclerAdapter.CycleVH>{

    private Context mContext;
    private ArrayList<Cycle> mCycles;

    public CyclesRecyclerAdapter(Context context, ArrayList<Cycle> cycles) {
        this.mContext = context;
        this.mCycles = cycles;
    }
    @Override
    public CycleVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cycles_list_item, parent, false);
        return new CycleVH(view);
    }

    @Override
    public void onBindViewHolder(CycleVH holder, final int position) {
        try {
            holder.mStartDateTextView.setText(DateHelper.dateToString(mCycles.get(position).getStartDate()));
            holder.mEndDateTextView.setText(DateHelper.dateToString(mCycles.get(position).getEndDate()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mCycles.size();
    }


    public class CycleVH extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView mStartDateTextView;
        TextView mEndDateTextView;
        LinearLayout mMainLayout;

        public CycleVH(View itemView) {
            super(itemView);

            mMainLayout = (LinearLayout) itemView.findViewById(R.id.cycles_list_item_linearLayout_mainLayout);
            mStartDateTextView = (TextView) itemView.findViewById(R.id.cycles_list_item_textView_startDate);
            mEndDateTextView = (TextView) itemView.findViewById(R.id.cycles_list_item_textView_endDate);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

            new AlertDialog.Builder(mContext, R.style.Cycle_DialogAlert)
                    .setTitle(mContext.getString(R.string.cycle_alert_title))
                    .setMessage(R.string.cycle_alert_message)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                GregorianCalendar startDate = new GregorianCalendar();
                                startDate.setTime(mCycles.get(getAdapterPosition()).getStartDate());

                                GregorianCalendar endDate = new GregorianCalendar();
                                endDate.setTime(mCycles.get(getAdapterPosition()).getEndDate());
                                endDate.set(GregorianCalendar.DAY_OF_MONTH, ((endDate.get(GregorianCalendar.DAY_OF_MONTH)) + 1));

                                Intent calendarntent = new Intent(Intent.ACTION_INSERT);
                                calendarntent.setType("vnd.android.cursor.item/event");
                                calendarntent.putExtra(Events.TITLE, mCycles.get(getAdapterPosition()).getName());
                                calendarntent.putExtra(Events.DESCRIPTION, mCycles.get(getAdapterPosition()).getDescription());
                                calendarntent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true);
                                calendarntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startDate.getTimeInMillis());
                                calendarntent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endDate.getTimeInMillis());
                                mContext.startActivity(calendarntent);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(mContext,
                                        mContext.getString(R.string.course_cycle_item_onclick_error),
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    })
                    .show();
        }
    }
}
