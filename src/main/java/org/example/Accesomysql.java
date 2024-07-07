package org.example;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Accesomysql {

    public static void nuevaInsercion(String rutacarpeta) {
        int contador = 0;
        final String ACCESS_FILE_PATH = rutacarpeta + "\\BD-Embalses.mdb";
        final String ACCESS_URL = "jdbc:ucanaccess://" + ACCESS_FILE_PATH;

        try (Connection accessConnection = DriverManager.getConnection(ACCESS_URL);
             Connection mysqlConnection = ConectorDB.getMySQLConnection();
             Statement accessStatement = accessConnection.createStatement();
             Statement mysqlStatement = mysqlConnection.createStatement()) {

            // Verificar si hay datos en la tabla Mediciones
            ResultSet rs = mysqlStatement.executeQuery("SELECT MAX(FECHA) AS ULTIMA_FECHA FROM Mediciones");
            if (rs.next() && rs.getDate("ULTIMA_FECHA") != null) {
                System.out.println("Ya existen datos en la base de datos. ¿Desea eliminar la base de datos existente y crear una nueva? (s/n)");
                Scanner scanner = new Scanner(System.in);
                String respuesta = scanner.nextLine().trim().toLowerCase();

                if (respuesta.equals("s")) {
                    // Eliminar las tablas existentes
                    mysqlStatement.executeUpdate("DROP TABLE IF EXISTS Mediciones");
                    mysqlStatement.executeUpdate("DROP TABLE IF EXISTS Embalses");
                    mysqlStatement.executeUpdate("DROP TABLE IF EXISTS Cuencas");
                    mysqlStatement.executeUpdate("DROP TABLE IF EXISTS Capacidad");
                    System.out.println("Tablas eliminadas. Creando nuevas tablas...");

                    // Crear de nuevo las tablas
                    mysqlStatement.executeUpdate("CREATE TABLE Cuencas (" +
                            "ID_CUENCA INT AUTO_INCREMENT PRIMARY KEY, " +
                            "CUENCA VARCHAR(50))");

                    mysqlStatement.executeUpdate("CREATE TABLE Embalses (" +
                            "ID_EMBALSE INT AUTO_INCREMENT PRIMARY KEY, " +
                            "ID_CUENCA INT, " +
                            "EMBALSE VARCHAR(50), " +
                            "ELECTRICO INT, " +
                            "FOREIGN KEY (ID_CUENCA) REFERENCES Cuencas(ID_CUENCA))");

                    mysqlStatement.executeUpdate("CREATE TABLE Capacidad (" +
                            "ID_CAPACIDAD INT AUTO_INCREMENT PRIMARY KEY, " +
                            "CAPACIDAD DECIMAL(10, 2))");

                    mysqlStatement.executeUpdate("CREATE TABLE Mediciones (" +
                            "ID_MEDICION INT AUTO_INCREMENT PRIMARY KEY, " +
                            "ID_EMBALSE INT, " +
                            "FECHA DATE, " +
                            "ID_CAPACIDAD INT, " +
                            "AGUA_ACTUAL DECIMAL(10, 2), " +
                            "FOREIGN KEY (ID_EMBALSE) REFERENCES Embalses(ID_EMBALSE), " +
                            "FOREIGN KEY (ID_CAPACIDAD) REFERENCES Capacidad(ID_CAPACIDAD))");
                } else {
                    System.out.println("Ejecute la actualización de datos en su lugar.");
                    return;
                }
            }

            // Mapas para almacenar las IDs de las entidades ya insertadas y evitar duplicados
            Map<String, Integer> cuencaMap = new HashMap<>();
            Map<String, Integer> embalseMap = new HashMap<>();
            Map<Double, Integer> capacidadMap = new HashMap<>();

            // Leer datos de Access
            DatabaseMetaData metaData = accessConnection.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "%", new String[] { "TABLE" });
            tables.next();
            String T_DatosEmbalses = tables.getString("TABLE_NAME");
            System.out.println("Tabla encontrada: " + T_DatosEmbalses);
            String query = String.format("SELECT * FROM [%s]", T_DatosEmbalses);
            ResultSet resultSet = accessStatement.executeQuery(query);

            // Preparar consultas para inserciones
            String insertCuencaQuery = "INSERT INTO Cuencas (CUENCA) VALUES (?)";
            String insertEmbalseQuery = "INSERT INTO Embalses (ID_CUENCA, EMBALSE, ELECTRICO) VALUES (?, ?, ?)";
            String insertCapacidadQuery = "INSERT INTO Capacidad (CAPACIDAD) VALUES (?)";
            String insertMedicionQuery = "INSERT INTO Mediciones (ID_EMBALSE, FECHA, ID_CAPACIDAD, AGUA_ACTUAL) VALUES (?, ?, ?, ?)";

            PreparedStatement cuencaStmt = mysqlConnection.prepareStatement(insertCuencaQuery, Statement.RETURN_GENERATED_KEYS);
            PreparedStatement embalseStmt = mysqlConnection.prepareStatement(insertEmbalseQuery, Statement.RETURN_GENERATED_KEYS);
            PreparedStatement capacidadStmt = mysqlConnection.prepareStatement(insertCapacidadQuery, Statement.RETURN_GENERATED_KEYS);
            PreparedStatement medicionStmt = mysqlConnection.prepareStatement(insertMedicionQuery);

            while (resultSet.next()) {
                // Extraer datos de cada fila
                String cuenca = resultSet.getString("AMBITO_NOMBRE");
                String embalse = resultSet.getString("EMBALSE_NOMBRE");
                Date fecha = resultSet.getDate("FECHA");
                String aguaTotalStr = resultSet.getString("AGUA_TOTAL");
                String aguaActualStr = resultSet.getString("AGUA_ACTUAL");
                // Reemplazar las comas por puntos y convertir a double
                if (aguaTotalStr == null) {aguaTotalStr="0,00";}
                if (aguaActualStr == null) {aguaActualStr="0,00";}
                double aguaTotal = Double.parseDouble(aguaTotalStr.replace(',', '.'));
                double aguaActual = Double.parseDouble(aguaActualStr.replace(',', '.'));
                int electrico = resultSet.getInt("ELECTRICO_FLAG");

                // Insertar o recuperar ID de Cuenca
                int idCuenca;
                if (cuencaMap.containsKey(cuenca)) {
                    idCuenca = cuencaMap.get(cuenca);
                } else {
                    PreparedStatement cuencaSelectStmt = mysqlConnection.prepareStatement("SELECT ID_CUENCA FROM Cuencas WHERE CUENCA = ?");
                    cuencaSelectStmt.setString(1, cuenca);
                    ResultSet cuencaRs = cuencaSelectStmt.executeQuery();
                    if (cuencaRs.next()) {
                        idCuenca = cuencaRs.getInt("ID_CUENCA");
                        cuencaMap.put(cuenca, idCuenca);
                    } else {
                        cuencaStmt.setString(1, cuenca);
                        cuencaStmt.executeUpdate();
                        ResultSet cuencaKeys = cuencaStmt.getGeneratedKeys();
                        if (cuencaKeys.next()) {
                            idCuenca = cuencaKeys.getInt(1);
                            cuencaMap.put(cuenca, idCuenca);
                        } else {
                            throw new SQLException("Failed to retrieve Cuenca ID.");
                        }
                    }
                }

                // Insertar o recuperar ID de Embalse
                int idEmbalse;
                String embalseKey = cuenca + "-" + embalse; // Clave única para identificar el embalse
                if (embalseMap.containsKey(embalseKey)) {
                    idEmbalse = embalseMap.get(embalseKey);
                } else {
                    PreparedStatement embalseSelectStmt = mysqlConnection.prepareStatement("SELECT ID_EMBALSE FROM Embalses WHERE ID_CUENCA = ? AND EMBALSE = ?");
                    embalseSelectStmt.setInt(1, idCuenca);
                    embalseSelectStmt.setString(2, embalse);
                    ResultSet embalseRs = embalseSelectStmt.executeQuery();
                    if (embalseRs.next()) {
                        idEmbalse = embalseRs.getInt("ID_EMBALSE");
                        embalseMap.put(embalseKey, idEmbalse);
                    } else {
                        embalseStmt.setInt(1, idCuenca);
                        embalseStmt.setString(2, embalse);
                        embalseStmt.setInt(3, electrico);
                        embalseStmt.executeUpdate();
                        ResultSet embalseKeys = embalseStmt.getGeneratedKeys();
                        if (embalseKeys.next()) {
                            idEmbalse = embalseKeys.getInt(1);
                            embalseMap.put(embalseKey, idEmbalse);
                        } else {
                            throw new SQLException("Failed to retrieve Embalse ID.");
                        }
                    }
                }

                // Insertar o recuperar ID de Capacidad
                int idCapacidad;
                if (capacidadMap.containsKey(aguaTotal)) {
                    idCapacidad = capacidadMap.get(aguaTotal);
                } else {
                    PreparedStatement capacidadSelectStmt = mysqlConnection.prepareStatement("SELECT ID_CAPACIDAD FROM Capacidad WHERE CAPACIDAD = ?");
                    capacidadSelectStmt.setDouble(1, aguaTotal);
                    ResultSet capacidadRs = capacidadSelectStmt.executeQuery();
                    if (capacidadRs.next()) {
                        idCapacidad = capacidadRs.getInt("ID_CAPACIDAD");
                        capacidadMap.put(aguaTotal, idCapacidad);
                    } else {
                        capacidadStmt.setDouble(1, aguaTotal);
                        capacidadStmt.executeUpdate();
                        ResultSet capacidadKeys = capacidadStmt.getGeneratedKeys();
                        if (capacidadKeys.next()) {
                            idCapacidad = capacidadKeys.getInt(1);
                            capacidadMap.put(aguaTotal, idCapacidad);
                        } else {
                            throw new SQLException("Failed to retrieve Capacidad ID.");
                        }
                    }
                }

                // Insertar Medición
                medicionStmt.setInt(1, idEmbalse);
                medicionStmt.setDate(2, fecha);
                medicionStmt.setInt(3, idCapacidad);
                medicionStmt.setDouble(4, aguaActual);
                medicionStmt.executeUpdate();
                System.out.println(++contador + " datos procesados");
            }

            System.out.println("Datos migrados exitosamente.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
