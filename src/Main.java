import java.sql.*;

public class Main {
    private static final String URL = "jdbc:oracle:thin:@localhost:1521:xe";
    private static final String USER = "RIBERA";
    private static final String PASS = "ribera";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            System.out.println("--- ESTADÍSTICAS Y CLASIFICACIONES ---");

            clasificacionGeneral(conn);
            clasificacionEquipos(conn);
            rankingEtapasLargas(conn);
            etapasSobreLaMedia(conn);

        } catch (SQLException e) {
            System.err.println("Error de conexión: " + e.getMessage());
        }
    }

    // 1. Clasificación general por puntos (TOP 10)
    public static void clasificacionGeneral(Connection conn) {
        String sql = "SELECT C.NOMBRE AS CICLISTA, E.NOMBRE AS EQUIPO, SUM(P.PUNTOS) AS TOTAL_PUNTOS " +
                "FROM CICLISTA C " +
                "JOIN EQUIPO E ON C.ID_EQUIPO = E.ID_EQUIPO " +
                "JOIN PARTICIPACION P ON C.ID_CICLISTA = P.ID_CICLISTA " +
                "GROUP BY C.NOMBRE, E.NOMBRE " +
                "ORDER BY TOTAL_PUNTOS DESC";

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("\n--- TOP 10 CLASIFICACIÓN GENERAL ---");
            int contador = 1;
            while (rs.next() && contador <= 10) {
                System.out.printf("%d. %s (%s) - %d pts%n",
                        contador++, rs.getString("CICLISTA"), rs.getString("EQUIPO"), rs.getInt("TOTAL_PUNTOS"));
            }
        } catch (SQLException e) {
            System.err.println("Error en Clasificación General: " + e.getMessage());
        }
    }

    // 2. Clasificación por equipos
    public static void clasificacionEquipos(Connection conn) {
        String sql = "SELECT E.NOMBRE, E.PAIS, SUM(P.PUNTOS) AS PUNTOS_EQUIPO " +
                "FROM EQUIPO E " +
                "JOIN CICLISTA C ON E.ID_EQUIPO = C.ID_EQUIPO " +
                "JOIN PARTICIPACION P ON C.ID_CICLISTA = P.ID_CICLISTA " +
                "GROUP BY E.NOMBRE, E.PAIS " +
                "ORDER BY PUNTOS_EQUIPO DESC";

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("\n--- CLASIFICACIÓN POR EQUIPOS ---");
            while (rs.next()) {
                System.out.printf("Equipo: %-15s | País: %-10s | Total: %d pts%n",
                        rs.getString("NOMBRE"), rs.getString("PAIS"), rs.getInt("PUNTOS_EQUIPO"));
            }
        } catch (SQLException e) {
            System.err.println("Error en Clasificación Equipos: " + e.getMessage());
        }
    }

    // 3. Ranking de etapas largas (TOP 3)
    public static void rankingEtapasLargas(Connection conn) {
        String sql = "SELECT NUMERO, ORIGEN, DESTINO, DISTANCIA_KM, FECHA " +
                "FROM ETAPA " +
                "ORDER BY DISTANCIA_KM DESC";

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("\n--- TOP 3 ETAPAS MÁS LARGAS ---");
            int contador = 1;
            while (rs.next() && contador <= 3) {
                System.out.printf("%d. Etapa %d: %s -> %s (%d km) [%s]%n",
                        contador++, rs.getInt("NUMERO"), rs.getString("ORIGEN"),
                        rs.getString("DESTINO"), rs.getInt("DISTANCIA_KM"), rs.getDate("FECHA"));
            }
        } catch (SQLException e) {
            System.err.println("Error en Ranking Etapas: " + e.getMessage());
        }
    }

    // OPCIONAL: Etapas por encima del promedio
    public static void etapasSobreLaMedia(Connection conn) {
        // Usamos una subconsulta para obtener el AVG en el WHERE
        String sql = "SELECT NUMERO, ORIGEN, DESTINO, DISTANCIA_KM " +
                "FROM ETAPA " +
                "WHERE DISTANCIA_KM > (SELECT AVG(DISTANCIA_KM) FROM ETAPA) " +
                "ORDER BY DISTANCIA_KM DESC";

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("\n--- ETAPAS POR ENCIMA DE LA MEDIA DE DISTANCIA ---");
            while (rs.next()) {
                System.out.printf("Etapa %d: %s -> %s (%d km)%n",
                        rs.getInt("NUMERO"), rs.getString("ORIGEN"),
                        rs.getString("DESTINO"), rs.getInt("DISTANCIA_KM"));
            }
        } catch (SQLException e) {
            System.err.println("Error en Etapas sobre la media: " + e.getMessage());
        }
    }
}