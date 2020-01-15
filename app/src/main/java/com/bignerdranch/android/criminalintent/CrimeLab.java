package com.bignerdranch.android.criminalintent;

import android.content.Context;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

public class CrimeLab {

    private static CrimeLab sCrimeLab;
    private Hashtable<UUID, Crime> mCrimesHashTable;
    private List<Crime> mCrimesList;

    public static CrimeLab get(Context context) {
        if (sCrimeLab == null) {
            sCrimeLab = new CrimeLab(context);
        }
        return  sCrimeLab;
    }

    private CrimeLab(Context context) {
        mCrimesHashTable = new Hashtable<>();
        mCrimesList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Crime crime = new Crime();

            crime.setTitle("Crime №" + (i + 1));
            crime.setSolved(i % 2 == 0);
            crime.setRequiresPolice(i % 3 == 0);

            mCrimesHashTable.put(crime.getId(), crime);
            mCrimesList.add(crime);
        }
    }

    public List<Crime> getCrimes() {
        return mCrimesList;
    }

    public Crime getCrime(UUID id) {
        return mCrimesHashTable.get(id);
    }
}
