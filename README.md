# gdx-sfx

Music player with playlist support and fade effects.

Spatial sounds.

## Disclaimer

Most of the code in this library grew up organically with no long-term architectural scheme and no master plan.

It is poorly documented as well.

However, it is currently in use in several in-house projects and as such, despite not being "battle-tested", deserves the informal title of "Code working OK".

## Setup

Add the pretty **bold** parts into your _build.gradle_ file:

<pre>
    repositories {
        <b>maven { url "http://dl.bintray.com/spookygames/oss" }</b>
    }
    
    ...
    
    project(":desktop") {
        
        ...
        
        dependencies {
            compile project(":core")
            ...
            <b>compile "net.spookygames.gdx:gdx-sfx-desktop:2.0.0"</b>
        }
    }
    
    project(":android") {
        
        ...
        
        dependencies {
            compile project(":core")
            ...
            <b>compile "net.spookygames.gdx:gdx-sfx-android:2.0.0"</b>
        }
    }
    
    project(":core") {
        
        ...
        
        dependencies {
            ...
            <b>compile "net.spookygames.gdx:gdx-sfx:2.0.0"</b>
        }
    }
</pre>

## Asset loading

This library is built upon two asset-related classes:
* `SfxMusic` (a wrapper around libGDX's `Music` instances that implements the `Music` interface)
* `SfxSound` (a wrapper around libGDX's `Sound` instances that implements the `Sound` interface)

As you may guess, the library doesn't make use of pure `Music` and `Sound` instances but handles `SfxMusic` and `SfxSound` objects instead.

The main difference between the Sfx- classes and their genuine counterparts is the `duration` attribute. In order for all the cool stuff to elegantly work out, the value of this field is of great importance. So let's see how to make it work as painlessly as possible.

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

Enters automatic audio duration resolution. This is kind of a _native_ stuff, so you'll need to initialize the magic in your platform-specific initializers.

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
* Duration resolution **will** take some time, whether it is successful or not. Not a _long_ time, but still longer than manually providing the value.

### Manual

You're from the unmanaged side of the fence, fine. Creating instances is as easy as for anything.

**Don't forget to dispose them**, of course, but you should know it by now. Disposing the Sfx- wrapper will also take care of disposing the wrapped instance.
    
    // Music
    FileHandle musicFile = Gdx.files.internal("music/cool_music.ogg");
    SfxMusic music = new SfxMusic(Gdx.audio.newMusic(musicFile), "Cool Music #1", 12.67f);	// Duration is in seconds
    
    // Sound
    FileHandle soundFile = Gdx.files.internal("random_sound_effect.ogg");
    SfxSound sound = new SfxSound(Gdx.audio.newSound(soundFile), "Random Effect!", 0.6547f);	// Seconds here too
    
    ...
    
    // Later on
    music.dispose();
    sound.dispose();

As you can see, duration resolution is manual here.

If you read section _Automatic audio duration resolution_ above, you might be craving some kind of automated duration resolution while avoiding managed asset handling. In this case, put up some `MusicDurationResolver` and `SoundDurationResolver` into your game class and follow the indications from [libgdx's wiki](https://github.com/libgdx/libgdx/wiki/Interfacing-with-platform-specific-code).

## Usage

### Music

TODO

### Sound

TODO

## Demo

A demo for the `SfxMusicPlayer` is available under the _Releases_ tab and you are invited to take a look at the sources [here](core/src/test/java/net/spookygames/gdx/sfx/demo/GdxSfxDemo.java).

## Platform support

- [x] Desktop
- [x] Android
- [ ] iOS
- [ ] HTML

> - can i haz moar os suport?

> - We'd be glad to receive your Pull Requests ;-)

## Roadmap

* Fill the Usage section of this README
* Javadoc
* Re-architecture, 'cause we should have been at least _that_ smart from the beginning

## Credits

Demo skin: [_shade_](https://github.com/czyzby/gdx-skins/tree/master/shade) by [Raymond "Raeleus" Buckley](http://www.badlogicgames.com/forum/viewtopic.php?f=22&t=21568).