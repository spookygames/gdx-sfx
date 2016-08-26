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

import net.spookygames.gdx.sfx.SfxSoundInstance.OnCompletionListener;

public class SoundLoopContainer {

	public enum SoundLoopState {
		IDLE {
			@Override public SoundLoopState next() {
				return IDLE;
			}
			@Override public SfxSound sound(SoundLoopContainer container) {
				return null;
			}
		},
		START {
			@Override public SoundLoopState next() {
				return LOOP;
			}
			@Override public SfxSound sound(SoundLoopContainer container) {
				return container.begin;
			}
		},
		LOOP {
			@Override public SoundLoopState next() {
				return END;
			}
			@Override public SfxSound sound(SoundLoopContainer container) {
				return container.loop;
			}
		},
		END {
			@Override public SoundLoopState next() {
				return IDLE;
			}
			@Override public SfxSound sound(SoundLoopContainer container) {
				return container.end;
			}
		};

		public abstract SoundLoopState next();
		public SfxSound sound(SoundLoopContainer container) {
			return null;
		}
	}

	public final SfxSound begin;
	public final SfxSound loop;
	public final SfxSound end;

	private SoundLoopState state;

	private SfxSoundInstance current;

	private SfxSoundPlayer player;

	public SoundLoopContainer(SfxSound begin, SfxSound loop, SfxSound end) {
		super();

		this.state = SoundLoopState.IDLE;
		this.current = null;

		this.begin = begin;
		this.loop = loop;
		this.end = end;
	}

	public void start() {
		setState(SoundLoopState.START);
	}

	public void end() {
		switch (state) {
		case START:
			current.setCompletionListener(new OnCompletionListener() {
				@Override public void onCompletion(SfxSoundInstance sound) {
					setState(SoundLoopState.END);
				}
			});
			break;
		case LOOP:
			current.setLooping(false);
			break;
		default:
			break;
		}
	}
	
	public void stop() {
		if(player != null)
			player.stop();
	}

	private void setState(final SoundLoopState state) {

		if(current != null) {
			current.stop();
		}
			
		this.state = state;
		SfxSound sc = state.sound(this);

		if(sc == null) {
			SoundLoopState next = state.next();
			if(next != state) {
				setState(next);
			}
		} else {
			current = this.player.playSound(sc, state == SoundLoopState.LOOP);
			current.setCompletionListener(new OnCompletionListener() {
				@Override public void onCompletion(SfxSoundInstance sound) {
					setState(state.next());
				}
			});
		}
	}

	public SfxSoundPlayer getPlayer() {
		return player;
	}

	public void setPlayer(SfxSoundPlayer player) {
		this.player = player;
	}
	
	public boolean isPlaying() {
		switch (state) {
		case END:
		case IDLE:
		default:
			return false;
		case LOOP:
		case START:
			return true;
		}
	}

}
