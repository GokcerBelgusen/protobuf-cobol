package com.legstar.protobuf.cobol;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;

/**
 * Integration test for sample CICS T1FILEAQ service.
 * 
 */
public class T1fileaqTestIT extends TestCase {

    private static final String SERVER_URL = "http://mainframe:4081/CICS/CWBA/T1FILEAQ";

    public void testExecuteLsfileaq() throws Exception {
        byte[] bytesReply = postJson(getSampleRequestContent());
        com.example.customers.CustomersProtos.CustomersQueryReply customersQueryReply = com.example.customers.CustomersProtos.CustomersQueryReply
                .parseFrom(bytesReply);
        assertEquals(5, customersQueryReply.getCustomersCount());
        assertEquals(100, customersQueryReply.getCustomers(0).getCustomerId());
        assertEquals(762, customersQueryReply.getCustomers(1).getCustomerId());
        assertEquals(6016, customersQueryReply.getCustomers(2).getCustomerId());
        assertEquals(200000, customersQueryReply.getCustomers(3)
                .getCustomerId());
        assertEquals(555555, customersQueryReply.getCustomers(4)
                .getCustomerId());

        assertEquals("S. D. BORMAN", customersQueryReply.getCustomers(0)
                .getPersonalData().getCustomerName());
        assertEquals("SUSAN MALAIKA", customersQueryReply.getCustomers(1)
                .getPersonalData().getCustomerName());
        assertEquals("SIR MICHAEL ROBERTS", customersQueryReply.getCustomers(2)
                .getPersonalData().getCustomerName());
        assertEquals("S. P. RUSSELL", customersQueryReply.getCustomers(3)
                .getPersonalData().getCustomerName());
        assertEquals("S.J. LAZENBY", customersQueryReply.getCustomers(4)
                .getPersonalData().getCustomerName());

        assertEquals(100.11d, customersQueryReply.getCustomers(0)
                .getLastTransactionAmount());
        assertEquals(0.0d, customersQueryReply.getCustomers(1)
                .getLastTransactionAmount());
        assertEquals(9.879999999999999d, customersQueryReply.getCustomers(2)
                .getLastTransactionAmount());
        assertEquals(20.0d, customersQueryReply.getCustomers(3)
                .getLastTransactionAmount());
        assertEquals(5.0d, customersQueryReply.getCustomers(4)
                .getLastTransactionAmount());

    }

    /**
     * POST protocol buffers content to the CICS service.
     * 
     * @param protobufBytes the protocol buffers payload
     * @return the reply
     * @throws Exception if execution fails
     */
    protected byte[] postJson(byte[] protobufBytes) throws Exception {
        URL url = new URL(SERVER_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        return doOutput(conn, "POST", protobufBytes);
    }

    /**
     * Sends protocol buffers content using the specified method.
     * 
     * @param conn the opened HTTP connection
     * @param method the HTTP method
     * @param protobufBytes the protocol buffers payload
     * @return the reply from the HTTP request
     * @throws Exception if request fails
     */
    protected byte[] doOutput(HttpURLConnection conn, String method,
            byte[] protobufBytes) throws Exception {
        conn.setDoOutput(true);
        conn.setRequestMethod(method);
        conn.setRequestProperty("Content-Type", "application/x-protobuf");
        conn.setRequestProperty("Accept", "application/x-protobuf");
        OutputStream wr = conn.getOutputStream();
        wr.write(protobufBytes);
        wr.flush();
        wr.close();
        if (conn.getResponseCode() > 299) {
            System.out.println(conn.getResponseMessage());
        }
        if (conn.getErrorStream() != null) {
            List < String > errors = IOUtils.readLines((conn.getErrorStream()));
            System.out.println(errors.toString());
        }
        return IOUtils.toByteArray(conn.getInputStream());
    }

    /**
     * @return a sample protocol buffers serialized request
     */
    protected byte[] getSampleRequestContent() {
        com.example.customers.CustomersProtos.CustomersQuery query = com.example.customers.CustomersProtos.CustomersQuery
                .newBuilder().setCustomerNamePattern("S*").setMaxReplies(5)
                .build();
        return query.toByteArray();

    }

}
