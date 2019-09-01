package craftedMods.audioPlayer.api;

import java.io.IOException;
import java.io.InputStream;

import org.osgi.annotation.versioning.ProviderType;

import craftedMods.eventManager.api.EventDispatchPolicy;
import craftedMods.eventManager.api.EventInfo;
import craftedMods.eventManager.api.PropertyKey;
import craftedMods.eventManager.base.DefaultEventInfo;
import craftedMods.eventManager.base.DefaultPropertyKey;

@ProviderType
public interface AudioPlayer {

	public static final String SUPPORTED_FORMATS_PROPERTY_KEY = "supportedFormats";
	public static final String PLAYING_MODE_PROPERTY_KEY = "playingMode";

	public static final String PLAYING_MODE_STREAM = "stream";
	public static final String PLAYING_MODE_CLIP = "clip";
	public static final String PLAYING_MODE_MIXED = "mixed";

	public static final EventInfo PLAY_TRACK_ERROR_EVENT = new DefaultEventInfo(AudioPlayer.class, "PLAY_TRACK_ERROR",
			EventDispatchPolicy.NOT_SPECIFIED);

	public static final PropertyKey<String> PLAY_TRACK_ERROR_EVENT_NAME = DefaultPropertyKey.createStringPropertyKey();
	public static final PropertyKey<Exception> PLAY_TRACK_ERROR_EVENT_EXCEPTION = DefaultPropertyKey
			.createPropertyKey(Exception.class);

	public static final long UNDEFINED = -1l;
	public static final String UNDEFINED_TRACK_NAME = "Undefined";

	/**
	 * Submits the new track with the specified name to the audio player. If there's
	 * a current active track, this function will return false and nothing will
	 * happen. What happens if the end of the track is reached, depends on the
	 * provider. The name mustn't be blank or null.
	 * 
	 * @param track The track data
	 * @param name  The track name
	 * @return Whether the track could be submitted
	 */
	public boolean play(InputStream track, String name);

	/**
	 * Plays the specified track. Submitting tracks via this object allows the
	 * player to cache track-related data (in relation to the track instance,
	 * assuming that the same track instance always provides the same track) which
	 * don't have to be computed every time they are required then. Generally this
	 * provides a much better experience, because loading times are reduced and the
	 * maximum track duration can be retrieved for tracks where it isn't accessible
	 * otherwise. The caching is disableable via
	 * {@link AudioPlayer#setIsCacheEnabled(boolean)}. Otherwise, the method works
	 * exactly like {@link AudioPlayer#play(InputStream, String)}.
	 * 
	 * @param track The track to play
	 * @return Whether the track could be submitted
	 * @throws IOException
	 */
	public boolean play(PlayableTrack track) throws IOException;

	/**
	 * Returns whether the audio player has an active track assigned. That track can
	 * be paused.
	 * 
	 * @return Whether a track is active
	 */
	public boolean isTrackActive();

	/**
	 * Returns the current active track or null, if no track is playing.
	 * 
	 * @return The current playing track
	 */
	public String getCurrentTrack();

	/**
	 * Returns whether the current active track is paused. If there's no current
	 * active track, it returns false.
	 * 
	 * @return Whether the current active track is paused
	 */
	public boolean isPaused();

	/**
	 * If there's a currently active and playing track, it'll be paused.
	 */
	public void pause();

	/**
	 * If there's a currently active and paused track, it'll be played again.
	 */
	public void resume();

	/**
	 * If there's a currently active track, it'll be removed from the player.
	 */
	public void stop();

	/**
	 * Returns the current playing position of the current track in milliseconds.
	 * 
	 * @return The playing position
	 */
	public long getPlayingPositionMillis();

	/**
	 * Returns whether one can set the playing position of the current active track.
	 * If {@link AudioPlayer#getTrackLengthMillis()} returns
	 * {@link AudioPlayer#UNDEFINED}, false has to be returned.
	 * 
	 * @return Whether the playing position can be set
	 */
	public boolean canSetPlayingPosition();

	/**
	 * Sets the current playing position of the current active track. It'll be
	 * capped between the track length and 0. If
	 * {@link AudioPlayer#canSetPlayingPosition()} returns false, an
	 * {@link UnsupportedOperationException} will be thrown. If no track is active,
	 * an {@link IllegalStateException} will be thrown.
	 * 
	 * @param playingPosition The new playing position
	 */
	public void setPlayingPositionMillis(long playingPosition);

	/**
	 * Returns the current track length in milliseconds or
	 * {@link AudioPlayer#UNDEFINED} if the track length couldn't be determined.
	 * 
	 * @return The track length
	 */
	public long getTrackLengthMillis();

	/**
	 * Returns the volume of the player, which is a value between 0 and 100.
	 * 
	 * @return The current volume
	 */
	public int getVolume();

	/**
	 * Sets the volume of the player. The supplied value will be capped between 0
	 * and 100.
	 * 
	 * @param volume The new volume
	 */
	public void setVolume(int volume);

	/**
	 * Returns whether caching is enabled for tracks submitted via the
	 * {@link AudioPlayer#play(PlayableTrack)} function. See there for a more
	 * detailed explanation about caching.
	 * 
	 * @return Whether caching is enabled
	 */
	public boolean isCacheEnabled();

	/**
	 * Disables or enables the track cache. See
	 * {@link AudioPlayer#play(PlayableTrack)} for a detailed explanation.
	 * 
	 * @param isCacheEnabled The new cache state
	 */
	public void setIsCacheEnabled(boolean isCacheEnabled);

	/**
	 * Flushes the cache if it's enabled.
	 */
	public void flushCache();

}
