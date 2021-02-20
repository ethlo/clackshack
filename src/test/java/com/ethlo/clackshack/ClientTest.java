package com.ethlo.clackshack;

import java.util.Collections;

import org.junit.Test;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class ClientTest
{
    static
    {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    @Test
    public void testProgress()
    {
        final Client client = new Client("http://localhost:8123");
        //final String query = "select toStartOfQuarter(created) as date, count() as count from validations where organization_id = {org_id:Int32} group by date";
        final String query = "SELECT count() FROM numbers(10000000000)";
        client.query(query, Collections.singletonMap("org_id", "123"), ProgressListener.STDOUT, System.out::println);
    }
}