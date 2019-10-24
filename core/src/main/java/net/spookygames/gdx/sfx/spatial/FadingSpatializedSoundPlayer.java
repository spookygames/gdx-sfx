package net.spookygames.gdx.sfx.spatial;

import java.util.Iterator;

import com.badlogic.gdx.Gdx;

import net.spookygames.gdx.sfx.SfxSound;

public class FadingSpatializedSoundPlayer<T> extends SpatializedSoundPlayer<T> {
	private float fadeTime = 0f;

	public void setFadeTime(float fadeTime) {
		this.fadeTime = fadeTime;
	}

	public float getFadeTime() {
		return fadeTime;
	}

	@Override
	protected SpatializedSound<T> newObject() {
		return new FadingSpatializedSound<T>();
	}

	public long play(T position, SfxSound sound, float pitch, boolean looping, boolean fadeIn) {
		FadingSpatializedSound<T> instance = (FadingSpatializedSound<T>) pool.obtain();

		float duration = sound.getDuration();

		Spatializer<T> spatializer = this.spatializer;

		long id = instance.initialize(sound, duration, position, 0f, pitch, 0f,	fadeTime, fadeIn);

		if (id == -1) {
			pool.free(instance);
			Gdx.app.error("gdx-sfx", "Couldn't play sound " + sound);
		} else {
			instance.setLooping(looping);
			spatializer.spatialize(instance, this.volume);

			sounds.put(id, instance);
		}

		return id;
	}

	public void update(float delta) {
		Spatializer<T> spatializer = this.spatializer;

		Iterator<SpatializedSound<T>> iterator = sounds.values();
		while (iterator.hasNext()) {
			SpatializedSound<T> instance = iterator.next();

			if (instance.update(delta)) {
				iterator.remove();
				pool.free(instance);
			} else if (!((FadingSpatializedSound<T>) instance).isFading()) {
				spatializer.spatialize(instance, this.volume);
			}
		}
	}
	
	public void stop(long id) {
		SpatializedSound<T> sound = sounds.get(id);

		if (sound != null) {
			sound.stop();
		}
	}
}
