package com.chrrubin.cherryrenderer.gui.custom;

import com.chrrubin.cherryrenderer.MediaObject;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.util.Duration;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.log.LogLevel;
import uk.co.caprica.vlcj.log.NativeLog;
import uk.co.caprica.vlcj.media.Media;
import uk.co.caprica.vlcj.media.MediaEventAdapter;
import uk.co.caprica.vlcj.media.VideoTrackInfo;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.base.State;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CallbackVideoSurface;
import uk.co.caprica.vlcj.player.embedded.videosurface.VideoSurfaceAdapters;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Media player based on an embedded VLC media player rendered on a JavaFX Canvas.
 * The video data from VLC is drawn onto the Canvas in 60FPS cause of course you need to manually render every frame if you're using JavaFX instead of Swing. Of course.
 * The direct rendering implementation mostly follows the examples in vlcj-javafx (https://github.com/caprica/vlcj-javafx)
 */
public class VlcPlayerCanvas extends Canvas implements IPlayer{
    private final Logger LOGGER = Logger.getLogger(VlcPlayerCanvas.class.getName());

    private PixelWriter pixelWriter = this.getGraphicsContext2D().getPixelWriter();
    private WritablePixelFormat<ByteBuffer> pixelFormat = PixelFormat.getByteBgraInstance();
    private MediaPlayerFactory mediaPlayerFactory = new MediaPlayerFactory("--no-osd", "--no-snapshot-preview");
    private EmbeddedMediaPlayer mediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();
    private NativeLog nativeLog = mediaPlayerFactory.application().newLog();
    private WritableImage videoImage;
    private final Semaphore renderingSemaphore = new Semaphore(1);
    private final Timeline timeline = new Timeline();

    private Runnable onBuffering;
    private Runnable onPlaying;
    private Runnable onPaused;
    private Runnable onStopped;
    private Runnable onFinished;
    private Runnable onError;
    private Runnable onMediaPlayerReady;
    private BooleanProperty isMute = new SimpleBooleanProperty(false);
    private DoubleProperty volume = new SimpleDoubleProperty();
    private ObjectProperty<Duration> currentTime = new SimpleObjectProperty<>();
    private ObjectProperty<State> state = new SimpleObjectProperty<>(State.NOTHING_SPECIAL);
    private int videoWidth;
    private int videoHeight;

    public VlcPlayerCanvas(){
        mediaPlayer.videoSurface().set(new JavaFxVideoSurface());

        timeline.setCycleCount(Timeline.INDEFINITE);
        double duration = 1000.0 / 60.0; // Render video in 60 FPS. This should cover most use cases
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(duration), event -> renderFrame()));

        mediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter(){
            @Override
            public void buffering(MediaPlayer mediaPlayer, float newCache) {
//                LOGGER.finest("VLC media player buffering");
                if(onBuffering != null){
                    Platform.runLater(onBuffering);
                }
            }

            @Override
            public void playing(MediaPlayer mediaPlayer) {
                LOGGER.finest("VLC media player playing");
                if(onPlaying != null) {
                    Platform.runLater(onPlaying);
                }
            }

            @Override
            public void paused(MediaPlayer mediaPlayer) {
                LOGGER.finest("VLC media player paused");
                if(onPaused != null) {
                    Platform.runLater(onPaused);
                }
            }

            @Override
            public void stopped(MediaPlayer mediaPlayer) {
                LOGGER.finest("VLC media player stopped");
                if(onStopped != null) {
                    Platform.runLater(onStopped);
                }
            }

            @Override
            public void finished(MediaPlayer mediaPlayer) {
                LOGGER.finest("VLC media player finished");
                if(onFinished != null) {
                    Platform.runLater(onFinished);
                }
            }

            @Override
            public void error(MediaPlayer mediaPlayer) {
                LOGGER.finest("VLC media player reached an error");
                if(onError != null) {
                    Platform.runLater(onError);   // TODO: File reading errors (eg expired links) for m3u8 files doesn't trigger error event. Not sure what to do about that.
                }
            }

            @Override
            public void mediaPlayerReady(MediaPlayer mediaPlayer) {
                LOGGER.finest("VLC media player is ready");
                timeline.playFromStart();
                if(onMediaPlayerReady != null) {
                    Platform.runLater(onMediaPlayerReady);
                }
            }

            @Override
            public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
                currentTime.set(Duration.millis(newTime));
            }

            @Override
            public void muted(MediaPlayer mediaPlayer, boolean muted) {
                LOGGER.finest("VLC muted changed to " + muted);
                isMute.set(muted);
            }

            @Override
            public void volumeChanged(MediaPlayer mediaPlayer, float volume) {
                LOGGER.finest("VLC volume changed to " + volume);  // For some reason this gets triggered with 1.0 whenever toggling between pause and play
                VlcPlayerCanvas.this.volume.set(volume);
            }
        });

        mediaPlayer.events().addMediaEventListener(new MediaEventAdapter(){
            @Override
            public void mediaStateChanged(Media media, State newState) {
                LOGGER.finest("VLC state changed to " + newState.name());  // I like how this doesn't get triggered when player is buffering even though there's a BUFFERING state.
                state.set(newState);
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Releasing VLC player resources...");
            nativeLog.release();
            mediaPlayer.release();
            mediaPlayerFactory.release();
            // FIXME: one of these randomly causes JVM to crash.... Who knows which one...
        }));

        nativeLog.setLevel(LogLevel.WARNING);
        nativeLog.addLogListener((level, module, file, line, name, header, id, message) -> {
            String logMessage = "[" + module + "]" + " " + name + ": " + message;
            switch(level){
                case NOTICE:
                    LOGGER.fine(logMessage);
                    break;
                case DEBUG:
                    LOGGER.finer(logMessage);
                    break;
                case WARNING:
                    LOGGER.warning(logMessage);
                    break;
                case ERROR:
                    LOGGER.severe(logMessage);
                    break;
            }
        });
    }

    private class JavaFxVideoSurface extends CallbackVideoSurface {
        JavaFxVideoSurface() {
            super(new JavaFxBufferFormatCallback(), new JavaFxRenderCallback(), true, VideoSurfaceAdapters.getVideoSurfaceAdapter());
        }
    }

    /**
     * Invoked by native libvlc thread when the buffer format of the video is changed.
     * sourceWidth and sourceHeight values are provided by libvlc, which is the buffer size of the video (different from display resolution).
     * Video resolution is also acquired here to support adaptive streaming that can change resolutions mid playback.
     */
    private class JavaFxBufferFormatCallback implements BufferFormatCallback {
        @Override
        public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
            LOGGER.finer("Buffer size: " + sourceWidth + "x" + sourceHeight);

            int currentTrack = mediaPlayer.video().track();
            List<VideoTrackInfo> videoTracks = mediaPlayer.media().info().videoTracks();

            if(videoTracks.isEmpty()){
                LOGGER.warning("Unable to get video tracks. Using buffer size as display size for now.");
                videoWidth = sourceWidth;
                videoHeight = sourceHeight;
            }
            else if(currentTrack < 0){ // Treat negative track int i as "last i element"
                videoWidth = videoTracks.get(videoTracks.size() + currentTrack).width();
                videoHeight = videoTracks.get(videoTracks.size() + currentTrack).height();
            }
            else{
                videoWidth = videoTracks.get(currentTrack).width();
                videoHeight = videoTracks.get(currentTrack).height();
            }

            LOGGER.finer("Video resolution: " + videoWidth + "x" + videoHeight);

            videoImage = new WritableImage(videoWidth, videoHeight);
            pixelWriter = videoImage.getPixelWriter();

            return new RV32BufferFormat(sourceWidth, sourceHeight);
        }
    }

    /**
     * Invoked by the native libvlc thread every video frame for rendering.
     * Semaphore prevents updating pixel data when image is being rendered on another thread.
     */
    private class JavaFxRenderCallback implements RenderCallback {
        @Override
        public void display(MediaPlayer mediaPlayer, ByteBuffer[] nativeBuffers, BufferFormat bufferFormat) {
            try {
                renderingSemaphore.acquire();
                pixelWriter.setPixels(0, 0, videoWidth, videoHeight, pixelFormat, nativeBuffers[0], bufferFormat.getPitches()[0]);
                renderingSemaphore.release();
            }
            catch (InterruptedException e) {
                LOGGER.log(Level.SEVERE, e.toString(), e);
            }
        }
    }

    /**
     * Called by the Timeline timer to render video pixel data onto the canvas.
     * Semaphore prevents trying to render image while pixel data is being updated on another thread.
     */
    private void renderFrame() {
        GraphicsContext graphicsContext = this.getGraphicsContext2D();

        double canvasWidth = this.getWidth();
        double canvasHeight = this.getHeight();

        graphicsContext.setFill(new Color(0, 0, 0, 1));
        graphicsContext.fillRect(0, 0, canvasWidth, canvasHeight);

        if (videoImage != null) {
            double imageWidth = videoImage.getWidth();
            double imageHeight = videoImage.getHeight();

            double sx = canvasWidth / imageWidth;
            double sy = canvasHeight / imageHeight;

            double sf = Math.min(sx, sy);

            double scaledWidth = imageWidth * sf;
            double scaledHeight = imageHeight * sf;

            Affine ax = graphicsContext.getTransform();

            graphicsContext.translate(
                    (canvasWidth - scaledWidth) / 2,
                    (canvasHeight - scaledHeight) / 2
            );

            if (sf != 1.0) {
                graphicsContext.scale(sf, sf);
            }

            try {
                renderingSemaphore.acquire();
                graphicsContext.drawImage(videoImage, 0, 0);
                renderingSemaphore.release();
            }
            catch (InterruptedException e) {
                LOGGER.log(Level.SEVERE, e.toString(), e);
            }

            graphicsContext.setTransform(ax);
        }
    }

    @Override
    public void playNewMedia(MediaObject mediaObject){
        if(Arrays.asList(PlayerStatus.PLAYING, PlayerStatus.PAUSED).contains(getStatus())){
            disposePlayer();
        }
        mediaPlayer.media().play(mediaObject.getUriString());
    }

    @Override
    public void play(){
        mediaPlayer.controls().play();
    }

    @Override
    public void pause(){
        mediaPlayer.controls().pause();
    }

    @Override
    public void stop(){
        mediaPlayer.controls().stop();
    }

    @Override
    public void seek(Duration target){
        mediaPlayer.controls().setTime((long)target.toMillis());
    }

    @Override
    public boolean isMute(){
        return isMute.get();
    }

    @Override
    public void setMute(boolean mute){
        mediaPlayer.audio().setMute(mute);
    }

    @Override
    public double getVolume(){
        return mediaPlayer.audio().volume();
    }

    @Override
    public void setVolume(double volume){
        mediaPlayer.audio().setVolume((int)volume);
    }

    @Override
    public Duration getCurrentTime(){
        return Duration.millis(mediaPlayer.status().time());
    }

    @Override
    public Duration getTotalTime(){
        return Duration.millis(mediaPlayer.status().length());
    }

    @Override
    public PlayerStatus getStatus(){
        switch(state.get()){
            case OPENING:
                return PlayerStatus.OPENING;
            case STOPPED:
                return PlayerStatus.STOPPED;
            case PLAYING:
                return PlayerStatus.PLAYING;
            case PAUSED:
                return PlayerStatus.PAUSED;
            case ERROR:
                return PlayerStatus.ERROR;
            case BUFFERING:
                return PlayerStatus.BUFFERING;
            default:
                return PlayerStatus.UNKNOWN;
        }
    }

    @Override
    public void setOnBuffering(Runnable onBuffering) {
        this.onBuffering = onBuffering;
    }

    @Override
    public void setOnPlaying(Runnable onPlaying) {
        this.onPlaying = onPlaying;
    }

    @Override
    public void setOnPaused(Runnable onPaused) {
        this.onPaused = onPaused;
    }

    @Override
    public void setOnStopped(Runnable onStopped) {
        this.onStopped = onStopped;
    }

    @Override
    public void setOnFinished(Runnable onFinished) {
        this.onFinished = onFinished;
    }

    @Override
    public void setOnError(Runnable onError) {
        this.onError = onError;
    }

    @Override
    public void setOnReady(Runnable onMediaPlayerReady) {
        this.onMediaPlayerReady = onMediaPlayerReady;
    }

    @Override
    public void disposePlayer() {
        LOGGER.finer("Setting all VLC player runnables to null.");
        onPlaying = null;
        onPaused = null;
        onMediaPlayerReady = null;
        onError = null;
        onFinished = null;
        onStopped = null;

        timeline.stop();
        videoWidth = 0;
        videoHeight = 0;
    }

    @Override
    public BooleanProperty muteProperty() {
        return isMute;
    }

    @Override
    public DoubleProperty volumeProperty() {
        return volume;
    }

    @Override
    public ReadOnlyObjectProperty<Duration> currentTimeProperty() {
        return currentTime;
    }

    @Override
    public Node getNode() {
        return this;
    }

    @Override
    public Throwable getError() {
        return null;
    }

    @Override
    public String getErrorMessage() {
        return "Native VLC Error.";
    }

    @Override
    public BufferedImage getSnapshot() {
        LOGGER.fine("Requesting VLC snapshot");
        return mediaPlayer.snapshots().get();
    }

    @Override
    public int getVideoWidth() {
        return videoWidth;
    }

    @Override
    public int getVideoHeight() {
        return videoHeight;
    }
}
