/*
b * Copyright 2013 Simon Willeke
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

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mobvoi.android.common.ConnectionResult;
import com.mobvoi.android.common.api.MobvoiApiClient;
import com.mobvoi.android.common.api.ResultCallback;
import com.mobvoi.android.wearable.MessageApi;
import com.mobvoi.android.wearable.MessageEvent;
import com.mobvoi.android.wearable.Wearable;
import com.mobvoi.knowledgegraph.wearcontroltetris.BlockBoardView;
import com.mobvoi.knowledgegraph.wearcontroltetris.Bytes;
import com.mobvoi.knowledgegraph.wearcontroltetris.GameSetting;
import com.mobvoi.knowledgegraph.wearcontroltetris.R;
import com.mobvoi.knowledgegraph.wearcontroltetris.RecognizeResult;
import com.mobvoi.knowledgegraph.wearcontroltetris.WorkThread;
import com.mobvoi.knowledgegraph.wearcontroltetris.components.Controls;
import com.mobvoi.knowledgegraph.wearcontroltetris.components.Display;
import com.mobvoi.knowledgegraph.wearcontroltetris.components.GameState;
import com.mobvoi.knowledgegraph.wearcontroltetris.components.Sound;

import java.io.UnsupportedEncodingException;

import static com.mobvoi.knowledgegraph.wearcontroltetris.Constants.GAME_DATA_PATH;
import static com.mobvoi.knowledgegraph.wearcontroltetris.Constants.GAME_START_PATH;
import static com.mobvoi.knowledgegraph.wearcontroltetris.Constants.GAME_START_RESP_PATH;
import static com.mobvoi.knowledgegraph.wearcontroltetris.Constants.GAME_STOP_PATH;

public class GameActivity extends FragmentActivity  implements
        MessageApi.MessageListener, MobvoiApiClient.ConnectionCallbacks, MobvoiApiClient.OnConnectionFailedListener {
    public static final String TAG="GameActivity";
	public Sound sound;
	public Controls controls;
	public Display display;
	public GameState game;
	private WorkThread mainThread;
	private DefeatDialogFragment dialog;
	private boolean layoutSwap;

	public static final int NEW_GAME = 0;
	public static final int RESUME_GAME = 1;

    private Handler handler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_layoutswap", false)) {
			setContentView(R.layout.activity_game_alt);
			layoutSwap = true;
		} else {
			setContentView(R.layout.activity_game);
			layoutSwap = false;
		}

		/* Read Starting Arguments */
		Bundle b = getIntent().getExtras();
		int value = NEW_GAME;
		
		/* Create Components */
		game = (GameState)getLastCustomNonConfigurationInstance();
		if(game == null) {
			/* Check for Resuming (or Resumption?) */
			if(b!=null)
				value = b.getInt("mode");
				
			if((value == NEW_GAME)) {
				game = GameState.getNewInstance(this);
				game.setLevel(b.getInt("level"));
			} else
				game = GameState.getInstance(this);
		}
		game.reconnect(this);
		dialog = new DefeatDialogFragment();
		controls = new Controls(this);
		display = new Display(this);
		sound = new Sound(this);
		
		/* Init Components */
		if(game.isResumable())
			sound.startMusic(Sound.GAME_MUSIC, game.getSongtime());
		sound.loadEffects();
		if(b!=null){
			value = b.getInt("mode");
			if(b.getString("playername") != null)
				game.setPlayerName(b.getString("playername"));
		} else 
			game.setPlayerName(getResources().getString(R.string.anonymous));
		dialog.setCancelable(false);
		if(!game.isResumable())
			gameOver(game.getScore(), game.getTimeString(), game.getAPM());
		
		/* Register Button callback Methods */
		((Button)findViewById(R.id.pausebutton_1)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				GameActivity.this.finish();
			}
		});
		((BlockBoardView)findViewById(R.id.boardView)).setOnTouchListener(new OnTouchListener() {
		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
		        if(event.getAction() == MotionEvent.ACTION_DOWN) {
		        	controls.boardPressed(event.getX(), event.getY());
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
		        	controls.boardReleased();
		        }
		        return true;
		    }
		});
		((ImageButton)findViewById(R.id.rightButton)).setOnTouchListener(new OnTouchListener() {
		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
		        if(event.getAction() == MotionEvent.ACTION_DOWN) {
		        	controls.rightButtonPressed();
		        	((ImageButton)findViewById(R.id.rightButton)).setPressed(true);
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
		        	controls.rightButtonReleased();
		        	((ImageButton)findViewById(R.id.rightButton)).setPressed(false);
		        }
		        return true;
		    }
		});
		((ImageButton)findViewById(R.id.leftButton)).setOnTouchListener(new OnTouchListener() {
		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
		        if(event.getAction() == MotionEvent.ACTION_DOWN) {
		        	controls.leftButtonPressed();
		        	((ImageButton)findViewById(R.id.leftButton)).setPressed(true);
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
		        	controls.leftButtonReleased();
		        	((ImageButton)findViewById(R.id.leftButton)).setPressed(false);
		        }
		        return true;
		    }
		});
		((ImageButton)findViewById(R.id.softDropButton)).setOnTouchListener(new OnTouchListener() {
		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
		        if(event.getAction() == MotionEvent.ACTION_DOWN) {
		        	controls.downButtonPressed();
		        	((ImageButton)findViewById(R.id.softDropButton)).setPressed(true);
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
		        	controls.downButtonReleased();
		        	((ImageButton)findViewById(R.id.softDropButton)).setPressed(false);
		        }
		        return true;
		    }
		});
		((ImageButton)findViewById(R.id.hardDropButton)).setOnTouchListener(new OnTouchListener() {
		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
		        if(event.getAction() == MotionEvent.ACTION_DOWN) {
		        	controls.dropButtonPressed();
		        	((ImageButton)findViewById(R.id.hardDropButton)).setPressed(true);
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
		        	controls.dropButtonReleased();
		        	((ImageButton)findViewById(R.id.hardDropButton)).setPressed(false);
		        }
		        return true;
		    }
		});
		((ImageButton)findViewById(R.id.rotateRightButton)).setOnTouchListener(new OnTouchListener() {
		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
		        if(event.getAction() == MotionEvent.ACTION_DOWN) {
		        	controls.rotateRightPressed();
		        	((ImageButton)findViewById(R.id.rotateRightButton)).setPressed(true);
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
		        	controls.rotateRightReleased();
		        	((ImageButton)findViewById(R.id.rotateRightButton)).setPressed(false);
		        }
		        return true;
		    }
		});
		((ImageButton)findViewById(R.id.rotateLeftButton)).setOnTouchListener(new OnTouchListener() {
		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
		        if(event.getAction() == MotionEvent.ACTION_DOWN) {
		        	controls.rotateLeftPressed();
		        	((ImageButton)findViewById(R.id.rotateLeftButton)).setPressed(true);
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
		        	controls.rotateLeftReleased();
		        	((ImageButton)findViewById(R.id.rotateLeftButton)).setPressed(false);
		        }
		        return true;
		    }
		});

		((BlockBoardView)findViewById(R.id.boardView)).init();
		((BlockBoardView)findViewById(R.id.boardView)).setHost(this);

        //added by LiLi
        mApiClient = new MobvoiApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                        // Request access only to the Wearable API
                .addApi(Wearable.API)
                .build();

        handler=new Handler();
	}
	
	/**
	 * Called by BlockBoardView upon completed creation
	 * @param caller
	 */
	public void startGame(BlockBoardView caller){
		mainThread = new WorkThread(this, caller.getHolder()); 
		mainThread.setFirstTime(false);
		game.setRunning(true);
		mainThread.setRunning(true);
		mainThread.start();
	}

	/**
	 * Called by BlockBoardView upon destruction
	 */
	public void destroyWorkThread() {
        boolean retry = true;
        mainThread.setRunning(false);
        while (retry) {
            try {
            	mainThread.join();
                retry = false;
            } catch (InterruptedException e) {
                
            }
        }
	}
	
	/**
	 * Called by GameState upon Defeat
	 * @param score
	 */
	public void putScore(long score) {
		String playerName = game.getPlayerName();
		if(playerName == null || playerName.equals(""))
			playerName = getResources().getString(R.string.anonymous);//"Anonymous";
		
		Intent data = new Intent();
		data.putExtra(MainActivity.PLAYERNAME_KEY, playerName);
		data.putExtra(MainActivity.SCORE_KEY, score);
		setResult(RESULT_OK, data);
		
		finish();
	}
	
	@Override
	protected void onPause() {
    	super.onPause();
    	sound.pause();
    	sound.setInactive(true);
    	game.setRunning(false);

        //added by LiLi
        this.stopSensorData();
        mApiClient.disconnect();
	};
    
    @Override
    protected void onStop() {
    	super.onStop();
    	sound.pause();
    	sound.setInactive(true);
    };
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	game.setSongtime(sound.getSongtime());
    	sound.release();
    	sound = null;
    	game.disconnect();
    };

    
    @Override
    protected void onResume() {
    	super.onResume();
    	sound.resume();
    	sound.setInactive(false);
    	
    	/* Check for changed Layout */
    	boolean tempswap = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_layoutswap", false);
		if(layoutSwap != tempswap) {
			layoutSwap = tempswap;
			if(layoutSwap) {
				setContentView(R.layout.activity_game_alt);
			} else {
				setContentView(R.layout.activity_game);
			}
		}
        //added by LiLi
        mApiClient.connect();

    };
    
    @Override
    public Object onRetainCustomNonConfigurationInstance () {
        return game;
    }
	
	public void gameOver(long score, String gameTime, int apm) {
		dialog.setData(score, gameTime, apm);
		dialog.show(getSupportFragmentManager(), "hamster");
	}


    //added by LiLi
    private MobvoiApiClient mApiClient;

    private void stopSensorData(){
        Wearable.MessageApi.sendMessage(
                mApiClient, "default_node", GAME_STOP_PATH, new byte[0]).setResultCallback(
                new ResultCallback<MessageApi.SendMessageResult>() {
                    @Override
                    public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                        if (!sendMessageResult.getStatus().isSuccess()) {
                            Log.e(TAG, "Failed to send message with status code: "
                                    + sendMessageResult.getStatus().getStatusCode());
                            Toast.makeText(GameActivity.this, "发送消息失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private volatile boolean waitingWearResp=false;

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected");
        Wearable.MessageApi.addListener(mApiClient, this);
        waitingWearResp=true;
        boolean leftHand = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_leftHand", false);
        GameSetting setting=new GameSetting();
        setting.watchInLeftWrist=leftHand;
        byte[] data=new byte[0];
        try {
            data=gson.toJson(setting).getBytes("UTF8");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        Wearable.MessageApi.sendMessage(
                mApiClient, "default_node", GAME_START_PATH, data).setResultCallback(
                new ResultCallback<MessageApi.SendMessageResult>() {
                    @Override
                    public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                        if (!sendMessageResult.getStatus().isSuccess()) {
                            Log.e(TAG, "Failed to send message with status code: "
                                    + sendMessageResult.getStatus().getStatusCode());
                            Toast.makeText(GameActivity.this, "发送消息失败，请重启app再尝试", Toast.LENGTH_SHORT).show();
                        }else{

                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if(waitingWearResp){
                                        Toast.makeText(GameActivity.this, "发送消息失败，请先启动手表端app",Toast.LENGTH_SHORT).show();
                                        waitingWearResp=false;
                                    }
                                }
                            }, 5000);
                        }
                    }
                }
        );
    }

    @Override
    public void onConnectionSuspended(int cause) {

    }

    private Gson gson=new Gson();
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        //Log.d(TAG, "onMessageReceived: "+messageEvent);
        if(messageEvent.getPath().equals(GAME_DATA_PATH)){
            String s= Bytes.toString(messageEvent.getData());
            RecognizeResult rr=gson.fromJson(s, RecognizeResult.class);
            switch (rr.direction){
                case RecognizeResult.DIRECTION_LEFT:
                    controls.leftButtonPressed();
                    controls.leftButtonReleased();
                    break;
                case RecognizeResult.DIRECTION_RIGHT:
                    controls.rightButtonPressed();
                    controls.rightButtonReleased();
                    break;
                case RecognizeResult.DIRECTION_UP:
                    controls.rotateRightPressed();
                    controls.rotateRightReleased();
                    break;
                case RecognizeResult.DIRECTION_DOWN:
                    //TODO replace with drop
                    controls.downButtonPressed();
                    controls.downButtonReleased();
                    break;
                case RecognizeResult.DIRECTION_DROP:
                    controls.dropButtonPressed();
                    controls.downButtonReleased();
                    break;
            }
        }else if(messageEvent.getPath().equals(GAME_START_RESP_PATH)){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
            });
            game.setRunning(true);
            waitingWearResp=false;
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {

    }
}
