package io.confluent.ad.working;

import com.imperva.ddc.core.query.ConnectionResponse;
import com.imperva.ddc.core.query.Endpoint;
import com.imperva.ddc.core.query.Status;
import com.imperva.ddc.service.DirectoryConnectorService;
import io.confluent.ad.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Iterator;

public class DDCExample {

    // https://github.com/imperva/domain-directory-controller

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void main(String[] args) {
        Endpoint endpoint = new Endpoint();
        endpoint.setSecuredConnection(false);
        endpoint.setPort(389);
        endpoint.setHost("192.168.1.98");
        endpoint.setPassword(Config.PASSWORD);
        //* Use the User's Distinguished Name for connection
        endpoint.setUserAccountName(Config.USERNAME+ Config.REALM);


        ConnectionResponse connectionResponse = DirectoryConnectorService.authenticate(endpoint);
        LOG.info("Keys? "+connectionResponse.getStatuses().size());
        Iterator<?> itr = connectionResponse.getStatuses().keySet().iterator();
        while (itr.hasNext()) {
            Object key = itr.next();
            Status value = connectionResponse.getStatuses().get(key);
            LOG.info(key + " :: " + value);
        }

        LOG.info("Successfully authenticated? "+ !connectionResponse.isError());
        //boolean succeeded = !connectionResponse.isError();
    }
}
