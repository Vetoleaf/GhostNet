import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.postgresql.ds.PGSimpleDataSource;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

@Named("dataLogicBean")
@RequestScoped
public class DataLogicBean {
    
    /**
     * Stellt eine Verbindung zur Datenbank her.
     *
     * @return Eine Datenbankverbindung.
     * @throws SQLException Wenn ein Verbindungsfehler auftritt.
     */
	public static Connection getConnection() throws SQLException {
	    PGSimpleDataSource ds = new PGSimpleDataSource();

	    String url = System.getenv("DB_URL");
	    String user = System.getenv("DB_USER");
	    String password = System.getenv("DB_PASSWORD");

	    ds.setUrl(url);
	    ds.setUser(user);
	    ds.setPassword(password);

	    return ds.getConnection();
	}


    /**
     * Speichert ein GhostNet in der Datenbank.
     *
     * @param name Der Name des Netzes.
     * @param telephoneNumber Die Telefonnummer.
     * @param latitude Der Breitengrad.
     * @param longitude Der Längengrad.
     * @param size Die Größe des Netzes.
     * @param status Der Status des Netzes.
     * @param personType Der Typ der Person (meldende oder bergende Person).
     * @throws SQLException Wenn ein Datenbankfehler auftritt.
     */
    public void saveGhostNet(String name, String telephoneNumber, String latitude, String longitude, String size, String status, String personType) throws SQLException {
        try (Connection connection = getConnection()) {
            long personId = createOrUpdatePersonAndGetId(name, telephoneNumber, personType);

            BigDecimal latitudeValue = new BigDecimal(latitude);
            BigDecimal longitudeValue = new BigDecimal(longitude);
            BigDecimal sizeValue = new BigDecimal(size);

            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO ghost_nets (latitude, longitude, size, status, person_id) VALUES (?, ?, ?, ?, ?)")) {
                statement.setBigDecimal(1, latitudeValue);
                statement.setBigDecimal(2, longitudeValue);
                statement.setBigDecimal(3, sizeValue);
                statement.setString(4, status);
                statement.setLong(5, personId);

                statement.executeUpdate();
            }
        }
    }

    /**
     * Überprüft, ob eine Rettungsperson bereits existiert.
     *
     * @param connection Die Datenbankverbindung.
     * @param netId Die ID des Netzes.
     * @param rescuerName Der Name der Rettungsperson.
     * @param status Der Status des Netzes.
     * @return True, wenn die Rettungsperson existiert, sonst False.
     * @throws SQLException Wenn ein Datenbankfehler auftritt.
     */
    public boolean checkRescuePersonExists(Connection connection, Long netId, String rescuerName, String status) throws SQLException {
        try (PreparedStatement checkStatement = connection.prepareStatement(
                "SELECT name, status FROM rescue_persons WHERE netz_id = ?")) {
            checkStatement.setLong(1, netId);
            ResultSet resultSet = checkStatement.executeQuery();

            while (resultSet.next()) {
                String existingName = resultSet.getString("name");
                String existingStatus = resultSet.getString("status");

                if (!existingName.equals(rescuerName) || 
                    existingStatus.equals("geborgen") || 
                    existingStatus.equals("verschollen") || 
                    (existingStatus.equals(status) && existingName.equals(rescuerName))) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Fügt eine Rettungsperson in die Datenbank ein.
     *
     * @param connection Die Datenbankverbindung.
     * @param rescuerName Der Name der Rettungsperson.
     * @param rescuerPhone Die Telefonnummer der Rettungsperson.
     * @param rescuerRole Die Rolle der Rettungsperson.
     * @param status Der Status des Netzes.
     * @param netId Die ID des Netzes.
     * @throws SQLException Wenn ein Datenbankfehler auftritt.
     */
    public void insertRescuePerson(Connection connection, String rescuerName, String rescuerPhone, String rescuerRole, String status, Long netId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO rescue_persons (name, telephone_number, role, status, netz_id) VALUES (?, ?, ?, ?, ?)")) {
            statement.setString(1, rescuerName);
            statement.setString(2, rescuerPhone);
            statement.setString(3, rescuerRole);
            statement.setString(4, status);
            statement.setLong(5, netId);

            statement.executeUpdate();
        }
    }
    
    /**
     * Aktualisiert den Status eines Geisternetzes in der Datenbank.
     *
     * @param netId Die ID des Netzes.
     * @param status Der neue Status des Netzes.
     * @throws SQLException Wenn ein Datenbankfehler auftritt.
     */
    public void updateStatusGhostNet(long netId, String status) throws SQLException {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE ghost_nets SET status = ? WHERE id = ?")) {
                statement.setString(1, status);
                statement.setLong(2, netId);
                int rowsAffected = statement.executeUpdate();

                if (rowsAffected <= 0) {
                    throw new SQLException("Keine Zeilen aktualisiert. Netz mit ID nicht gefunden: " + netId);
                }
            }
        }
    }

    /**
     * Aktualisiert die ID der Rettungsperson in der Tabelle 'ghost_nets'.
     *
     * @param netId Die ID des Netzes.
     * @throws SQLException Wenn ein Datenbankfehler auftritt.
     */
    public void updateRescuePersonId(long netId) throws SQLException {
        try (Connection connection = getConnection()) {
            // Lese die ID aus der rescue_person Tabelle aus
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT id FROM rescue_persons WHERE netz_id = ?")) {
                statement.setLong(1, netId);
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    long rescuePersonId = resultSet.getLong("id");

                    // Trage die ID in die ghost_nets Tabelle ein
                    try (PreparedStatement updateStatement = connection.prepareStatement(
                            "UPDATE ghost_nets SET rescue_person_id = ? WHERE id = ?")) {
                        updateStatement.setLong(1, rescuePersonId);
                        updateStatement.setLong(2, netId);
                        int rowsAffected = updateStatement.executeUpdate();

                        if (rowsAffected <= 0) {
                            throw new SQLException("Keine Zeilen aktualisiert. Netz mit ID nicht gefunden: " + netId);
                        }
                    }
                } else {
                    throw new SQLException("Keine Übereinstimmung gefunden. Netz mit ID nicht gefunden: " + netId);
                }
            }
        }
    }
    
    /**
     * Aktualisiert den Status eines Netzes.
     *
     * @param netId Die ID des Netzes.
     * @param rescuerName Der Name der Rettungsperson.
     * @param rescuerPhone Die Telefonnummer der Rettungsperson.
     * @param rescuerRole Die Rolle der Rettungsperson.
     * @throws SQLException Wenn ein Datenbankfehler auftritt.
     */
    public void updateStatus(long netId, String rescuerName, String rescuerPhone, String rescuerRole) throws SQLException {
        try (Connection connection = getConnection()) {
            long personId = createOrUpdatePersonAndGetId(rescuerName, rescuerPhone, rescuerRole);

            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE ghost_nets SET status = ?, person_id = ? WHERE id = ?")) {
                statement.setString(1, "geborgen");
                statement.setLong(2, personId);
                statement.setLong(3, netId);
                int rowsAffected = statement.executeUpdate();

                if (rowsAffected <= 0) {
                    throw new RuntimeException("Keine Zeilen aktualisiert. Netz mit ID nicht gefunden: " + netId);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Aktualisieren des Status", e);
        }
    }
    
    public long createOrUpdatePersonAndGetId(String personName, String personTelephoneNumber, String role) throws SQLException {
        try (Connection connection = getConnection()) {
            long existingPersonId = getPersonIdForNameAndTelephoneNumber(connection, personName, personTelephoneNumber);
            if (existingPersonId != -1) {
                return existingPersonId;
            }

            try (PreparedStatement insertStatement = connection.prepareStatement(
                    "INSERT INTO persons (name, telephone_number, role) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                insertStatement.setString(1, personName);
                insertStatement.setString(2, (personTelephoneNumber != null && !personTelephoneNumber.isEmpty()) ? personTelephoneNumber : "keine");
                insertStatement.setString(3, role);
                insertStatement.executeUpdate();

                try (ResultSet generatedKeys = insertStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getLong(1);
                    } else {
                        throw new SQLException("Error creating person: " + personName);
                    }
                }
            }
        }
    }
    
    private long getPersonIdForNameAndTelephoneNumber(Connection connection, String personName, String personTelephoneNumber) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id FROM persons WHERE name = ? AND telephone_number = ?")) {
            statement.setString(1, personName);
            statement.setString(2, personTelephoneNumber);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong("id");
                } else {
                    return -1; // Person not found
                }
            }
        }
    }
    
    /**
     * Ruft die Details eines einzelnen Netzes ab.
     *
     * @param netId Die ID des Netzes.
     * @return Eine Liste von Objekten, die die Details des Netzes repräsentieren.
     * @throws SQLException Wenn ein Datenbankfehler auftritt.
     */
    public List<List<Object>> getretrieveOneNet(long netId) throws SQLException {
        List<List<Object>> oneNet = new ArrayList<>();

        try (Connection connection = DataLogicBean.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT g.id, g.latitude, g.longitude, g.size, g.status, g.created_at, p.role, " +
                     "p.name AS reportingPersonName, p.telephone_number AS reportingPersonTelephoneNumber " +
                     "FROM ghost_nets g " +
                     "JOIN persons p ON g.person_id = p.id " +
                     "WHERE g.id = ?")) {
        	
        	statement.setLong(1, netId);
        	
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                	List<Object> net = new ArrayList<>();
                    net.add(resultSet.getLong("id"));
                    net.add(resultSet.getBigDecimal("latitude"));
                    net.add(resultSet.getBigDecimal("longitude"));
                    net.add(resultSet.getBigDecimal("size"));
                    net.add(resultSet.getTimestamp("created_at"));
                    net.add(resultSet.getString("status"));
                    net.add(resultSet.getString("reportingPersonName"));
                    net.add(resultSet.getString("reportingPersonTelephoneNumber"));
                    net.add(resultSet.getString("role"));
                    
                    oneNet.add(net);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving one net", e);
        }
        return oneNet;
    }
    
    /**
     * Ruft die Details aller Netze ab.
     *
     * @return Eine Liste von Listen, die die Details aller Netze repräsentieren.
     * @throws SQLException Wenn ein Datenbankfehler auftritt.
     */
    public List<List<Object>> getAllNets() throws SQLException {
        List<List<Object>> allNets = new ArrayList<>();

        try (Connection connection = DataLogicBean.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT g.id, g.latitude, g.longitude, g.size, g.status, g.created_at, p.role, " +
                     "p.name AS reportingPersonName, p.telephone_number AS reportingPersonTelephoneNumber " +
                     "FROM ghost_nets g " +
                     "JOIN persons p ON g.person_id = p.id")) {

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    List<Object> net = new ArrayList<>();
                    net.add(resultSet.getLong("id"));
                    net.add(resultSet.getBigDecimal("latitude"));
                    net.add(resultSet.getBigDecimal("longitude"));
                    net.add(resultSet.getBigDecimal("size"));
                    net.add(resultSet.getString("status"));
                    net.add(resultSet.getTimestamp("created_at"));
                    net.add(resultSet.getString("reportingPersonName"));
                    net.add(resultSet.getString("reportingPersonTelephoneNumber"));
                    net.add(resultSet.getString("role"));

                    allNets.add(net);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving all nets", e);
        }
        return allNets;
    }
    
    /**
     * Ruft die Details aller Rettungspersonen ab.
     *
     * @param netId Die ID des Netzes.
     * @return Eine Liste von Listen, die die Details der Rettungspersonen repräsentieren.
     * @throws SQLException Wenn ein Datenbankfehler auftritt.
     */
    public List<List<Object>> getAllRescuers(long netId) throws SQLException {
        List<List<Object>> allRescuers = new ArrayList<>();

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT r.name, r.telephone_number, r.role, r.status, r.created_at, g.latitude, g.longitude, g.size FROM rescue_persons r JOIN ghost_nets g ON r.netz_id = g.id WHERE r.netz_id = ?")) 
        {
            statement.setLong(1, netId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    List<Object> rescuer = new ArrayList<>();
                    rescuer.add(resultSet.getString("name"));
                    rescuer.add(resultSet.getString("telephone_number"));
                    rescuer.add(resultSet.getString("role"));
                    rescuer.add(resultSet.getString("status"));
                    rescuer.add(resultSet.getTimestamp("created_at"));
                    rescuer.add(resultSet.getDouble("latitude"));
                    rescuer.add(resultSet.getDouble("longitude"));
                    rescuer.add(resultSet.getDouble("size"));
                    
                    allRescuers.add(rescuer);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving all rescuers", e);
        }
        return allRescuers;
    }
}


