package com.bignerdranch.android.criminalintent;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class CrimeLab {

    private static CrimeLab sCrimeLab;
    private HashMap<UUID, Crime> mCrimesHashMap;
    private List<Crime> mCrimesList;

    public static CrimeLab get(Context context) {
        if (sCrimeLab == null) {
            sCrimeLab = new CrimeLab(context);
        }
        return  sCrimeLab;
    }

    private CrimeLab(Context context) {
        mCrimesHashMap = new HashMap<>();
        mCrimesList = new ArrayList<>();
    }

    public void addCrime(Crime crime){
        mCrimesList.add(crime);
        mCrimesHashMap.put(crime.getId(), crime);
    }

    public void deleteCrime(Crime crime){
        mCrimesList.remove(crime);
        mCrimesHashMap.remove(crime.getId());
    }

    public List<Crime> getCrimes() {
        return mCrimesList;
    }

    public Crime getCrime(UUID id) {
        return mCrimesHashMap.get(id);
    }
}
