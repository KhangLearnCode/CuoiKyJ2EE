package com.cuoiky.Nhom13.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class JobsPageController {
    
    @GetMapping("/jobs")
    public String jobsList() {
        return "jobs-new";  // Trỏ đến jobs-new.html
    }
    
    @GetMapping("/jobs/details")
    public String jobDetails() {
        return "job-details";
    }
    
    @GetMapping("/jobs/parts-management")
    public String partsManagement() {
        return "parts-management";
    }
    
    @GetMapping("/jobs/scanner")
    public String scanner() {
        return "scanner";
    }
    
    @GetMapping("/jobs/notifications")
    public String notifications() {
        return "notifications";
    }
    
    @GetMapping("/jobs/data-tools")
    public String dataTools() {
        return "data-tools";
    }
}
