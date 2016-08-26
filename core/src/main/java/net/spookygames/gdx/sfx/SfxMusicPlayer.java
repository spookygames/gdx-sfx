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
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class SfxMusicPlayer {

	private static Array<SfxMusic> allPlaying = new Array<SfxMusic>();

	private float volume = 1f; // In range [0, 1]

	private float pan = 0f; // In range [-1, 1]

	private boolean play = false;
	private boolean pause = false;

	private boolean repeat = false;
	private boolean shuffle = false;

	// Playlist
	private final Array<SfxMusic> tmpPlaylist = new Array<SfxMusic>();
	private final Array<SfxMusic> playlist = new Array<SfxMusic>();
	private final Array<SfxMusic> actualPlaylist = new Array<SfxMusic>();

	private SfxMusic current = null;
	private SfxMusic former = null;
	private int index = -1;

	// Fade
	private boolean fade = false;
	private float fadeDuration = 2.0f; // In seconds, > 0
	private Interpolation fadeInterpolation = Interpolation.linear;

	private float fadeOut = 0f;
	private boolean transitionSelf = false;

	public SfxMusicPlayer() {
		super();
	}

	public SfxMusic currentlyPlayed() {
		return current;
	}

	public String currentlyPlayedTitle() {
		return current == null ? "" : current.title;
	}

	public float getVolume() {
		return this.volume;
	}

	public void setVolume(float volume) {
		if (volume < 0f)
			volume = 0f;
		if (volume > 1f)
			volume = 1f;

		this.volume = volume;
	}

	public float getPan() {
		return this.pan;
	}

	public void setPan(float pan) {
		if (pan < -1f)
			pan = -1f;
		if (pan > 1f)
			pan = 1f;

		this.pan = pan;
	}

	public boolean isShuffleEnabled() {
		return shuffle;
	}

	public void setShuffleEnabled(boolean shuffle) {
		this.shuffle = shuffle;

		// Handle actual playlist
		if (shuffle) {
			actualPlaylist.shuffle();
		} else {
			actualPlaylist.clear();
			actualPlaylist.addAll(playlist);
		}
	}

	public void setFadingEnabled(boolean fade) {
		this.fade = fade;
	}

	public boolean isFadingEnabled() {
		return fade;
	}

	public float getFadeDuration() {
		return fadeDuration;
	}

	public void setFadeDuration(float fadeDuration) {
		if (fadeDuration <= 0f)
			return;

		this.fadeDuration = fadeDuration;
	}

	public Interpolation getFadeInterpolation() {
		return fadeInterpolation;
	}

	public void setFadeInterpolation(Interpolation fadeInterpolation) {
		if (fadeInterpolation == null)
			return;
		this.fadeInterpolation = fadeInterpolation;
	}

	public boolean isRepeatEnabled() {
		return repeat;
	}

	public void setRepeatEnabled(boolean repeat) {
		this.repeat = repeat;
	}

	public Array<SfxMusic> getPlaylist() {
		tmpPlaylist.clear();
		tmpPlaylist.addAll(playlist);
		return tmpPlaylist;
	}

	public boolean addToPlaylist(SfxMusic music) {
		// Check this music isn't use elsewhere
		if (allPlaying.contains(music, true)) {
			Gdx.app.debug("gdx-sfx",
					"Unable to add music " + music + " to playlist as it is already in the playlist of a player");
			return false;
		}

		// Add to global usage
		allPlaying.add(music);

		// Add to nominal playlist
		playlist.add(music);

		if (shuffle) {
			// If shuffle, add randomly to remaining playlist
			actualPlaylist.insert(MathUtils.random(index + 1, actualPlaylist.size), music);
		} else {
			// If not shuffle, add to end
			actualPlaylist.add(music);
		}

		return true;
	}

	public boolean removeFromPlaylist(SfxMusic music) {
		if (playlist.removeValue(music, true)) {
			allPlaying.removeValue(music, true);
			
			int i = actualPlaylist.indexOf(music, true);
			actualPlaylist.removeIndex(i);
			
			if(i < index)
				index--;

			if (current == music) {
				if(actualPlaylist.size == 0)
					stop();
				else
					next();
			}
			
			return true;
		} else {
			return false;
		}
	}

	public void setPlaylist(SfxMusic music) {
		if (music == null) {
			setPlaylist((Array<SfxMusic>) null);
		} else {
			tmpPlaylist.clear();
			tmpPlaylist.add(music);
			setPlaylist(tmpPlaylist);
		}
	}

	public void setPlaylist(Array<SfxMusic> playlist) {
		if(playlist == null) {
			for(SfxMusic music : this.playlist)
				removeFromPlaylist(music);
		} else {
			for(SfxMusic music : this.playlist)
				if(!playlist.contains(music, true))
					removeFromPlaylist(music);
			
			for(SfxMusic music : playlist)
				if(!this.playlist.contains(music, true))
					addToPlaylist(music);
		}
	}

	public boolean isPlaying() {
		return play;
	}

	public boolean isPaused() {
		return pause;
	}

	public void play() {
		if (play)
			return; // Already playing

		// Set markers
		play = true;
		pause = false;

		// Start music
		if (current == null)
			next(); // We were stopped
		else
			tryPlay(current); // We were paused
	}

	public void pause() {
		if (pause || !play)
			return; // Already paused or unpausable

		// Set markers
		pause = true;
		play = false;

		// Pause current music if any
		if (current != null)
			current.pause();

		// Remove former music if any (yeah, no fade out when resuming)
		if (former != null) {
			former.stop();
			former = null;
		}
	}

	public void stop() {
		if (!play || current == null)
			return; // Already stopped or unstoppable

		// Set markers
		play = false;
		pause = false;
		resetIndex();

		// Stop current music
		stopCurrent();
	}

	public void update(float delta) {
		if (pause)
			return;

		if (current != null) {
			if (fade) {
				float position = current.getPosition();
				if (position < fadeDuration) {
					// We're in a fade-in
					current.setPan(pan, MathUtils.clamp(fadeInterpolation.apply(0f, volume, position / fadeDuration), 0f, 1f));
				} else if (current.getPan() != pan) {
					current.setPan(pan, volume);
				} else if (current.getVolume() != volume) {
					current.setVolume(volume);
				}

				if (position >= current.duration - fadeDuration) {
					stopCurrent();
					next();
				}
			} else {
				if (current.getPan() != pan)
					current.setPan(pan, volume);
				else if (current.getVolume() != volume)
					current.setVolume(volume);

				if (!current.isPlaying())
					next();
			}
		}

		if (former != null) {
			if (fade) {
				// We're in a fade-out
				fadeOut += delta;
				if (fadeOut >= fadeDuration) {
					former.stop();
				} else {
					former.setPan(pan, MathUtils.clamp(fadeInterpolation.apply(volume, 0f, fadeOut / fadeDuration), 0f, 1f));
				}
			} else {
				// We're not in a fade-out
				if (former.getPan() != pan)
					former.setPan(pan, volume);
				else if (former.getVolume() != volume)
					former.setVolume(volume);
			}
			if (!former.isPlaying()) { // Former has eventually ended
				if (transitionSelf) {
					current = former;
					startCurrent();
					transitionSelf = false;
				}
				former = null;
			}
		}
	}

	public void next() {
		transition(nextMusic());
	}

	public void previous() {
		transition(previousMusic());
	}

	private void transition(SfxMusic music) {
		if (music == null) {
			stop();
			return;
		}

		if (music == current) {
			stopCurrent();
			transitionSelf = true;
			return;
		}

		if (current != null)
			stopCurrent();

		current = music;

		if (play)
			startCurrent();
	}

	private SfxMusic nextMusic() {
		index++;
		if (index == actualPlaylist.size) {
			resetIndex();
			if (repeat) {
				if (shuffle) {
					actualPlaylist.shuffle();
					// Don't let shuffling put the same track twice in a row
					if(actualPlaylist.first() == current)
						actualPlaylist.add(actualPlaylist.pop());
				}
				return nextMusic();
			} else {
				return null;
			}
		}

		SfxMusic nextMusic = actualPlaylist.get(index);
		return nextMusic;
	}

	private SfxMusic previousMusic() {
		index--;
		if (index < 0) {
			resetIndex();
			// Beware, behavior here is different from above [next()]
			// If you go previous at the beginning of the actual playlist, you
			// will simply get nothing
			// No re-shuffle, no infinite playlist memory
			return null;
		}

		SfxMusic previousMusic = actualPlaylist.get(index);
		return previousMusic;
	}

	private void resetIndex() {
		index = -1;
	}

	private void startCurrent() {
		current.setPan(pan, fade ? 0f : volume);

		tryPlay(current);
	}
	
	private void tryPlay(SfxMusic music) {
		music.play();
		if(!music.isPlaying())
			Gdx.app.debug("gdx-sfx", "Unable to actually play " + music);
	}

	private void stopCurrent() {
		if (former != null)
			former.stop(); // No fade out here

		if (fade) {
			fadeOut = 0f;
			former = current;
		} else if(current.isPlaying()) {
			current.stop();
		}

		current = null;
	}

	@Override
	public String toString() {
		String output = "SfxMusicPlayer";
		output += "\nPlaying? " + play + " - Paused? " + pause;
		output += "\nCurrent: " + current;
		output += "\nFormer (fade-out): " + former;
		output += "\nVolume: " + volume;
		output += "\nPanning: " + pan;
		return output;
	}

}
