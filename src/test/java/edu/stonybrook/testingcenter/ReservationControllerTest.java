package edu.stonybrook.testingcenter;

import edu.stonybrook.testingcenter.model.Reservation;
import edu.stonybrook.testingcenter.repository.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ReservationControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    public void testCreateAndRetrieveReservation() {
        // Create a new reservation instance
        Reservation res = new Reservation();
        res.setFirstName("Test");
        res.setLastName("User");
        res.setStudentEmail("test.user@example.com");
        res.setStudentId("999999");
        res.setReservationType("non-course");
        res.setStartTime(LocalDateTime.of(2025, 4, 15, 9, 0));
        res.setEndTime(LocalDateTime.of(2025, 4, 15, 9, 45));
        res.setStationNumber(1);
        // The default status "pending" will be set automatically.

        // Create HTTP headers and request entity for JSON content.
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Reservation> request = new HttpEntity<>(res, headers);

        // Send POST request to create the reservation.
        ResponseEntity<Reservation> postResponse = restTemplate.postForEntity("/api/reservations", request, Reservation.class);
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Reservation created = postResponse.getBody();
        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();

        // Retrieve the reservation using GET request.
        ResponseEntity<Reservation> getResponse = restTemplate.getForEntity("/api/reservations/" + created.getId(), Reservation.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Reservation retrieved = getResponse.getBody();
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getFirstName()).isEqualTo("Test");
        assertThat(retrieved.getStatus()).isEqualTo("pending");
    }
}
