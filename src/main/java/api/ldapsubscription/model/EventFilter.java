package api.ldapsubscription.model;

import java.util.List;
import java.util.UUID;

public class EventFilter {
    private UUID eventFilterId;
    private List<String> instanceTypes;
    private List<String> transProtocols;
    private List<String> ptpProfiles;

    public UUID getEventFilterId() {
        return eventFilterId;
    }

    public void setEventFilterId(UUID eventFilterId) {
        this.eventFilterId = eventFilterId;
    }

    public List<String> getInstanceTypes() {
        return instanceTypes;
    }

    public void setInstanceTypes(List<String> instanceTypes) {
        this.instanceTypes = instanceTypes;
    }

    public List<String> getTransProtocols() {
        return transProtocols;
    }

    public void setTransProtocols(List<String> transProtocols) {
        this.transProtocols = transProtocols;
    }

    public List<String> getPtpProfiles() {
        return ptpProfiles;
    }

    public void setPtpProfiles(List<String> ptpProfiles) {
        this.ptpProfiles = ptpProfiles;
    }
}