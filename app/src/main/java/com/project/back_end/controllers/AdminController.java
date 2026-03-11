package com.project.back_end.controllers;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import com.project.back_end.models.Admin;
import com.project.back_end.services.Service;
//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController // 1. Mark as REST controller
@RequestMapping("${api.path}" + "admin")
public class AdminController {
    private final Service service;
    // 2. Constructor-based injection
    @Autowired
    public AdminController(Service service) {
        this.service = service;
    }

    // 3. Admin login endpoint
    //@PostMapping("/login")
    //public ResponseEntity<?> adminLogin(@RequestBody Admin admin) {
    //    return service.validateAdmin(admin.getUsername(), admin.getPassword());
    //}
    @PostMapping
    public ResponseEntity<Map<String, String>> adminLogin(@RequestBody Admin admin) {
        return service.validateAdmin(admin);
    }

    @GetMapping("/dashboard/{token}")
    public String adminDashboard(@PathVariable String token) {
        Map<String, String> map=service.validateToken(token,"admin").getBody();
        if(map==null)
        {
            return "admin/adminDashboard";
        }
        return "redirect:http://localhost:8080/";    
    }
}



//public class AdminController {

// 1. Set Up the Controller Class:
//    - Annotate the class with `@RestController` to indicate that it's a REST controller, used to handle web requests and return JSON responses.
//    - Use `@RequestMapping("${api.path}admin")` to define a base path for all endpoints in this controller.
//    - This allows the use of an external property (`api.path`) for flexible configuration of endpoint paths.


// 2. Autowire Service Dependency:
//    - Use constructor injection to autowire the `Service` class.
//    - The service handles core logic related to admin validation and token checking.
//    - This promotes cleaner code and separation of concerns between the controller and business logic layer.


// 3. Define the `adminLogin` Method:
//    - Handles HTTP POST requests for admin login functionality.
//    - Accepts an `Admin` object in the request body, which contains login credentials.
//    - Delegates authentication logic to the `validateAdmin` method in the service layer.
//    - Returns a `ResponseEntity` with a `Map` containing login status or messages.



//}

