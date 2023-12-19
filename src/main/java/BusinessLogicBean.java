import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("businessLogicBean")
@RequestScoped
public class BusinessLogicBean {

    @Inject
    private DataLogicBean dataLogicBean;

    /**
     * Speichert Informationen zur Rettungsperson und aktualisiert das Netz und die Rettungspersonen-Daten.
     *
     * @param rescuerName Der Name der Rettungsperson.
     * @param rescuerPhone Die Telefonnummer der Rettungsperson.
     * @param rescuerRole Die Rolle der Rettungsperson.
     * @param status Der Status des Netzes.
     * @param netId Die ID des Netzes.
     */
    public void saveRescueInfo(String rescuerName, String rescuerPhone, String rescuerRole, String status, Long netId) {
        try (Connection connection = DataLogicBean.getConnection()) {
            if (!additionalChecks(connection, netId, rescuerName, status)) {
                return; // Beendet die Methode, wenn zusätzliche Überprüfungen fehlschlagen
            }

            dataLogicBean.insertRescuePerson(connection, rescuerName, rescuerPhone, rescuerRole, status, netId);
            dataLogicBean.updateStatusGhostNet(netId, status);
            dataLogicBean.updateRescuePersonId(netId);
            
        } catch (SQLException e) {
            throw new RuntimeException("Error saving rescue info", e);
        }
    }

    /**
     * Überprüft ob das Netz bereits von einer anderen Person geborgen wird, sich im Status "in Bergung" befindet und ob es schon als "geborgen" oder "verschollen" gemeldet wurde.
     *
     * @param connection Die Datenbankverbindung.
     * @param netId Die ID des Netzes.
     * @param rescuerName Der Name der Rettungsperson.
     * @param status Der Status des Netzes.
     * @return True, wenn die Überprüfungen erfolgreich sind, sonst False.
     * @throws SQLException Wenn ein Datenbankfehler auftritt.
     */
    private boolean additionalChecks(Connection connection, Long netId, String rescuerName, String status) throws SQLException {
        try (PreparedStatement checkStatement = connection.prepareStatement(
                "SELECT name, status FROM rescue_persons WHERE netz_id = ?")) {
            checkStatement.setLong(1, netId);
            ResultSet resultSet = checkStatement.executeQuery();

            while (resultSet.next()) {
                String existingName = resultSet.getString("name");
                String existingStatus = resultSet.getString("status");
                if (!existingName.equals(rescuerName)) {
                    addErrorMessage("Achtung, das Netz wird bereits von einer anderen Person geborgen. Bitte setzen Sie sich mit ihr in Verbindung");
                    return false;
                }
                if (existingStatus.equals("geborgen") || existingStatus.equals("verschollen")) {
                    addErrorMessage("Das Netz ist bereits als \"" + existingStatus + "\" gemeldet. Es sind keine Änderungen mehr möglich.");
                    return false;
                }
                if (existingStatus.equals(status) && existingName.equals(rescuerName)) {
                    addErrorMessage("Sie haben den Status bereits auf \"in Bergung\" gesetzt.");
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Fügt eine Fehlermeldung zum aktuellen FacesContext hinzu.
     *
     * @param message Die Fehlermeldung.
     */
    private void addErrorMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
    }
}
