package com.bignerdranch.android.criminalintent;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.google.android.material.snackbar.Snackbar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static android.content.ContentValues.TAG;
import static androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE;

public class CrimeListFragment extends Fragment {

    private static final String SAVED_SUBTITLE_VISIBLE = "subtitle";

    private RecyclerView mCrimeRecyclerView;
    private CrimeAdapter mAdapter;
    private int mLastUpdatedPosition = -1;
    private boolean mSubtitleVisible;
    private View mLayout;
    private Button mAddFirstCrimeButton;
    private Callbacks mCallBacks;
    private OnDeleteCrimeListener mDeleteCallback;
    private CoordinatorLayout mCoordinatorLayout;

    public interface Callbacks {
        void onCrimeSelected(Crime crime);
    }

    public interface OnDeleteCrimeListener {
        void onCrimeIdSelected(UUID crimeId);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mCallBacks = (Callbacks) context;
        mDeleteCallback = (OnDeleteCrimeListener) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime_list, container, false);

        mCrimeRecyclerView = (RecyclerView) view.findViewById(R.id.crime_recycler_view);
        mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        setCrimeRecyclerViewItemTouchListener();

        mCoordinatorLayout = (CoordinatorLayout) view.findViewById(R.id.coordinatorLayout);
        mLayout = view.findViewById(R.id.add_crime_view);
        mAddFirstCrimeButton = view.findViewById(R.id.add_crime);
        mAddFirstCrimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Crime crime = new Crime();
                CrimeLab.get(getActivity()).addCrime(crime);
                Intent intent = CrimePagerActivity.newIntent(getActivity(), crime.getId());
                startActivity(intent);
            }
        });

        if (savedInstanceState != null){
            mSubtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
        }
        updateUI();

        return view;
    }

    private void setCrimeRecyclerViewItemTouchListener() {

        ItemTouchHelper.SimpleCallback itemTouchCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView,
                                  RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();
                final Crime crime = mAdapter.mCrimes.get(position);
                Log.d(TAG, "onSwiped: " + crime.getId());
                crime.setDeleted(true);
                CrimeLab.get(getActivity()).updateCrime(crime);
                updateUI();
                Snackbar snackbar = Snackbar
                        .make(mCoordinatorLayout,getResources().getString(R.string.delete_message),
                                Snackbar.LENGTH_SHORT)
                        .setAction(getResources().getString(R.string.undo_button),
                                new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                crime.setDeleted(false);
                                CrimeLab.get(getActivity()).updateCrime(crime);
                                mAdapter.mCrimes.add(position, crime);
                                mAdapter.notifyItemInserted(position);
                                updateUI();
                            }
                        }).setCallback(new Snackbar.Callback(){
                            @Override
                            public void onDismissed(Snackbar snackbar1, int dismissType) {
                                super.onDismissed(snackbar1, dismissType);
                                if(dismissType == DISMISS_EVENT_TIMEOUT || dismissType == DISMISS_EVENT_SWIPE
                                        || dismissType == DISMISS_EVENT_CONSECUTIVE || dismissType == DISMISS_EVENT_MANUAL) {
                                    mDeleteCallback.onCrimeIdSelected(crime.getId());
                                }
                            }
                        });
                snackbar.show();
            }

            @Override
            public void onChildDraw(@NonNull Canvas c,
                                    @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX,
                                    float dY,
                                    int actionState,
                                    boolean isCurrentlyActive) {

                View itemView = viewHolder.itemView;

                Drawable deleteIcon =
                        ContextCompat.getDrawable(getContext(), R.drawable.ic_menu_delete_crime);
                float iconHeight = deleteIcon.getIntrinsicHeight();
                float iconWidth = deleteIcon.getIntrinsicWidth();
                float itemHeight = itemView.getBottom() - itemView.getTop();

                if (actionState == ACTION_STATE_SWIPE) {
                    Log.d(TAG, "ACTION STATE SWAP is true: ");

                    int deleteIconTop = (int) (itemView.getTop() + (itemHeight - iconHeight) / 2);
                    int deleteIconBottom = (int) (deleteIconTop + iconHeight);
                    int deleteIconMargin = (int) ((itemHeight - iconHeight) / 2);
                    int deleteIconLeft = (int)(itemView.getRight() - deleteIconMargin - iconWidth);
                    int deleteIconRight = (int) itemView.getRight() - deleteIconMargin;

                    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                    // ниже нужно что-то сделать с размером layout
                    RectF layout = new RectF(itemView.getRight() + dX,
                            itemView.getTop(),
                            itemView.getRight(),
                            itemView.getBottom());

                    paint.setColor(Color.parseColor("#f44336"));
                    c.drawRect(layout, paint);
                    deleteIcon.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight,
                            deleteIconBottom);
                    deleteIcon.draw(c);
                    getDefaultUIUtil().onDraw(c, recyclerView, viewHolder.itemView, dX, dY,
                            actionState, isCurrentlyActive);
                } else {
                    Log.d(TAG, "ACTION STATE SWAP is false: ");
                }
                itemView.clearAnimation();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mCrimeRecyclerView);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE, mSubtitleVisible);
        updateUI();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallBacks = null;
        mDeleteCallback = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list, menu);

        MenuItem subtitleItem = menu.findItem(R.id.show_subtitle);
        if (mSubtitleVisible){
            subtitleItem.setTitle(R.string.hide_subtitle);
        } else {
            subtitleItem.setTitle(R.string.show_subtitle);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.new_crime:
                Crime crime = new Crime();
                CrimeLab.get(getActivity()).addCrime(crime);
                mCallBacks.onCrimeSelected(crime);
                return true;
            case R.id.show_subtitle:
                mSubtitleVisible = !mSubtitleVisible;
                getActivity().invalidateOptionsMenu();
                updateSubtitle();
                return true;
             default:
                 return super.onOptionsItemSelected(item);
        }
    }

    private void updateSubtitle(){
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        int crimeCount = crimeLab.getCrimes().size();
        String subtitle = getResources()
                .getQuantityString(R.plurals.subtitle_plural, crimeCount, crimeCount);

        if (!mSubtitleVisible){
            subtitle = null;
        }

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setSubtitle(subtitle);
    }

    public void updateUI () {
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        List<Crime> crimes = crimeLab.getCrimes();
        if (mAdapter == null) {
            mAdapter = new CrimeAdapter(crimes);
            mCrimeRecyclerView.setAdapter(mAdapter);
        }
        else if (mLastUpdatedPosition > - 1) {
            mAdapter.notifyItemChanged(mLastUpdatedPosition);
            mLastUpdatedPosition = -1;
        } else {
            mAdapter.setCrimes(crimes);
            mAdapter.notifyDataSetChanged();
        }
        updateSubtitle();
        mLayout.setVisibility(crimes.isEmpty() ? View.VISIBLE : View.GONE);
    }

    public void deleteCrime(UUID crimeId) {
        Crime crime = CrimeLab.get(getActivity()).getCrime(crimeId);
        CrimeLab.get(getActivity()).deleteCrime(crime);
    }

    private class CrimeHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView mTitleTextView;
        private TextView mDateTextView;
        private TextView mTimeTextView;
        private ImageView mSolvedImageView;
        private Crime mCrime;
        private Button mContactPoliceButton;

        public CrimeHolder(LayoutInflater inflater, ViewGroup parent, int viewType) {
            super(inflater.inflate(viewType, parent, false));
            itemView.setOnClickListener(this);

            mTitleTextView = (TextView) itemView.findViewById(R.id.crime_title);
            mDateTextView = (TextView) itemView.findViewById(R.id.crime_date);
            mTimeTextView = (TextView) itemView.findViewById(R.id.crime_time);
            mSolvedImageView = (ImageView) itemView.findViewById(R.id.crime_solved);
        }

        public void bind(Crime crime) {
            mCrime = crime;
            mTitleTextView.setText(mCrime.getTitle());

            DateFormat dateFormat =
                    new SimpleDateFormat(CrimeFragment.DATE_FORMAT, Locale.getDefault());
            DateFormat timeFormat =
                    new SimpleDateFormat(CrimeFragment.TIME_FORMAT, Locale.getDefault());
            mDateTextView.setText(dateFormat.format(mCrime.getDate()));
            mTimeTextView.setText(timeFormat.format(mCrime.getDate()));

            mSolvedImageView.setVisibility(crime.isSolved() ? View.VISIBLE : View.GONE);

            if (mCrime.isSerious()) {
                mContactPoliceButton = (Button) itemView.findViewById(R.id.contact_police);
                mContactPoliceButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:112"));
                        startActivity(callIntent);
                    }
                });
            }
        }

        @Override
        public void onClick(View view) {
            mCallBacks.onCrimeSelected(mCrime);
            mLastUpdatedPosition = this.getAdapterPosition();
        }
    }

    private class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder> {
        private List<Crime> mCrimes;

        public CrimeAdapter(List<Crime> crimes) {
            mCrimes = crimes;
        }

        @Override
        public void onBindViewHolder(CrimeHolder holder, int position) {
            Crime crime = mCrimes.get(position);
            holder.bind(crime);
        }

        @Override
        public CrimeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new CrimeHolder(layoutInflater, parent, viewType);
        }

        @Override
        public int getItemCount() {
            return mCrimes.size();
        }

        public void setCrimes(List<Crime> crimes) {
            mCrimes = crimes;
        }

        @Override
        public int getItemViewType(int position) {
            if (mCrimes.get(position).isSerious()) {
                return R.layout.list_item_crime_police;
            } else {
                return R.layout.list_item_crime;
            }
        }
    }
}
