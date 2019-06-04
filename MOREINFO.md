# More Info

## Table of Contents
 - [Troubleshooting](#troubleshooting)
   - [I have OpenJDK 8 installed and I can't run CherryRenderer.](#i-have-openjdk-8-installed-and-i-cant-run-cherryrenderer)
   - [I have a VLC installation, but CherryRenderer doesn't detect it as a valid libVLC directory.](#i-have-a-vlc-installation-but-cherryrenderer-doesnt-detect-it-as-a-valid-libvlc-directory)
   - [Some of the buttons are turning into white boxes.](#some-of-the-buttons-are-turning-into-white-boxes)
 - [Application Specific Information](#application-specific-information)
   - [What control point applications have been tested with CherryRenderer?](#what-control-point-applications-have-been-tested-with-cherryrenderer)
   - [What control point application should I use?](#what-control-point-application-should-i-use)
   - [How do I use the embedded VLC player?](#how-do-i-use-the-embedded-vlc-player)
   - [How do I differentiate between the JavaFX player and the embedded VLC player?](#how-do-i-differentiate-between-the-javafx-player-and-the-embedded-vlc-player)
   - [What video formats are supported by CherryRenderer?](#what-video-formats-are-supported-by-cherryrenderer)
 - [General Information](#general-information)
   - [What is a UPnP MediaRenderer and how does it work?](#what-is-a-upnp-mediarenderer-and-how-does-it-work)
   - [What about UPnP Media Servers and UPnP Clients? How are they different from a MediaRenderer?](#what-about-upnp-media-servers-and-upnp-clients-how-are-they-different-from-a-mediarenderer)
   - [DLNA? Chromecast? Airplay? Miracast? This is so confusing.](#dlna-chromecast-airplay-miracast-this-is-so-confusing)
   - [Why was CherryRenderer created?](#why-was-cherryrenderer-created)
   
## Troubleshooting
### I have OpenJDK 8 installed and I can't run CherryRenderer.
 - Unlike Oracle's JDK/JRE, OpenJDK 8 does not include JavaFX by default. You would have to install OpenJFX 8 manually based on your distro's package manager, or manually build OpenJFX 8 following [these instructions](https://wiki.openjdk.java.net/display/OpenJFX/Building+OpenJFX+8u).
 - On Ubuntu/Debian based distros, you can install OpenJFX 8 with the following:
 ```
 sudo apt install openjfx=8u161-b12-1ubuntu2 libopenjfx-java=8u161-b12-1ubuntu2 libopenjfx-jni=8u161-b12-1ubuntu2
 ``` 
 - I blame Oracle for making everyone's lives harder post license change.

### I have a VLC installation, but CherryRenderer doesn't detect it as a valid libVLC directory.
 - The most common cause of this issue is that your Java and VLC installations are in different architectures. 32-bit programs can't access 64-bit native libraries and vice versa.
 - **On Windows, the default Java installation is 32-bit while the default VLC installation is 64-bit**. Annoying, I know. Blame Oracle.
     - You can download the 64-bit version of Java on [Oracle's manual download page](https://www.java.com/en/download/manual.jsp).
 - You can check your Java architecture by running `java -version` in a terminal.
   - A 32-bit version of Java will output something along the lines of:
   ```
   java version "1.8.0_201"
   Java(TM) SE Runtime Environment (build 1.8.0_201-b09)
   Java HotSpot(TM) Client VM (build 25.201-b09, mixed mode, sharing)
   ```
   - A 64-bit version of Java will output something along the lines of:
   ```
   java version "1.8.0_202"
   Java(TM) SE Runtime Environment (build 1.8.0_202-b08)
   Java HotSpot(TM) 64-Bit Server VM (build 25.202-b08, mixed mode)
   ```
   - Note the absence of `64-Bit` in the 32-bit version of Java.

### Some of the buttons are turning into white boxes.
 - This is a JavaFX rendering issue that occurs on certain systems.
 - Try disabling hardware acceleration via `Menu - Preferences - Interface`.
 - However, do note that disabling hardware acceleration may slow down UI rendering significantly in older systems. Use with care.


## Application Specific Information
### What control point applications have been tested with CherryRenderer?
 - Tested and works:
   - [BubbleUPnP](https://play.google.com/store/apps/details?id=com.bubblesoft.android.bubbleupnp&hl=en)
   - [Localcast](https://www.localcast.app/)
   - [All Screen](https://play.google.com/store/apps/details?id=com.toxic.apps.chrome&hl=en)
   - [All Cast](https://www.allcast.io/)
   - [Web Video Caster](http://www.webvideocaster.com)

Note that all of the above are tested on Android only. Please do let me know if other applications work as well.

### What control point application should I use?
 - **Just choose whichever works for you.** Each app works slightly differently than others. Preferably not one that has been tested to not work with CherryRenderer.

### How do I use the embedded VLC player?
 - If you have a normal installation of VLC media player, the required native libVLC libraries *should* be automatically detected and used.
 - If the native libraries are not automatically detected, you can manually set the libVLC directory in `Menu - Preferences - Advanced`.
   - On Windows, the libVLC directory would be your VLC installation directory. Example: `C:\Program Files\VideoLAN\VLC`
   - On Linux, the libVLC directory would be the distro dependant lib folder. Examples: `/usr/lib64`, `/usr/local/lib64`
   - On macOS, the libVLC directory is inside `VLC.app`. Example: `/Applications/VLC.app/Contents/MacOS/lib`

### How do I differentiate between the JavaFX player and the embedded VLC player?
 - You will see an indication on the end of the title of the window. It will say `[JFX]` if the JavaFX player is used and `[VLC]` if the embedded VLC player is used.
 - Here's an example screenshot of CherryRenderer when using the **JavaFX player**:
 
 ![cherryjfx](https://media.discordapp.net/attachments/480408561290182667/582566105323667457/cherryjfx.png "Using default JavaFX player")
 
 - Here's an example screenshot of CherryRenderer when using the **embedded VLC player**:
 
 ![cherryvlc](https://media.discordapp.net/attachments/480408561290182667/582566107240595460/cherryvlc.png "Using embedded VLC player")

### What video formats are supported by CherryRenderer?
 - If you are using the **default JavaFX player**, CherryRenderer supports a few popular video formats as per [javafx.scene.media docs](https://docs.oracle.com/javase/8/javafx/api/javafx/scene/media/package-summary.html).
   - These are the `.fxm`, `.flv`, `.m3u8`, `.mp4` and `.m4v` video formats.
   - There are also certain limitations to these formats, especially for the `.m3u8` format, of which please refer to the [javafx.scene.media docs](https://docs.oracle.com/javase/8/javafx/api/javafx/scene/media/package-summary.html).
   - You may also need to install additional codecs depending on your operating system. Please refer to Oracle's [Certified System Configurations page](https://www.oracle.com/technetwork/java/javase/certconfig-2095354.html) and scroll down to the `JavaFX Media` section.
 - If you are using the **embedded VLC player**, CherryRenderer *theoretically* supports all playable video formats that VLC player supports as per [VLC's wiki page](https://wiki.videolan.org/VLC_Features_Formats/).
   - Notable video format support when using the embedded VLC player are the MPEG-DASH `.mpd` format and less limitations on the HLS `.m3u8` format.


## General Information
### What is a UPnP MediaRenderer and how does it work?
 - A UPnP MediaRenderer is a device that is able to play (render) media content when connected with a UPnP Control Point application.
 - The Control Point sends the URL of the media resource to the MediaRenderer (also known as casting), which then loads and plays the media content.
 - All the loading and buffering of the content are handled by the MediaRenderer device.
 - The Control Point also acts as a remote control, allowing users to control the media playback on the MediaRenderer from the Control Point application.
 - If you have a "Smart TV", chances are it can be used as a MediaRenderer device.

### What about UPnP Media Servers and UPnP Clients? How are they different from a MediaRenderer?
 - A UPnP Media Server is like a media file server that shares its media content to UPnP Clients on the network.
 - A Client can connect to a Media Server, browse the files and select whichever media file to play on the Client's device.
 - A MediaRenderer has no browsing ability, and can only play media files that are specified by a Control Point. However, a Control Point *can* specify the MediaRenderer to play media files from a Media Server.

### DLNA? Chromecast? Airplay? Miracast? This is so confusing.
 - Yes, my child. Yes it is.
 - **DLNA** is a specification derived from UPnP that allows sharing of media content between DLNA-certified devices. While the UPnP protocol covers basically any type of network device, DLNA focuses on media devices, adding functions such as DRM protection that were absent in UPnP.
   - Although pure UPnP devices and pure DLNA devices are technically not compatible with each other, most consumer devices that support one would support the other, some even calling the two equivalent.
   - As such, most control point apps that support DLNA supports UPnP, and by proxy, CherryRenderer.
 - **Chromecast** (also known as Google Cast) and **Airplay** are proprietary protocols developed by Google and Apple respectively. In *very* oversimplification terms, the two serves the same purpose as DLNA and UPnP AV with the added "proprietary-ness".
   - No, CherryRenderer does not support Google Cast or Airplay.
 - **Miracast** is a standard that allows users to mirror their screens to a remote device wirelessly. While Miracast has its benefits, it is not the most ideal choice for sharing media content as it mirrors the device's entire screen.
   - No, CherryRenderer does not support Miracast.
   
### Why was CherryRenderer created?
 - I was introduced to UPnP casting from an Android Anime app called [AnYme](https://anyme.app). While casting to a TV or Raspberry Pi running Kodi was fine, I wanted a way to cast to PC.
 - A quick Google search showed almost nothing that just worked as a MediaRenderer. Popular video players like VLC only works as a UPnP client, while the only other solutions were to install huge bundled software such as Kodi on my PC, which was overkill for just wanting a MediaRenderer implementation.
 - A small group of the AnYme community also wanted a way to watch via the app on their PC, most of which had to resort to screen mirroring or emulators. 
 