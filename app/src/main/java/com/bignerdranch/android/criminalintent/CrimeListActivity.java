package com.bignerdranch.android.criminalintent;

import android.content.Intent;

import androidx.fragment.app.Fragment;

import java.util.UUID;


public class CrimeListActivity extends SingleFragmentActivity
    implements CrimeListFragment.Callbacks, CrimeFragment.Callbacks, CrimeListFragment.OnDeleteCrimeListener {

    @Override
    protected Fragment createFragment() {
        return new CrimeListFragment();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_masterdetail;
    }

    @Override
    public void onCrimeSelected(Crime crime) {
        if (findViewById(R.id.detail_fragment_container) == null) {
            Intent intent = CrimePagerActivity.newIntent(this, crime.getId());
            startActivity(intent);
        } else {
            Fragment newDetail = CrimeFragment.newInstance(crime.getId());
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_fragment_container, newDetail)
                    .commit();
        }
    }

    @Override
    public void onCrimeUpdated(Crime crime) {
        CrimeListFragment listFragment = (CrimeListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        listFragment.updateUI();
    }

    @Override
    public void onCrimeIdSelected(UUID crimeId) {
        CrimeFragment crimeFragment = (CrimeFragment) getSupportFragmentManager().findFragmentById(R.id.detail_fragment_container);
        CrimeListFragment listFragment = (CrimeListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        listFragment.deleteCrime(crimeId);
        listFragment.updateUI();
        if (crimeFragment == null){
            return;
        } else {
            listFragment
                    .getActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .remove(crimeFragment)
                    .commit();
        }
    }
}
