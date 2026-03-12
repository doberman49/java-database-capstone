package com.project.back_end.services;

import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@org.springframework.stereotype.Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final TokenService tokenService;
    private final Service service;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              PatientRepository patientRepository,
                              DoctorRepository doctorRepository,
                              TokenService tokenService,
                              Service service) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.tokenService = tokenService;
        this.service = service;
    }

    @Transactional
    public int bookAppointment(Appointment appointment) {
        try {
            appointmentRepository.save(appointment);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    @Transactional
    public ResponseEntity<Map<String, String>> updateAppointment(Appointment appointment) {
        try {
            Map<String, String> res = new HashMap<>();
            if (appointment.getId() == null) {
                res.put("message", "ID de cita no localizada");
                return ResponseEntity.badRequest().body(res);
            }

            Optional<Appointment> existingOpt = appointmentRepository.findById(appointment.getId());
            if (existingOpt.isEmpty()) {
                res.put("message", "ID de cita no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
            }

            Appointment existing = existingOpt.get();

            // Ensure same patient
            if (appointment.getPatient() != null && appointment.getPatient().getId() != null) {
                if (!existing.getPatient().getId().equals(appointment.getPatient().getId())) {
                    res.put("message", "Unauthorized");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
                }
            }

            // Validate appointment slot
            int valid = service.validateAppointment(appointment);
            if (valid == -1) {
                res.put("message", "ID de doctor invalido");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(res);
            }
            if (valid == 0) {
                res.put("message", "Horario de cita no disponible");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(res);
            }

            existing.setAppointmentTime(appointment.getAppointmentTime());
            existing.setStatus(appointment.getStatus());
            existing.setDoctor(appointment.getDoctor());
            existing.setPatient(appointment.getPatient());
            appointmentRepository.save(existing);
            res.put("message", "Cita actualizada");
            return ResponseEntity.ok(res);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error interno en servidor"));
        }
    }

    @Transactional
    public ResponseEntity<Map<String, String>> cancelAppointment(long id, String token) {
        try {
            Map<String, String> res = new HashMap<>();
            Optional<Appointment> existingOpt = appointmentRepository.findById(id);
            if (existingOpt.isEmpty()) {
                res.put("message", "Cita no localizada");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
            }
            Appointment appt = existingOpt.get();
            String email = tokenService.extractIdentifier(token);
            Patient p = patientRepository.findByEmail(email);
            if (p == null || !p.getId().equals(appt.getPatient().getId())) {
                res.put("message", "Unauthorized");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
            }
            appointmentRepository.delete(appt);
            res.put("message", "Cita ha sido cancelada");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error interno en servidor"));
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAppointment(String pname, LocalDate date, String token) {
        String doctorEmail = tokenService.extractIdentifier(token);
        Doctor doc = doctorRepository.findByEmail(doctorEmail);
        if (doc == null) return Map.of("appointments", List.of());
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay().minusNanos(1);
        List<Appointment> appointments;
        if (pname == null || pname.isBlank() || "null".equalsIgnoreCase(pname)) {
            appointments = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(doc.getId(), start, end);
        } else {
            appointments = appointmentRepository.findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
                    doc.getId(), pname, start, end
            );
        }
        return Map.of("appointments", appointments);
    }

    @Transactional
    public void changeStatus(int status, long id) {
        appointmentRepository.updateStatus(status, id);
    }
}
//public class AppointmentService {
// 1. **Add @Service Annotation**:
//    - To indicate that this class is a service layer class for handling business logic.
//    - The `@Service` annotation should be added before the class declaration to mark it as a Spring service component.
//    - Instruction: Add `@Service` above the class definition.

// 2. **Constructor Injection for Dependencies**:
//    - The `AppointmentService` class requires several dependencies like `AppointmentRepository`, `Service`, `TokenService`, `PatientRepository`, and `DoctorRepository`.
//    - These dependencies should be injected through the constructor.
//    - Instruction: Ensure constructor injection is used for proper dependency management in Spring.

// 3. **Add @Transactional Annotation for Methods that Modify Database**:
//    - The methods that modify or update the database should be annotated with `@Transactional` to ensure atomicity and consistency of the operations.
//    - Instruction: Add the `@Transactional` annotation above methods that interact with the database, especially those modifying data.

// 4. **Book Appointment Method**:
//    - Responsible for saving the new appointment to the database.
//    - If the save operation fails, it returns `0`; otherwise, it returns `1`.
//    - Instruction: Ensure that the method handles any exceptions and returns an appropriate result code.

// 5. **Update Appointment Method**:
//    - This method is used to update an existing appointment based on its ID.
//    - It validates whether the patient ID matches, checks if the appointment is available for updating, and ensures that the doctor is available at the specified time.
//    - If the update is successful, it saves the appointment; otherwise, it returns an appropriate error message.
//    - Instruction: Ensure proper validation and error handling is included for appointment updates.

// 6. **Cancel Appointment Method**:
//    - This method cancels an appointment by deleting it from the database.
//    - It ensures the patient who owns the appointment is trying to cancel it and handles possible errors.
//    - Instruction: Make sure that the method checks for the patient ID match before deleting the appointment.

// 7. **Get Appointments Method**:
//    - This method retrieves a list of appointments for a specific doctor on a particular day, optionally filtered by the patient's name.
//    - It uses `@Transactional` to ensure that database operations are consistent and handled in a single transaction.
//    - Instruction: Ensure the correct use of transaction boundaries, especially when querying the database for appointments.

// 8. **Change Status Method**:
//    - This method updates the status of an appointment by changing its value in the database.
//    - It should be annotated with `@Transactional` to ensure the operation is executed in a single transaction.
//    - Instruction: Add `@Transactional` before this method to ensure atomicity when updating appointment status.


//}
