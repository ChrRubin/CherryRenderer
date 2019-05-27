# CherryRenderer

A standalone UPnP MediaRenderer video player for Windows, Linux and (probably[*](#more-info)) macOS.

Cast videos from your Android/iOS phone etc to your PC as if it was a Smart TV!

![screenshot](https://cdn.discordapp.com/attachments/480408561290182667/582642305413021717/cherryrenderer3.png)
*(Screenshot was taken running [Web Video Caster](http://www.webvideocaster.com) on Android and CherryRenderer on Arch Linux + XFCE)*

CherryRenderer is built with JavaFX and [Cling](https://github.com/4thline/cling) (yes, I know it is no longer being maintained).

## Instructions
 1. Make sure Java 8 is installed. You can check your Java version by following [these instructions](https://www.java.com/en/download/help/version_manual.xml). 
 2. [Download](https://github.com/ChrRubin/CherryRenderer/releases) and run CherryRenderer_*[version]*.jar.
 3. Open the control point application on your remote device.
 4. Connect to the CherryRenderer device.
 5. Start casting!

*&ast; OpenJDK 8 does not include JavaFX by default. Read more about this [here](MOREINFO.md#i-have-openjdk-8-installed-and-i-cant-run-cherryrenderer).*

*&ast; If you are planning to use the embedded VLC player, make sure that your Java and VLC installations are of the same architecture. More info about this can be found [here](MOREINFO.md#i-have-a-vlc-installation-but-cherryrenderer-doesnt-detect-it-as-a-valid-libvlc-directory).*

## Features
 - **UPnP MediaRenderer support**
   - **Cast videos** to CherryRenderer via a control point application
   - **Control video playback** such as Play, Pause, Stop, Rewind, Fast Forward from the control point application
   - **Control the volume** of CherryRenderer from control point applications that support this feature
 - **&ast;NEW&ast;** **Embedded VLC Player** for additional video format support if you have VLC player installed
   - If you do not have VLC player installed, the default JavaFX player will be used
 - Optional **dark theme** available
 - **Take snapshots** of the currently playing video
 - Hotkey support:
 
 | Function          | Key   |
 |----------         |:-----:|
 | Play/Pause        | Space |
 | Toggle fullscreen | F     |
 | Stop              | S     |
 | Rewind            | Left  |
 | Fast Forward      | Right |
 | Volume Up         | Up    |
 | Volume Down       | Down  |
 | Toggle Mute       | M     |
 | Cycle Zoom Level  | Z     |

## More Info
For more info such as what control point apps have been tested and what video formats CherryRenderer supports, click [here](MOREINFO.md).

*&ast; I do not own a macOS device, nor do my alpha testers, but technically as long as Java 8 is installed it should run fine.*

## Credits
 - [Cling](https://github.com/4thline/cling)
 - [easy-events](https://github.com/Fylipp/easy-events)
 - [Gson](https://github.com/google/gson)
 - [vlcj](https://github.com/caprica/vlcj)
 - Special thanks to my alpha testers for testing this program when it was extremely buggy.

## License
CherryRenderer is released under [GNU GPLv3](https://www.gnu.org/licenses/gpl-3.0.en.html). The full license can be found in the [COPYING](COPYING) file in the repository's root directory.