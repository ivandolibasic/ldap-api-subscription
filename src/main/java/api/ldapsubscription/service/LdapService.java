package api.ldapsubscription.service;

import api.ldapsubscription.model.EventFilter;
import api.ldapsubscription.model.Snssai;
import api.ldapsubscription.model.Subscription;
import org.ldaptive.*;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LdapService {
    private static PooledConnectionFactory connectionFactory;

    //    public static PooledConnectionFactory connect() {
//        if (connectionFactory == null) {
//            connectionFactory = new PooledConnectionFactory("ldap://localhost");
//            connectionFactory.initialize();
//        }
//        return connectionFactory;
//    }
//    public static void bind() throws LdapException {
//        BindOperation bind = new BindOperation(connect());
//        BindResponse response = bind.execute(SimpleBindRequest.builder()
//                .dn("cn=admin,dc=example,dc=com")
//                .password("secret")
//                .build());
//        if (response.isSuccess()) {
//            return true;
//        }
//        return false;
//    }
    public static PooledConnectionFactory connectAndBind() throws LdapException {
        if (connectionFactory == null) {
            connectionFactory = new PooledConnectionFactory("ldap://localhost");
            connectionFactory.initialize();
        }
        BindOperation bind = new BindOperation(connectionFactory);
        BindResponse response = bind.execute(SimpleBindRequest.builder()
                .dn("cn=admin,dc=example,dc=com")
                .password("secret")
                .build());
        return connectionFactory;
    }
    public static void closeConnection() {
        if (connectionFactory != null) {
            connectionFactory.close();
        }
    }

    public static Subscription ldapSearch(UUID subscriptionId) throws LdapException {
        SearchOperation searchSubscription = new SearchOperation(connectAndBind());

        SearchResponse responseSubscription = searchSubscription.execute(SearchRequest.builder()
                .dn(String.format("subscriptionId=%s,ou=subscription,dc=example,dc=com", subscriptionId))
                .filter("objectClass=subscription")
                .build());
        if (responseSubscription.getEntry() != null) {
            LdapEntry subscriptionEntry = responseSubscription.getEntry();
            Subscription subscription = new Subscription();
            subscription.setSubscriptionId(UUID.fromString(subscriptionEntry.getAttribute("subscriptionId").getStringValue()));
            subscription.setSupis(new ArrayList<>(subscriptionEntry.getAttribute("supis").getStringValues()));
            subscription.setInterGrpId(subscriptionEntry.getAttribute("interGrpId").getStringValue());
            subscription.setAnyUeInd(Boolean.parseBoolean(subscriptionEntry.getAttribute("anyUeInd").getStringValue()));
            subscription.setNotifMethod(subscriptionEntry.getAttribute("notifMethod").getStringValue());
            subscription.setDnn(subscriptionEntry.getAttribute("dnn").getStringValue());
            subscription.setSubscribedEvents(new ArrayList<>(subscriptionEntry.getAttribute("subscribedEvents").getStringValues()));
            subscription.setSubsNotifUri(subscriptionEntry.getAttribute("subsNotifUri").getStringValue());
            subscription.setSubsNotifId(subscriptionEntry.getAttribute("subsNotifId").getStringValue());
            subscription.setMaxReportNbr(Integer.parseInt(subscriptionEntry.getAttribute("maxReportNbr").getStringValue()));
            subscription.setExpiry(LocalDateTime.parse(subscriptionEntry.getAttribute("expiry").getStringValue(), DateTimeFormatter.ISO_DATE_TIME));
            subscription.setRepPeriod(Integer.parseInt(subscriptionEntry.getAttribute("repPeriod").getStringValue()));
            subscription.setSuppFeat(subscriptionEntry.getAttribute("suppFeat").getStringValue());

            SearchOperation searchSnssai = new SearchOperation(connectAndBind());
            SearchResponse responseSnssai = searchSnssai.execute(SearchRequest.builder()
                    .dn(String.format("subscriptionId=%s,ou=subscription,dc=example,dc=com", subscriptionId))
                    .filter("objectClass=snssai")
                    .build());
            LdapEntry snssaiEntry = responseSnssai.getEntry();
            Snssai snssai = new Snssai();
            snssai.setSst(Integer.parseInt(snssaiEntry.getAttribute("sst").getStringValue()));
            snssai.setSd(snssaiEntry.getAttribute("sd").getStringValue());
            subscription.setSnssai(snssai);

            SearchOperation searchEventFilters = new SearchOperation(connectAndBind());
            SearchResponse responseEventFilters = searchEventFilters.execute(SearchRequest.builder()
                    .dn(String.format("subscriptionId=%s,ou=subscription,dc=example,dc=com", subscriptionId))
                    .filter("objectClass=eventFilters")
                    .build());
            List<EventFilter> eventFilters = new ArrayList<>();
            for (LdapEntry filterEntry : responseEventFilters.getEntries()) {
                EventFilter filter = new EventFilter();
                filter.setEventFilterId(UUID.fromString(filterEntry.getAttribute("eventFilterId").getStringValue()));
                filter.setInstanceTypes(new ArrayList<>(filterEntry.getAttribute("instanceTypes").getStringValues()));
                filter.setTransProtocols(new ArrayList<>(filterEntry.getAttribute("transProtocols").getStringValues()));
                filter.setPtpProfiles(new ArrayList<>(filterEntry.getAttribute("ptpProfiles").getStringValues()));
                eventFilters.add(filter);
            }
            subscription.setEventFilters(eventFilters);
            closeConnection();
            return subscription;
        }
        return null;
    }
    public static Boolean ldapDelete(UUID subscriptionId) throws LdapException {
        SearchOperation search = new SearchOperation(connectAndBind());
        SearchResponse response = search.execute(SearchRequest.builder()
                .dn("ou=subscription,dc=example,dc=com")
                .filter(String.format("(subscriptionId=%s)", subscriptionId))
                .returnAttributes("dn")
                .build());
        if (response.getEntry() != null) {
            String dn = response.getEntry().getDn();
            SearchOperation searchChildren = new SearchOperation(connectAndBind());
            SearchResponse searchResponse = searchChildren.execute(SearchRequest.builder()
                    .dn(dn)
                    .filter("(|(objectClass=snssai)(objectClass=eventFilters)")
                    .build());
            DeleteOperation delete = new DeleteOperation(connectAndBind());
            for (LdapEntry entry : searchResponse.getEntries()) {
                DeleteResponse childResponse = delete.execute(new DeleteRequest(entry.getDn()));
                if (!childResponse.isSuccess()) {
                    closeConnection();
                    return false;
                }
            }
            DeleteResponse mainResponse = delete.execute(new DeleteRequest(dn));
            if (mainResponse.isSuccess()) {
                closeConnection();
                return true;
            }
        }
        return false;
    }
    public static Boolean ldapAdd(Subscription subscription) throws LdapException {
        try {
            AddOperation add = new AddOperation(connectAndBind());
            List<String> supis = subscription.getSupis();
            LdapAttribute supisAttribute = new LdapAttribute("supis", supis.toArray(new String[supis.size()]));
            List<String> subscribedEvents = subscription.getSubscribedEvents();
            LdapAttribute subscribedEventsAttribute = new LdapAttribute("subscribedEvents", subscribedEvents.toArray(new String[subscribedEvents.size()]));
            AddRequest subscriptionRequest = AddRequest.builder()
                    .dn(String.format("subscriptionId=%s,%s", subscription.getSubscriptionId().toString(), "ou=subscription,dc=example,dc=com"))
                    .attributes(
                            new LdapAttribute("objectClass", "subscription"),
                            new LdapAttribute("subscriptionId", subscription.getSubscriptionId().toString()),
                            new LdapAttribute("interGrpId", subscription.getInterGrpId()),
                            new LdapAttribute("anyUeInd", subscription.getAnyUeInd().toString()),
                            new LdapAttribute("notifMethod", subscription.getNotifMethod()),
                            new LdapAttribute("dnn", subscription.getDnn()),
                            new LdapAttribute("subsNotifUri", subscription.getSubsNotifUri()),
                            new LdapAttribute("subsNotifId", subscription.getSubsNotifId()),
                            new LdapAttribute("maxReportNbr", subscription.getMaxReportNbr().toString()),
                            new LdapAttribute("expiry", subscription.getExpiry().toString()),
                            new LdapAttribute("repPeriod", subscription.getRepPeriod().toString()),
                            new LdapAttribute("suppFeat", subscription.getSuppFeat()),
                            supisAttribute,
                            subscribedEventsAttribute
                    )
                    .build();
            AddResponse addResponse = add.execute(subscriptionRequest);
            if (!addResponse.isSuccess()) {
                System.out.println("Failed to add subscription.");
                return false;
            }
            AddRequest snssaiRequest = AddRequest.builder()
                    .dn(String.format("sst=%s,subscriptionId=%s,%s", subscription.getSnssai().getSst(), subscription.getSubscriptionId().toString(), "ou=subscription,dc=example,dc=com"))
                    .attributes(
                            new LdapAttribute("objectClass", "snssai"),
                            new LdapAttribute("sst", subscription.getSnssai().getSst().toString()),
                            new LdapAttribute("sd", subscription.getSnssai().getSd())
                    )
                    .build();
            addResponse = add.execute(snssaiRequest);
            if (!addResponse.isSuccess()) {
                System.out.println("Failed to add snssai.");
                return false;
            }
            for(EventFilter filter: subscription.getEventFilters()){
                LdapAttribute instanceTypesAttribute = new LdapAttribute("instanceTypes", filter.getInstanceTypes().toArray(new String[filter.getInstanceTypes().size()]));
                LdapAttribute transProtocolsAttribute = new LdapAttribute("transProtocols", filter.getTransProtocols().toArray(new String[filter.getTransProtocols().size()]));
                LdapAttribute ptpProfilesAttribute = new LdapAttribute("ptpProfiles", filter.getPtpProfiles().toArray(new String[filter.getPtpProfiles().size()]));

                AddRequest eventFilterRequest = AddRequest.builder()
                        .dn(String.format("eventFilterId=%s,subscriptionId=%s,%s", filter.getEventFilterId().toString(), subscription.getSubscriptionId().toString(), "ou=subscription,dc=example,dc=com"))
                        .attributes(
                                new LdapAttribute("objectClass", "eventFilter"),
                                new LdapAttribute("eventFilterId", filter.getEventFilterId().toString()),
                                instanceTypesAttribute,
                                transProtocolsAttribute,
                                ptpProfilesAttribute
                        )
                        .build();
                addResponse = add.execute(eventFilterRequest);
                if (!addResponse.isSuccess()) {
                    System.out.println("Failed to add eventFilter with eventFilterId=" + filter.getEventFilterId());
                    return false;
                }
            }
            closeConnection();
            return true;
        } catch (Exception ex) {
            System.out.println("An error occurred while trying to add a subscription: " + ex.getMessage());
            closeConnection();
            return false;
        }
    }

    @Value("${apiRoot}")
    private String apiRoot;

    @Value("${apiVersion}")
    private String apiVersion;
}