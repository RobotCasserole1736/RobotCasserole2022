package frc.lib.Webserver2;

import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;

/*
 *******************************************************************************************
 * Copyright (C) FRC Team 1736 Robot Casserole - www.robotcasserole.org
 *******************************************************************************************
 *
 * This software is released under the MIT Licence - see the license.txt
 *  file in the root of this repo.
 *
 * Non-legally-binding statement from Team 1736:
 *  Thank you for taking the time to read through our software! We hope you
 *   find it educational and informative! 
 *  Please feel free to snag our software for your own use in whatever project
 *   you have going on right now! We'd love to be able to help out! Shoot us 
 *   any questions you may have, all our contact info should be on our website
 *   (listed above).
 *  If you happen to end up using our software to make money, that is wonderful!
 *   Robot Casserole is always looking for more sponsors, so we'd be very appreciative
 *   if you would consider donating to our club to help further STEM education.
 */

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import frc.lib.Logging.LogFileWrangler;
import frc.lib.Webserver2.DashboardConfig.DashboardConfig;
import frc.lib.Webserver2.LogFiles.LogFileStreamerServlet;
import frc.robot.Robot;

public class Webserver2 {

    static Server server;

    /**
     * Main dashboard configuration object. User should call methods from this
     * object to configure
     */
    public final DashboardConfig dashboard = new DashboardConfig();

    final String resourceBaseLocal = "./src/main/deploy/www";
    final String resourceBaseRIO = "/home/lvuser/deploy/www";

    String resourceBase = resourceBaseRIO; // default to roboRIO

    /**
     * Starts the web server in a new thread. Should be called at the end of robot
     * initialization.
     */
    public void startServer() {

        // Pick web resources path approprate to execution environment
        if (Robot.isReal()) {
            resourceBase = resourceBaseRIO;
        } else {
            resourceBase = resourceBaseLocal;
        }

        // New server will be on the robot's address plus port 5805
        server = new Server();

        // By default - serve all files under the calculated resourceBase folder
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        final HttpConfiguration httpConfiguration = new HttpConfiguration();
        httpConfiguration.setSecureScheme("https");
        httpConfiguration.setSecurePort(5804);

        final ServerConnector http = new ServerConnector(server,
            new HttpConnectionFactory(httpConfiguration));
        http.setPort(5805);
        server.addConnector(http);
        final SslContextFactory sslContextFactory = new SslContextFactory(resourceBase + "/../keystore/localkey.jks");
        sslContextFactory.setKeyStorePassword("aaaaaa");
        final HttpConfiguration httpsConfiguration = new HttpConfiguration(httpConfiguration);
        httpsConfiguration.addCustomizer(new SecureRequestCustomizer());
        final ServerConnector httpsConnector = new ServerConnector(server,
            new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
            new HttpConnectionFactory(httpsConfiguration));
        httpsConnector.setPort(5804);
        server.addConnector(httpsConnector);


        ResourceHandler log_files_rh = new ResourceHandler();
        log_files_rh.setDirectoriesListed(true);
        log_files_rh.setResourceBase(LogFileWrangler.getInstance().logFilePath.toString());
        server.insertHandler(log_files_rh);

        ResourceHandler main_web_files_rh = new ResourceHandler();
        main_web_files_rh.setDirectoriesListed(true);
        main_web_files_rh.setWelcomeFiles(new String[] { "index.html" });
        main_web_files_rh.setResourceBase(resourceBase);
        server.insertHandler(main_web_files_rh);



        // Separately - Dashboard html/js is auto-generated from templates
        ServletHolder dashboardSH = new ServletHolder("dashboard", new DashboardServlet(dashboard));
        context.addServlet(dashboardSH, "/dashboard/dashboard.html");
        context.addServlet(dashboardSH, "/dashboard/dashboard.js");

        // Log File JSON API
        ServletHolder logDataHolder = new ServletHolder("logData", new LogFileStreamerServlet());
        context.addServlet(logDataHolder, "/logData");



        // Kick off server in brand new, low-priority thread.
        Thread serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    server.start();
                    server.join();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        serverThread.setName("WebServer2");
        serverThread.setPriority(2);
        serverThread.start();

    }

}
