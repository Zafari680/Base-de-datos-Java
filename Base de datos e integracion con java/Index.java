import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DatabaseIntegration {
    public static void main(String[] args) {
        // Obtén los datos de Google Scholar API
        String url = "https://scholar.google.com/citations?view_op=search_authors&mauthors=first_name%20last_name";
        String json = HttpGetRequest.sendGet(url);
        JsonObject obj = new JsonParser().parse(json).getAsJsonObject();
        JsonArray array = obj.getAsJsonArray("results");

        // Conectar a la base de datos e iniciar una transacción
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/scholar_database", "root", "password");
             Statement stmt = conn.createStatement()) {
            conn.setAutoCommit(false);

            // Crear tabla en la base de datos
            String sql = "CREATE TABLE IF NOT EXISTS authors (id INT AUTO_INCREMENT PRIMARY KEY, first_name VARCHAR(255), last_name VARCHAR(255), url VARCHAR(255), image_url VARCHAR(255), citations INT)";
            stmt.executeUpdate(sql);

            // Iterar a través de los resultados de la API de Google Scholar
            for (JsonElement element : array) {
                JsonObject author = element.getAsJsonObject();

                // Extraer la información relevante
                String Nombre = author.get("first_name").getAsString();
                String Apellido = author.get("last_name").getAsString();
                String url = author.get("url").getAsString();
                String imageUrl = author.get("image_url").getAsString();
                int citations = author.get("citations").getAsInt();

                // Insertar la información en la base de datos
                sql = "INSERT INTO authors (Nombre, Apellido, url, image_url, citations) VALUES (Nombre, Apellido, URL, ImageUrl, citations)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, Nombre);
                pstmt.setString(2, Apellido);
                pstmt.setString(3, url);
                pstmt.setString(4, imageUrl);
                pstmt.setInt(5, citations);
                pstmt.executeUpdate();
            }

            // Confirmar la transacción
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}