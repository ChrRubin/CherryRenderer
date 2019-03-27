package com.chrrubin.cherryrenderer.upnp;

import com.chrrubin.cherryrenderer.upnp.states.RendererNoMediaPresent;
import com.chrrubin.cherryrenderer.upnp.states.RendererStateMachine;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.binding.LocalServiceBindingException;
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.*;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.support.avtransport.impl.AVTransportService;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportLastChangeParser;
import org.fourthline.cling.support.lastchange.LastChangeAwareServiceManager;
import org.fourthline.cling.support.lastchange.LastChangeParser;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RendererService {
    private String friendlyName;

    private ExecutorService mainExecutor = Executors.newSingleThreadExecutor();
    private ScheduledExecutorService lastChangeExecutor = Executors.newSingleThreadScheduledExecutor();

    public RendererService(String friendlyName){
        this.friendlyName = friendlyName;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    private LocalDevice createDevice() throws ValidationException, LocalServiceBindingException, URISyntaxException {

        DeviceIdentity identity = new DeviceIdentity(UDN.uniqueSystemIdentifier("CherryRenderer"));

        DeviceType type = new UDADeviceType("MediaRenderer", 1);

        DeviceDetails details = new DeviceDetails(
                friendlyName,
                new ManufacturerDetails("ChrRubin", new URI("http://chrrubin.com")),
                new ModelDetails(
                        "CherryRenderer",
                        "CherryRenderer - Standalone UPnP Media Renderer",
                        "1"
                        )
        );

        LocalService<AVTransportService> service = new AnnotationLocalServiceBinder().read(AVTransportService.class);

        LastChangeParser lastChangeParser = new AVTransportLastChangeParser();

        service.setManager(
                new LastChangeAwareServiceManager<AVTransportService>(service, lastChangeParser) {
                    @Override
                    protected AVTransportService createServiceInstance() throws Exception {
                        return new AVTransportService(
                                RendererStateMachine.class,   // All states
                                RendererNoMediaPresent.class  // Initial state
                        );
                    }
                }
        );

        lastChangeExecutor.scheduleWithFixedDelay(() ->{
//            System.out.println("Flushing last change");
            LastChangeAwareServiceManager manager = (LastChangeAwareServiceManager)service.getManager();
            manager.fireLastChange();
        },0,500, TimeUnit.MILLISECONDS);

        return new LocalDevice(identity, type, details, service);
    }

    public void startService() {
        // TODO: Figure out why 2 instances spawn instead after the first time running the program
        mainExecutor.submit(() -> {
            try{
                final UpnpService upnpService = new UpnpServiceImpl();

                Runtime.getRuntime().addShutdownHook(new Thread(() ->{
                    // TODO: The service doesn't shutdown properly? It's not showing signs of it in the log
                    upnpService.shutdown();
                    lastChangeExecutor.shutdown();
                }));

                upnpService.getRegistry().addDevice(createDevice());
            }
            catch (Exception e){
                e.printStackTrace();
                System.exit(1);
            }
        });

    }

}
