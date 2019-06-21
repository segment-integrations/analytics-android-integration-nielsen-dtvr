package com.segment.analytics.android.integrations.nielsendtvr.sampleapp;

import android.media.MediaPlayer;
import android.media.TimedMetaData;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.MediaController;

import java.nio.charset.StandardCharsets;

import static android.media.MediaPlayer.MEDIA_INFO_BUFFERING_END;
import static android.media.MediaPlayer.MEDIA_INFO_BUFFERING_START;
import static android.media.MediaPlayer.OnPreparedListener;
import static android.media.MediaPlayer.OnCompletionListener;
import static android.media.MediaPlayer.OnInfoListener;
import static android.media.MediaPlayer.OnTimedMetaDataAvailableListener;
import static android.media.MediaPlayer.OnSeekCompleteListener;

public class VideoActivity extends AppCompatActivity implements OnPreparedListener, OnCompletionListener,
        OnInfoListener, OnTimedMetaDataAvailableListener, OnSeekCompleteListener {

    private AnalyticsVideoView videoView;
    private AnalyticsController analytics;
    private int savedProgress;
    private boolean isPlaying = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        initAnalytics();
        initVideoView();
        initControls();
    }

    @Override
    protected void onPause() {
        super.onPause();

        saveProgress();
    }

    private void initAnalytics() {
        videoView = findViewById(R.id.video);
        analytics = ((AnalyticsApplication) getApplication()).getAnalytics();
        videoView.setAnalytics(analytics);
    }

    private void initVideoView() {
        videoView.setOnPreparedListener(this);
        videoView.setOnCompletionListener(this);
        videoView.setOnInfoListener(this);
        videoView.setVideoURI(Uri.parse("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"));
    }

    private void initControls() {
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
    }

    private void saveProgress() {
        if (isFinishing()) {
            analytics.trackEnd();
        } else {
            savedProgress = videoView.getCurrentPosition();
            isPlaying = videoView.isPlaying();
            analytics.trackApplicationBackgrounded();
        }
    }

    private void resumeProgress(MediaPlayer mp) {
        if (savedProgress > 0) {
            videoView.seekTo(savedProgress);
        }
        if (isPlaying) {
            if (videoView.isResuming()) {
                analytics.trackPlaybackResumed();
            } else {
                analytics.trackContentStart();
                videoView.setResuming();
            }
            mp.start();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.setOnSeekCompleteListener(this);
        mp.setOnTimedMetaDataAvailableListener(this);

        resumeProgress(mp);
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MEDIA_INFO_BUFFERING_START:
                analytics.trackBufferStarted();
                break;
            case MEDIA_INFO_BUFFERING_END:
                analytics.trackBufferCompleted();
                break;
        }

        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        analytics.trackContentCompleted();
        analytics.trackEnd();
        videoView.clearState();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        analytics.trackSeekCompleted();
    }

    @Override
    public void onTimedMetaDataAvailable(MediaPlayer mp, TimedMetaData data) {
        if (data != null && mp.isPlaying()) {
            byte[] metadata = data.getMetaData();

            if (metadata != null) {
                String iD3Payload = new String(metadata, StandardCharsets.UTF_8);

                int index = iD3Payload.indexOf("www.nielsen.com");
                if (index != -1) {
                    String id3String = iD3Payload.substring(index, (index + 249));
                    analytics.trackID3(id3String);
                }
            }
        }
    }
}
