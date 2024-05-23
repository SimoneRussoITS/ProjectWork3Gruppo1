package org.acme.persistence.repository;

import com.mysql.cj.jdbc.DatabaseMetaData;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.persistence.model.Category;
import org.acme.persistence.model.Course;
import org.acme.persistence.model.State;
import org.acme.persistence.model.User;

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
                    var resultSet = statement.executeQuery();
                    while (resultSet.next()) {
                        var course = new Course();
                        course.setIdCourse(resultSet.getInt("id"));
                        course.setName(resultSet.getString("name"));
                        course.setCategory(Category.valueOf(resultSet.getString("category")));
                        courses.add(course);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return courses;
    }

    public List<Course> getCoursesByCategory(String category) {
        List<Course> courses = new ArrayList<>();
        try {
            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("SELECT id, name, category FROM course WHERE category = ?")) {
                    statement.setString(1, category);
                    var resultSet = statement.executeQuery();
                    while (resultSet.next()) {
                        var course = new Course();
                        course.setIdCourse(resultSet.getInt("id"));
                        course.setName(resultSet.getString("name"));
                        course.setCategory(Category.valueOf(resultSet.getString("category")));
                        courses.add(course);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return courses;
    }
    }
