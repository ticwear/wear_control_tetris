/*
 * Copyright 2013 Simon Willeke
 * contact: hamstercount@hotmail.com
 */

/*
    This file is part of Blockinger.

    Blockinger is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Blockinger is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Blockinger.  If not, see <http://www.gnu.org/licenses/>.

 */

package com.mobvoi.knowledgegraph.wearcontroltetris.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.mobvoi.knowledgegraph.wearcontroltetris.R;


public class DefeatDialogFragment extends DialogFragment {

	private CharSequence scoreString;
	private CharSequence timeString;
	private CharSequence apmString;
	private long score;
	
	public DefeatDialogFragment() {
		super();
		scoreString = "unknown";
		timeString = "unknown";
		apmString = "unknown";
	}
	
	public void setData(long scoreArg, String time, int apm) {
		scoreString = String.valueOf(scoreArg);
		timeString = time;
		apmString = String.valueOf(apm);
		score = scoreArg;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstance) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.defeatDialogTitle);
		builder.setMessage(
				getResources().getString(R.string.scoreLabel) +
				"\n    " + scoreString + "\n\n" +
				getResources().getString(R.string.timeLabel) +
				"\n    " + timeString + "\n\n" +
				getResources().getString(R.string.apmLabel) +
				"\n    " + apmString + "\n\n" +
				getResources().getString(R.string.hint)
				);
		builder.setNeutralButton(R.string.defeatDialogReturn, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				((GameActivity)getActivity()).putScore(score);
			}
		});
		return builder.create();
	}
}
