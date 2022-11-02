/*
 * Android Beeper. MIT (c) 2022 miktim@mail.ru
 *
 * Release notes:
 *  - beeps are queued;
 *  - in Android 4.0 the volume of the beep depends on the system volume,
 *    in Android 7.0 it is the absolute volume.
 * See:
 *   https://developer.android.com/reference/android/media/ToneGenerator
 *   https://stackoverflow.com/questions/29509010/how-to-play-a-short-beep-to-android-phones-loudspeaker-programmatically
 *   https://stackoverflow.com/questions/11964623/audioflinger-could-not-create-track-status-12
 */
package org.miktim;

import android.media.ToneGenerator;

import static android.media.AudioManager.STREAM_MUSIC;
import static android.media.AudioManager.STREAM_NOTIFICATION;

public final class Beeper {
    public static final int DEFAULT_TONE = ToneGenerator.TONE_CDMA_ABBR_ALERT;
    public static final int DEFAULT_VOLUME = 75; //(AudioManager.STREAM_SYSTEM * ToneGenerator.MAX_VOLUME) / 15;
    public static final int DEFAULT_DURATION = 100;

    private static final int TONE_PAUSE = 0xffffffff;
    private static final Thread DEFAULT_THREAD = new Thread();
    private static Thread sBeepThread = DEFAULT_THREAD;

    public static void beep() {
        beep(DEFAULT_VOLUME);
    }

    public static void beep(int volume) {
        beep(DEFAULT_TONE, volume, DEFAULT_DURATION);
    }

    public static void beep(int volume, int duration) {
        beep(DEFAULT_TONE, volume, duration);
    }

    synchronized public static void beep(int toneType, int volume, int durationMs) {
        (sBeepThread = new BeepGenerator(toneType, volume, durationMs)).start();
    }

    public static void pause(int durationMs) {
        beep(TONE_PAUSE, 0, durationMs);
    }

    public static void cancel() {
        sBeepThread.interrupt();
    }

    public static void await() {
        try {
            sBeepThread.join();
        } catch (InterruptedException ignore) {
        }
    }

    private static class BeepGenerator extends Thread {
        private int mTone;
        private int mVolume;
        private int mDuration;
        private Thread mPrevBeepThread = sBeepThread;

        BeepGenerator(int toneType, int volume, int durationMs) {
            mTone = toneType;
            mVolume = volume;
            mDuration = durationMs;
        }

        public void run() {
            ToneGenerator toneGen = null;
            try {
                mPrevBeepThread.join();
                if (mTone != TONE_PAUSE) {
//                    toneGen = new ToneGenerator(STREAM_MUSIC, mVolume);
                    toneGen = new ToneGenerator(STREAM_NOTIFICATION, mVolume);
                    toneGen.startTone(mTone, mDuration);
                }
                Thread.sleep(mDuration);
            } catch (InterruptedException ie) {
                mPrevBeepThread.interrupt();
            } catch (RuntimeException ignored) {
                ignored.printStackTrace();
            }
            if (toneGen != null) {
                toneGen.stopTone();
                toneGen.release();
            }
            mPrevBeepThread = DEFAULT_THREAD; // free beep thread
        }
    }

}
