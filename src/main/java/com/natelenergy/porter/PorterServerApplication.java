package com.natelenergy.porter;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import io.federecio.dropwizard.swagger.*;

import java.lang.invoke.MethodHandles;

import javax.servlet.FilterRegistration;

import org.glassfish.jersey.media.multipart.*;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bazaarvoice.dropwizard.redirect.PathRedirect;
import com.bazaarvoice.dropwizard.redirect.RedirectBundle;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.natelenergy.porter.api.v0.InfoResource;
import com.natelenergy.porter.api.v0.JSONResource;
import com.natelenergy.porter.api.v0.UploadResource;
import com.natelenergy.porter.health.SimpleHealthCheck;
import com.natelenergy.porter.tasks.EchoTask;
import com.natelenergy.porter.util.IllegalArgumentExceptionMapper;

import org.eclipse.jetty.servlets.CrossOriginFilter;
import javax.servlet.DispatcherType;
import java.util.EnumSet;

public class PorterServerApplication extends Application<PorterServerConfiguration> {
  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  
  public static void main(String[] args) throws Exception {
    new PorterServerApplication().run(args);
  }

  @Override
  public String getName() {
    return "data-porter";
  }
  
  @Override
  public void initialize(Bootstrap<PorterServerConfiguration> bootstrap) {
    bootstrap.addBundle(new SwaggerBundle<PorterServerConfiguration>() {
      @Override
      protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(PorterServerConfiguration configuration) {
        return configuration.swaggerBundleConfiguration;
      }
    });
    
//    // Custom Jackson stuff
//    SimpleModule testModule = new SimpleModule("UpstreamModule", new Version(1, 0, 0, null, null, null));
//    testModule.addSerializer(CellReference.class, new CellReferenceSerializer());
//    testModule.addDeserializer(CellReference.class, new CellReferenceDeserializer());
//   
//    ObjectMapper mapper = Jackson.newObjectMapper();
//    mapper.registerModule(testModule);
//    bootstrap.setObjectMapper(mapper);
    
    // Enable variable substitution with environment variables
    bootstrap.setConfigurationSourceProvider(
        new SubstitutingSourceProvider(
            bootstrap.getConfigurationSourceProvider(),
            new EnvironmentVariableSubstitutor(false)
        )
    );
    bootstrap.addBundle(new AssetsBundle());
    bootstrap.addBundle(new ViewBundle<>());
    
    // Redirect the home to swagger
    bootstrap.addBundle(new RedirectBundle(
      new PathRedirect("/", "/swagger")
    ));
  }

  @Override
  public void run(PorterServerConfiguration configuration, Environment environment) {
    // Enable CORS headers
    final FilterRegistration.Dynamic cors =
        environment.servlets().addFilter("CORS", CrossOriginFilter.class);

    // Configure CORS parameters
    cors.setInitParameter("allowedOrigins", "*");
    cors.setInitParameter("allowedHeaders", "X-Requested-With,Content-Type,Accept,Origin");
    cors.setInitParameter("allowedMethods", "OPTIONS,GET,PUT,POST,DELETE,HEAD");

    // Add URL mapping
    cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");

    
    environment.healthChecks().register("simple", new SimpleHealthCheck());
    environment.admin().addTask(new EchoTask());
    environment.jersey().register(IllegalArgumentExceptionMapper.class);
    environment.jersey().register(RolesAllowedDynamicFeature.class);
    environment.jersey().register(MultiPartFeature.class);
    
    
    ObjectMapper mapper = environment.getObjectMapper();
    InfoResource info = new InfoResource();
    LOGGER.info("Loading: \n"
        + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n"
        + ">>  "+info.getGitDescription()+"\n>>\n"
        + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" );
    
    // The resources
    environment.jersey().register(new UploadResource());
    environment.jersey().register(new JSONResource(configuration.liveDB));
    environment.jersey().register(info);
  }
}
