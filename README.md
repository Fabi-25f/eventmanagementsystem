# eventmanagementsystem
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import javax.swing.table.DefaultTableModel;
import javax.imageio.ImageIO;  // Add this import

public class EventManagementGUI {
    private JFrame frame;
    private JTextField adminIdField;
    private JPasswordField passwordField;
    private JPanel loginPanel, adminPanel;
    private JTabbedPane tabbedPane;
    private Connection connection;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                EventManagementGUI window = new EventManagementGUI();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public EventManagementGUI() {
        initialize();
        connectToDatabase();
    }

    private void connectToDatabase() {
        try {
            String url = "jdbc:mysql://localhost:3306/EventManagement?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            String username = "root";
            String password = "Mysql@123";
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Database Connection Error: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void initialize() {
        frame = new JFrame("Event Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);

        createLoginPanel();
        frame.getContentPane().add(loginPanel);

    }

    private void createLoginPanel() {
        // Create a panel with background image
        loginPanel = new JPanel(new BorderLayout(10, 10)) {
            private Image backgroundImage;

            {
                // Load the background image
                try {
                    backgroundImage = ImageIO.read(getClass().getResource("/background.jpg"));
                } catch (IOException e) {
                    System.err.println("Error loading background image: " + e.getMessage());
                }
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    // Scale image to fit panel
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };

        loginPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create a semi-transparent panel for the login form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false); // Make transparent

        // Create a content panel with semi-transparent background
        JPanel contentPanel = new JPanel(new GridLayout(3, 3, 5, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contentPanel.setBackground(new Color(255, 255, 255, 180)); // Semi-transparent white

        // Add components to content panel
        JLabel titleLabel = new JLabel("EVENTRA", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Impact", Font.BOLD, 28));
        contentPanel.add(titleLabel);
        contentPanel.add(new JLabel("")); // Empty cell

        contentPanel.add(new JLabel("Admin ID:", SwingConstants.CENTER));
        adminIdField = new JTextField(15);
        contentPanel.add(adminIdField);

        contentPanel.add(new JLabel("Password:", SwingConstants.CENTER));
        passwordField = new JPasswordField(15);
        contentPanel.add(passwordField);

        // Add login button
        JButton loginButton = new JButton("Login");
        loginButton.setPreferredSize(new Dimension(200, 35)); // Width: 120px, Height: 35px
        loginButton.setFont(new Font("Arial", Font.BOLD, 16)); // Optional: Set font size
        loginButton.addActionListener(e -> validateAdmin());

        // Add components to form panel
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 0, 0);

        formPanel.add(contentPanel, gbc);
        formPanel.add(loginButton, gbc);

        // Add form panel to main panel
        loginPanel.add(formPanel, BorderLayout.CENTER);
    }


    private void validateAdmin() {
        try {
            int adminId = Integer.parseInt(adminIdField.getText());
            String password = new String(passwordField.getPassword());

            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM Admin WHERE AdminID=? AND Password=?")) {
                ps.setInt(1, adminId);
                ps.setString(2, password);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    createAdminPanel();
                    frame.getContentPane().removeAll();
                    frame.getContentPane().add(tabbedPane);
                    frame.revalidate();
                    frame.repaint();
                } else {
                    JOptionPane.showMessageDialog(frame, "Invalid Credentials",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Please enter a valid Admin ID",
                    "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Database Error: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createAdminPanel() {
        // Set the background color for the tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(new Color(0, 112, 246)); // Light gray background

        // Create a common panel style with background color
        Color panelColor = new Color(0, 112, 246); // Slightly lighter gray

        // Events Tab
        JPanel eventsPanel = new JPanel(new BorderLayout());
        eventsPanel.setBackground(panelColor);
        eventsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTable eventsTable = new JTable();
        eventsTable.setBackground(new Color(204, 208, 248, 220));
        JScrollPane eventsScrollPane = new JScrollPane(eventsTable);
        eventsScrollPane.setBackground(panelColor);
        eventsPanel.add(eventsScrollPane, BorderLayout.CENTER);

        JPanel eventsButtonPanel = new JPanel(new FlowLayout());
        eventsButtonPanel.setBackground(panelColor);
        JButton refreshEventsBtn = new JButton("Refresh");
        refreshEventsBtn.addActionListener(e -> refreshEventsTable(eventsTable));
        eventsButtonPanel.add(refreshEventsBtn);

        JButton addEventBtn = new JButton("Add Event");
        addEventBtn.addActionListener(e -> showAddEventDialog(eventsTable));
        eventsButtonPanel.add(addEventBtn);

        JButton updateEventBtn = new JButton("Update Event");
        updateEventBtn.addActionListener(e -> showUpdateEventDialog(eventsTable));
        eventsButtonPanel.add(updateEventBtn);

        JButton deleteEventBtn = new JButton("Delete Event");
        deleteEventBtn.addActionListener(e -> deleteEvent(eventsTable));
        eventsButtonPanel.add(deleteEventBtn);

        eventsPanel.add(eventsButtonPanel, BorderLayout.SOUTH);

        // Attendees Tab
        JPanel attendeesPanel = new JPanel(new BorderLayout());
        attendeesPanel.setBackground(panelColor);
        attendeesPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTable attendeesTable = new JTable();
        attendeesTable.setBackground(new Color(204, 208, 248, 220));
        JScrollPane attendeesScrollPane = new JScrollPane(attendeesTable);
        attendeesScrollPane.setBackground(panelColor);
        attendeesPanel.add(attendeesScrollPane, BorderLayout.CENTER);

        JPanel attendeesButtonPanel = new JPanel(new FlowLayout());
        attendeesButtonPanel.setBackground(panelColor);
        JButton refreshAttendeesBtn = new JButton("Refresh");
        refreshAttendeesBtn.addActionListener(e -> refreshAttendeesTable(attendeesTable));
        attendeesButtonPanel.add(refreshAttendeesBtn);

        JButton addAttendeeBtn = new JButton("Add Attendee");
        addAttendeeBtn.addActionListener(e -> showAddAttendeeDialog(attendeesTable));
        attendeesButtonPanel.add(addAttendeeBtn);

        attendeesPanel.add(attendeesButtonPanel, BorderLayout.SOUTH);

        // Venues Tab
        JPanel venuesPanel = new JPanel(new BorderLayout());
        venuesPanel.setBackground(panelColor);
        venuesPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTable venuesTable = new JTable();
        venuesTable.setBackground(new Color(204, 208, 248, 220));
        JScrollPane venuesScrollPane = new JScrollPane(venuesTable);
        venuesScrollPane.setBackground(panelColor);
        venuesPanel.add(venuesScrollPane, BorderLayout.CENTER);

        JPanel venuesButtonPanel = new JPanel(new FlowLayout());
        venuesButtonPanel.setBackground(panelColor);
        JButton refreshVenuesBtn = new JButton("Refresh");
        refreshVenuesBtn.addActionListener(e -> refreshVenuesTable(venuesTable));
        venuesButtonPanel.add(refreshVenuesBtn);

        JButton addVenueBtn = new JButton("Add Venue");
        addVenueBtn.addActionListener(e -> showAddVenueDialog(venuesTable));
        venuesButtonPanel.add(addVenueBtn);

        venuesPanel.add(venuesButtonPanel, BorderLayout.SOUTH);

        // Bookings Tab
        JPanel bookingsPanel = new JPanel(new BorderLayout());
        bookingsPanel.setBackground(panelColor);
        bookingsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTable bookingsTable = new JTable();
        bookingsTable.setBackground(new Color(204, 208, 248, 220));
        JScrollPane bookingsScrollPane = new JScrollPane(bookingsTable);
        bookingsScrollPane.setBackground(panelColor);
        bookingsPanel.add(bookingsScrollPane, BorderLayout.CENTER);

        JPanel bookingsButtonPanel = new JPanel(new FlowLayout());
        bookingsButtonPanel.setBackground(panelColor);
        JButton refreshBookingsBtn = new JButton("Refresh");
        refreshBookingsBtn.addActionListener(e -> refreshBookingsTable(bookingsTable));
        bookingsButtonPanel.add(refreshBookingsBtn);

        JButton addBookingBtn = new JButton("Add Booking");
        addBookingBtn.addActionListener(e -> showAddBookingDialog(bookingsTable));
        bookingsButtonPanel.add(addBookingBtn);

        JButton cancelBookingBtn = new JButton("Cancel Booking");
        cancelBookingBtn.addActionListener(e -> cancelBooking(bookingsTable));
        bookingsButtonPanel.add(cancelBookingBtn);

        bookingsPanel.add(bookingsButtonPanel, BorderLayout.SOUTH);

        // Admin Tab
        JPanel adminTabPanel = new JPanel(new BorderLayout());
        adminTabPanel.setBackground(panelColor);
        adminTabPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel adminButtonPanel = new JPanel(new FlowLayout());
        adminButtonPanel.setBackground(panelColor);
        JButton changePasswordBtn = new JButton("Change Password");
        changePasswordBtn.addActionListener(e -> showChangePasswordDialog());
        adminButtonPanel.add(changePasswordBtn);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> logout());
        adminButtonPanel.add(logoutBtn);

        adminTabPanel.add(adminButtonPanel, BorderLayout.CENTER);

        // Add all tabs to the tabbed pane
        tabbedPane.addTab("Events", eventsPanel);
        tabbedPane.addTab("Attendees", attendeesPanel);
        tabbedPane.addTab("Venues", venuesPanel);
        tabbedPane.addTab("Bookings", bookingsPanel);
        tabbedPane.addTab("Admin", adminTabPanel);

        // Set background for the tabbed pane content area
        tabbedPane.setBackgroundAt(0, panelColor);
        tabbedPane.setBackgroundAt(1, panelColor);
        tabbedPane.setBackgroundAt(2, panelColor);
        tabbedPane.setBackgroundAt(3, panelColor);
        tabbedPane.setBackgroundAt(4, panelColor);

        // Refresh all tables
        refreshEventsTable(eventsTable);
        refreshAttendeesTable(attendeesTable);
        refreshVenuesTable(venuesTable);
        refreshBookingsTable(bookingsTable);
    }

    private void refreshEventsTable(JTable table) {
        try {
            String query = "SELECT e.*, v.Name as VenueName FROM Events e LEFT JOIN Venues v ON e.VenueID = v.VenueID";
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("Event ID");
            model.addColumn("Name");
            model.addColumn("Date");
            model.addColumn("Venue");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("EventID"),
                        rs.getString("Name"),
                        rs.getDate("Date"),
                        rs.getString("VenueName")
                });
            }

            table.setModel(model);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error loading events: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddEventDialog(JTable eventsTable) {
        JTextField nameField = new JTextField();
        JTextField dateField = new JTextField();
        JComboBox<String> venueCombo = new JComboBox<>(getVenues().toArray(new String[0]));

        Object[] message = {
                "Event Name:", nameField,
                "Event Date (YYYY-MM-DD):", dateField,
                "Venue:", venueCombo
        };

        int option = JOptionPane.showConfirmDialog(frame, message, "Add New Event", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                String venueId = venueCombo.getSelectedItem().toString().split(" - ")[0];
                String query = "INSERT INTO Events (Name, Date, VenueID) VALUES (?, ?, ?)";
                PreparedStatement ps = connection.prepareStatement(query);
                ps.setString(1, nameField.getText());
                ps.setString(2, dateField.getText());
                ps.setInt(3, Integer.parseInt(venueId));
                ps.executeUpdate();

                JOptionPane.showMessageDialog(frame, "Event added successfully!");
                refreshEventsTable(eventsTable);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(frame, "Error adding event: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private ArrayList<String> getVenues() {
        ArrayList<String> venues = new ArrayList<>();
        try {
            String query = "SELECT VenueID, Name FROM Venues";
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                venues.add(rs.getInt("VenueID") + " - " + rs.getString("Name"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error loading venues: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        return venues;
    }

    private void logout() {
        tabbedPane = null;
        adminIdField.setText("");
        passwordField.setText("");
        frame.getContentPane().removeAll();
        frame.getContentPane().add(loginPanel);
        frame.revalidate();
        frame.repaint();
    }

    // Implement similar methods for other tabs (Attendees, Venues, Bookings)
    // Following the same pattern as the Events tab methods

    private void refreshAttendeesTable(JTable table) {
        try {
            String query = "SELECT * FROM Attendees";
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("Attendee ID");
            model.addColumn("Name");
            model.addColumn("Email");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("AttendeeID"),
                        rs.getString("Name"),
                        rs.getString("Email")
                });
            }

            table.setModel(model);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error loading attendees: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddAttendeeDialog(JTable attendeesTable) {
        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();

        Object[] message = {
                "Name:", nameField,
                "Email:", emailField
        };

        int option = JOptionPane.showConfirmDialog(frame, message, "Add New Attendee", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                String query = "INSERT INTO Attendees (Name, Email) VALUES (?, ?)";
                PreparedStatement ps = connection.prepareStatement(query);
                ps.setString(1, nameField.getText());
                ps.setString(2, emailField.getText());
                ps.executeUpdate();

                JOptionPane.showMessageDialog(frame, "Attendee added successfully!");
                refreshAttendeesTable(attendeesTable);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(frame, "Error adding attendee: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void refreshVenuesTable(JTable table) {
        try {
            String query = "SELECT * FROM Venues";
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("Venue ID");
            model.addColumn("Name");
            model.addColumn("Capacity");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("VenueID"),
                        rs.getString("Name"),
                        rs.getInt("Capacity")
                });
            }

            table.setModel(model);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error loading venues: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddVenueDialog(JTable venuesTable) {
        JTextField nameField = new JTextField();
        JTextField capacityField = new JTextField();

        Object[] message = {
                "Venue Name:", nameField,
                "Capacity:", capacityField
        };

        int option = JOptionPane.showConfirmDialog(frame, message, "Add New Venue", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                String query = "INSERT INTO Venues (Name, Capacity) VALUES (?, ?)";
                PreparedStatement ps = connection.prepareStatement(query);
                ps.setString(1, nameField.getText());
                ps.setInt(2, Integer.parseInt(capacityField.getText()));
                ps.executeUpdate();

                JOptionPane.showMessageDialog(frame, "Venue added successfully!");
                refreshVenuesTable(venuesTable);
            } catch (SQLException | NumberFormatException e) {
                JOptionPane.showMessageDialog(frame, "Error adding venue: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void refreshBookingsTable(JTable table) {
        try {
            String query = "SELECT b.BookingID, e.Name as EventName, a.Name as AttendeeName " +
                    "FROM Bookings b " +
                    "JOIN Events e ON b.EventID = e.EventID " +
                    "JOIN Attendees a ON b.AttendeeID = a.AttendeeID";
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("Booking ID");
            model.addColumn("Event");
            model.addColumn("Attendee");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("BookingID"),
                        rs.getString("EventName"),
                        rs.getString("AttendeeName")
                });
            }

            table.setModel(model);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error loading bookings: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddBookingDialog(JTable bookingsTable) {
        JComboBox<String> eventCombo = new JComboBox<>(getEvents().toArray(new String[0]));
        JComboBox<String> attendeeCombo = new JComboBox<>(getAttendees().toArray(new String[0]));

        Object[] message = {
                "Event:", eventCombo,
                "Attendee:", attendeeCombo
        };

        int option = JOptionPane.showConfirmDialog(frame, message, "Add New Booking", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                String eventId = eventCombo.getSelectedItem().toString().split(" - ")[0];
                String attendeeId = attendeeCombo.getSelectedItem().toString().split(" - ")[0];

                String query = "INSERT INTO Bookings (EventID, AttendeeID) VALUES (?, ?)";
                PreparedStatement ps = connection.prepareStatement(query);
                ps.setInt(1, Integer.parseInt(eventId));
                ps.setInt(2, Integer.parseInt(attendeeId));
                ps.executeUpdate();

                JOptionPane.showMessageDialog(frame, "Booking added successfully!");
                refreshBookingsTable(bookingsTable);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(frame, "Error adding booking: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private ArrayList<String> getEvents() {
        ArrayList<String> events = new ArrayList<>();
        try {
            String query = "SELECT EventID, Name FROM Events";
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                events.add(rs.getInt("EventID") + " - " + rs.getString("Name"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error loading events: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        return events;
    }

    private ArrayList<String> getAttendees() {
        ArrayList<String> attendees = new ArrayList<>();
        try {
            String query = "SELECT AttendeeID, Name FROM Attendees";
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                attendees.add(rs.getInt("AttendeeID") + " - " + rs.getString("Name"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error loading attendees: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        return attendees;
    }

    private void cancelBooking(JTable bookingsTable) {
        int selectedRow = bookingsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Please select a booking to cancel",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int bookingId = (int) bookingsTable.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(frame,
                "Are you sure you want to cancel this booking?", "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String query = "DELETE FROM Bookings WHERE BookingID = ?";
                PreparedStatement ps = connection.prepareStatement(query);
                ps.setInt(1, bookingId);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(frame, "Booking cancelled successfully!");
                refreshBookingsTable(bookingsTable);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(frame, "Error cancelling booking: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showUpdateEventDialog(JTable eventsTable) {
        int selectedRow = eventsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Please select an event to update",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int eventId = (int) eventsTable.getValueAt(selectedRow, 0);
        String currentName = (String) eventsTable.getValueAt(selectedRow, 1);
        String currentDate = eventsTable.getValueAt(selectedRow, 2).toString();
        String currentVenue = (String) eventsTable.getValueAt(selectedRow, 3);

        JTextField nameField = new JTextField(currentName);
        JTextField dateField = new JTextField(currentDate);
        JComboBox<String> venueCombo = new JComboBox<>(getVenues().toArray(new String[0]));
        venueCombo.setSelectedItem(currentVenue);

        Object[] message = {
                "Event Name:", nameField,
                "Event Date (YYYY-MM-DD):", dateField,
                "Venue:", venueCombo
        };

        int option = JOptionPane.showConfirmDialog(frame, message, "Update Event", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                String venueId = venueCombo.getSelectedItem().toString().split(" - ")[0];
                String query = "UPDATE Events SET Name = ?, Date = ?, VenueID = ? WHERE EventID = ?";
                PreparedStatement ps = connection.prepareStatement(query);
                ps.setString(1, nameField.getText());
                ps.setString(2, dateField.getText());
                ps.setInt(3, Integer.parseInt(venueId));
                ps.setInt(4, eventId);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(frame, "Event updated successfully!");
                refreshEventsTable(eventsTable);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(frame, "Error updating event: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteEvent(JTable eventsTable) {
        int selectedRow = eventsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Please select an event to delete",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int eventId = (int) eventsTable.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(frame,
                "Are you sure you want to delete this event?", "Confirm Deletion",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String query = "DELETE FROM Events WHERE EventID = ?";
                PreparedStatement ps = connection.prepareStatement(query);
                ps.setInt(1, eventId);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(frame, "Event deleted successfully!");
                refreshEventsTable(eventsTable);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(frame, "Error deleting event: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showChangePasswordDialog() {
        JPasswordField currentPasswordField = new JPasswordField();
        JPasswordField newPasswordField = new JPasswordField();
        JPasswordField confirmPasswordField = new JPasswordField();

        Object[] message = {
                "Current Password:", currentPasswordField,
                "New Password:", newPasswordField,
                "Confirm New Password:", confirmPasswordField
        };

        int option = JOptionPane.showConfirmDialog(frame, message, "Change Password", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String currentPassword = new String(currentPasswordField.getPassword());
            String newPassword = new String(newPasswordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            if (!newPassword.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(frame, "New passwords do not match",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                int adminId = Integer.parseInt(adminIdField.getText());
                String query = "UPDATE Admin SET Password = ? WHERE AdminID = ? AND Password = ?";
                PreparedStatement ps = connection.prepareStatement(query);
                ps.setString(1, newPassword);
                ps.setInt(2, adminId);
                ps.setString(3, currentPassword);
                int rowsAffected = ps.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(frame, "Password changed successfully!");
                } else {
                    JOptionPane.showMessageDialog(frame, "Current password is incorrect",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(frame, "Error changing password: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

}
