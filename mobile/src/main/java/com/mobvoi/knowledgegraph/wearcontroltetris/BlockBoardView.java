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

package com.mobvoi.knowledgegraph.wearcontroltetris;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import com.mobvoi.knowledgegraph.wearcontroltetris.activities.GameActivity;


public class BlockBoardView extends SurfaceView implements Callback {

	private GameActivity host;
	
	public BlockBoardView(Context context) {
		super(context);
	}

	public BlockBoardView(Context context, AttributeSet attrs) {
		super(context,attrs);
	}

	public BlockBoardView(Context context, AttributeSet attrs, int defStyle) {
		super(context,attrs,defStyle);
	}
	
	public void setHost(GameActivity ga) {
		host = ga;
	}
	
	public void init() {
		setZOrderOnTop(true);
		getHolder().addCallback(this);
		getHolder().setFormat(PixelFormat.TRANSPARENT);
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		host.startGame(this);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		host.destroyWorkThread();
	}
}

