package edu.stonybrook.testingcenter.controller;

import edu.stonybrook.testingcenter.model.Reservation;
import edu.stonybrook.testingcenter.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(origins = "http://localhost:3000") // Adjust if your front end runs on a different origin.
public class ReservationController {

    @Autowired
    private ReservationRepository reservationRepository;

    @GetMapping("/test")
    public String testEndpoint() {
        return "Reservation Controller is working!";
    }

    // Retrieve reservations, optionally filtering by status and/or date
    @GetMapping
    public List<Reservation> getAllReservations(
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "date", required = false) String dateParam,
            @RequestParam(name = "excludeStaff", required = false, defaultValue = "false") boolean excludeStaff
    ) {
        // First, handle the existing filters:
        List<Reservation> results;

        // 1. If neither filter is provided, return all.
        if ((status == null || status.isEmpty()) && (dateParam == null || dateParam.isEmpty())) {
            results = reservationRepository.findAll();
        }
        // 2. If only status is provided
        else if (dateParam == null || dateParam.isEmpty()) {
            results = reservationRepository.findByStatus(status);
        }
        else {
            // We have a date param
            LocalDate filterDate = LocalDate.parse(dateParam);
            LocalDateTime startOfDay = filterDate.atStartOfDay();
            LocalDateTime endOfDay = filterDate.plusDays(1).atStartOfDay();
            if (status == null || status.isEmpty()) {
                // Filter only by date
                results = reservationRepository.findByStartTimeBetween(startOfDay, endOfDay);
            } else {
                // Filter by BOTH status AND date
                results = reservationRepository.findByStatusAndStartTimeBetween(status, startOfDay, endOfDay);
            }
        }

        // **NEW**: If excludeStaff == true, filter out reservations with createdByStaff = true.
        if (excludeStaff) {
            List<Reservation> filtered = new ArrayList<>();
            for (Reservation r : results) {
                if (Boolean.FALSE.equals(r.getCreatedByStaff())) {
                    // i.e. createdByStaff is false or null
                    filtered.add(r);
                }
            }
            results = filtered;
        }

        return results;
    }

    // Retrieve reservations for a specific student by student ID
    @GetMapping("/student/{studentId}")
    public List<Reservation> getReservationsByStudent(@PathVariable String studentId) {
        return reservationRepository.findByStudentIdAndCreatedByStaffFalse(studentId);
    }

    // Retrieve all group reservations (created by staff)
    @GetMapping("/group")
    public List<Reservation> getGroupReservations() {
        return reservationRepository.findByCreatedByStaffTrue();
    }

    // Retrieve a specific reservation by its ID
    @GetMapping("/{id}")
    public Reservation getReservation(@PathVariable Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found with id " + id));
    }

    // Create a new reservation (single reservation) with time validation
    @PostMapping
    public Reservation createReservation(@RequestBody Reservation reservation) {
        validateReservationTimes(reservation);
        return reservationRepository.save(reservation);
    }

    // Update an existing reservation (with time validation)
    @PutMapping("/{id}")
    public Reservation updateReservation(@PathVariable Long id, @RequestBody Reservation updatedReservation) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found with id " + id));

        validateReservationTimes(updatedReservation);

        reservation.setFirstName(updatedReservation.getFirstName());
        reservation.setLastName(updatedReservation.getLastName());
        reservation.setStudentEmail(updatedReservation.getStudentEmail());
        reservation.setStudentId(updatedReservation.getStudentId());
        reservation.setReservationType(updatedReservation.getReservationType());
        reservation.setProfessorName(updatedReservation.getProfessorName());
        reservation.setProfessorEmail(updatedReservation.getProfessorEmail());
        reservation.setCourseName(updatedReservation.getCourseName());
        reservation.setExamType(updatedReservation.getExamType());
        reservation.setExamFormat(updatedReservation.getExamFormat());
        reservation.setStartTime(updatedReservation.getStartTime());
        reservation.setEndTime(updatedReservation.getEndTime());
        reservation.setStationNumber(updatedReservation.getStationNumber());
        reservation.setRemarks(updatedReservation.getRemarks());

        return reservationRepository.save(reservation);
    }

    // Delete a reservation by its ID
    @DeleteMapping("/{id}")
    public void deleteReservation(@PathVariable Long id) {
        reservationRepository.deleteById(id);
    }

    // Update the reservation status (approval/decline) along with optional remarks
    @PutMapping("/{id}/status")
    public Reservation updateReservationStatus(@PathVariable Long id, @RequestBody StatusUpdateRequest statusUpdateRequest) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found with id " + id));

        reservation.setStatus(statusUpdateRequest.getStatus());
        if (statusUpdateRequest.getRemarks() != null) {
            reservation.setRemarks(statusUpdateRequest.getRemarks());
        }
        return reservationRepository.save(reservation);
    }

    // Endpoint to check available stations for a given time range.
    @GetMapping("/available-stations")
    public List<Integer> getAvailableStations(@RequestParam String startTime, @RequestParam String endTime) {
        LocalDateTime start = LocalDateTime.parse(startTime);
        LocalDateTime end = LocalDateTime.parse(endTime);
        List<Integer> reservedStations = reservationRepository.findReservedStationNumbers(start, end);
        List<Integer> allStations = new ArrayList<>();
        for (int i = 1; i <= 65; i++) {
            allStations.add(i);
        }
        allStations.removeAll(reservedStations);
        return allStations;
    }

    // New endpoint to handle group reservations.
    @PostMapping("/group")
    public List<Reservation> createGroupReservation(@RequestBody GroupReservationRequest groupRequest) {
        // Validate that the number of students matches the counts provided.
        if (groupRequest.getNumberOfStudents() != groupRequest.getStudentEmails().size() ||
                groupRequest.getNumberOfStudents() != groupRequest.getStudentNames().size() ||
                groupRequest.getNumberOfStudents() != groupRequest.getStudentIds().size() ||
                groupRequest.getNumberOfStudents() != groupRequest.getSelectedStations().size()) {
            throw new RuntimeException("Number of students must match the count of student emails, names, ids, and selected stations.");
        }

        // Combine date and time to form ISO 8601 strings.
        String formattedStartTime = groupRequest.getReservationDate() + "T" + groupRequest.getStartTime() + ":00";
        String formattedEndTime = groupRequest.getReservationDate() + "T" + groupRequest.getEndTime() + ":00";

        List<Reservation> createdReservations = new ArrayList<>();

        // Create an individual Reservation for each student.
        for (int i = 0; i < groupRequest.getNumberOfStudents(); i++) {
            Reservation reservation = new Reservation();
            // Set student-specific fields:
            reservation.setFirstName(groupRequest.getStudentNames().get(i));
            reservation.setLastName(""); // or split if necessary
            reservation.setStudentEmail(groupRequest.getStudentEmails().get(i));
            reservation.setStudentId(groupRequest.getStudentIds().get(i));
            reservation.setReservationType(groupRequest.getReservationType());
            reservation.setStartTime(LocalDateTime.parse(formattedStartTime));
            reservation.setEndTime(LocalDateTime.parse(formattedEndTime));
            reservation.setStationNumber(groupRequest.getSelectedStations().get(i));
            reservation.setRemarks(groupRequest.getRemarks());

            // If course reservation, set course details
            if ("course".equalsIgnoreCase(groupRequest.getReservationType())) {
                reservation.setProfessorName(groupRequest.getProfessorName());
                reservation.setProfessorEmail(groupRequest.getProfessorEmail());
                reservation.setCourseName(groupRequest.getCourseName());
                reservation.setExamType(groupRequest.getExamType());
                reservation.setExamFormat(groupRequest.getExamFormat());
            }
            // **Important:** Mark this as created by staff.
            reservation.setCreatedByStaff(true);

            validateReservationTimes(reservation);
            Reservation saved = reservationRepository.save(reservation);
            createdReservations.add(saved);
        }

        return createdReservations;
    }

    // Private helper method to validate that the reservation times follow the 15-minute rule.
    private void validateReservationTimes(Reservation reservation) {
        LocalDateTime start = reservation.getStartTime();
        LocalDateTime end = reservation.getEndTime();

        if (start == null || end == null) {
            throw new RuntimeException("Start time and end time must not be null");
        }

        if (start.getMinute() % 15 != 0 || end.getMinute() % 15 != 0) {
            throw new RuntimeException("Both start time and end time must be on a 15-minute mark");
        }

        long durationMinutes = Duration.between(start, end).toMinutes();
        if (durationMinutes <= 0 || durationMinutes % 15 != 0) {
            throw new RuntimeException("Reservation duration must be a positive multiple of 15 minutes");
        }

        int startHour = start.getHour();
        int endHour = end.getHour();
        if (startHour < 9) {
            throw new RuntimeException("Start time cannot be before 9:00 AM");
        }
        if (endHour > 20 || (endHour == 20 && end.getMinute() > 0)) {
            throw new RuntimeException("End time cannot be later than 8:00 PM");
        }
    }

    // DTO class for updating the status
    public static class StatusUpdateRequest {
        private String status;
        private String remarks;

        public String getStatus() {
            return status;
        }
        public void setStatus(String status) {
            this.status = status;
        }
        public String getRemarks() {
            return remarks;
        }
        public void setRemarks(String remarks) {
            this.remarks = remarks;
        }
    }

    // DTO class for group reservations
    public static class GroupReservationRequest {
        private int numberOfStudents;
        private List<String> studentEmails;
        private List<String> studentNames;
        private List<String> studentIds;
        private String reservationType;
        private String reservationDate; // Format: YYYY-MM-DD
        private String startTime;       // Format: HH:mm (e.g., "09:00")
        private String endTime;         // Format: HH:mm (e.g., "09:45")
        private String remarks;
        private List<Integer> selectedStations;
        // Course related fields (if applicable)
        private String professorName;
        private String professorEmail;
        private String courseName;
        private String examType;
        private String examFormat;

        // Getters and setters are provided below.

        public int getNumberOfStudents() {
            return numberOfStudents;
        }
        public void setNumberOfStudents(int numberOfStudents) {
            this.numberOfStudents = numberOfStudents;
        }
        public List<String> getStudentEmails() {
            return studentEmails;
        }
        public void setStudentEmails(List<String> studentEmails) {
            this.studentEmails = studentEmails;
        }
        public List<String> getStudentNames() {
            return studentNames;
        }
        public void setStudentNames(List<String> studentNames) {
            this.studentNames = studentNames;
        }
        public List<String> getStudentIds() {
            return studentIds;
        }
        public void setStudentIds(List<String> studentIds) {
            this.studentIds = studentIds;
        }
        public String getReservationType() {
            return reservationType;
        }
        public void setReservationType(String reservationType) {
            this.reservationType = reservationType;
        }
        public String getReservationDate() {
            return reservationDate;
        }
        public void setReservationDate(String reservationDate) {
            this.reservationDate = reservationDate;
        }
        public String getStartTime() {
            return startTime;
        }
        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }
        public String getEndTime() {
            return endTime;
        }
        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }
        public String getRemarks() {
            return remarks;
        }
        public void setRemarks(String remarks) {
            this.remarks = remarks;
        }
        public List<Integer> getSelectedStations() {
            return selectedStations;
        }
        public void setSelectedStations(List<Integer> selectedStations) {
            this.selectedStations = selectedStations;
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
    }
}
