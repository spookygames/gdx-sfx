/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-2025 Spooky Games
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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

@SuppressWarnings("rawtypes")
public class SfxMusicPlaylist implements SfxMusic {

	private final Array<SfxMusicEffect> effects = new Array<SfxMusicEffect>(0);

	private final Array<SfxMusic> content = new Array<SfxMusic>();
	
	private SfxMusic current = null;
	private int index = -1;

	private OnCompletionListener listener = null;

	private float volume = 1f; // In range [0, 1]

	private float pan = 0f; // In range [-1, 1]

	private boolean play = false;
	private boolean pause = false;
	private boolean stopPending = false;

	private boolean repeat = false;

	public SfxMusicPlaylist() {
		super();
	}

	public Array<SfxMusic> getContent() {
		return content;
	}

	public boolean isEmpty() {
		return content.size == 0;
	}

	public int size() {
		return content.size;
	}

	public void addMusic(SfxMusic music) {
		content.add(music);
	}

	public void containsMusic(SfxMusic music) {
		content.contains(music, true);
	}

	public boolean removeMusic(SfxMusic music) {
		int i = content.indexOf(music, true);
		if (i >= 0) {
			content.removeIndex(i);

			if (i < index) {
				index--;
			} else if (i == index) {
				if (content.size == 0)
					stop();
				else
					next();
			}

			return true;
		} else {
			return false;
		}
	}

	public void clearContent() {
		stop();
		content.clear();
	}

	public void setContent(Array<SfxMusic> playlist) {
		if (playlist == null || playlist.size == 0) {
			clearContent();
		} else {
			// Handle smooth transition when music currently playing is also
			// added
			SfxMusic current = getCurrentlyPlayed();
			int newIndex = playlist.indexOf(current, true);
			if (newIndex < 0) {
				clearContent();
				content.addAll(playlist);
			} else {
				content.clear();
				content.addAll(playlist);
				this.index = newIndex;
			}
		}
	}

	public SfxMusic getCurrentlyPlayed() {
		return this.current;
	}

	@Override
	public String getTitle() {
		SfxMusic current = getCurrentlyPlayed();
		return current == null ? null : current.getTitle();
	}

	@Override
	public float getDuration() {
		float duration = 0f;
		Array<SfxMusic> content = this.content;
		for (int i = 0, n = content.size; i < n; i++)
			duration += content.get(i).getDuration();
		return duration;
	}

	public void shuffle() {
		content.shuffle();
	}

	@Override
	public void play() {
		if (play)
			return; // Already playing

		if (content.size == 0)
			return; // Empty

		// Set markers
		play = true;
		stopPending = false;

		// Start music
		if (pause) {
			// We were paused, remove marker and resume current
			pause = false;
			if (current != null)
				current.play();
		} else {
			// We were stopped, start playing
			next();
		}
	}

	@Override
	public boolean isPlaying() {
		return play;
	}

	@Override
	public void pause() {
		if (pause || !play)
			return; // Already paused or unpausable

		// Set markers
		pause = true;
		play = false;

		// Pause current music
		getCurrentlyPlayed().pause();
	}

	public boolean isPaused() {
		return pause;
	}

	@Override
	public void stop() {
		if (!play)
			return; // Already stopped

		stopPending = true;

		// Stop current music
		if (current != null)
			current.stop();

		// Reset index
		resetIndex();
	}

	@Override
	public boolean isLooping() {
		return repeat;
	}

	@Override
	public void setLooping(boolean isLooping) {
		this.repeat = isLooping;
	}

	@Override
	public float getVolume() {
		return this.volume;
	}

	public void setVolume(float volume) {
		this.volume = volume;
		SfxMusic current = getCurrentlyPlayed();
		if (current != null)
			current.setVolume(volume);
	}

	public float getPan() {
		return this.pan;
	}

	public void setPan(float pan) {
		this.pan = pan;
		SfxMusic current = getCurrentlyPlayed();
		if (current != null)
			current.setPan(pan, volume);
	}

	@Override
	public void setPan(float pan, float volume) {
		this.pan = pan;
		SfxMusic current = getCurrentlyPlayed();
		if (current != null)
			current.setPan(pan, volume);
	}

	@Override
	public void setPosition(float position) {
		throw new UnsupportedOperationException();
	}

	@Override
	public float getPosition() {
		throw new UnsupportedOperationException();
	}

	public Array<SfxMusicEffect> getEffects() {
		return effects;
	}

	public boolean hasEffects() {
		return effects.size > 0;
	}

	public void addEffect(SfxMusicEffect effect) {
		if (!this.effects.contains(effect, true))
			this.effects.add(effect);
		SfxMusic current = getCurrentlyPlayed();
		if (current != null)
			current.addEffect(effect);
	}

	public void removeEffect(SfxMusicEffect effect) {
		if (effects.removeValue(effect, true)) {
			SfxMusic current = getCurrentlyPlayed();
			if (current != null)
				current.removeEffect(effect);
		}
	}

	public void clearEffects() {
		SfxMusic current = getCurrentlyPlayed();
		if (current != null) {
			for (int i = 0, n = effects.size; i < n; i++)
				current.removeEffect(effects.get(i));
		}
		effects.clear();
	}

	@Override
	public boolean update(float deltaTime) {
		if (play) {
			if (current == null || current.update(deltaTime)) {
				if (stopPending) {
					// Stop effectively
					play = false;
					pause = false;
					return true;
				} else {
					next();
				}
			}

			
			return false;
		} else {
			return true;
		}
	}

	public void next() {
		transition(nextMusic());
	}

	public void previous() {
		transition(previousMusic());
	}

	@Override
	public void dispose() {
	}

	public OnCompletionListener getOnCompletionListener() {
		return listener;
	}

	@Override
	public void setOnCompletionListener(OnCompletionListener listener) {
		this.listener = listener;
	}

	private void resetIndex() {
		index = -1;
	}

	private void tryPlay(SfxMusic music) {
		if (current != null) {
			removeEffectsFromMusic(current);
			current.stop();
		}
		addEffectsToMusic(music);
		music.setPan(pan, volume);
		music.play();
		if (music.isPlaying()) {
			current = music;
		} else {
			removeEffectsFromMusic(music);
			
			Gdx.app.debug("gdx-sfx", "Unable to actually play " + music);
		}
	}

	private void addEffectsToMusic(SfxMusic music) {
		for (int i = 0, n = effects.size; i < n; i++)
			music.addEffect(effects.get(i));
	}

	private void removeEffectsFromMusic(SfxMusic music) {
		for (int i = 0, n = effects.size; i < n; i++) {
			SfxMusicEffect effect = effects.get(i);
			
			// Save from brutal pool-freeing
			Pool pool = effect.getPool();
			music.removeEffect(effect);
			effect.setPool(pool);
		}
	}

	private void transition(SfxMusic music) {
		if (music == null) {
			stop();
		} else if (play) {
			tryPlay(music);
		}
	}

	private SfxMusic nextMusic() {
		index++;
		if (index >= content.size) {
			resetIndex();
			if (repeat) {
				return nextMusic();
			} else {
				return null;
			}
		}

		return content.get(index);
	}

	private SfxMusic previousMusic() {
		index--;
		if (index < 0) {
			resetIndex();
			// Beware, behavior here is different from above [next()]
			// If you go previous at the beginning of the actual playlist, you
			// will simply get nothing
			return null;
		}

		return content.get(index);
	}

	@Override
	public String toString() {
		String output = "SfxMusicPlaylist";
		output += "\nPlaying? " + play + " - Paused? " + pause;
		output += "\nCurrent: " + getCurrentlyPlayed();
		output += "\nVolume: " + volume;
		output += "\nPanning: " + pan;
		return output;
	}

}
