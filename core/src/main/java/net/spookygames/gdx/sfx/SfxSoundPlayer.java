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
package net.spookygames.gdx.sfx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.LongMap;

public class SfxSoundPlayer {

	public final LongMap<SfxSoundInstance> sounds = new LongMap<SfxSoundInstance>();

	private float volume = 1f;

	public SfxSoundPlayer(float soundVolume) {
		super();
		this.volume = soundVolume;
	}

	public SfxSoundInstance playSound(SfxSound sound, boolean looping) {

		SfxSoundInstance soundInstance = new SfxSoundInstance(sound, volume);
		long id = soundInstance.id;

		if (id == -1) {
			Gdx.app.error("SoundPlayer", "Couldn't play sound " + sound);
			return null;
		} else {
			soundInstance.setLooping(looping);

			sounds.put(soundInstance.id, soundInstance);
			
			return soundInstance;
		}
	}

	public void update(float delta) {
		SfxSoundInstance sound;
		LongMap.Keys indexes = sounds.keys();

		while (indexes.hasNext) {
			long index = indexes.next();
			sound = sounds.get(index);
			if (sound.update(delta)) {
				sounds.remove(index);
			}
		}
	}

	public float getVolume() {
		return volume;
	}

	public void setVolume(float volume) {
		this.volume = volume;

		for (SfxSoundInstance sound : sounds.values())
			sound.setVolume(volume);
	}

	public void stop() {
		LongMap.Keys indexes = sounds.keys();

		while (indexes.hasNext) {
			sounds.remove(indexes.next()).stop();
		}
	}

	public void stopSound(long id) {
		SfxSoundInstance sound = sounds.remove(id);

		if (sound != null)
			sound.stop();
	}

	public void resume() {
		for (SfxSoundInstance sound : sounds.values())
			sound.resume();
	}

	public void pause() {
		SfxSoundInstance sound;
		LongMap.Keys indexes = sounds.keys();

		while (indexes.hasNext) {
			long index = indexes.next();
			sound = sounds.get(index);
			if (sound.isLooping()) {
				sound.pause();
			} else {
				sounds.remove(index).stop();
			}
		}
	}
}
