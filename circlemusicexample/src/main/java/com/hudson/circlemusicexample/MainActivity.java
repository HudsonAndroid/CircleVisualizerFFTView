package com.hudson.circlemusicexample;


import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.hudson.circlevisualizerfftview.CircleVisualizerFFTView;

import java.io.FileNotFoundException;

public class MainActivity extends Activity {
    private Button play;
    private static final int REQUESTCODE = 2;
    private MediaPlayer mediaPlayer;
    protected boolean isRunning;
    private Visualizer mVisualizer;
    private CircleVisualizerFFTView mFftView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        mFftView = (CircleVisualizerFFTView) findViewById(R.id.fft);
        play = (Button) findViewById(R.id.play);
        play.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent openFileExplorerIntent = new Intent(Intent.ACTION_GET_CONTENT);
                openFileExplorerIntent.setType("*/*");
                openFileExplorerIntent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(openFileExplorerIntent, REQUESTCODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==REQUESTCODE&&resultCode==RESULT_OK){
            Uri uri = data.getData();
            String[] proj = {MediaStore.Audio.Media.DATA};
            Cursor cursor = managedQuery(uri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
            cursor.moveToFirst();
            String musicpath = cursor.getString(column_index);
            if(musicpath.endsWith(".mp3")){
                play(musicpath);
                play.setVisibility(View.GONE);
            }else {
                Toast.makeText(this, "所选并不是音乐文件！", Toast.LENGTH_LONG).show();
            }
            if(Integer.parseInt(Build.VERSION.SDK) < 14)
            {
                cursor.close();
            }
        }
    }

    /**
     * 播放
     *
     */
    public void play(String path) {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(path);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.prepare();
            mediaPlayer.start();
            initVisualizer();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    play.setVisibility(View.VISIBLE);
                    mVisualizer.setEnabled(false);
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    isRunning = false;
                    mediaPlayer = null;
                }
            });
            isRunning = true;
        } catch(FileNotFoundException e){
            e.printStackTrace();
            Toast.makeText(this, "文件没有找到!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initVisualizer() {
        mVisualizer = new Visualizer(mediaPlayer.getAudioSessionId());
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform,
                                              int samplingRate) {
            }

            //这个回调采集的是快速傅里叶变换有关的数据,fft就是采集到的byte数据（频域波形图）
            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] fft,
                                         int samplingRate) {
                mFftView.updateVisualizer(fft);
            }
        }, Visualizer.getMaxCaptureRate() / 2, true, true);
        mVisualizer.setEnabled(true);
    }

    @Override
    protected void onDestroy() {
        isRunning = false;
        if(mediaPlayer!=null&&mediaPlayer.isPlaying()){
            mVisualizer.setEnabled(false);
            mVisualizer = null;
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }
}
