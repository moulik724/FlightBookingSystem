package utils;

public class ViewFactory {

    public static View getView(String role) {
        switch (role.toLowerCase()) {
            case "admin":
                return new AdminDashboardView();
            case "customer":
                return new CustomerDashboardView();
            case "login":
                return new LoginView();
            default:
                throw new IllegalArgumentException("Unknown role: " + role);
        }
    }
}
