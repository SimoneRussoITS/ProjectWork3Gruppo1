package org.acme.persistence.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.persistence.model.Category;
import org.acme.persistence.model.Course;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class CourseRepository {

    private final DataSource dataSource;

    public CourseRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();
        try {
            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("SELECT id, name, category FROM course")) {
                    newCourse(courses, statement);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return courses;
    }

    public Course getCourseById(int courseId) {
        Course course = new Course();
        try {
            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("SELECT id, name, category FROM course WHERE id = ?")) {
                    statement.setInt(1, courseId);
                    var resultSet = statement.executeQuery();
                    if (resultSet.next()) {
                        course.setIdCourse(resultSet.getInt("id"));
                        course.setName(resultSet.getString("name"));
                        course.setCategory(Category.valueOf(resultSet.getString("category")));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return course;
    }

    public List<Course> getCoursesByCategory(String category) {
        List<Course> courses = new ArrayList<>();
        try {
            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("SELECT id, name, category FROM course WHERE category = ?")) {
                    statement.setString(1, category);
                    newCourse(courses, statement);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return courses;
    }

    private void newCourse(List<Course> courses, PreparedStatement statement) throws SQLException {
        var resultSet = statement.executeQuery();
        while (resultSet.next()) {
            var course = new Course();
            course.setIdCourse(resultSet.getInt("id"));
            course.setName(resultSet.getString("name"));
            course.setCategory(Category.valueOf(resultSet.getString("category")));
            courses.add(course);
        }
    }

    public void deleteCourse(int courseId) {
        try {
            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("DELETE FROM course WHERE id = ?")) {
                    statement.setInt(1, courseId);
                    int rowsAffected = statement.executeUpdate();
                    if (rowsAffected == 0) {
                        throw new SQLException("Failed to delete course, no rows affected.");
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void createCourse(Course course) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement checkStatement = connection.prepareStatement("SELECT COUNT(*) FROM course WHERE name = ?")) {
                checkStatement.setString(1, course.getName());
                ResultSet resultSet = checkStatement.executeQuery();
                if (resultSet.next() && resultSet.getInt(1) > 0) {
                    throw new RuntimeException("A course with the same name already exists.");
                }
            }

            try (PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO course (name, category) VALUES (?, ?)")) {
                insertStatement.setString(1, course.getName());
                insertStatement.setString(2, course.getCategory().name());
                insertStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public void updateCourse(int courseId, String name, String category) {
        try {
            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("UPDATE course SET name = ?, category = ? WHERE id = ?")) {
                    statement.setString(1, name);
                    statement.setString(2, category);
                    statement.setInt(3, courseId);
                    statement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
