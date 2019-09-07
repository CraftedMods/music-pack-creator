package craftedMods.audioPlayer.provider;

import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.log.Logger;
import org.osgi.service.log.LoggerFactory;

import craftedMods.audioPlayer.api.AudioPlayer;
import craftedMods.eventManager.api.EventManager;

@Component(scope = ServiceScope.PROTOTYPE, property = { AudioPlayer.SUPPORTED_FORMATS_PROPERTY_KEY + "=ogg",
		AudioPlayer.PLAYING_MODE_PROPERTY_KEY + "=" + AudioPlayer.PLAYING_MODE_STREAM }, service = AudioPlayer.class)
public class StreamingAudioPlayerImpl extends AbstractAudioPlayer {

	@Reference(service=LoggerFactory.class)
	private Logger logger;

	@Reference
	private EventManager eventManager;

	private volatile SourceDataLine sourceDataLine = null;

	private byte[] streamingBuffer;

	@Activate
	public void onActivate(Configuration config) {
		super.onActivate(logger, eventManager, config);
	}

	@Deactivate
	public void onModify(Configuration config) {
		super.onModify(config);
	}

	@Deactivate
	public void onDeactivate() {
		super.onDeactivate();
	}

	@Override
	public boolean canSetPlayingPosition() {
		return true;
	}

	private boolean wasPlayingPositionSet = false;

	@Override
	public void setPlayingPositionMillis(long playingPosition) {
		super.setPlayingPositionMillis(playingPosition);
		wasPlayingPositionSet = true;
	}

	@Override
	protected DataLine constructDataLine(AudioFormat decodedAudioFormat) throws LineUnavailableException {
		return sourceDataLine = (SourceDataLine) AudioSystem
				.getLine(new DataLine.Info(SourceDataLine.class, decodedAudioFormat));
	}

	@Override
	protected void openDataLine(AudioInputStream decodedAudioInputStream) throws LineUnavailableException {
		streamingBuffer = new byte[4096];
		sourceDataLine.open(decodedAudioInputStream.getFormat());
	}

	@Override
	protected boolean playbackLoop(AudioInputStream decodedAudioInputStream) throws IOException {
		if (wasPlayingPositionSet) {
			decodedAudioInputStream.skip(streamingBuffer.length);
			wasPlayingPositionSet = false;
		}
		int bytesRead = decodedAudioInputStream.read(streamingBuffer, 0, streamingBuffer.length);
		if (bytesRead != -1) {
			sourceDataLine.write(streamingBuffer, 0, bytesRead);
			return true;
		}
		return false;
	}

	@Override
	protected boolean onTrackEnd() {
		this.stop();
		return false;
	}

}
