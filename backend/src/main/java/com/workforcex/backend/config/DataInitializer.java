package com.workforcex.backend.config;

import com.workforcex.backend.entity.*;
import com.workforcex.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds 200 realistic dummy jobs across 10 employer accounts on every startup.
 * Only runs when the jobs table is empty — safe to leave on during development.
 */
@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final EmployerProfileRepository employerProfileRepository;
    private final JobRepository jobRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (jobRepository.count() > 0) {
            log.info("DataInitializer: jobs already exist, skipping seed.");
            return;
        }
        log.info("DataInitializer: seeding 200 dummy jobs...");
        createJobs();
        log.info("DataInitializer: seeding complete.");
    }

    private void createJobs() {
        // 10 employer accounts
        String[][] employers = {
            {"9800000001", "Securitas India Pvt Ltd", "Rajesh Mehta"},
            {"9800000002", "G4S Security Solutions", "Priya Shah"},
            {"9800000003", "TeamLease Services", "Amit Verma"},
            {"9800000004", "Quess Corp Limited", "Sunita Rao"},
            {"9800000005", "ISS Facility Services", "Deepak Nair"},
            {"9800000006", "Sodexo India", "Meera Pillai"},
            {"9800000007", "JLL India", "Vikram Singh"},
            {"9800000008", "CBRE India", "Anita Sharma"},
            {"9800000009", "Transguard Group", "Rohit Gupta"},
            {"9800000010", "Allied Universal India", "Kavita Joshi"}
        };

        String[][] jobTemplates = {
            // title, skills, experience, location, salaryMin, salaryMax, openPositions, description
            {"Security Guard", "security,patrolling,surveillance", "1", "Mumbai,Thane,Navi Mumbai", "14000", "16000", "10", "Day/night shift security guard for residential/commercial complex."},
            {"Senior Security Guard", "security,patrolling,leadership", "3", "Delhi,Noida,Gurgaon", "18000", "22000", "5", "Senior guard to manage a team of 3-4 guards."},
            {"Security Supervisor", "security,supervision,reporting", "5", "Bangalore,Mysore", "25000", "30000", "3", "Oversee security operations across multiple sites."},
            {"CCTV Operator", "cctv,surveillance,monitoring", "1", "Hyderabad,Secunderabad", "15000", "18000", "8", "Monitor CCTV cameras and report incidents."},
            {"Access Control Operator", "security,access-control,data-entry", "1", "Chennai,Coimbatore", "14000", "17000", "6", "Manage entry/exit records and visitor management."},
            {"Driver (Car)", "driving,navigation,car", "2", "Mumbai,Pune", "16000", "20000", "15", "Personal/corporate car driver. Must have valid LMV license."},
            {"Driver (Truck/HCV)", "driving,truck,hcv,logistics", "3", "Delhi,Jaipur,Lucknow", "20000", "28000", "20", "Heavy vehicle driver for logistics company."},
            {"Delivery Boy (2-Wheeler)", "driving,bike,delivery", "0", "Bangalore,Chennai,Hyderabad", "12000", "15000", "50", "Last-mile delivery on motorcycle. Own bike preferred."},
            {"Chauffeur", "driving,car,english-communication", "3", "Mumbai,Delhi,Bangalore", "22000", "30000", "8", "Executive chauffeur for corporate clients."},
            {"Auto/Cab Driver", "driving,auto,cab,navigation", "1", "Hyderabad,Bangalore,Pune", "15000", "20000", "25", "Drive company vehicle for employee transport."},
            {"Housekeeping Supervisor", "housekeeping,supervision,chemicals", "3", "Bangalore,Chennai", "18000", "22000", "4", "Supervise housekeeping staff in corporate office."},
            {"Housekeeping Staff", "housekeeping,cleaning,mopping", "0", "Mumbai,Delhi,Hyderabad", "10000", "13000", "30", "Cleaning and maintenance of commercial premises."},
            {"Facility Manager", "facility-management,maintenance,vendor-management", "5", "Delhi,Gurgaon,Noida", "35000", "45000", "2", "End-to-end facility management for corporate campus."},
            {"Electrician", "electrical,wiring,maintenance", "2", "Pune,Mumbai,Nashik", "18000", "25000", "12", "Electrical maintenance and minor repairs."},
            {"Plumber", "plumbing,pipe-fitting,maintenance", "2", "Bangalore,Mysore,Mangalore", "16000", "22000", "10", "Plumbing maintenance and installation."},
            {"Carpenter", "carpentry,woodwork,furniture", "3", "Chennai,Madurai,Coimbatore", "18000", "24000", "8", "Furniture fabrication and repair."},
            {"Painter", "painting,wall-painting,finishing", "2", "Hyderabad,Vijayawada,Vizag", "16000", "20000", "15", "Interior and exterior painting work."},
            {"AC Technician", "ac-repair,hvac,refrigeration", "2", "Delhi,Noida,Faridabad", "18000", "25000", "10", "AC service and repair for residential/commercial."},
            {"Lift Technician", "lift,elevator,maintenance", "3", "Mumbai,Thane", "22000", "28000", "5", "Elevator installation, service and maintenance."},
            {"Generator Operator", "generator,dg-set,electrical", "2", "Chennai,Bangalore", "16000", "20000", "6", "Operate and maintain DG sets."},
            {"Peon/Office Boy", "office-assistance,filing,tea-coffee", "0", "Delhi,Mumbai,Kolkata", "8000", "11000", "20", "General office assistance, filing, and pantry duties."},
            {"Receptionist", "reception,communication,computer", "1", "Bangalore,Hyderabad,Chennai", "12000", "16000", "10", "Front desk management and visitor handling."},
            {"Data Entry Operator", "data-entry,computer,typing", "1", "Ahmedabad,Surat,Vadodara", "12000", "15000", "15", "Data entry and basic computer work."},
            {"Warehouse Associate", "warehouse,loading,unloading,inventory", "1", "Mumbai,Pune,Delhi", "14000", "18000", "25", "Pick, pack, and ship in warehouse operations."},
            {"Construction Worker (Mason)", "masonry,construction,concrete", "3", "Bangalore,Pune,Hyderabad", "18000", "25000", "20", "Brick laying, plastering and construction work."},
            {"Construction Helper", "construction,material-handling,labour", "0", "Delhi,Mumbai,Chennai", "10000", "14000", "40", "Support masons and other skilled workers on site."},
            {"Cook (South Indian)", "cooking,south-indian,kitchen", "2", "Bangalore,Chennai,Hyderabad", "14000", "18000", "8", "Prepare South Indian cuisine for corporate cafeteria."},
            {"Cook (North Indian)", "cooking,north-indian,kitchen,chapati", "2", "Delhi,Lucknow,Jaipur", "14000", "18000", "8", "Prepare North Indian food for staff canteen."},
            {"Hospital Attendant", "patient-care,hospital,assistance", "1", "Mumbai,Pune,Delhi", "12000", "16000", "12", "Assist patients with mobility and basic care in hospital."},
            {"Laundry Operator", "laundry,washing,ironing,linen", "1", "Bangalore,Chennai,Mumbai", "13000", "17000", "6", "Operate industrial laundry equipment in hotel/hospital."},
        };

        int jobIndex = 0;
        for (String[] emp : employers) {
            User user = userRepository.findByMobileNumber(emp[0]).orElseGet(() -> {
                User u = new User();
                u.setCountryCode("+91");
                u.setMobileNumber(emp[0]);
                u.setPassword(passwordEncoder.encode(emp[0]));
                u.setRole(Role.EMPLOYER);
                return userRepository.save(u);
            });

            EmployerProfile profile = employerProfileRepository.findByUserId(user.getId())
                    .orElseGet(() -> {
                        EmployerProfile p = new EmployerProfile();
                        p.setUser(user);
                        return p;
                    });
            profile.setCompanyName(emp[1]);
            profile.setContactPerson(emp[2]);
            employerProfileRepository.save(profile);

            // Each employer gets 20 jobs (10 employers × 20 = 200)
            for (int i = 0; i < 20; i++) {
                String[] t = jobTemplates[(jobIndex + i) % jobTemplates.length];
                Job job = new Job();
                job.setEmployer(user);
                job.setTitle(t[0]);
                job.setSkillsRequired(t[1]);
                job.setExperienceRequired(Integer.parseInt(t[2]));
                job.setLocation(t[3]);
                job.setSalaryMin(Double.parseDouble(t[4]));
                job.setSalaryMax(Double.parseDouble(t[5]));
                job.setOpenPositions(Integer.parseInt(t[6]));
                job.setDescription(t[7]);
                jobRepository.save(job);
            }
            jobIndex += 3;
        }
    }
}
