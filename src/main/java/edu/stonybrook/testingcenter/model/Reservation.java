package edu.stonybrook.testingcenter.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Student Details
    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "student_email")
    private String studentEmail;

    @Column(name = "student_id")
    private String studentId;

    // Reservation Type: "course" or "non-course"
    @Column(name = "reservation_type")
    private String reservationType;

    // For Course Related Reservations
    @Column(name = "professor_name")
    private String professorName;

    @Column(name = "professor_email")
    private String professorEmail;

    @Column(name = "course_name")
    private String courseName;

    @Column(name = "exam_type")
    private String examType; // e.g., midterm, quiz, etc.

    @Column(name = "exam_format")
    private String examFormat; // e.g., online, paper exam

    // Reservation Timing and Station
    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "station_number")
    private int stationNumber;

    // Remarks by Testing Center Staff
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    // Reservation status: "pending", "approved", or "declined"
    @Column(name = "status")
    private String status = "pending";

    // NEW FIELD: Indicates if the reservation was created by staff (e.g., a group reservation)
    @Column(name = "created_by_staff")
    private Boolean createdByStaff = false;

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getReservationType() {
        return reservationType;
    }

    public void setReservationType(String reservationType) {
        this.reservationType = reservationType;
    }

    public String getProfessorName() {
        return professorName;
    }

    public void setProfessorName(String professorName) {
        this.professorName = professorName;
    }

    public String getProfessorEmail() {
        return professorEmail;
    }

    public void setProfessorEmail(String professorEmail) {
        this.professorEmail = professorEmail;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getExamType() {
        return examType;
    }

    public void setExamType(String examType) {
        this.examType = examType;
    }

    public String getExamFormat() {
        return examFormat;
    }

    public void setExamFormat(String examFormat) {
        this.examFormat = examFormat;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public int getStationNumber() {
        return stationNumber;
    }

    public void setStationNumber(int stationNumber) {
        this.stationNumber = stationNumber;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getCreatedByStaff() {
        return createdByStaff;
    }

    public void setCreatedByStaff(Boolean createdByStaff) {
        this.createdByStaff = createdByStaff;
    }

}




/*
JPA stands for Java Persistence API. It is an object-relational mapping (ORM) framework that allows us to map Java objects to tables in a relational database. In other words, JPA provides a way to persist Java objects in a database using a set of annotations that define the mapping between Java classes and database tables.

JPA in Java is typically used in enterprise applications that require access to relational databases, such as web applications and enterprise resource planning (ERP) systems. JPA provides a standardized approach to working with databases that is portable across different ORM frameworks.
 */