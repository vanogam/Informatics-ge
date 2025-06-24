package ge.informatics.sandbox.dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import ge.informatics.sandbox.model.TestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SubmissionTestResultDao {

    private static final HikariDataSource dataSource;

    private static final Logger log = LoggerFactory.getLogger(SubmissionTestResultDao.class);

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(System.getenv("JDBC_URL"));
        config.setUsername(System.getenv("JDBC_USERNAME"));
        config.setPassword(System.getenv("JDBC_PASSWORD"));
        config.setMaximumPoolSize(10); // Adjust pool size as needed
        dataSource = new HikariDataSource(config);
    }

    private static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void saveTestResult(String submissionId, String testCaseKey, double score, TestStatus status, String errorMessage, int time, int memory, String outcome) {
        String sql = "INSERT INTO submission_submissiontestresults (submission_id, testkey, score, teststatus, time, memory, outcome, message) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = getConnection();
             var preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, Long.parseLong(submissionId));
            preparedStatement.setString(2, testCaseKey);
            preparedStatement.setDouble(3, score);
            preparedStatement.setInt(4, status.ordinal());
            preparedStatement.setInt(5, time);
            preparedStatement.setInt(6, memory);
            preparedStatement.setString(7, outcome);
            preparedStatement.setString(8, errorMessage);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error("Error saving test result for submission {}: {}", submissionId, e.getMessage());
            throw new RuntimeException("Failed to save test result for submission " + submissionId, e);
        }
    }
}
