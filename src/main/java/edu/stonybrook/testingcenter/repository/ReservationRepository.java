package edu.stonybrook.testingcenter.repository;

import edu.stonybrook.testingcenter.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // Existing methods...
    List<Reservation> findByStatus(String status);
    List<Reservation> findByStudentId(String studentId);
    List<Reservation> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
    List<Reservation> findByStatusAndStartTimeBetween(String status, LocalDateTime start, LocalDateTime end);

    @Query("SELECT r.stationNumber FROM Reservation r " +
            "WHERE r.startTime < :endTime AND r.endTime > :startTime " +
            "AND r.status IN ('pending', 'approved')")
    List<Integer> findReservedStationNumbers(@Param("startTime") LocalDateTime startTime,
                                             @Param("endTime") LocalDateTime endTime);

    // NEW: For student queries—exclude reservations created by staff
    List<Reservation> findByStudentIdAndCreatedByStaffFalse(String studentId);

    // NEW: For staff queries—return only reservations created by staff (group reservations)
    List<Reservation> findByCreatedByStaffTrue();
}

