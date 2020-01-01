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
package net.spookygames.gdx.sfx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

@SuppressWarnings("rawtypes")
public class SfxMusicLoader
		extends AsynchronousAssetLoader<SfxMusic, SfxMusicLoader.MusicParameters> {

	private static MusicDurationResolver durationResolver = null;

	public static void setDurationResolver(MusicDurationResolver musicDurationResolver) {
		durationResolver = musicDurationResolver;
	}

	private final float defaultDuration;

	private Music music;

	public SfxMusicLoader(FileHandleResolver resolver) {
		this(resolver, 60f);
	}

	public SfxMusicLoader(FileHandleResolver resolver, float defaultDuration) {
		super(resolver);
		this.defaultDuration = defaultDuration;
	}

	@Override
	public void loadAsync(AssetManager manager, String fileName, FileHandle file, MusicParameters parameter) {
		music = Gdx.audio.newMusic(file);
	}

	@Override
	public SfxMusic loadSync(AssetManager manager, String fileName, FileHandle file, MusicParameters parameter) {

		// Content
		Music music = this.music;
		this.music = null;

		// Title
		String title = null;
		if (parameter != null)
			title = parameter.title;
		if (title == null)
			title = fileName;

		// Duration
		float duration = -1f;
		if (parameter != null)
			duration = parameter.duration;
		if (duration <= 0f && durationResolver != null)
			duration = durationResolver.resolveMusicDuration(music, file);
		if (duration <= 0f)
			duration = defaultDuration; // Default music duration

		return new SfxMusicWrapper(music, title, duration);
	}

	@Override
	public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, MusicParameters parameter) {
		return null;
	}

	public static class MusicParameters extends AssetLoaderParameters<SfxMusic> {

		public String title = null;
		public float duration = -1f;

	}

}
