package edu.berkeley.cs.amplab.carat.android.fragments;

import android.support.v4.app.Fragment;

public abstract class ExtendedTitleFragment extends Fragment{

	@Override
	public void onResume() {
		super.onResume();
		getActivity().setTitle(getTag());
	}	
	
}
