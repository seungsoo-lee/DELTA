package org.deltaproject.webui;

import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketApplication;

/**
 * Created by jinwookim on 2018. 7. 20..
 */
public class IFrameApplication extends WebSocketApplication {
    @Override
    public void onMessage(WebSocket socket, String text) {
        super.onMessage(socket, text);
        socket.send("hi");
    }
}
