package api.ldapsubscription.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class Subscription {
    private UUID subscriptionId;
    private List<String> supis;
    private String interGrpId;
    private Boolean anyUeInd;
    private String notifMethod;
    private String dnn;
    private Snssai snssai;
    private List<String> subscribedEvents;
    private List<EventFilter> eventFilters;
    private String subsNotifUri;
    private String subsNotifId;
    private Integer maxReportNbr;
    private LocalDateTime expiry;
    private Integer repPeriod;
    private String suppFeat;

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(UUID subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public List<String> getSupis() {
        return supis;
    }

    public void setSupis(List<String> supis) {
        this.supis = supis;
    }

    public String getInterGrpId() {
        return interGrpId;
    }

    public void setInterGrpId(String interGrpId) {
        this.interGrpId = interGrpId;
    }

    public Boolean getAnyUeInd() {
        return anyUeInd;
    }

    public void setAnyUeInd(Boolean anyUeInd) {
        this.anyUeInd = anyUeInd;
    }

    public String getNotifMethod() {
        return notifMethod;
    }

    public void setNotifMethod(String notifMethod) {
        this.notifMethod = notifMethod;
    }

    public String getDnn() {
        return dnn;
    }

    public void setDnn(String dnn) {
        this.dnn = dnn;
    }

    public Snssai getSnssai() {
        return snssai;
    }

    public void setSnssai(Snssai snssai) {
        this.snssai = snssai;
    }

    public List<String> getSubscribedEvents() {
        return subscribedEvents;
    }

    public void setSubscribedEvents(List<String> subscribedEvents) {
        this.subscribedEvents = subscribedEvents;
    }

    public List<EventFilter> getEventFilters() {
        return eventFilters;
    }

    public void setEventFilters(List<EventFilter> eventFilters) {
        this.eventFilters = eventFilters;
    }

    public String getSubsNotifUri() {
        return subsNotifUri;
    }

    public void setSubsNotifUri(String subsNotifUri) {
        this.subsNotifUri = subsNotifUri;
    }

    public String getSubsNotifId() {
        return subsNotifId;
    }

    public void setSubsNotifId(String subsNotifId) {
        this.subsNotifId = subsNotifId;
    }

    public Integer getMaxReportNbr() {
        return maxReportNbr;
    }

    public void setMaxReportNbr(Integer maxReportNbr) {
        this.maxReportNbr = maxReportNbr;
    }

    public LocalDateTime getExpiry() {
        return expiry;
    }

    public void setExpiry(LocalDateTime expiry) {
        this.expiry = expiry;
    }

    public Integer getRepPeriod() {
        return repPeriod;
    }

    public void setRepPeriod(Integer repPeriod) {
        this.repPeriod = repPeriod;
    }

    public String getSuppFeat() {
        return suppFeat;
    }

    public void setSuppFeat(String suppFeat) {
        this.suppFeat = suppFeat;
    }
}