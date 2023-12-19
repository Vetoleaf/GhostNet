import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import jakarta.faces.context.ExternalContext;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import com.google.gson.Gson;

@Named("managedBean")
@ApplicationScoped
public class ManagedBean implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Inject
    private DataLogicBean dataLogicBean;

    @Inject
    private ValidationBean validationBean;

    @Inject
    private BusinessLogicBean businessLogicBean;

    private String name;
    private String telephoneNumber;
    private String latitude;
    private String longitude;
    private String size;
    private String personType = "meldende Person";
    private String status;
    private String rescuerName;
    private String rescuerPhone;
    private String rescuerRole = "bergende Person";
    private String popupMessage;
    private Long netId;
    
    @PostConstruct
    public void init() {
    }

    // Getter und Setter für alle Felder
    public String getpersonType() {
        return personType;
    }

    public void setpersonType(String personType) {
        this.personType = personType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    public void setTelephoneNumber(String telephoneNumber) {
        this.telephoneNumber = telephoneNumber;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getNetId() {
        return netId;
    }
    
    public void setNetId(Long netId) {
        this.netId = netId;
    }

    public String getRescuerName() {
        return rescuerName;
    }

    public void setRescuerName(String rescuerName) {
        this.rescuerName = rescuerName;
    }

    public String getRescuerPhone() {
        return rescuerPhone;
    }

    public void setRescuerPhone(String rescuerPhone) {
        this.rescuerPhone = rescuerPhone;
    }

    public String getRescuerRole() {
        return rescuerRole;
    }

    public void setRescuerRole(String rescuerRole) {
        this.rescuerRole = rescuerRole;
    }
    
    public String getPopupMessage() {
        return popupMessage;
    }

    public void setPopupMessage(String popupMessage) {
        this.popupMessage = popupMessage;
    }

 // Leitet zur Detailseite für ein bestimmtes Netz um
    public String redirectToDetails() {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        externalContext.getRequestMap().put("netId", netId);
        return "details.xhtml?faces-redirect=true" + netId;
    }

    // Speichert Informationen über eine Rettungsaktion
    public void saveRescueInfo() {
        if (validationBean.validateRescuerInfo(rescuerName, rescuerPhone)) {
            businessLogicBean.saveRescueInfo(rescuerName, rescuerPhone, rescuerRole, status, netId);
            resetRescuerValues();
        }
    }

    // Setzt Rettungsinformationen zurück
    public void resetRescuerValues() {
        rescuerName = null;
        rescuerPhone = null;
        rescuerRole = null;
    }

    // Ruft Informationen über ein spezifisches Netz ab
    public List<List<Object>> getretrieveOneNet() {
        if (netId == null) {
            return null;
        }
        try {
            return dataLogicBean.getretrieveOneNet(netId);
        } catch (Exception e) {
            // Fehlerbehandlung
        }
        return null;
    }

    // Ruft Informationen über alle Netze ab
    public List<List<Object>> getAllNets() {
        try {
            return dataLogicBean.getAllNets();
        } catch (Exception e) {
            // Fehlerbehandlung
        }
        return new ArrayList<>();
    }

    // Konvertiert Informationen über alle Netze in JSON
    public String getAllNetsJson() throws SQLException {
        List<List<Object>> allNets = dataLogicBean.getAllNets();
        List<List<Object>> reportedNets = new ArrayList<>();
        for (List<Object> net : allNets) {
            String netStatus = (String) net.get(4);
            if ("gemeldet".equals(netStatus) || "in bergung".equals(netStatus)) {
                reportedNets.add(net);
            }
        }
        return new Gson().toJson(reportedNets);
    }

    // Ruft Informationen über alle Retter ab
    public List<List<Object>> getAllRescuers() {
        try {
            return dataLogicBean.getAllRescuers(netId);
        } catch (Exception e) {
            // Fehlerbehandlung
        }
        return new ArrayList<>();
    }

    // Speichert Informationen über ein Geisternetz
    public void saveGhostNet() {
        if (!validationBean.validateGhostNetData(name, telephoneNumber, latitude, longitude, size, status)) {
            return;
        }
        try {
            dataLogicBean.saveGhostNet(name, telephoneNumber, latitude, longitude, size, status, personType);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Erfolgreich gespeichert!", null));
            resetValues();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler beim Speichern! Überprüfen Sie Ihre Eingaben.", null));
        }
    }

    // Setzt Werte von netz-melden zurück
    public void resetValues() {
        name = null;
        telephoneNumber = null;
        latitude = null;
        longitude = null;
        size = null;
    }
}