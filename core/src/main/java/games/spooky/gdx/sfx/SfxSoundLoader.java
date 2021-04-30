/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-2019 Spooky Games
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
package games.spooky.gdx.sfx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

@SuppressWarnings("rawtypes")
public class SfxSoundLoader
		extends AsynchronousAssetLoader<SfxSound, SfxSoundLoader.SoundParameters> {

	private static SoundDurationResolver durationResolver = null;

	public static void setDurationResolver(SoundDurationResolver soundDurationResolver) {
		durationResolver = soundDurationResolver;
	}

	private final float defaultDuration;

	private Sound sound;

	public SfxSoundLoader(FileHandleResolver resolver) {
		this(resolver, 1f);
	}

	public SfxSoundLoader(FileHandleResolver resolver, float defaultDuration) {
		super(resolver);
		this.defaultDuration = defaultDuration;
	}

	@Override
	public void loadAsync(AssetManager manager, String fileName, FileHandle file, SoundParameters parameter) {
		sound = Gdx.audio.newSound(file);
	}

	@Override
	public SfxSound loadSync(AssetManager manager, String fileName, FileHandle file, SoundParameters parameter) {

		// Content
		Sound sound = this.sound;
		this.sound = null;

		// Title
		String title = null;
		if (parameter != null)
			title = parameter.title;
		if (title == null)
			title = fileName;

		// Duration
		float duration = -1;
		if (parameter != null)
			duration = parameter.duration;
		if (duration <= 0f && durationResolver != null)
			duration = durationResolver.resolveSoundDuration(sound, file);
		if (duration <= 0f)
			duration = defaultDuration; // Default sound duration

		// pitch shifting
		float pitchRange = 0f;
		if (parameter != null)
			pitchRange = parameter.pitchRange;
		if (pitchRange > 1f)
			pitchRange = 1f;
		
		if (pitchRange <= 0f)
			return new SfxSoundWrapper(sound, title, duration);
		else
			return new SfxPitchShiftingSoundWrapper(sound, title, duration, pitchRange);
	}

	@Override
	public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, SoundParameters parameter) {
		return null;
	}

	public static class SoundParameters extends AssetLoaderParameters<SfxSound> {
		public String title = null;
		public float duration = -1f;
		public float pitchRange = 0f;
	}

}
