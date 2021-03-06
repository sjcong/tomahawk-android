/* == This file is part of Tomahawk Player - <http://tomahawk-player.org> ===
 *
 *   Copyright 2013, Enno Gottschalk <mrmaffen@googlemail.com>
 *
 *   Tomahawk is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Tomahawk is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Tomahawk. If not, see <http://www.gnu.org/licenses/>.
 */
package org.tomahawk.tomahawk_android.utils;

import org.tomahawk.libtomahawk.collection.Track;
import org.tomahawk.libtomahawk.resolver.spotify.LibSpotifyWrapper;
import org.tomahawk.tomahawk_android.TomahawkApp;

import android.media.AudioManager;
import android.media.MediaPlayer;

import java.io.IOException;

/**
 * Author Enno Gottschalk <mrmaffen@googlemail.com> Date: 23.06.13
 */
public class TomahawkMediaPlayer
        implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    private MediaPlayer mMediaPlayer;

    private boolean mUseMediaPlayer;

    private boolean mIsPreparing;

    private boolean mIsPlaying;

    private OnPreparedListener mOnPreparedListener;

    private OnErrorListener mOnErrorListener;

    private OnCompletionListener mOnCompletionListener;


    public TomahawkMediaPlayer() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnCompletionListener(this);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (mUseMediaPlayer) {
            onPrepared();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        if (mUseMediaPlayer) {
            return onError(what, extra);
        } else {
            return false;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mUseMediaPlayer) {
            onCompletion();
        }
    }

    public void onPrepared() {
        mIsPreparing = false;
        mOnPreparedListener.onPrepared(this);
    }

    public boolean onError(int what, int extra) {
        return mOnErrorListener.onError(this, what, extra);
    }

    public void onCompletion() {
        mOnCompletionListener.onCompletion(this);
    }


    public int getCurrentPosition() {
        if (mUseMediaPlayer) {
            return mMediaPlayer.getCurrentPosition();
        } else {
            return LibSpotifyWrapper.getCurrentPosition();
        }
    }

    public void setVolume(float leftVolume, float rightVolume) {
        mMediaPlayer.setVolume(leftVolume, rightVolume);
    }

    public void release() {
        mMediaPlayer.release();
    }

    public void setOnPreparedListener(OnPreparedListener listener) {
        mOnPreparedListener = listener;
    }

    public void setOnErrorListener(OnErrorListener listener) {
        mOnErrorListener = listener;
    }

    public void setOnCompletionListener(OnCompletionListener listener) {
        mOnCompletionListener = listener;
    }

    public boolean isPlaying() {
        return mIsPlaying;
    }

    public void start() throws IllegalStateException {
        mIsPlaying = true;
        if (mUseMediaPlayer) {
            try {
                mMediaPlayer.start();
            } catch (IllegalStateException e) {
                throw e;
            }
        } else {
            LibSpotifyWrapper.play();
        }
    }

    public void pause() throws IllegalStateException {
        mIsPlaying = false;
        if (mUseMediaPlayer) {
            try {
                mMediaPlayer.pause();
            } catch (IllegalStateException e) {
                throw e;
            }
        } else {
            LibSpotifyWrapper.pause();
        }
    }

    public void stop() throws IllegalStateException {
        mIsPlaying = false;
        if (mUseMediaPlayer) {
            try {
                mMediaPlayer.stop();
            } catch (IllegalStateException e) {
                throw e;
            }
        } else {
            LibSpotifyWrapper.pause();
        }
    }

    public void seekTo(int msec) throws IllegalStateException {
        if (mUseMediaPlayer) {
            try {
                mMediaPlayer.seekTo(msec);
            } catch (IllegalStateException e) {
                throw e;
            }
        } else {
            LibSpotifyWrapper.seek(msec);
        }
    }

    public void prepare(Track track) throws IllegalStateException, IOException {
        mIsPreparing = true;
        try {
            if (!track.isLocal() && track.getResolver() != null
                    && track.getResolver().getId() == TomahawkApp.RESOLVER_ID_SPOTIFY) {
                mUseMediaPlayer = false;
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                }
                mMediaPlayer.reset();
                LibSpotifyWrapper.prepare(track.getPath(), this);
            } else {
                mUseMediaPlayer = true;
                LibSpotifyWrapper.pause();
                mMediaPlayer.setDataSource(track.getPath());
                mMediaPlayer.prepare();
            }
        } catch (IllegalStateException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        }
    }

    public boolean isPreparing() {
        return mIsPreparing;
    }
}
