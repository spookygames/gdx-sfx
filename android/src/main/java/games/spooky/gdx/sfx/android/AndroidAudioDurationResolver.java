/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-2021 Spooky Games
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package games.spooky.gdx.sfx.android;

import java.io.FileDescriptor;
import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.backends.android.AndroidFileHandle;
import com.badlogic.gdx.backends.android.AndroidMusic;
import com.badlogic.gdx.files.FileHandle;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.res.AssetFileDescriptor;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import games.spooky.gdx.sfx.MusicDurationResolver;
import games.spooky.gdx.sfx.SfxMusicLoader;
import games.spooky.gdx.sfx.SfxSoundLoader;
import games.spooky.gdx.sfx.SoundDurationResolver;

public class AndroidAudioDurationResolver implements MusicDurationResolver, SoundDurationResolver {

	@Override
	public float resolveMusicDuration(Music music, FileHandle musicFile) {
		return ((AndroidMusic) music).getDuration();
	}

	@SuppressLint("NewApi")
	@Override
	public float resolveSoundDuration(Sound sound, FileHandle soundFile) {
		// TODO Change the world, make this happen
		// return ((AndroidSound)sound).getDuration();

		if (Build.VERSION.SDK_INT >= 10) {
			try {
				return sdk10Duration(soundFile);
			} catch (Exception ex) {
				Gdx.app.error("gdx-sfx", "Unable to resolve duration of sound file " + soundFile.toString(), ex);
			}
		}

		return -1f;
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
	private float sdk10Duration(FileHandle soundFile) throws IOException {

		float duration = -1f;

		MediaMetadataRetriever mmr = new MediaMetadataRetriever();
		try {
			AndroidFileHandle androidFile = (AndroidFileHandle) soundFile;
			AssetFileDescriptor fd = androidFile.getAssetFileDescriptor();
			FileDescriptor descriptor = fd.getFileDescriptor();

			if (fd.getDeclaredLength() < 0) {
				mmr.setDataSource(descriptor);
			} else {
				mmr.setDataSource(descriptor, fd.getStartOffset(), fd.getDeclaredLength());
			}

			String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
			if (durationStr != null) {
				duration = Integer.parseInt(durationStr) / 1000f;
			}
		} finally {
			mmr.release();
		}

		return duration;
	}

	public static void initialize() {
		AndroidAudioDurationResolver resolver = new AndroidAudioDurationResolver();
		SfxMusicLoader.setDurationResolver(resolver);
		SfxSoundLoader.setDurationResolver(resolver);
	}

}
