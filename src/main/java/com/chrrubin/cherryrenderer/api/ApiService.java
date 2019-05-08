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
import java.util.logging.Level;
import java.util.logging.Logger;

public class ApiService {
    private final Logger LOGGER = Logger.getLogger(ApiService.class.getName());
    private final Gson gson = new Gson();
    private final int PORT_DEFAULT = 54321;
    private final int PORT_FALLBACK = 54322; // TODO: Subject to change

    private Status currentStatus = new Status(RendererState.STOPPED);
    private MediaObject[] videoQueue;
    private CurrentlyPlaying currentlyPlaying = new CurrentlyPlaying();

    private final Event<MediaObject> mediaObjectEvent = new SimpleEvent<>();

    public ApiService() throws IOException {
        HttpServer server;

        if(checkPortAvailable(PORT_DEFAULT)){
            server = HttpServer.create(new InetSocketAddress(PORT_DEFAULT), 0);
        }
        else if(checkPortAvailable(PORT_FALLBACK)){
            server = HttpServer.create(new InetSocketAddress(PORT_FALLBACK), 0);
        }
        else {
            throw new RuntimeException("Both default port " + PORT_DEFAULT + " and fallback port " + PORT_FALLBACK + " unavailable.");
        }

        server.createContext("/status", httpExchange -> handleGetRequests(httpExchange, currentStatus, Status.class));
        server.createContext("/currentlyPlaying", httpExchange -> handleGetRequests(httpExchange, currentlyPlaying, CurrentlyPlaying.class));
        server.createContext("/play", httpExchange -> handlePostRequests(httpExchange, Action.PLAY));
        server.createContext("/togglePause", httpExchange -> handlePostRequests(httpExchange, Action.TOGGLE_PAUSE));
        server.createContext("/stop", httpExchange -> handlePostRequests(httpExchange, Action.STOP));
        server.createContext("/seek", httpExchange -> handlePostRequests(httpExchange, Action.SEEK));
        server.createContext("/toggleMute", httpExchange -> handlePostRequests(httpExchange, Action.TOGGLE_MUTE));
        server.createContext("/setVolume", httpExchange -> handlePostRequests(httpExchange, Action.SET_VOLUME));

        server.start();
    }

    private void handleGetRequests(HttpExchange exchange, Response responseObject, Type objectType) throws IOException{
        String response;
        if(exchange.getRequestMethod().equals("GET")){
            response = gson.toJson(responseObject, objectType);

            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.getBytes().length);
        }
        else{
            response = "405 Method Not Allowed";
            exchange.getResponseHeaders().add("Content-Type", "text/plain");
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
                switch (action) {
                    case PLAY:
                        response = onPlay(gson.fromJson(reader, PlayArguments.class));
                        break;
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

                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.getBytes().length);
            }
            catch (Exception e){ //TODO: Change this pls thx
                LOGGER.log(Level.SEVERE, e.toString(), e);
                response = "500 Internal Server Error: " + e.toString();
                exchange.getResponseHeaders().add("Content-Type", "text/plain");
                exchange.sendResponseHeaders(500, response.getBytes().length);
            }
        }
        else{
            response = "405 Method Not Allowed";
            exchange.getResponseHeaders().add("Content-Type", "text/plain");
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

        videoQueue = arguments.getVideos();
        MediaObject mediaObject = new ApiMediaObject(videoQueue[0]);
        currentlyPlaying.setVideo(mediaObject);
        mediaObjectEvent.trigger(mediaObject);

        return null;
    }

    private String onTogglePause(){
        return null;
    }

    private String onSeek(SeekArguments arguments){
        return null;
    }

    private String onStop(){
        return null;
    }

    private String onToggleMute(){
        return null;
    }

    private String onSetVolume(SetVolumeArguments arguments){
        return null;
    }

    public void setCurrentStatus(RendererState currentStatus) {
        this.currentStatus.setStatus(currentStatus);
    }

    public Event<MediaObject> getMediaObjectEvent() {
        return mediaObjectEvent;
    }

    public void setCurrentlyPlayingMedia(MediaObject mediaObject){
        currentlyPlaying.setVideo(mediaObject);
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
