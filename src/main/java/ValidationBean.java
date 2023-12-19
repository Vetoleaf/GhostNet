import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.util.ArrayList;
import java.util.List;

@Named("validationBean")
@RequestScoped
public class ValidationBean {

    /**
     * Validiert die Eingabedaten für ein Geisternetz.
     * 
     * @param name Der Name des Geisternetzes.
     * @param telephoneNumber Die Telefonnummer (optional).
     * @param latitude Der Breitengrad.
     * @param longitude Der Längengrad.
     * @param size Die Größe des Netzes (geschätzt).
     * @param status Der Status des Netzes.
     * @return True, wenn die Validierung erfolgreich ist, sonst False.
     */
    public boolean validateGhostNetData(String name, String telephoneNumber, String latitude, String longitude, String size, String status) {
        List<String> missingFields = new ArrayList<>();
        checkField(name, "Name", missingFields);
        checkField(latitude, "Breitengrad", missingFields);
        checkField(longitude, "Längengrad", missingFields);
        checkField(size, "Größe in m (geschätzt)", missingFields);
        checkField(status, "Status", missingFields);

        if (!missingFields.isEmpty()) {
            addErrorMessage("Folgende Felder sind Pflichtfelder und dürfen nicht leer sein: " + String.join(", ", missingFields));
            return false;
        }

        if (!isPhoneNumberValidOrEmpty(telephoneNumber)) {
            addErrorMessage("Bitte tragen Sie im Feld 'Telefonnummer' ausschließlich Zahlen ein.");
            return false;
        }

        return true;
    }

    /**
     * Validiert die Eingabedaten für eine Rettungsperson.
     * 
     * @param rescuerName Der Name der Rettungsperson.
     * @param rescuerPhone Die Telefonnummer der Rettungsperson.
     * @return True, wenn die Validierung erfolgreich ist, sonst False.
     */
    public boolean validateRescuerInfo(String rescuerName, String rescuerPhone) {
        List<String> missingFields = new ArrayList<>();
        checkField(rescuerName, "Name", missingFields);
        checkField(rescuerPhone, "Telefonnummer", missingFields);

        if (!missingFields.isEmpty()) {
            addErrorMessage("Folgende Felder sind Pflichtfelder und dürfen nicht leer sein: " + String.join(", ", missingFields));
            return false;
        }

        if (!isPhoneNumberValid(rescuerPhone)) {
            addErrorMessage("Bitte tragen Sie im Feld 'Telefonnummer' ausschließlich Zahlen ein.");
            return false;
        }

        return true;
    }

    // Hilfsmethoden

    private void checkField(String fieldValue, String fieldName, List<String> missingFields) {
        if (fieldValue == null || fieldValue.isEmpty()) {
            missingFields.add(fieldName);
        }
    }

    private boolean isPhoneNumberValidOrEmpty(String phoneNumber) {
        return phoneNumber == null || phoneNumber.isEmpty() || phoneNumber.matches("\\d*");
    }

    private boolean isPhoneNumberValid(String phoneNumber) {
        return phoneNumber.matches("\\d*");
    }

    private void addErrorMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
    }
}
