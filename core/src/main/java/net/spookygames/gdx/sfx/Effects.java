/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-2017 Spooky Games
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

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

public class Effects {
	
	static {
		Pools.set(FadeIn.class, new Pool<FadeIn>() { @Override protected FadeIn newObject() { return new FadeIn(); } });
		Pools.set(FadeOut.class, new Pool<FadeOut>() { @Override protected FadeOut newObject() { return new FadeOut(); } });
	}

	static public <T extends SfxMusicEffect> T effect(Class<T> type) {
		Pool<T> pool = Pools.get(type);
		T effect = pool.obtain();
		effect.setPool(pool);
		return effect;
	}

	static public FadeIn fadeIn(float duration) {
		return fadeIn(duration, null);
	}

	static public FadeIn fadeIn(float duration, Interpolation interpolation) {
		FadeIn effect = effect(FadeIn.class);
		effect.setDuration(duration);
		effect.setInterpolation(interpolation);
		return effect;
	}

	static public FadeOut fadeOut(float duration) {
		return fadeOut(duration, null);
	}

	static public FadeOut fadeOut(float duration, Interpolation interpolation) {
		FadeOut effect = effect(FadeOut.class);
		effect.setDuration(duration);
		effect.setInterpolation(interpolation);
		return effect;
	}
}
