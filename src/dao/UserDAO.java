package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.User;
import db.DBConnection;

public class UserDAO {

    // Add a new user
    public static boolean addUser(User user) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getRole());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("SQL Error during registration: " + e.getMessage());
            return false;
        }
    }

    // Fetch a user based on username and password
    public static User getUser(String username, String password) {
    try (Connection conn = DBConnection.getConnection()) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, username);
        stmt.setString(2, password);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            int userId = rs.getInt("id");
            System.out.println("Retrieved User ID: " + userId); // Debug
            return new User(
                userId,
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("role")
            );
        } else {
            System.out.println("No user found with given credentials."); // Debug
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null;
}


    // Fetch all users
    public static List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM users";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                users.add(new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("role")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    // Delete a user by ID
    public static boolean deleteUser(int userId) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "DELETE FROM users WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("SQL Error during user deletion: " + e.getMessage());
            return false;
        }
    }

    // Update a user's role
    public static boolean updateUserRole(int userId, String newRole) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE users SET role = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, newRole);
            stmt.setInt(2, userId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("SQL Error during user role update: " + e.getMessage());
            return false;
        }
    }
}
