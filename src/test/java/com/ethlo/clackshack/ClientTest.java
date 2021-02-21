package com.ethlo.clackshack;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.ethlo.clackshack.model.ProgressListener;

public class ClientTest
{
    private static final Logger logger = LoggerFactory.getLogger(ClientTest.class);

    static
    {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    @org.junit.Before
    public void setup() throws IOException, URISyntaxException
    {
        // Load content
        final List<String> lines = Files.lines(Paths.get(ClassLoader.getSystemResource("http-response.txt")
                .toURI())).collect(Collectors.toList());

        new Thread(() ->
        {
            try
            {
                final int port = 19999;
                final int delayMs = 500;
                final ServerSocket serverSocket = new ServerSocket(port);
                final Socket socket = serverSocket.accept();
                logger.info("Socket open");
                try (final PrintWriter w = new PrintWriter(new OutputStreamWriter(socket.getOutputStream())))
                {
                    for (final String line : lines)
                    {
                        logger.info("Server sending: {}", line);
                        w.print(line);
                        w.print("\n");
                        w.flush();
                        try
                        {
                            Thread.sleep(delayMs);
                        }
                        catch (InterruptedException e)
                        {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }
            catch (IOException exc)
            {
                throw new UncheckedIOException(exc);
            }
        }).start();
    }

    @Test
    @org.junit.Ignore
    public void testAgainstClickHouse()
    {
        final Client client = new Java11Client("http://localhost:8123");
        final String query = "SELECT count() FROM numbers(30000000000)";
        client.query(query, ProgressListener.LOGGER, System.err::println);
    }

    @Test
    public void testAgainstMockServer()
    {
        final Client client = new Java11Client("http://localhost:19999");
        client.query("", ProgressListener.LOGGER, System.err::println);
    }
}