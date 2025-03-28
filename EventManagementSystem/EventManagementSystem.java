import java.sql.*;
import java.util.Scanner;

public class EventManagementSystem {
    private static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        adminLogin();
    }

    private static Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/EventManagement?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        String username = "root";
        String password = "Mysql@123";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found", e);
        }
    }

    static void adminLogin() {
        while (true) {
            System.out.println("Enter Admin ID:");
            int adminId = sc.nextInt();
            sc.nextLine();
            System.out.println("Enter Password:");
            String password = sc.nextLine();

            if (validateAdmin(adminId, password)) {
                System.out.println("Admin logged in successfully.");
                adminMenu();
                break;
            } else {
                System.out.println("Invalid Admin credentials. Try again.");
            }
        }
    }

    public static boolean validateAdmin(int adminId, String password) {
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM Admin WHERE AdminID=? AND Password=?")) {
            ps.setInt(1, adminId);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.out.println("Admin validation error: " + e.getMessage());
            return false;
        }
    }

    static void adminMenu() {
        boolean session = true;
        while (session) {
            System.out.println("\n--- Admin Menu ---");
            System.out.println("1. View All Events");
            System.out.println("2. View All Attendees");
            System.out.println("3. View Attendees for a Specific Event");
            System.out.println("4. Create a New Event");
            System.out.println("5. Add Attendee to an Event");
            System.out.println("6. Delete an Event");
            System.out.println("7. Update Event Details");
            System.out.println("8. View All Venues");
            System.out.println("9. Add a New Venue");
            System.out.println("10. Delete a Venue");
            System.out.println("11. View Bookings for a Specific Event");
            System.out.println("12. Cancel an Attendee's Booking");
            System.out.println("13. Search Attendees by Email or Name");
            System.out.println("14. Change Admin Password");
            System.out.println("15. Logout");
            System.out.print("Enter your choice: ");

            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1: viewAllEvents(); break;
                case 2: viewAllAttendees(); break;
                case 3: viewAttendeesForEvent(); break;
                case 4: createEvent(); break;
                case 5: addAttendeeToEvent(); break;
                case 6: deleteEvent(); break;
                case 7: updateEventDetails(); break;
                case 8: viewAllVenues(); break;
                case 9: addNewVenue(); break;
                case 10: deleteVenue(); break;
                case 11: viewBookingsForEvent(); break;
                case 12: cancelAttendeeBooking(); break;
                case 13: searchAttendee(); break;
                case 14: changeAdminPassword(); break;
                case 15:
                    System.out.println("Logged out successfully.");
                    session = false;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    // Event Management Methods
    static void viewAllEvents() {
        try (Connection con = getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT e.*, v.Name as VenueName FROM Events e LEFT JOIN Venues v ON e.VenueID = v.VenueID")) {

            System.out.println("\n--- All Events ---");
            System.out.printf("%-10s %-30s %-15s %-20s%n", "Event ID", "Event Name", "Date", "Venue");
            System.out.println("------------------------------------------------------------");

            while (rs.next()) {
                System.out.printf("%-10d %-30s %-15s %-20s%n",
                        rs.getInt("EventID"),
                        rs.getString("Name"),
                        rs.getDate("Date"),
                        rs.getString("VenueName"));
            }
        } catch (SQLException e) {
            System.out.println("Error viewing events: " + e.getMessage());
        }
    }

    static void createEvent() {
        System.out.println("\n--- Create New Event ---");
        System.out.println("Enter Event Name:");
        String name = sc.nextLine();
        System.out.println("Enter Event Date (YYYY-MM-DD):");
        String date = sc.nextLine();
        System.out.println("Enter Venue ID:");
        int venueId = sc.nextInt();
        sc.nextLine();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO Events (Name, Date, VenueID) VALUES (?, ?, ?)")) {

            ps.setString(1, name);
            ps.setString(2, date);
            ps.setInt(3, venueId);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Event created successfully!");
            } else {
                System.out.println("Failed to create event.");
            }
        } catch (SQLException e) {
            System.out.println("Error creating event: " + e.getMessage());
        }
    }

    static void updateEventDetails() {
        System.out.println("\n--- Update Event Details ---");
        System.out.println("Enter Event ID to update:");
        int eventId = sc.nextInt();
        sc.nextLine();

        System.out.println("Enter new Event Name:");
        String eventName = sc.nextLine();
        System.out.println("Enter new Event Date (YYYY-MM-DD):");
        String eventDate = sc.nextLine();
        System.out.println("Enter new Venue ID:");
        int venueId = sc.nextInt();
        sc.nextLine();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE Events SET Name=?, Date=?, VenueID=? WHERE EventID=?")) {
            ps.setString(1, eventName);
            ps.setString(2, eventDate);
            ps.setInt(3, venueId);
            ps.setInt(4, eventId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Event updated successfully.");
            } else {
                System.out.println("Event update failed.");
            }
        } catch (SQLException e) {
            System.out.println("Error updating event: " + e.getMessage());
        }
    }

    static void deleteEvent() {
        System.out.println("\n--- Delete Event ---");
        System.out.println("Enter Event ID to delete:");
        int eventId = sc.nextInt();
        sc.nextLine();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM Events WHERE EventID=?")) {

            ps.setInt(1, eventId);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Event deleted successfully!");
            } else {
                System.out.println("No event found with ID: " + eventId);
            }
        } catch (SQLException e) {
            System.out.println("Error deleting event: " + e.getMessage());
        }
    }

    // Attendee Management Methods
    static void viewAllAttendees() {
        try (Connection con = getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Attendees")) {

            System.out.println("\n--- All Attendees ---");
            System.out.printf("%-12s %-30s %-30s%n", "Attendee ID", "Name", "Email");
            System.out.println("------------------------------------------------------------");

            while (rs.next()) {
                System.out.printf("%-12d %-30s %-30s%n",
                        rs.getInt("AttendeeID"),
                        rs.getString("Name"),
                        rs.getString("Email"));
            }
        } catch (SQLException e) {
            System.out.println("Error viewing attendees: " + e.getMessage());
        }
    }

    static void viewAttendeesForEvent() {
        System.out.println("\nEnter Event ID to view attendees:");
        int eventId = sc.nextInt();
        sc.nextLine();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT a.* FROM Attendees a " +
                             "JOIN Bookings b ON a.AttendeeID = b.AttendeeID " +
                             "WHERE b.EventID = ?")) {

            ps.setInt(1, eventId);
            ResultSet rs = ps.executeQuery();

            System.out.println("\n--- Attendees for Event ID " + eventId + " ---");
            System.out.printf("%-12s %-30s %-30s%n", "Attendee ID", "Name", "Email");
            System.out.println("------------------------------------------------------------");

            while (rs.next()) {
                System.out.printf("%-12d %-30s %-30s%n",
                        rs.getInt("AttendeeID"),
                        rs.getString("Name"),
                        rs.getString("Email"));
            }
        } catch (SQLException e) {
            System.out.println("Error viewing attendees: " + e.getMessage());
        }
    }

    static void addAttendeeToEvent() {
        System.out.println("\n--- Add Attendee to Event ---");
        System.out.println("Enter Attendee ID:");
        int attendeeId = sc.nextInt();
        sc.nextLine();
        System.out.println("Enter Event ID:");
        int eventId = sc.nextInt();
        sc.nextLine();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO Bookings (EventID, AttendeeID) VALUES (?, ?)")) {

            ps.setInt(1, eventId);
            ps.setInt(2, attendeeId);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Attendee added to event successfully!");
            } else {
                System.out.println("Failed to add attendee to event.");
            }
        } catch (SQLException e) {
            System.out.println("Error adding attendee to event: " + e.getMessage());
        }
    }

    static void searchAttendee() {
        System.out.println("\n--- Search Attendees ---");
        System.out.println("Enter search term (name or email):");
        String searchTerm = sc.nextLine();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT * FROM Attendees WHERE Name LIKE ? OR Email LIKE ?")) {

            ps.setString(1, "%" + searchTerm + "%");
            ps.setString(2, "%" + searchTerm + "%");
            ResultSet rs = ps.executeQuery();

            System.out.println("\n--- Search Results ---");
            System.out.printf("%-12s %-30s %-30s%n", "Attendee ID", "Name", "Email");
            System.out.println("------------------------------------------------------------");

            while (rs.next()) {
                System.out.printf("%-12d %-30s %-30s%n",
                        rs.getInt("AttendeeID"),
                        rs.getString("Name"),
                        rs.getString("Email"));
            }
        } catch (SQLException e) {
            System.out.println("Error searching attendees: " + e.getMessage());
        }
    }

    // Venue Management Methods
    static void viewAllVenues() {
        try (Connection con = getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Venues")) {

            System.out.println("\n--- All Venues ---");
            System.out.printf("%-10s %-30s %-10s%n", "Venue ID", "Name", "Capacity");
            System.out.println("------------------------------------------------");

            while (rs.next()) {
                System.out.printf("%-10d %-30s %-10d%n",
                        rs.getInt("VenueID"),
                        rs.getString("Name"),
                        rs.getInt("Capacity"));
            }
        } catch (SQLException e) {
            System.out.println("Error viewing venues: " + e.getMessage());
        }
    }

    static void addNewVenue() {
        System.out.println("\n--- Add New Venue ---");
        System.out.println("Enter Venue Name:");
        String name = sc.nextLine();
        System.out.println("Enter Venue Capacity:");
        int capacity = sc.nextInt();
        sc.nextLine();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO Venues (Name, Capacity) VALUES (?, ?)")) {

            ps.setString(1, name);
            ps.setInt(2, capacity);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Venue added successfully!");
            } else {
                System.out.println("Failed to add venue.");
            }
        } catch (SQLException e) {
            System.out.println("Error adding venue: " + e.getMessage());
        }
    }

    static void deleteVenue() {
        System.out.println("\n--- Delete Venue ---");
        System.out.println("Enter Venue ID to delete:");
        int venueId = sc.nextInt();
        sc.nextLine();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM Venues WHERE VenueID=?")) {

            ps.setInt(1, venueId);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Venue deleted successfully!");
            } else {
                System.out.println("No venue found with ID: " + venueId);
            }
        } catch (SQLException e) {
            System.out.println("Error deleting venue: " + e.getMessage());
        }
    }

    // Booking Management Methods
    static void viewBookingsForEvent() {
        System.out.println("\nEnter Event ID to view bookings:");
        int eventId = sc.nextInt();
        sc.nextLine();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT b.BookingID, a.Name, a.Email " +
                             "FROM Bookings b JOIN Attendees a ON b.AttendeeID = a.AttendeeID " +
                             "WHERE b.EventID = ?")) {

            ps.setInt(1, eventId);
            ResultSet rs = ps.executeQuery();

            System.out.println("\n--- Bookings for Event ID " + eventId + " ---");
            System.out.printf("%-12s %-30s %-30s%n", "Booking ID", "Attendee Name", "Email");
            System.out.println("------------------------------------------------------------");

            while (rs.next()) {
                System.out.printf("%-12d %-30s %-30s%n",
                        rs.getInt("BookingID"),
                        rs.getString("Name"),
                        rs.getString("Email"));
            }
        } catch (SQLException e) {
            System.out.println("Error viewing bookings: " + e.getMessage());
        }
    }

    static void cancelAttendeeBooking() {
        System.out.println("\n--- Cancel Booking ---");
        System.out.println("Enter Booking ID to cancel:");
        int bookingId = sc.nextInt();
        sc.nextLine();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM Bookings WHERE BookingID=?")) {

            ps.setInt(1, bookingId);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Booking cancelled successfully!");
            } else {
                System.out.println("No booking found with ID: " + bookingId);
            }
        } catch (SQLException e) {
            System.out.println("Error cancelling booking: " + e.getMessage());
        }
    }

    // Admin Management Methods
    static void changeAdminPassword() {
        System.out.println("\n--- Change Admin Password ---");
        System.out.println("Enter Admin ID:");
        int adminId = sc.nextInt();
        sc.nextLine();
        System.out.println("Enter Current Password:");
        String currentPassword = sc.nextLine();

        if (!validateAdmin(adminId, currentPassword)) {
            System.out.println("Incorrect current password.");
            return;
        }

        System.out.println("Enter New Password:");
        String newPassword = sc.nextLine();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE Admin SET Password=? WHERE AdminID=?")) {
            ps.setString(1, newPassword);
            ps.setInt(2, adminId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Password updated successfully.");
            } else {
                System.out.println("Password update failed.");
            }
        } catch (SQLException e) {
            System.out.println("Error changing password: " + e.getMessage());
        }
    }
}