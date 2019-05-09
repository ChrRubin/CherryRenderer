package com.chrrubin.cherryrenderer.api;

import com.chrrubin.cherryrenderer.MediaObject;
import com.chrrubin.cherryrenderer.upnp.states.RendererState;
import com.google.gson.Gson;
import com.pploder.events.Event;
import com.pploder.events.SimpleEvent;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import javafx.util.Duration;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ApiService {
    private final Logger LOGGER = Logger.getLogger(ApiService.class.getName());
    private final Gson gson = new Gson();
    private final int PORT_DEFAULT = 54325;
    private final int PORT_FALLBACK = 54326; // TODO: Subject to change

    private IsAlive isAlive = new IsAlive();
    private CurrentlyPlaying currentlyPlaying = new CurrentlyPlaying();

    private final Event<MediaObject> mediaObjectEvent = new SimpleEvent<>();
    private final Event<Object> togglePauseEvent = new SimpleEvent<>();
    private final Event<Duration> seekEvent = new SimpleEvent<>();
    private final Event<Object> stopEvent = new SimpleEvent<>();
    private final Event<Object> toggleMuteEvent = new SimpleEvent<>();
    private final Event<Integer> setVolumeEvent = new SimpleEvent<>();

    public ApiService() throws IOException {
        HttpServer server;

        if(checkPortAvailable(PORT_DEFAULT)){
            LOGGER.fine("Creating HTTP server on default port " + PORT_DEFAULT);
            server = HttpServer.create(new InetSocketAddress(PORT_DEFAULT), 0);
        }
        else if(checkPortAvailable(PORT_FALLBACK)){
            LOGGER.fine("Creating HTTP server on fallback port " + PORT_FALLBACK);
            server = HttpServer.create(new InetSocketAddress(PORT_FALLBACK), 0);
        }
        else {
            throw new RuntimeException("Both default port " + PORT_DEFAULT + " and fallback port " + PORT_FALLBACK + " unavailable.");
        }

        server.createContext("/", this::handleDefault);
        server.createContext("/isAlive", httpExchange -> handleGetRequests(httpExchange, isAlive, IsAlive.class));
        server.createContext("/currentlyPlaying", httpExchange -> handleGetRequests(httpExchange, currentlyPlaying, CurrentlyPlaying.class));
        server.createContext("/play", httpExchange -> handlePostRequests(httpExchange, Action.PLAY));
        server.createContext("/togglePause", httpExchange -> handlePostRequests(httpExchange, Action.TOGGLE_PAUSE));
        server.createContext("/stop", httpExchange -> handlePostRequests(httpExchange, Action.STOP));
        server.createContext("/seek", httpExchange -> handlePostRequests(httpExchange, Action.SEEK));
        server.createContext("/toggleMute", httpExchange -> handlePostRequests(httpExchange, Action.TOGGLE_MUTE));
        server.createContext("/setVolume", httpExchange -> handlePostRequests(httpExchange, Action.SET_VOLUME));

        server.start();
    }

    private void handleDefault(HttpExchange exchange) throws IOException{
        String response = gson.toJson(new HttpResponse(404));
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(404, response.getBytes().length);

        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(response.getBytes());
        outputStream.close();
    }

    private void handleGetRequests(HttpExchange exchange, GetResponse getResponseObject, Type objectType) throws IOException{
        String response;
        if(exchange.getRequestMethod().equals("GET")){
            response = gson.toJson(getResponseObject, objectType);

            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.getBytes().length);
        }
        else{
            response = gson.toJson(new HttpResponse(405));
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(405, response.getBytes().length);
        }

        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(response.getBytes());
        outputStream.close();
    }

    private void handlePostRequests(HttpExchange exchange, Action action) throws IOException{
        String response;
        if(exchange.getRequestMethod().equals("POST")){
            InputStreamReader reader = new InputStreamReader(exchange.getRequestBody());
            try {
                if(action == Action.PLAY){
                    response = onPlay(gson.fromJson(reader, PlayArguments.class));
                }
                else if(Arrays.asList(RendererState.PLAYING, RendererState.PAUSED).contains(currentlyPlaying.getStatus())) {
                    switch (action) {
                        case TOGGLE_PAUSE:
                            response = onTogglePause();
                            break;
                        case SEEK:
                            response = onSeek(gson.fromJson(reader, SeekArguments.class));
                            break;
                        case STOP:
                            response = onStop();
                            break;
                        case TOGGLE_MUTE:
                            response = onToggleMute();
                            break;
                        case SET_VOLUME:
                            response = onSetVolume(gson.fromJson(reader, SetVolumeArguments.class));
                            break;
                        default:
                            throw new RuntimeException("Unknown POST action");
                    }
                }
                else{
                    response = gson.toJson(new HttpResponse(400, "Player is not currently playing."));
                }

                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.getBytes().length);
            }
            catch (Exception e){ //TODO: Change this pls thx
                LOGGER.log(Level.SEVERE, e.toString(), e);
                response = gson.toJson(new HttpResponse(500, e.toString()));
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(500, response.getBytes().length);
            }
        }
        else{
            response = gson.toJson(new HttpResponse(405));
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(405, response.getBytes().length);
        }

        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(response.getBytes());
        outputStream.close();
    }

    private String onPlay(PlayArguments arguments){
        if(arguments.getVideos()[0] == null){
            RuntimeException e = new RuntimeException("No videos found from payload");
            LOGGER.log(Level.SEVERE, e.toString(), e);
            throw e;
        }

        MediaObject mediaObject = new ApiMediaObject(arguments.getVideos()[0]);
        mediaObjectEvent.trigger(mediaObject);

        return gson.toJson(new HttpResponse(200));
    }

    private String onTogglePause(){
        togglePauseEvent.trigger();
        return gson.toJson(new HttpResponse(200));
    }

    private String onSeek(SeekArguments arguments){
        seekEvent.trigger(Duration.seconds(arguments.getTargetTime()));
        return gson.toJson(new HttpResponse(200));
    }

    private String onStop(){
        stopEvent.trigger();
        return gson.toJson(new HttpResponse(200));
    }

    private String onToggleMute(){
        toggleMuteEvent.trigger();
        return gson.toJson(new HttpResponse(200));
    }

    private String onSetVolume(SetVolumeArguments arguments){
        setVolumeEvent.trigger(arguments.getTargetVolume());
        return gson.toJson(new HttpResponse(200));
    }

    public Event<MediaObject> getMediaObjectEvent() {
        return mediaObjectEvent;
    }

    public Event<Object> getTogglePauseEvent() {
        return togglePauseEvent;
    }

    public Event<Duration> getSeekEvent() {
        return seekEvent;
    }

    public Event<Object> getStopEvent() {
        return stopEvent;
    }

    public Event<Object> getToggleMuteEvent() {
        return toggleMuteEvent;
    }

    public Event<Integer> getSetVolumeEvent() {
        return setVolumeEvent;
    }

    public void setCurrentStatus(RendererState status) {
        currentlyPlaying.setStatus(status);
    }

    public void setCurrentlyPlayingMedia(MediaObject mediaObject){
        currentlyPlaying.setMedia(mediaObject);
    }

    public void updateCurrentlyPlayingInfo(Duration currentTime, Duration totalTime, boolean mute, int volume){
        currentlyPlaying.setCurrentTime((int)Math.round(currentTime.toSeconds()));
        currentlyPlaying.setTotalTime((int)Math.round(totalTime.toSeconds()));
        currentlyPlaying.setMute(mute);
        currentlyPlaying.setVolume(volume);
    }

    public void clearInfo(){
        currentlyPlaying = new CurrentlyPlaying();
    }

    private boolean checkPortAvailable(int port){
        try(ServerSocket serverSocket = new ServerSocket(port)){
            serverSocket.setReuseAddress(true);
            return true;
        }
        catch (IOException e){
            return false;
        }
    }
}
