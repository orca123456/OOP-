import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PayrollGUI extends JFrame {
    private JTextField empIdField;
    private JTextArea outputArea;
    private JButton calculateButton;
    private JButton saveToFileButton;
    private JButton showHistoryButton;
    private EmployeeManagementSystem employeeManagementSystem;

    public PayrollGUI(EmployeeManagementSystem employeeManagementSystem) {
        this.employeeManagementSystem = employeeManagementSystem;

        setTitle("Payroll Management System");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());

        // Input field for Employee ID
        add(new JLabel("Enter Employee ID:"));
        empIdField = new JTextField(10);
        add(empIdField);

        // Output area
        outputArea = new JTextArea(10, 30);
        outputArea.setEditable(false);
        add(new JScrollPane(outputArea));

        // Calculate Button
        calculateButton = new JButton("Calculate Salary");
        add(calculateButton);

        // Save to File Button
        saveToFileButton = new JButton("Save to File");
        add(saveToFileButton);

        // Show Payroll History Button
        showHistoryButton = new JButton("Show Payroll History");
        add(showHistoryButton);

        // Action listener for Calculate button
        calculateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calculateSalary();
            }
        });

        // Action listener for Save to File button
        saveToFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveToFile();
            }
        });

        // Action listener for Show Payroll History button
        showHistoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPayrollHistory();
            }
        });

        setVisible(true);
    }

    private void calculateSalary() {
        String empIdText = empIdField.getText();
        if (empIdText.isEmpty()) {
            outputArea.setText("Please enter an Employee ID.");
            return;
        }
    
        int empId;
        try {
            empId = Integer.parseInt(empIdText);
        } catch (NumberFormatException e) {
            outputArea.setText("Invalid Employee ID. Please enter a numeric value.");
            return;
        }
    
        Map<Integer, Employee> employeeMap = employeeManagementSystem.getEmployeeMap();
        Employee employee = employeeMap.get(empId);
    
        if (employee == null) {
            outputArea.setText("Employee not found.");
            return;
        }
    
        // Date selection dialog
        String[] dates = {"15", "30"};
        String selectedDay = (String) JOptionPane.showInputDialog(
            this,
            "Select the Payroll Day (15 or 30):",
            "Payroll Period Selection",
            JOptionPane.QUESTION_MESSAGE,
            null,
            dates,
            dates[0]
        );
    
        if (selectedDay == null) {
            outputArea.setText("Payroll day selection is required.");
            return;
        }
    
        int payrollDay;
        try {
            payrollDay = Integer.parseInt(selectedDay);
        } catch (NumberFormatException e) {
            outputArea.setText("Invalid payroll day. Please select a valid day.");
            return;
        }
    
        if (payrollDay != 15 && payrollDay != 30) {
            outputArea.setText("Error: Payroll day must be either 15 or 30.");
            return;
        }
    
        // Construct the payroll period
        LocalDateTime now = LocalDateTime.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();
        String payrollDate = now.getMonth().toString().charAt(0) + now.getMonth().toString().substring(1).toLowerCase() + " " + selectedDay + ", " + currentYear;
    
        StringBuilder output = new StringBuilder();
        output.append("Payroll Date: ").append(payrollDate).append("\n");
        output.append("Employee ID: ").append(employee.getId()).append("\n");
        output.append("Name: ").append(employee.getName()).append("\n");
    
        double grossPay = 0.0;
        double deductions = 0.0;
        double netPay = 0.0;
    
        try {
            if (employee instanceof FullTime) {
                FullTime fullTimeEmployee = (FullTime) employee;
    
                String daysPresentStr = JOptionPane.showInputDialog("Enter Days Present:");
                int daysPresent = Integer.parseInt(daysPresentStr);
    
                String absencesStr = JOptionPane.showInputDialog("Enter Days Absent:");
                int absences = Integer.parseInt(absencesStr);
    
                grossPay = fullTimeEmployee.getDailyRate() * daysPresent;
    
                output.append("Days Present: ").append(daysPresent).append("\n");
                output.append("Days Absent: ").append(absences).append("\n");
    
            } else if (employee instanceof PartTime) {
                PartTime partTimeEmployee = (PartTime) employee;
    
                String hoursWorkedStr = JOptionPane.showInputDialog("Enter Hours Worked:");
                int hoursWorked = Integer.parseInt(hoursWorkedStr);
    
                grossPay = partTimeEmployee.getHourlyRate() * hoursWorked;
    
                output.append("Hours Worked: ").append(hoursWorked).append("\n");
    
            } else if (employee instanceof Contract) {
                Contract contractEmployee = (Contract) employee;
    
                String absencesStr = JOptionPane.showInputDialog("Enter Absence Days:");
                int absences = Integer.parseInt(absencesStr);
    
                double dailyRate = contractEmployee.getMonthlyRate() / 30.0;
                grossPay = contractEmployee.getMonthlyRate() - (dailyRate * absences);
    
                output.append("Days Absent: ").append(absences).append("\n");
            }
    
            // Deductions
            double sss = grossPay * 0.11;       // 11% SSS contribution
            double pagIbig = 100.0;            // Fixed Pag-IBIG contribution
            double philHealth = grossPay * 0.035; // 3.5% PhilHealth contribution
    
            deductions = sss + pagIbig + philHealth;
            netPay = grossPay - deductions;
    
            output.append("Gross Pay: ").append(String.format("%.2f", grossPay)).append("\n");
            output.append("Deductions:\n");
            output.append("  SSS: ").append(String.format("%.2f", sss)).append("\n");
            output.append("  Pag-IBIG: ").append(String.format("%.2f", pagIbig)).append("\n");
            output.append("  PhilHealth: ").append(String.format("%.2f", philHealth)).append("\n");
            output.append("Total Deductions: ").append(String.format("%.2f", deductions)).append("\n");
            output.append("Net Pay: ").append(String.format("%.2f", netPay)).append("\n");
    
            outputArea.setText(output.toString());
    
        } catch (NumberFormatException e) {
            outputArea.setText("Invalid input. Please enter numeric values where required.");
        }
    }    
    
    
    private void saveToFile() {
        String payrollData = outputArea.getText();
        if (payrollData.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No data to save.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Get current date and time
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = now.format(formatter);

        // Append the data to payroll.txt
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("payroll.txt", true))) {
            writer.write("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"); // Add a divider
            writer.write("Date and Time: " + formattedDateTime + "\n"); // Add date and time
            writer.write(payrollData);
            writer.write("\n"); // Add a newline after each entry
            JOptionPane.showMessageDialog(this, "Payroll data saved to payroll.txt");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showPayrollHistory() {
        try (BufferedReader reader = new BufferedReader(new FileReader("payroll.txt"))) {
            StringBuilder history = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                history.append(line).append("\n");
            }
            outputArea.setText(history.toString());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading payroll history: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        EmployeeManagementSystem employeeManagementSystem = new EmployeeManagementSystem();
        SwingUtilities.invokeLater(() -> new PayrollGUI(employeeManagementSystem));
    }
}