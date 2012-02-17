package org.openstack.client.common;

import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.openstack.client.OpenstackCredentials;
import org.openstack.client.OpenstackException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.api.json.JSONConfiguration;

public class OpenstackClient {
    final OpenstackAuthenticationClient authenticationClient;

    Client buildClient(boolean verbose) {
        ClientConfig config = new DefaultClientConfig();

        config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);

        ObjectMapper objectMapper = buildObjectMapper();
        if (objectMapper != null) {
            config.getSingletons().add(new ObjectMapperProvider(objectMapper));
        }

        Client client = Client.create(config);

        if (verbose) {
            client.addFilter(new LoggingFilter(System.out));
        }

        return client;
    }

    /**
     * Build a custom JSON ObjectMapper, or null if we should use default.
     * 
     * @return
     */
    private ObjectMapper buildObjectMapper() {
        // WRAP_ROOT_VALUE puts a top-level element in the JSON, and avoids having to use dummy objects
        ObjectMapper objectMapper = new ObjectMapper();

        {
            // If we want to put a top-level element in the JSON
            // objectMapper.configure(SerializationConfig.Feature.WRAP_ROOT_VALUE, true);
            // objectMapper.configure(DeserializationConfig.Feature.UNWRAP_ROOT_VALUE, true);
        }

        {
            // If we wanted to use a module for further customization...
            // SimpleModule module = new SimpleModule();
            // objectMapper.registerModule(module);
        }

        {
            // If we wanted to force UTC...
            // SerializationConfig serConfig = mapper.getSerializationConfig();
            // SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
            // dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            //
            // serConfig.setDateFormat(dateFormat);
            //
            // DeserializationConfig deserializationConfig = mapper.getDeserializationConfig();
            // deserializationConfig.setDateFormat(dateFormat);
            //
            // mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
        }

        {
            // Use Jackson annotations if they're present, otherwise use JAXB
            AnnotationIntrospector primary = new JacksonAnnotationIntrospector();
            AnnotationIntrospector secondary = new JaxbAnnotationIntrospector();
            AnnotationIntrospector introspector = new AnnotationIntrospector.Pair(primary, secondary);

            objectMapper.setAnnotationIntrospector(introspector);
        }

        return objectMapper;
    }

    public OpenstackClient(String url, OpenstackCredentials credentials, boolean verbose) {
        Client client = buildClient(verbose);

        this.authenticationClient = new OpenstackAuthenticationClient(client, url, credentials);
    }

    public OpenstackImageClient getImageClient() throws OpenstackException {
        return new OpenstackImageClient(authenticationClient);
    }

    public OpenstackComputeClient getComputeClient() throws OpenstackException {
        return new OpenstackComputeClient(authenticationClient);
    }

    public OpenstackAuthenticationClient getAuthenticationClient() {
        return authenticationClient;
    }
}
