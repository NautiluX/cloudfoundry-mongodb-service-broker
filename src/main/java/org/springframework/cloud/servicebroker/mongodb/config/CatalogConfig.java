package org.springframework.cloud.servicebroker.mongodb.config;

import static org.springframework.cloud.servicebroker.mongodb.config.CatalogConfig.ServicePlanIdentifier.*;

import java.util.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.servicebroker.model.Catalog;
import org.springframework.cloud.servicebroker.model.Plan;
import org.springframework.cloud.servicebroker.model.ServiceDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CatalogConfig {

    public static final String COSTS = "costs";
    public static final String BULLETS = "bullets";
    public static final String AMOUNT = "amount";
    public static final String UNIT = "unit";
    public static final String STORAGE = "storage";
    public static final String REPLICAS = "replicas";
    public static final String CURRENCY = "usd";

    enum ServicePlanIdentifier {
        D, G, P
    }

    public enum ServicePlan {

        DEFAULT("v3.4-dev" , "v3.4-dev" , "This is a default mongo free plan", true, true, getPlanMetadata(D)),
        GOLD("gold", "gold", "This is a paid mongo plan", false, true, getPlanMetadata(G)),
        PLATINUM("platinum", "platinum", "This is a paid premium mongo plan", false, true, getPlanMetadata(P));

        ServicePlan(String id, String name, String description, boolean free, boolean bindable,
                    Map<String, Object> metadata) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.free = free;
            this.bindable = bindable;
            this.metadata = metadata;
        }

        private String id;
        private String name;
        private String description;
        private Map<String, Object> metadata;
        private boolean free;
        private boolean bindable;

        private static Map<String, Object> getPlanMetadata(ServicePlanIdentifier plan) {
            Map<String, Object> planMetadata = new HashMap<>();
            Map<String, Object> costsMap = new HashMap<>();
            List<String> bulletsList = null;
            switch (plan) {
                case D:
                    costsMap.put(AMOUNT, new HashMap<String, Object>() {
                        {
                            put(CURRENCY, 0.0);
                        }
                    });
                    costsMap.put(UNIT, "MONTHLY");
                    costsMap.put(STORAGE, "128Mi");
                    costsMap.put(REPLICAS, "1");
                    bulletsList = Arrays.asList("128Mi Storage (enforced)", "Single instance");
                    break;
                case G:
                    costsMap.put(AMOUNT, new HashMap<String, Object>() {
                        {
                            put(CURRENCY, 100.0);
                        }
                    });
                    costsMap.put(UNIT, "MONTHLY");
                    costsMap.put(STORAGE, "1Gi");
                    costsMap.put(REPLICAS, "3");
                    bulletsList = Arrays.asList("1Gi Storage (enforced)", "3 instances");
                    break;
                case P:
                    costsMap.put(AMOUNT, new HashMap<String, Object>() {
                        {
                            put(CURRENCY, 500.0);
                        }
                    });
                    costsMap.put(UNIT, "MONTHLY");
                    costsMap.put(STORAGE, "10Gi");
                    costsMap.put(REPLICAS, "5");
                    bulletsList = Arrays.asList("10Gi Storage (enforced)", "5 instances");
                    break;
            }
            planMetadata.put(COSTS, Collections.singletonList(costsMap));
            planMetadata.put(BULLETS, bulletsList);
            return planMetadata;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public Map<String, Object> getMetadata() {
            return metadata;
        }

        public boolean isFree() {
            return free;
        }

        public boolean isBindable() {
            return bindable;
        }
    }

    @Value("${service.id:mongodb}")
    private String serviceId;

    @Bean
    public Catalog catalog() {

        List<Plan> planList = new ArrayList<>();
        for (ServicePlan plan : ServicePlan.values()) {
            planList.add(new Plan(serviceId + plan.getId(), plan.getName(),
                    plan.getDescription(), plan.getMetadata(), plan.isFree(), plan.isBindable()));
        }

        return new Catalog(Collections.singletonList(new ServiceDefinition(serviceId,
                serviceId, "A PKS MongoDB on-demand service broker implementation",
                true, false, planList, Arrays.asList("mongodb", "document"),
                getServiceDefinitionMetadata(), null, null)));
    }

    /* Used by Pivotal CF console */

    private Map<String, Object> getServiceDefinitionMetadata() {
        Map<String, Object> sdMetadata = new HashMap<>();
        sdMetadata.put("displayName", "MongoDB");
        sdMetadata.put("imageUrl",
                "http://info.mongodb.com/rs/mongodb/images/MongoDB_Logo_Full.png");
        sdMetadata.put("longDescription", "MongoDB Service");
        sdMetadata.put("providerDisplayName", "Pivotal");
        sdMetadata.put("documentationUrl",
                "https://github.com/srinivasa-vasu/cloudfoundry-service-broker");
        sdMetadata.put("supportUrl",
                "https://github.com/srinivasa-vasu/cloudfoundry-service-broker");
        return sdMetadata;
    }

}
