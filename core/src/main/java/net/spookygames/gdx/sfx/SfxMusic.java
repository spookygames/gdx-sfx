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

import com.badlogic.gdx.audio.Music;

public class SfxMusic implements Music {

	public final String title;
	public final float duration;	// In seconds
	
	private final Music music;
	
	// Copy of some stuff as we have no direct access to them
	private float pan = 0f;
	private OnCompletionListener listener = null;

	public SfxMusic(Music wrappedMusic, String title, float duration) {
		this.title = title;
		this.duration = duration;
		this.music = wrappedMusic;
	}

	@Override
	public void play() {
		music.play();
	}

	@Override
	public void pause() {
		music.pause();
	}

	@Override
	public void stop() {
		music.stop();
		if(listener != null)
			listener.onCompletion(this);
	}

	@Override
	public boolean isPlaying() {
		return music.isPlaying();
	}

	@Override
	public void setLooping(boolean isLooping) {
		music.setLooping(isLooping);
	}

	@Override
	public boolean isLooping() {
		return music.isLooping();
	}

	@Override
	public void setVolume(float volume) {
		music.setVolume(volume);
	}

	@Override
	public float getVolume() {
		return music.getVolume();
	}
	
	public float getPan() {
		return this.pan;
	}

	/**
	 * Just so you know: Panning only works with Mono samples!
	 * Now you know.
	 */
	@Override
	public void setPan(float pan, float volume) {
		this.pan = pan;
		music.setPan(pan, volume);
	}

	@Override
	public float getPosition() {
		return music.getPosition();
	}

	@Override
	public void setPosition(float position) {
		music.setPosition(position);
	}

	@Override
	public void dispose() {
		music.dispose();
	}

	@Override
	public void setOnCompletionListener(OnCompletionListener listener) {
		music.setOnCompletionListener(listener);
		this.listener  = listener;
	}
	
	@Override
	public String toString() {
		return title + " [" + getPosition() + "/" + duration + "] (SfxMusic)";
	}

}
