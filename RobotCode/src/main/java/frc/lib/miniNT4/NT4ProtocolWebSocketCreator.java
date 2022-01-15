package frc.lib.miniNT4;

import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;

public class NT4ProtocolWebSocketCreator implements WebSocketCreator {
    
    public NT4ProtocolWebSocketCreator()
    {

    }

    @Override
    public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp)
    {
        for (String subprotocol : req.getSubProtocols())
        {
            if ("networktables.first.wpi.edu".equals(subprotocol))
            {
                resp.setAcceptedSubProtocol(subprotocol);
                return new Socket();
            }
        }

        // No valid subprotocol in request, ignore the request
        return null;
    }

}
