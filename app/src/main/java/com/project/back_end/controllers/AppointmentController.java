package com.project.back_end.controllers;

import com.project.back_end.models.Appointment;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("${api.path}appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final Service service;

    public AppointmentController(AppointmentService appointmentService, Service service) {
        this.appointmentService = appointmentService;
        this.service = service;
    }

    @GetMapping("/{date}/{patientName}/{token}")
    public ResponseEntity<Map<String, Object>> getAppointments(
            @PathVariable String date,
            @PathVariable String patientName,
            @PathVariable String token
    ) {
        var tokenRes = service.validateToken(token, "doctor");
        if (!tokenRes.getBody().isEmpty()) {
            return ResponseEntity.status(tokenRes.getStatusCode()).body(Map.of("message", tokenRes.getBody().get("message")));
        }
        LocalDate d = LocalDate.parse(date);
        return ResponseEntity.ok(appointmentService.getAppointment(patientName, d, token));
    }

    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> bookAppointment(@Valid @RequestBody Appointment appointment, @PathVariable String token) {
        var tokenRes = service.validateToken(token, "patient");
        if (!tokenRes.getBody().isEmpty()) return ResponseEntity.status(tokenRes.getStatusCode()).body(tokenRes.getBody());

        int valid = service.validateAppointment(appointment);
        if (valid == -1) return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Invalid doctor id"));
        if (valid == 0) return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Appointment slot unavailable"));

        int booked = appointmentService.bookAppointment(appointment);
        if (booked == 1) return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Appointment booked"));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Internal server error"));
    }

    @PutMapping("/{token}")
    public ResponseEntity<Map<String, String>> updateAppointment(@Valid @RequestBody Appointment appointment, @PathVariable String token) {
        var tokenRes = service.validateToken(token, "patient");
        if (!tokenRes.getBody().isEmpty()) return ResponseEntity.status(tokenRes.getStatusCode()).body(tokenRes.getBody());

        return appointmentService.updateAppointment(appointment);
    }

    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, String>> cancelAppointment(@PathVariable long id, @PathVariable String token) {
        var tokenRes = service.validateToken(token, "patient");
        if (!tokenRes.getBody().isEmpty()) return ResponseEntity.status(tokenRes.getStatusCode()).body(tokenRes.getBody());

        return appointmentService.cancelAppointment(id, token);
    }
}
//public class AppointmentController {

// 1. Set Up the Controller Class:
//    - Annotate the class with `@RestController` to define it as a REST API controller.
//    - Use `@RequestMapping("/appointments")` to set a base path for all appointment-related endpoints.
//    - This centralizes all routes that deal with booking, updating, retrieving, and canceling appointments.


// 2. Autowire Dependencies:
//    - Inject `AppointmentService` for handling the business logic specific to appointments.
//    - Inject the general `Service` class, which provides shared functionality like token validation and appointment checks.


// 3. Define the `getAppointments` Method:
//    - Handles HTTP GET requests to fetch appointments based on date and patient name.
//    - Takes the appointment date, patient name, and token as path variables.
//    - First validates the token for role `"doctor"` using the `Service`.
//    - If the token is valid, returns appointments for the given patient on the specified date.
//    - If the token is invalid or expired, responds with the appropriate message and status code.


// 4. Define the `bookAppointment` Method:
//    - Handles HTTP POST requests to create a new appointment.
//    - Accepts a validated `Appointment` object in the request body and a token as a path variable.
//    - Validates the token for the `"patient"` role.
//    - Uses service logic to validate the appointment data (e.g., check for doctor availability and time conflicts).
//    - Returns success if booked, or appropriate error messages if the doctor ID is invalid or the slot is already taken.


// 5. Define the `updateAppointment` Method:
//    - Handles HTTP PUT requests to modify an existing appointment.
//    - Accepts a validated `Appointment` object and a token as input.
//    - Validates the token for `"patient"` role.
//    - Delegates the update logic to the `AppointmentService`.
//    - Returns an appropriate success or failure response based on the update result.


// 6. Define the `cancelAppointment` Method:
//    - Handles HTTP DELETE requests to cancel a specific appointment.
//    - Accepts the appointment ID and a token as path variables.
//    - Validates the token for `"patient"` role to ensure the user is authorized to cancel the appointment.
//    - Calls `AppointmentService` to handle the cancellation process and returns the result.


//}
