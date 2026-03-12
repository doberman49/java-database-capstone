package com.project.back_end.services;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@org.springframework.stereotype.Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    public DoctorService(DoctorRepository doctorRepository,
                         AppointmentRepository appointmentRepository,
                         TokenService tokenService) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    public List<String> getDoctorAvailability(Long doctorId, LocalDate date) {
        Optional<Doctor> docOpt = doctorRepository.findById(doctorId);
        if (docOpt.isEmpty()) 
            return List.of();
        Doctor doctor = docOpt.get();
        List<String> baseSlots = doctor.getAvailableTimes() == null ? new ArrayList<>() : new ArrayList<>(doctor.getAvailableTimes());

        // remove booked slots for that date
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay().minusNanos(1);

        List<Appointment> booked = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(doctorId, start, end);

        Set<String> bookedStartTimes = new HashSet<>();
        for (Appointment a : booked) {
            bookedStartTimes.add(a.getAppointmentTime().toLocalTime().toString()); // "09:00"
        }

        List<String> available = new ArrayList<>();
        for (String slot : baseSlots) {
            // slot format "09:00-10:00" OR "09:00 -10:00"
            String normalized = slot.replace(" ", "");
            String startTime = normalized.split("-")[0];
            if (!bookedStartTimes.contains(startTime)) {
                available.add(normalized);
            }
        }
        return available;
    }

    public int saveDoctor(Doctor doctor) {
        try {
            Doctor existing = doctorRepository.findByEmail(doctor.getEmail());
            if (existing != null) 
                return -1;
            doctorRepository.save(doctor);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    public int updateDoctor(Doctor doctor) {
        try {
            if (doctor.getId() == null) return -1;
            Optional<Doctor> existing = doctorRepository.findById(doctor.getId());
            if (existing.isEmpty()) 
                return -1;
            Doctor d = existing.get();
            d.setName(doctor.getName());
            d.setEmail(doctor.getEmail());
            d.setPhone(doctor.getPhone());
            d.setPassword(doctor.getPassword());
            d.setSpecialty(doctor.getSpecialty());
            d.setAvailableTimes(doctor.getAvailableTimes());

            doctorRepository.save(d);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    public List<Doctor> getDoctors() {
        return doctorRepository.findAll();
    }

    public int deleteDoctor(long id) {
        try {
            Optional<Doctor> existing = doctorRepository.findById(id);
            if (existing.isEmpty()) 
                return -1;
            appointmentRepository.deleteAllByDoctorId(id);
            doctorRepository.deleteById(id);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    public ResponseEntity<Map<String, String>> validateDoctor(Login login) {
        try {
            Doctor d = doctorRepository.findByEmail(login.getIdentifier());
            if (d == null || !d.getPassword().equals(login.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Credenciales invalidas"));
            }
            String token = tokenService.generateToken(d.getEmail());
            return ResponseEntity.ok(Map.of("token", token));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Error interno en servidor"));
        }
    }

    public Map<String, Object> findDoctorByName(String name) {
        return Map.of("doctors", doctorRepository.findByNameLike(name));
    }

    public Map<String, Object> filterDoctorsByNameSpecilityandTime(String name, String specialty, String amOrPm) {
        List<Doctor> docs = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
        return Map.of("doctors", filterDoctorByTime(docs, amOrPm));
    }

    public Map<String, Object> filterDoctorByNameAndTime(String name, String amOrPm) {
        List<Doctor> docs = doctorRepository.findByNameLike(name);
        return Map.of("doctors", filterDoctorByTime(docs, amOrPm));
    }

    public Map<String, Object> filterDoctorByNameAndSpecility(String name, String specilty) {
        List<Doctor> docs = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specilty);
        return Map.of("doctors", docs);
    }

    public Map<String, Object> filterDoctorByTimeAndSpecility(String specilty, String amOrPm) {
        List<Doctor> docs = doctorRepository.findBySpecialtyIgnoreCase(specilty);
        return Map.of("doctors", filterDoctorByTime(docs, amOrPm));
    }

    public Map<String, Object> filterDoctorBySpecility(String specilty) {
        return Map.of("doctors", doctorRepository.findBySpecialtyIgnoreCase(specilty));
    }

    public Map<String, Object> filterDoctorsByTime(String amOrPm) {
        return Map.of("doctors", filterDoctorByTime(doctorRepository.findAll(), amOrPm));
    }

    private List<Doctor> filterDoctorByTime(List<Doctor> doctors, String amOrPm) {
        if (amOrPm == null || amOrPm.isBlank() || "null".equalsIgnoreCase(amOrPm)) return doctors;

        String target = amOrPm.trim().toUpperCase(Locale.ROOT);

        List<Doctor> filtered = new ArrayList<>();
        for (Doctor d : doctors) {
            List<String> times = d.getAvailableTimes() == null ? List.of() : d.getAvailableTimes();
            boolean match = times.stream().anyMatch(slot -> {
                String s = slot.replace(" ", "");
                // basic AM/PM inference from start hour
                String start = s.split("-")[0]; // "09:00"
                int hour = Integer.parseInt(start.split(":")[0]);
                boolean isAm = hour < 12;
                return ("AM".equals(target) && isAm) || ("PM".equals(target) && !isAm);
            });
            if (match) filtered.add(d);
        }
        return filtered;
    }
}
//public class DoctorService {

// 1. **Add @Service Annotation**:
//    - This class should be annotated with `@Service` to indicate that it is a service layer class.
//    - The `@Service` annotation marks this class as a Spring-managed bean for business logic.
//    - Instruction: Add `@Service` above the class declaration.

// 2. **Constructor Injection for Dependencies**:
//    - The `DoctorService` class depends on `DoctorRepository`, `AppointmentRepository`, and `TokenService`.
//    - These dependencies should be injected via the constructor for proper dependency management.
//    - Instruction: Ensure constructor injection is used for injecting dependencies into the service.

// 3. **Add @Transactional Annotation for Methods that Modify or Fetch Database Data**:
//    - Methods like `getDoctorAvailability`, `getDoctors`, `findDoctorByName`, `filterDoctorsBy*` should be annotated with `@Transactional`.
//    - The `@Transactional` annotation ensures that database operations are consistent and wrapped in a single transaction.
//    - Instruction: Add the `@Transactional` annotation above the methods that perform database operations or queries.

// 4. **getDoctorAvailability Method**:
//    - Retrieves the available time slots for a specific doctor on a particular date and filters out already booked slots.
//    - The method fetches all appointments for the doctor on the given date and calculates the availability by comparing against booked slots.
//    - Instruction: Ensure that the time slots are properly formatted and the available slots are correctly filtered.

// 5. **saveDoctor Method**:
//    - Used to save a new doctor record in the database after checking if a doctor with the same email already exists.
//    - If a doctor with the same email is found, it returns `-1` to indicate conflict; `1` for success, and `0` for internal errors.
//    - Instruction: Ensure that the method correctly handles conflicts and exceptions when saving a doctor.

// 6. **updateDoctor Method**:
//    - Updates an existing doctor's details in the database. If the doctor doesn't exist, it returns `-1`.
//    - Instruction: Make sure that the doctor exists before attempting to save the updated record and handle any errors properly.

// 7. **getDoctors Method**:
//    - Fetches all doctors from the database. It is marked with `@Transactional` to ensure that the collection is properly loaded.
//    - Instruction: Ensure that the collection is eagerly loaded, especially if dealing with lazy-loaded relationships (e.g., available times). 

// 8. **deleteDoctor Method**:
//    - Deletes a doctor from the system along with all appointments associated with that doctor.
//    - It first checks if the doctor exists. If not, it returns `-1`; otherwise, it deletes the doctor and their appointments.
//    - Instruction: Ensure the doctor and their appointments are deleted properly, with error handling for internal issues.

// 9. **validateDoctor Method**:
//    - Validates a doctor's login by checking if the email and password match an existing doctor record.
//    - It generates a token for the doctor if the login is successful, otherwise returns an error message.
//    - Instruction: Make sure to handle invalid login attempts and password mismatches properly with error responses.

// 10. **findDoctorByName Method**:
//    - Finds doctors based on partial name matching and returns the list of doctors with their available times.
//    - This method is annotated with `@Transactional` to ensure that the database query and data retrieval are properly managed within a transaction.
//    - Instruction: Ensure that available times are eagerly loaded for the doctors.


// 11. **filterDoctorsByNameSpecilityandTime Method**:
//    - Filters doctors based on their name, specialty, and availability during a specific time (AM/PM).
//    - The method fetches doctors matching the name and specialty criteria, then filters them based on their availability during the specified time period.
//    - Instruction: Ensure proper filtering based on both the name and specialty as well as the specified time period.

// 12. **filterDoctorByTime Method**:
//    - Filters a list of doctors based on whether their available times match the specified time period (AM/PM).
//    - This method processes a list of doctors and their available times to return those that fit the time criteria.
//    - Instruction: Ensure that the time filtering logic correctly handles both AM and PM time slots and edge cases.


// 13. **filterDoctorByNameAndTime Method**:
//    - Filters doctors based on their name and the specified time period (AM/PM).
//    - Fetches doctors based on partial name matching and filters the results to include only those available during the specified time period.
//    - Instruction: Ensure that the method correctly filters doctors based on the given name and time of day (AM/PM).

// 14. **filterDoctorByNameAndSpecility Method**:
//    - Filters doctors by name and specialty.
//    - It ensures that the resulting list of doctors matches both the name (case-insensitive) and the specified specialty.
//    - Instruction: Ensure that both name and specialty are considered when filtering doctors.


// 15. **filterDoctorByTimeAndSpecility Method**:
//    - Filters doctors based on their specialty and availability during a specific time period (AM/PM).
//    - Fetches doctors based on the specified specialty and filters them based on their available time slots for AM/PM.
//    - Instruction: Ensure the time filtering is accurately applied based on the given specialty and time period (AM/PM).

// 16. **filterDoctorBySpecility Method**:
//    - Filters doctors based on their specialty.
//    - This method fetches all doctors matching the specified specialty and returns them.
//    - Instruction: Make sure the filtering logic works for case-insensitive specialty matching.

// 17. **filterDoctorsByTime Method**:
//    - Filters all doctors based on their availability during a specific time period (AM/PM).
//    - The method checks all doctors' available times and returns those available during the specified time period.
//    - Instruction: Ensure proper filtering logic to handle AM/PM time periods.

   
//}
