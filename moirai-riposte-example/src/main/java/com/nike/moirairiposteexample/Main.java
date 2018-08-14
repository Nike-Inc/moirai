package com.nike.moirairiposteexample;

import com.google.common.io.Resources;
import com.nike.moirai.ConfigFeatureFlagChecker;
import com.nike.moirai.Suppliers;
import com.nike.moirai.config.ConfigDecisionInput;
import com.nike.moirairiposteexample.endpoints.GetShoeListEndpoint;
import com.nike.moirai.typesafeconfig.TypesafeConfigDecider;
import com.nike.moirai.typesafeconfig.TypesafeConfigReader;
import com.nike.riposte.server.Server;
import com.nike.riposte.server.config.ServerConfig;
import com.nike.riposte.server.http.Endpoint;
import com.nike.riposte.server.logging.AccessLogger;
import com.typesafe.config.Config;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;
import java.util.function.Supplier;


public class Main {

    public static class AppServerConfig implements ServerConfig {

        ConfigFeatureFlagChecker<Config> featureFlagChecker() {
                Predicate<ConfigDecisionInput<Config>> whiteListedUsersDecider = TypesafeConfigDecider.ENABLED_USERS
                        .or(TypesafeConfigDecider.PROPORTION_OF_USERS);
                String conf;
                try {
                    conf = Resources.toString(Resources.getResource("moirai.conf"), Charset.forName("UTF-8"));
                } catch(Exception e) {
                    throw new RuntimeException(e);
                }
                Supplier<Config> supp = Suppliers.supplierAndThen(() -> conf, TypesafeConfigReader.FROM_STRING);
                return ConfigFeatureFlagChecker.forConfigSupplier(supp, whiteListedUsersDecider);
        }
        private final Collection<Endpoint<?>> endpoints = Collections.singleton(new GetShoeListEndpoint(featureFlagChecker()));
        private final AccessLogger accessLogger = new AccessLogger();

        @Override
        public Collection<Endpoint<?>> appEndpoints() {
            return endpoints;
        }

        @Override
        public AccessLogger accessLogger() {
            return accessLogger;
        }
    }

    public static void main(String[] args) throws Exception {
        Server server = new Server(new AppServerConfig());
        server.startup();
    }
}