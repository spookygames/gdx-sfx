/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Spooky Games
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
package net.spookygames.gdx.sfx.android;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.backends.android.AndroidMusic;
import com.badlogic.gdx.files.FileHandle;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import net.spookygames.gdx.sfx.MusicDurationResolver;
import net.spookygames.gdx.sfx.SfxMusicLoader;
import net.spookygames.gdx.sfx.SfxSoundLoader;
import net.spookygames.gdx.sfx.SoundDurationResolver;

public class AndroidAudioDurationResolver implements MusicDurationResolver, SoundDurationResolver {

	private final String namespace;

	public AndroidAudioDurationResolver(String namespace) {
		super();
		this.namespace = namespace;
	}

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
			} catch (IllegalArgumentException ex) {
				Gdx.app.error("gdx-sfx", "Unable to resolve duration of sound file " + soundFile.toString(), ex);
			}
		}

		return -1f;
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
	private float sdk10Duration(FileHandle soundFile) {
		String mediaPath = Uri.parse("android.resource://" + namespace + "/raw/" + soundFile.path()).getPath();
		MediaMetadataRetriever mmr = new MediaMetadataRetriever();
		float duration = -1f;
		try {
			mmr.setDataSource(mediaPath);
			String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
			if (durationStr != null) {
				duration = Integer.parseInt(durationStr) / 1000f;
			}
		} finally {
			mmr.release();
		}
		return duration;
	}

	public static void initialize(String namespace) {
		AndroidAudioDurationResolver resolver = new AndroidAudioDurationResolver(namespace);
		SfxMusicLoader.setDurationResolver(resolver);
		SfxSoundLoader.setDurationResolver(resolver);
	}

}
