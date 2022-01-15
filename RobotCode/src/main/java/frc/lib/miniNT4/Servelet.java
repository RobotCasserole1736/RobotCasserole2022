package frc.lib.miniNT4;

import javax.servlet.annotation.WebServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

@SuppressWarnings("serial")
@WebServlet(name = "Mini NT4 Streamer Servlet", urlPatterns = {"/nt/*"})
class Servlet extends WebSocketServlet {

    @Override
    public void configure(WebSocketServletFactory factory) {
        factory.getPolicy().setIdleTimeout(1000000); //1000-second keep-alive
        factory.register(Socket.class);
        factory.setCreator(new NT4ProtocolWebSocketCreator());
    }
}