package com.auth.controller;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.auth.component.EmailByAdmin;
import com.auth.component.EmailHelper;
import com.auth.component.EmailOTPHelper;
import com.auth.component.StoreOTP;
import com.auth.entity.AdminAllDetail;
import com.auth.entity.AdminCurrentlyLogged;
import com.auth.entity.AdminDetail;
import com.auth.entity.CurrentUserWhoBooked;
import com.auth.entity.CurrentlyLogged;
import com.auth.entity.Feeback;
import com.auth.entity.FlightsAvailable;
import com.auth.entity.PassengerDetails;
import com.auth.entity.PassengersBooked;
import com.auth.entity.UserDetail;
import com.auth.repository.AdminCurrentlyLoggedRepository;
import com.auth.repository.AdminRepository;
import com.auth.repository.BookingsRepository;
import com.auth.repository.FeedbackRepository;
import com.auth.repository.LoggedInRepository;
import com.auth.repository.PassengerBookedRepository;
import com.auth.repository.PassengerRepository;
import com.auth.repository.UserRepository;
import com.auth.service.AdminService;
import com.auth.service.UserService;
import com.auth.component.ContactEmail;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class AdminController {
	
	@Autowired
	private UserRepository repo;
	
	@Autowired
	private AdminCurrentlyLoggedRepository adminLogRepo;
	
	@Autowired
	private LoggedInRepository logRepo;
	
	@Autowired
	private AdminRepository repoAdmin;
	
	@Autowired
	private PassengerRepository repoPass;
	
	@Autowired
	private BCryptPasswordEncoder bp;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private AdminService adminService;
	
	
	@Autowired
	private BookingsRepository repoBook;
	
	@Autowired
	private PassengerBookedRepository repoBookingsStore;
	
	@Autowired
	private FeedbackRepository feedRepo;
	
	
	
	
	
	@Autowired
    private EmailHelper emailHelper;
	
	@Autowired
	private EmailOTPHelper emailOTPHelper;
	
	@Autowired
	private EmailByAdmin emailByAdmin;
	
	@Autowired
	private StoreOTP otpStorage;
	
	
	
	
	@PostMapping("/admin_cancel_ticket")
	public String cancelBooking(
			@RequestParam (name="uniqueId") String uid,
			@RequestParam(name="arrival", required = false) String arrival, 
			@RequestParam(name = "departure",required=false) String departure,
			Model model,
			HttpSession session
			
			) {
		
		List<FlightsAvailable> flights = adminService.isViewAvailable(arrival, departure);
        System.out.println(flights);
        
        
        
       // model.addAttribute("Viewflights", flights);
        model.addAttribute("arrival", arrival);
        model.addAttribute("departure", departure);
        
        
      //now getting all details
        AdminCurrentlyLogged hereUser2 = adminLogRepo.findCurrentAdmin().get(0);
	     List<AdminAllDetail> user = adminService.findAllAdminDetails(hereUser2.getRegisterEmail());
	     
	     model.addAttribute("stored_email",user.get(0).getRegisterEmail());
	     model.addAttribute("stored_dob",user.get(0).getDob());
	     model.addAttribute("userName",user.get(0).getRegisterName());
		
     
	    
	    List<Integer> userIds2 = userService.findAllCurrentUserId(hereUser2.getRegisterEmail());
	    
	    
	    
	    List<PassengersBooked> allBookingsPassengers = userService.findAllPassengersBookings();
	    
	    // Group bookings by currentUserWhoBooked.id
	    Map<Integer, List<PassengersBooked>> bookingsByUserId = allBookingsPassengers.stream()
	            .collect(Collectors.groupingBy(passenger -> passenger.getCurrentUserWhoBooked().getId()));

	    
	    
	    model.addAttribute("previousBookings", bookingsByUserId);
	    
 // now use this userids for deleting all passenger list
		
		String flightName = userService.findFlightWithUserId(Integer.parseInt(uid));
		
		
	    
	    int answer = userService.deleteAllPassengers(Integer.parseInt(uid));
	    
	    System.out.println(answer);
	    
	    
	    // now finally delete this uid from currentUserWhoBooked;
	    
	    int deleted = userService.deleteUserBookId(Integer.parseInt(uid));
	    
	    
	    System.out.println(deleted);
	    
	    // Create CurrentUserWhoBooked object
		CurrentlyLogged hereUser = logRepo.select();
		System.out.print(hereUser);
		
        // now get the user id of this user by using his mail id currentUserWhoBooked 
	    
	    List<Integer> userIds = userService.findAllCurrentUserId(hereUser.getRegisterEmail());
	    
	    System.out.println(userIds);
	    
	    
	    // now use this userids for fetching all passenger list
	    
	    List<PassengersBooked> allBookingsPassengers2 = userService.findAllPassengers(userIds);
	    
	    // Group bookings by currentUserWhoBooked.id
	    Map<Integer, List<PassengersBooked>> bookingsByUserId2 = allBookingsPassengers2.stream()
	            .collect(Collectors.groupingBy(passenger -> passenger.getCurrentUserWhoBooked().getId()));

	    System.out.println(bookingsByUserId2);
	    
	    model.addAttribute("isCancelled",true);
	    
	    model.addAttribute("userName",hereUser.getRegisterName());
	    
	    
	    model.addAttribute("previousBookings", bookingsByUserId2);
	    
	    //now getting all details
	    
	     List<UserDetail> user2 = repo.findByRegisterEmail(hereUser.getRegisterEmail());
	     
	     model.addAttribute("stored_email",user2.get(0).getRegisterEmail());
	     model.addAttribute("stored_dob",user2.get(0).getDob());

	     model.addAttribute("message_of_deletion","Booking successfully removed.");  
	     
	     
	     // Now find all the email ids of the users who had done booking on this flight
	     List<String> emails = new ArrayList<>();
	     emails.add(hereUser.getRegisterEmail());

	     System.out.println("emails found => " + emails);

	     // Notify all recipients about the cancellation of their booking
	     emailByAdmin.sendEmail(emails, "Cancellation of Your Booking for Flight " + flightName,
	             "Hi,<br>We have successfully processed the cancellation of your booking for flight " + flightName + "."
	             + "<br>If you have any questions or need further assistance, please contact our support team."
	             + "<br><br>Thank you for choosing FlyhighHub.com.<br>Best regards,<br>FlyhighHub.com");

			
			return "admin_things";
    }


	@PostMapping("/update_details_of_admin_profile")
	public String updatingAdminDetails(
			@RequestParam(name="updated_name") String updated_name,
			@RequestParam(name="updated_dob") String updated_dob,
			
			HttpServletRequest request,
			Model model
			) {
		
		
        String currentAdminEmail = adminService.select();
		String currentDob = adminService.selectDob(currentAdminEmail);
		
		
		String mail=currentAdminEmail;
		//delete this data from database
		adminLogRepo.deleteAll();
		
		
		
		//updating currentlyLogged
		adminLogRepo.save(new AdminCurrentlyLogged(updated_name,mail));
		
		
		
		//updating admin detail
		adminService.updateAdminDetailFromLoginRepo(mail,updated_name);
		
		
		//updating admin all detail
		if(updated_dob != currentDob) {
			adminService.updateAdminDetailFromAllDetailRepo(mail,updated_name,updated_dob);
		}
		
		
		
		List<AdminCurrentlyLogged> hereUser = adminService.selectAll();

		if(hereUser == null) {
			 System.out.println("get request null hai: ");				
			 model.addAttribute("stored_email","Not Defined");
		     model.addAttribute("stored_dob","");
		     model.addAttribute("userName","Not Defined");
		     model.addAttribute("message","Please Log In First!");
            return "admin_things";

		}
		
		//now getting all details
	    
		List<AdminCurrentlyLogged> user = hereUser;
	     model.addAttribute("userName", user.get(0).getRegisterName());
	     model.addAttribute("stored_email", user.get(0).getRegisterEmail());
	     model.addAttribute("stored_dob", updated_dob);
	     
	     model.addAttribute("message_of_updation","Details updated successfully!");

		
		return "adminPage";
	}
	
	
	//list all flights from arrival to destination
		@PostMapping("/update_details_of_view_admin")
		public String saveAdminView(
				@RequestParam(name="updated_name") String updated_name,
				@RequestParam(name="updated_dob") String updated_dob,
				@RequestParam(name="arrival", required = false) String arrival, 
				@RequestParam(name = "departure",required=false) String departure,
				Model model,
				HttpSession session) {
			
			List<FlightsAvailable> flights = adminService.isViewAvailable(arrival, departure);
	        System.out.println(flights);
	        
	        
	        
	       // model.addAttribute("Viewflights", flights);
	        model.addAttribute("arrival", arrival);
	        model.addAttribute("departure", departure);
	        
	        
	      //now getting all details
	        AdminCurrentlyLogged hereUser = adminLogRepo.findCurrentAdmin().get(0);
		     List<AdminAllDetail> user = adminService.findAllAdminDetails(hereUser.getRegisterEmail());
		     
		     model.addAttribute("stored_email",user.get(0).getRegisterEmail());
		     model.addAttribute("stored_dob",user.get(0).getDob());
		     model.addAttribute("userName",user.get(0).getRegisterName());
			
	     
		    
		    List<Integer> userIds = userService.findAllCurrentUserId(hereUser.getRegisterEmail());
		    
		    
		    
		    List<PassengersBooked> allBookingsPassengers = userService.findAllPassengersBookings();
		    
		    // Group bookings by currentUserWhoBooked.id
		    Map<Integer, List<PassengersBooked>> bookingsByUserId = allBookingsPassengers.stream()
		            .collect(Collectors.groupingBy(passenger -> passenger.getCurrentUserWhoBooked().getId()));

		    
		    
		    model.addAttribute("previousBookings", bookingsByUserId);
		    
		    
		    String currentAdminEmail = adminService.select();
			String currentDob = adminService.selectDob(currentAdminEmail);
			
			
			String mail=currentAdminEmail;
			//delete this data from database
			adminLogRepo.deleteAll();
			
			
			
			//updating currentlyLogged
			adminLogRepo.save(new AdminCurrentlyLogged(updated_name,mail));
			
			
			
			//updating admin detail
			adminService.updateAdminDetailFromLoginRepo(mail,updated_name);
			
			
			//updating admin all detail
			if(updated_dob != currentDob) {
				adminService.updateAdminDetailFromAllDetailRepo(mail,updated_name,updated_dob);
			}
			
			
			
			List<AdminCurrentlyLogged> hereUser2 = adminService.selectAll();

			if(hereUser2 == null) {
				 System.out.println("get request null hai: ");				
				 model.addAttribute("stored_email","Not Defined");
			     model.addAttribute("stored_dob","");
			     model.addAttribute("userName","Not Defined");
			     model.addAttribute("message","Please Log In First!");
	            return "admin_things";

			}
			
			//now getting all details
		    
			List<AdminCurrentlyLogged> user2 = hereUser2;
		     model.addAttribute("userName", user2.get(0).getRegisterName());
		     model.addAttribute("stored_email", user2.get(0).getRegisterEmail());
		     model.addAttribute("stored_dob", updated_dob);
		     
		     model.addAttribute("message_of_updation","Details updated successfully!");

		    
			return "admin_things";
			
			
		}
	

	
	
	@PostMapping("/contact_support")
	public String storingFeedback(
			@RequestParam(name="page_name") String page,
			@RequestParam(name="contact_name") String name,
	        @RequestParam(name="contact_email") String email,
	        @RequestParam(name="contact_phone") String phone,
	        @RequestParam(name="contact_message") String message,
	        HttpServletRequest request, // Inject the HttpServletRequest object
	        RedirectAttributes redirectAttributes) {
	    Feeback feed = new Feeback(name, email, phone, message);
	    feedRepo.save(feed);
	    
	    ContactEmail emailer = new ContactEmail();
	    //want list of admins
	    
	    List<String> emails = adminService.allAdminsEmail();
	    emailer.sendEmail(emails, "Flyhighhub.com", message, email, name, phone);

	    redirectAttributes.addFlashAttribute("message", "Your Feedback is sent successfully");

	    // Get the referer URL
	    String referer = request.getHeader("referer");

	    // Redirect back to the referer URL
	    return "redirect:/"+page;
	}
	

	
	@PostMapping("/admin_view_delete_indirectly")
	public String deleteAdminViewIndirect(@RequestParam(name = "flightName") String flightName,
			@RequestParam(name = "arrival") String arrival,
			@RequestParam(name = "departure") String departure,
			Model model,
			RedirectAttributes redirectAttributes) {
		
		
		
		
		
		//now delete that flight with given name
		
		int ans=adminService.deleteFlight(flightName);
		
		System.out.println(flightName);
		System.out.println(arrival);
		System.out.println(departure);
		
		
		List<FlightsAvailable> flights = adminService.isViewAvailable(arrival, departure);
        System.out.println(flights);
        System.out.println("haaaaaaaa");
        
        
        model.addAttribute("Viewflights", flights);
        model.addAttribute("arrival", arrival);
        
        model.addAttribute("departure", departure);
        
       
		
		model.addAttribute("message_of_deletion","Flight Is Removed Successfully!");
		
		//now getting all details
        AdminCurrentlyLogged hereUser = adminLogRepo.findCurrentAdmin().get(0);
	     List<AdminAllDetail> user = adminService.findAllAdminDetails(hereUser.getRegisterEmail());
	     
	     model.addAttribute("stored_email",user.get(0).getRegisterEmail());
	     model.addAttribute("stored_dob",user.get(0).getDob());
	     model.addAttribute("userName",user.get(0).getRegisterName());
		
		
		return "admin_things";
	}
	
	@PostMapping("/admin_search_delete_indirectly")
	public String deleteAdminSearchIndirect(@RequestParam(name = "flightName") String flightName,
			@RequestParam(name = "arrival") String arrival,
			@RequestParam(name = "departure") String departure,
			@RequestParam(name = "departure_date") String departure_date,
			Model model,
			RedirectAttributes redirectAttributes) {
		
		
		
		
		
		//now delete that flight with given name
		
		int ans=adminService.deleteFlight(flightName);
		
		// i need to delete in user profile also about if that user has done ticket in this flight then that ticket gone
		
        // now use this userids for deleting all passenger list
	    
	    //before deleting from passengers return all userIds 
		
		List<Integer> userIdsWhoBooked = userService.findAllUserIdsSpecificToFlight(flightName);
		
		System.out.println("UserIDsBooked  ---> " + userIdsWhoBooked);
		
	    
	    int res = userService.deleteAllPassengersSpecificToFlight(flightName);
	    
	    System.out.println("deleteAllPassengersSpecificToFlight  ---> " + res);
	    
	    //now delete all currentUsersWhoBooked that flight
	    
	    int deletedUsers = userService.deletedBookingCurrentUserIds(userIdsWhoBooked);
	    
	    System.out.println("deletedBookingCurrentUserIds -----> " + deletedUsers);
	    
	    
	    System.out.println(flightName);
		
		
		System.out.println(" ---------------suyash--------------");
		System.out.println(arrival);
		System.out.println(departure);
		System.out.println(departure_date);
		
		model.addAttribute("message","Flight Is Removed Successfully!");
		
		List<FlightsAvailable> flights = adminService.isSearchAvailable(arrival, departure,departure_date);
        System.out.println(flights);
        
        
        model.addAttribute("Searchflights", flights);
        model.addAttribute("Viewflights", null);
        
        model.addAttribute("arrival", arrival); 
        
        model.addAttribute("departure", departure);
       
        model.addAttribute("departure_date", departure_date);
        
      //now getting all details
        AdminCurrentlyLogged hereUser = adminLogRepo.findCurrentAdmin().get(0);
	     List<AdminAllDetail> user = adminService.findAllAdminDetails(hereUser.getRegisterEmail());
	     
	     model.addAttribute("stored_email",user.get(0).getRegisterEmail());
	     model.addAttribute("stored_dob",user.get(0).getDob());
	     model.addAttribute("userName",user.get(0).getRegisterName());
        
        // now find all the email ids of the users who had done booking on this flight
    	List<String> emails = userService.findAllEmailIdsWithThisFlight(flightName.split(" ")[1]);
    	
    	System.out.println("emails found => "+emails);
    	
    	// Notify all recipients about the deletion of the flight
    	emailByAdmin.sendEmail(emails, "Deletion of Flight " + flightName,
    	        "Hi,<br>We regret to inform you that the flight " + flightName + " has been deleted from our schedule. We apologize for any inconvenience this may cause."
    	        + "<br>If you have any questions or need further assistance, please contact our support team."
    	        + "<br><br>Thank you for your understanding.<br>Best regards,<br>FlyhighHub.com");
		
		
		return "admin_things";
	}

	
	@PostMapping("/admin_delete")
	public String deleteAdmin(@RequestParam(name = "flightName") String flightName,
			RedirectAttributes redirectAttributes) {
		
		
		
		//search in database of adminFlight
		
		boolean isFlightFound = adminService.isFlightNameFound(flightName);
		
		if(isFlightFound == false) {
			System.out.println(flightName);
			redirectAttributes.addFlashAttribute("message","No Flight Found With Given Flight Name");
			return "redirect:/adminPage";
		}
		
		//now delete that flight with given name
		
		int ans=adminService.deleteFlight(flightName);
		
		
        // i need to delete in user profile also about if that user has done ticket in this flight then that ticket gone
		
        // now use this userids for deleting all passenger list
	    
	    //before deleting from passengers return all userIds 
		
		List<Integer> userIdsWhoBooked = userService.findAllUserIdsSpecificToFlight(flightName);
		
		System.out.println("UserIDsBooked  ---> " + userIdsWhoBooked);
		
	    
	    int res = userService.deleteAllPassengersSpecificToFlight(flightName);
	    
	    System.out.println("deleteAllPassengersSpecificToFlight  ---> " + res);
	    
	    //now delete all currentUsersWhoBooked that flight
	    
	    int deletedUsers = userService.deletedBookingCurrentUserIds(userIdsWhoBooked);
	    
	    System.out.println("deletedBookingCurrentUserIds -----> " + deletedUsers);
	    
	    
	    System.out.println(flightName);
		
		System.out.println(ans);
		
		redirectAttributes.addFlashAttribute("message","Flight Is Removed Successfully!");
		
		// now find all the email ids of the users who had done booking on this flight
    	List<String> emails = userService.findAllEmailIdsWithThisFlight(flightName.split(" ")[1]);
    	
    	System.out.println("emails found => "+emails);
    	
    	// Notify all recipients about the deletion of the flight
    	emailByAdmin.sendEmail(emails, "Deletion of Flight " + flightName,
    	        "Hi,<br>We regret to inform you that the flight " + flightName + " has been deleted from our schedule. We apologize for any inconvenience this may cause."
    	        + "<br>If you have any questions or need further assistance, please contact our support team."
    	        + "<br><br>Thank you for your understanding.<br>Best regards,<br>FlyhighHub.com");
		
		return "redirect:/adminPage";
	}
	
	@PostMapping("/admin_add")
	public String saveAdmin(@ModelAttribute FlightsAvailable admin, RedirectAttributes redirectAttributes,
			HttpSession session) {
		
		//finding is already exist
		
		boolean ans = adminService.isFlightNameFound(admin.getFlightName());
		
		if(ans) {
			redirectAttributes.addFlashAttribute("message","Flight Already Exist!");
			return "redirect:/adminPage";
		}
		repoAdmin.save(admin);
		redirectAttributes.addFlashAttribute("message","Changes Done Successfully");
		return "redirect:/adminPage";
		
		
	}
	
	@PostMapping("/admin_update")
	public String saveAdminUpdate(@RequestParam String flightName, @RequestParam String arrival, @RequestParam String departure,
			@RequestParam String seatsAvailable, @RequestParam String costPrice ,
			RedirectAttributes redirectAttributes,HttpSession session) {
		
        int cost = Integer.parseInt(costPrice);
        boolean result=adminService.isUpdateDone(flightName, arrival, departure, seatsAvailable, cost);
        
        
        
        
        if(result==false) {
        	redirectAttributes.addFlashAttribute("message","Error in updating the flight details");
        	return "redirect:/adminPage";
        }
        else {
        	
        	// now find all the email ids of the users who had done booking on this flight
        	List<String> emails = userService.findAllEmailIdsWithThisFlight(flightName.split(" ")[1]);
        	
        	System.out.println("emails found => "+emails);
        	
        	// send all of them an email of updating location
        	emailByAdmin.sendEmail(emails, "Updation in Schedule of Flight " + flightName,
                    "Hi,<br>We have updated the schedule of the flight. Sorry for the inconvenience. Boarding pass will be sent soon."
                    		+ "<br>If you have any questions or need further assistance, please contact our support team."
                    		+ "<br><br>Thank you for your understanding.<br>Best regards,<br>FlyhighHub.com");
        	
        	redirectAttributes.addFlashAttribute("message","Updated Successfully");
        	return "redirect:/adminPage";
        }
		
		
		
	}
	
	
	
	
	//list all flights from arrival to destination
	@PostMapping("/admin_view")
	public String saveAdminView(
			@RequestParam(name="arrival", required = false) String arrival, 
			@RequestParam(name = "departure",required=false) String departure,
			Model model,
			HttpSession session) {
		
		List<FlightsAvailable> flights = adminService.isViewAvailable(arrival, departure);
        System.out.println(flights);
        System.out.println("haaaaaaaa");
        
        
       // model.addAttribute("Viewflights", flights);
        model.addAttribute("arrival", arrival);
        model.addAttribute("departure", departure);
        
        
      //now getting all details
        AdminCurrentlyLogged hereUser = adminLogRepo.findCurrentAdmin().get(0);
	     List<AdminAllDetail> user = adminService.findAllAdminDetails(hereUser.getRegisterEmail());
	     
	     model.addAttribute("stored_email",user.get(0).getRegisterEmail());
	     model.addAttribute("stored_dob",user.get(0).getDob());
	     model.addAttribute("userName",user.get(0).getRegisterName());
		
     // 
        // now get the user id of this user by using his mail id currentUserWhoBooked 
	    
	    List<Integer> userIds = userService.findAllCurrentUserId(hereUser.getRegisterEmail());
	    
	    System.out.println(userIds);
	    
     // now use this userids for fetching all passenger list
	    
	    List<PassengersBooked> allBookingsPassengers = userService.findAllPassengersBookings();
	    
	    // Group bookings by currentUserWhoBooked.id
	    Map<Integer, List<PassengersBooked>> bookingsByUserId = allBookingsPassengers.stream()
	            .collect(Collectors.groupingBy(passenger -> passenger.getCurrentUserWhoBooked().getId()));

	    System.out.println(bookingsByUserId);
	    
	    model.addAttribute("previousBookings", bookingsByUserId);
	    
		return "admin_things";
		
		
	}
	
	@PostMapping("/admin_search")
	public String saveAdminSearch(@RequestParam String arrival, @RequestParam String departure,@RequestParam String departure_date, Model model, HttpSession session) {
		
		
		List<FlightsAvailable> flights = adminService.isSearchAvailable(arrival, departure,departure_date);
        System.out.println(flights);
        System.out.println("naaaaaaaa");
        
        
        model.addAttribute("Searchflights", flights);
        model.addAttribute("arrival", arrival);
        model.addAttribute("departure", departure);
        model.addAttribute("departure_date", departure_date);
      //now getting all details
        AdminCurrentlyLogged hereUser = adminLogRepo.findCurrentAdmin().get(0);
	     List<AdminAllDetail> user = adminService.findAllAdminDetails(hereUser.getRegisterEmail());
	     
	     model.addAttribute("stored_email",user.get(0).getRegisterEmail());
	     model.addAttribute("stored_dob",user.get(0).getDob());
	     model.addAttribute("userName",user.get(0).getRegisterName());
		
		return "admin_things";
		
		
	}

}
