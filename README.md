# gdx-sfx

Some goodies for better sound effects in libgdx.

Music player with playlist support and fade effects.

Pitch shifting for sounds.

Spatial sounds.

## Disclaimer

Most of the code in this library grew up organically with no long-term architectural scheme and no master plan.

It is poorly documented as well.

However, it is currently in use in several in-house projects and as such, despite not being "battle-tested", deserves the informal title of "Code working OK".

## Setup

Add the pretty **bold** parts into your _build.gradle_ file:

<pre>
    allprojects {
        ext {
            <b>gdxSfxVersion = '3.1.0'</b>
        }
    }
    
    project(":desktop") {
        
        ...
        
        dependencies {
            compile project(":core")
            ...
            <b>compile "games.spooky.gdx:gdx-sfx-desktop:$gdxSfxVersion"</b>
        }
    }
    
    project(":android") {
        
        ...
        
        dependencies {
            compile project(":core")
            ...
            <b>compile "games.spooky.gdx:gdx-sfx-android:$gdxSfxVersion"</b>
        }
    }
    
    project(":core") {
        
        ...
        
        dependencies {
            ...
            <b>compile "games.spooky.gdx:gdx-sfx:$gdxSfxVersion"</b>
        }
    }
</pre>

## Asset loading

This library is built upon two asset-related interfaces:
* `SfxMusic` (extends the `Music` interface)
* `SfxSound` (extends the `Sound` interface)

As you may guess, the library doesn't make use of pure `Music` and `Sound` instances but handles `SfxMusic` and `SfxSound` objects instead.

The main difference between the Sfx- classes and their genuine counterparts is the `getDuration()` method and the ability to apply effects to them. In order for all the cool stuff to elegantly work out, the duration is of great importance. So let's see how to make it work as painlessly as possible.

### Automatic (AssetManager)

If you feel like standing in the managed side of the fence, and want to load your audio assets through an [AssetManager](https://github.com/libgdx/libgdx/wiki/Managing-your-assets), you've come to the right place. Ditch the old `MusicLoader` and `SoundLoader` pre-baked into your `AssetManager` instance and fill it with these awesome new loaders instead.

    FileHandleResolver resolver = new InternalFileHandleResolver();
    AssetManager assetManager = new AssetManager(resolver);
    
    // Music
    assetManager.setLoader(SfxMusic.class, new SfxMusicLoader(resolver));
    
    // Sound
    assetManager.setLoader(SfxSound.class, new SfxSoundLoader(resolver));

You may now use the godly power of asset management to load and unload those beloved audio assets of yours.
    
    // Music
    SfxMusicLoader.MusicParameters musicParameters = new SfxMusicLoader.MusicParameters();
    musicParameters.title = "Cool Music #1";	// Defaults to file name if nothing provided
    musicParameters.duration = 12.67f;	// In seconds
    assetManager.load("music/cool_music.ogg", GdxMusic.class, musicParameters);
    
    // Sound
    SfxSoundLoader.SoundParameters soundParameters = new SfxSoundLoader.SoundParameters();
    soundParameters.title = "Random Effect!";
    soundParameters.duration = 0.6547f;	// Seconds too
    assetManager.load("random_sound_effect.ogg", GdxAudio.class, soundParameters);

#### Automatic audio duration resolution

Tired of creating parameter objects for every assets you create? Us too.

Enters automatic audio duration resolution. This is not cross-platform so you'll need to initialize the magic in your platform-specific initializers.

Android:

<pre>
    public class MyAwesomeGameAndroid extends AndroidApplication {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            ...
            <b>AndroidAudioDurationResolver.initialize();</b>
            initialize(new MyAwesomeGame(), new AndroidApplicationConfiguration());
        }
    }
</pre>

Desktop:

<pre>
    public class MyAwesomeGameDesktop {
        public static void main(String[] args) {
            ...
            <b>DesktopAudioDurationResolver.initialize();</b>
            new LwjglApplication(new MyAwesomeGame());
        }
    }
</pre>

After that, asset loading becomes way more concise.
    
    // Music, but works for Sound too
    assetManager.load("music/cool_music.ogg", GdxMusic.class);

A word of warning though:
* The `duration` property **might** not be successfully resolved, in this case the value will be the default -1, just as if no resolution had actually happened.
* Duration resolution **will** take some time, whether it is successful or not. Not necessarily a _long_ time, but still longer than manually providing the value.

### Manual

You're from the unmanaged side of the fence, fine. Creating instances is as easy as for anything. The idea is to create a SfxMusicWrapper of SfxSoundWrapper using original Music/Sound in the constructor.

**Don't forget to dispose them**, of course, but you should know it by now. Disposing the Sfx- wrapper will also take care of disposing the wrapped instance.
    
    // Music
    FileHandle musicFile = Gdx.files.internal("music/cool_music.ogg");
    SfxMusic music = new SfxMusicWrapper(Gdx.audio.newMusic(musicFile), "Cool Music #1", 12.67f);	// Duration is in seconds
    
    // Sound
    FileHandle soundFile = Gdx.files.internal("random_sound_effect.ogg");
    SfxSound sound = new SfxSoundWrapper(Gdx.audio.newSound(soundFile), "Random Effect!", 0.6547f);	// Seconds here too
    
    ...
    
    // Later on
    music.dispose();
    sound.dispose();

As you can see, duration resolution is manual here.

If you read section _Automatic audio duration resolution_ above, you might be craving some kind of automated duration resolution while avoiding managed asset handling. In this case, put up some `MusicDurationResolver` and `SoundDurationResolver` into your game class and follow the indications from [libgdx's wiki](https://github.com/libgdx/libgdx/wiki/Interfacing-with-platform-specific-code).

## Usage

### Music player

    SfxMusicPlaylist musicPlayer;
    
    @Override
    public void create() {
      ...
      SfxMusicPlaylist musicPlayer = new SfxMusicPlaylist();
      musicPlayer.setVolume(0.8f);
      musicPlayer.addMusic(assetManager.get("myMusic.ogg", SfxMusic.class));
      musicPlayer.addMusic(assetManager.get("myMusic2.ogg", SfxMusic.class));
      musicPlayer.addEffect(Effects.fadeIn(5f));
      musicPlayer.addEffect(Effects.fadeOut(2f));
      musicPlayer.play();
      ...
    }
    
	@Override
	protected void render(float delta) {
      ...
      musicPlayer.update(delta);
      ...
	}
    
	@Override
	protected void pause() {
      ...
      musicPlayer.pause();
      ...
	}
    
	@Override
	protected void resume() {
      ...
      musicPlayer.play();
      ...
	}
    
	@Override
	protected void dispose() {
      ...
      assetManager.dispose();
      // Nothing to specifically dispose here, resources are handled by AssetManager
      ...
	}

### Pitch-shifting sounds, via AssetManager

    assetManager.setLoader(SfxSound.class, new SfxSoundLoader(resolver));
    
    SoundParameters parameter = new SoundParameters();
    parameter.pitchRange = 0.2f;
    
    assetManager.load("MySound.wav", SfxSound.class, parameter);

That's about it.

### Spatial sounds (2D example)

    Spatializer<Vector2> spatializer;
    SpatializedSoundPlayer<Vector2> spatializedPlayer;
    
    float minZoom = 0.5f;
    float maxZoom = 2f;
    
    Camera camera;
    
    @Override
    public void create() {
      ...
      	this.spatializer = new SomeSoundSpatializer2();	// This one is just an example
		this.spatializer.setVerticalRange(2f);
		this.spatializer.setHorizontalRange(6f);
		
		this.spatializedPlayer = new SpatializedSoundPlayer<Vector2>();
		this.spatializedPlayer.setSpatializer(spatializer);
		this.spatializedPlayer.setVolume(0.7f);
      ...
    }
    
	@Override
	protected void render(float delta) {
      ...
      Vector3 position = camera.position;
      Vector2 position = camera.getPosition();
      float relativeZoom = (camera.zoom - minZoom) / (maxZoom - minZoom);
		spatializer.setCenter(position.x, position.y, relativeZoom);
		spatializedPlayer.update(delta);
      ...
	}
    
    public void play(Vector2 position, SfxSound sound) {
      spatializedPlayer.play(position, sound);
    }

## Demo

A demo for the `SfxMusicPlayer` is available under the _Releases_ tab and you are invited to take a look at the sources [here](core-demo/src/main/java/games/spooky/gdx/sfx/demo/GdxSfxDemo.java).

## Platform support

- [x] Desktop
- [x] Android
- [ ] iOS
- [ ] HTML

## Credits

Demo skin: [_shade_](https://github.com/czyzby/gdx-skins/tree/master/shade) by [Raymond "Raeleus" Buckley](http://www.badlogicgames.com/forum/viewtopic.php?f=22&t=21568).
