package com.chrrubin.cherryrenderer;

import com.chrrubin.cherryrenderer.prefs.AbstractPreference;
import com.chrrubin.cherryrenderer.prefs.LibVlcDirectoryPreference;
import uk.co.caprica.vlcj.factory.discovery.provider.DiscoveryDirectoryProvider;

import java.nio.file.Files;
import java.nio.file.Paths;

public class CherryVlcProvider implements DiscoveryDirectoryProvider {
    private boolean supported = true;
    private String directory;

    public CherryVlcProvider(){
        AbstractPreference<String> directoryPreference = new LibVlcDirectoryPreference();
        String directory = directoryPreference.get();
        if(directory.isEmpty() || Files.notExists(Paths.get(directory))){
            supported = false;
            return;
        }

        this.directory = directory;
    }

    @Override
    public int priority() {
        return 1;
    }

    @Override
    public String[] directories() {
        return new String[]{directory};
    }

    @Override
    public boolean supported() {
        return supported;
    }
}
