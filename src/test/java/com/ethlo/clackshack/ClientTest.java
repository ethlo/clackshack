package com.ethlo.clackshack;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ethlo.clackshack.model.QueryProgress;

public class ClientTest
{
    private static final Logger logger = LoggerFactory.getLogger(ClientTest.class);
    
    @Test
    public void testAgainstClickHouse()
    {
        final List<QueryProgress> progressList = new LinkedList<>();
        final Client client = new JettyClient("http://localhost:8123");
        final String query = "SELECT count() as count FROM numbers(2000000000)";
        client.query(query, progressList::add, System.err::println);

        sleep(10_000);
        client.close();

        assertThat(progressList).isNotEmpty();
    }

    private void sleep(int ms)
    {
        try
        {
            Thread.sleep(ms);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    @org.junit.Ignore
    @Test
    public void testAgainstMockServer() throws URISyntaxException, IOException
    {
        // Load content
        final List<String> lines = Files.lines(Paths.get(ClassLoader.getSystemResource("http-response.txt")
                .toURI())).collect(Collectors.toList());

        new Thread(() ->
        {
            try
            {
                final int port = 19999;
                final int delayMs = 100;
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
                        sleep(delayMs);
                    }
                }
            }
            catch (IOException exc)
            {
                throw new UncheckedIOException(exc);
            }
        }).start();

        final Client client = new JettyClient("http://localhost:19999");
        client.query("", QueryProgressListener.LOGGER, System.err::println);

        sleep(20_000);
    }
}