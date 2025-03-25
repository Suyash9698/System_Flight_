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
import com.auth.repository.AdminAllDetailRepository;
import com.auth.repository.AdminCurrentlyLoggedRepository;
import com.auth.repository.AdminLoginRepository;
import com.auth.repository.AdminRepository;
import com.auth.repository.BookingsRepository;
import com.auth.repository.FeedbackRepository;
import com.auth.repository.LoggedInRepository;
import com.auth.repository.PassengerBookedRepository;
import com.auth.repository.PassengerRepository;
import com.auth.repository.UserRepository;
import com.auth.service.AdminService;
import com.auth.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class UserController {
	
	@Autowired
	private UserRepository repo;
	
	@Autowired
	private LoggedInRepository logRepo;
	
	@Autowired
	private AdminCurrentlyLoggedRepository adminLogRepo;
	
	
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
	private AdminAllDetailRepository adminAllDetailRepo;
	
	@Autowired
	private AdminLoginRepository adminLoginRepo;
	
	
	
	
	
	@Autowired
    private EmailHelper emailHelper;
	
	@Autowired
	private EmailOTPHelper emailOTPHelper;
	
	@Autowired
	private EmailByAdmin emailByAdmin;
	
	@Autowired
	private StoreOTP otpStorage;
	
	@GetMapping("/index_B")
	public String bookFlight() {
	    return "index_B"; // Make sure bookFlight.html is in src/main/resources/templates
	}

	
	@GetMapping("/index")
	public String f() {
		return "index";
	}
	
	@GetMapping("/login_bhai")
	public String f2(Model model) {
		
		model.addAttribute("bandaa",true);
		return "index_B";
	}
	
	@GetMapping("/previous")
	public String getPrevious(
			Model model) {
		
		
			
			
			
			model.addAttribute("message","Welcome to FlyHighHub.com!!!");  
			
			
			
			
			// Create CurrentUserWhoBooked object
			CurrentlyLogged hereUser = logRepo.select();
			System.out.print(hereUser);
			
            // now get the user id of this user by using his mail id currentUserWhoBooked 
		    
		    List<Integer> userIds = userService.findAllCurrentUserId(hereUser.getRegisterEmail());
		    
		    System.out.println(userIds);
		    
		    
		    // now use this userids for fetching all passenger list
		    
		    List<PassengersBooked> allBookingsPassengers = userService.findAllPassengers(userIds);
		    
		    // Group bookings by currentUserWhoBooked.id
		    Map<Integer, List<PassengersBooked>> bookingsByUserId = allBookingsPassengers.stream()
		            .collect(Collectors.groupingBy(passenger -> passenger.getCurrentUserWhoBooked().getId()));

		    System.out.println(bookingsByUserId);
		    
		    model.addAttribute("previousBookings", bookingsByUserId);
		    
		    String mail = hereUser.getRegisterEmail();
		    
		    model.addAttribute("stored_dob",userService.getDob(mail));
		    model.addAttribute("stored_email",mail);
		    model.addAttribute("userName", hereUser.getRegisterName());
		    
			return "previous_bookings";
		}
	
	
	@GetMapping("/profile_validation")
	public String profile_log(RedirectAttributes redirectAttributes,HttpServletRequest request,Model model) {
		         
		System.out.println("aaya");
		        // Create CurrentUserWhoBooked object
				CurrentlyLogged hereUser = logRepo.select();
				System.out.println("get request: "+hereUser);				
				if(hereUser == null) {
					System.out.println("get request null hai: ");				
					 redirectAttributes.addFlashAttribute("stored_email","Not Defined");
				     redirectAttributes.addFlashAttribute("stored_dob","");
				     redirectAttributes.addFlashAttribute("userName","Not Defined");
				     redirectAttributes.addFlashAttribute("message","Please Log In First!");
				     // Get the referer URL
					 String referer = request.getHeader("referer");

					 // Redirect back to the referer URL
					 return "redirect:" + referer;

				}
				
				//now getting all details
			    
			     List<UserDetail> user = repo.findByRegisterEmail(hereUser.getRegisterEmail());
			     
			     System.out.println(user.get(0).getRegisterEmail());
			     
			     redirectAttributes.addFlashAttribute("stored_email",user.get(0).getRegisterEmail());
			     redirectAttributes.addFlashAttribute("stored_dob",user.get(0).getDob());
			     redirectAttributes.addFlashAttribute("userName",user.get(0).getRegisterName());
			     
			    
			       // Get the referer URL
				    String referer = request.getHeader("referer");

				    // Redirect back to the referer URL
				    return "redirect:" + referer;

	}
	
	@GetMapping("/signout")
	public String logOut(
			RedirectAttributes redirectAttributes,HttpServletRequest request
			){
		
		// Create CurrentUserWhoBooked object
		CurrentlyLogged hereUser = logRepo.select();
		
		//suppose user is not logged in only then handle this case
		long present = logRepo.count();
		if(present == 0) {
			redirectAttributes.addFlashAttribute("messageLogOut","Please Log In First!");
			return "redirect:/index_B";
		}
		
		//delete this data from database
		logRepo.deleteAll();
		
		redirectAttributes.addFlashAttribute("message","Logged Out Successfully!");
		
		return "redirect:/index";
		
	}
	
	
	@GetMapping("/adminPage")
	public String adminRedirect() {
		return "adminPage";
	}
	
	
	@GetMapping("/oldBookings")
	public String seeOld() {
		return "oldBookings";
	}
	
	
	@GetMapping("/reset_password")
    public String resetPassword() {
        
        return "reset_password";
    }
	
    
	
	
	@GetMapping("/home")
	public String home() {
		return "index";
	}
	
	
	
	@GetMapping("/flight_booking")
    public String showFlightBookingPage(Model model) {
		     
		return "flight_booking"; // This will return the flight_booking.html template
    }
	
	@GetMapping("/boarding_pass")
	public String getPass() {
		return "/boarding_pass";
	}
	
	@GetMapping("/admin_things")
    public String showAdminView() {
        return "admin_things"; // This will return the flight_booking.html template
    }
	
	@GetMapping("/admin")
	public String handleAdmin() {
		return "adminPage";
	}
	
	@GetMapping("/passenger_details")
	public String handleDetails() {
		return "passenger_booking";
	}
	
	@GetMapping("/book_2")
	public String handleDetail() {
		return "passenger_booking";
	}
	
	@GetMapping("/seatChoose")
	public String handleSeat() {
		return "seatBook";
	}
	
	@GetMapping("/foodChoose")
	public String handleFood() {
		return "shoppingBook";
	}
	
	
	@PostMapping("/update_details_of_profile_passenger_detail")
	public String updating_profile(@RequestParam(name="updated_name") String name,
			@RequestParam(name="updated_dob") String dob,
			@RequestParam(name="page_name") String page_name,
			
			
			@RequestParam("capturedFlightName2") String flightName,
	        @RequestParam("capturedPrice2") String price,
	        @RequestParam("capturedArrival2") String arrival,
	        @RequestParam("capturedDeparture2") String departure,
	        @RequestParam("capturedSeatsAvailable2") String seatsAvailable,
	        @RequestParam("userName2") String userName,
	        @RequestParam("stored_email2") String email,
	        @RequestParam("stored_dob2") String sdob,
	        Model model,
			
			RedirectAttributes redirectAttributes,
			 HttpServletRequest request
			
			) 
	{
		
		
				
		System.out.println("dddd: "+flightName);
		
		System.out.println("haa aaya tha");
		CurrentlyLogged hereUser = logRepo.select();
		
		
		
		String mail=hereUser.getRegisterEmail();
		//delete this data from database
		logRepo.deleteAll();
		
		//updating currentlyLogged
		logRepo.save(new CurrentlyLogged(name,mail));
		
		//updating currentUserWhoBooked
		
		int res = userService.updateEntry(name, mail);
		
		//updating userDetail
		
		userService.updateDetailsEntry(name, mail,dob);
		
		
		hereUser = logRepo.select();

		if(hereUser == null) {
			System.out.println("get request null hai: ");				
			 redirectAttributes.addFlashAttribute("stored_email","Not Defined");
		     redirectAttributes.addFlashAttribute("stored_dob","");
		     redirectAttributes.addFlashAttribute("userName","Not Defined");
		     redirectAttributes.addFlashAttribute("message","Please Log In First!");
		     // Get the referer URL
			

			 // Redirect back to the referer URL
			 return "redirect:/" + page_name;

		}
		
		//now getting all details
	    
	     List<UserDetail> user = repo.findByRegisterEmail(hereUser.getRegisterEmail());
	     
	     System.out.println(user.get(0).getRegisterEmail());
	     
	     
	     
	     model.addAttribute("userName", user.get(0).getRegisterName());
	     model.addAttribute("stored_email", user.get(0).getRegisterEmail());
	     model.addAttribute("stored_dob", user.get(0).getDob());
	     
	     model.addAttribute("capturedFlightName", flightName);
	        model.addAttribute("capturedPrice", price);
	        model.addAttribute("capturedArrival", arrival);
	        model.addAttribute("capturedDeparture", departure);
	        model.addAttribute("capturedSeatsAvailable", seatsAvailable);
	     
		model.addAttribute("message_of_updation","Details Updated Successfully");
		
		
	    // Redirect back to the referer URL
	    return  page_name;
		
	}
	
	
	
		
	
	
	
	@PostMapping("/update_details_of_profile_flight_booking")
	public String updating_profile(@RequestParam(name="updated_name") String name,
			@RequestParam(name="updated_dob") String dob,
			@RequestParam(name="page_name") String page_name,
			
			
			
	        Model model,
			
			RedirectAttributes redirectAttributes,
			 HttpServletRequest request
			
			) 
	{
		
		
				
		
		System.out.println("haa aaya tha");
		CurrentlyLogged hereUser = logRepo.select();
		
		
		
		String mail=hereUser.getRegisterEmail();
		//delete this data from database
		logRepo.deleteAll();
		
		//updating currentlyLogged
		logRepo.save(new CurrentlyLogged(name,mail));
		
		//updating currentUserWhoBooked
		
		int res = userService.updateEntry(name, mail);
		
		//updating userDetail
		
		userService.updateDetailsEntry(name, mail,dob);
		
		
		hereUser = logRepo.select();

		if(hereUser == null) {
			System.out.println("get request null hai: ");				
			 model.addAttribute("stored_email","Not Defined");
		     model.addAttribute("stored_dob","");
		     model.addAttribute("userName","Not Defined");
		     model.addAttribute("message","Please Log In First!");
		     // Get the referer URL
			

			 // Redirect back to the referer URL
			 return "redirect:/" + page_name;

		}
		
		//now getting all details
	    
	     List<UserDetail> user = repo.findByRegisterEmail(hereUser.getRegisterEmail());
	     
	     
	    
	     
	     model.addAttribute("userName", user.get(0).getRegisterName());
	     model.addAttribute("stored_email", user.get(0).getRegisterEmail());
	     model.addAttribute("stored_dob", user.get(0).getDob());
	     
	    
		model.addAttribute("message","Welcome to FlyHighHub.com!!!");  
		
	    // Redirect back to the referer URL
	    return  page_name;
		
	}
	
	
	
	@PostMapping("/update_details_of_profile_seat_book")
	public String updating_profile(@RequestParam(name="updated_name") String name,
			@RequestParam(name="updated_dob") String dob,
			@RequestParam(name="page_name") String page_name,
			
			
			// Captured Flight Details
	        @RequestParam(name = "capturedFlightName", required = false) String capturedFlightName,
	        @RequestParam(name = "capturedFlightId", required = false) String capturedFlightId,
	        @RequestParam(name = "capturedPrice", required = false) String capturedPrice,

	        // Travel Details
	        @RequestParam(name = "from", required = false) String from,
	        @RequestParam(name = "to", required = false) String to,
	        @RequestParam(name = "arrivalTime", required = false) String arrivalTime,
	        @RequestParam(name = "departTime", required = false) String departTime,
	        @RequestParam(name = "arrivalDate", required = false) String arrivalDate,
	        @RequestParam(name = "departDate", required = false) String departDate,
	        @RequestParam(name = "travelHours", required = false) String travelHours,
	        @RequestParam(name = "travelDays", required = false) String travelDays,
	        @RequestParam(name = "boardingTime", required = false) String boardingTime,
	        @RequestParam(name = "parsedArrivalDate", required = false) String parsedArrivalDate,
	        @RequestParam(name = "parsedDepartDate", required = false) String parsedDepartDate,
	        @RequestParam(name = "number", required = false) String number,

		        
		        @RequestParam(name = "firstName_1", required = false) String firstName_1,
		        @RequestParam(name = "lastName_1", required = false) String lastName_1,
		        @RequestParam(name = "age_1", required = false) Integer age_1,
		        @RequestParam(name = "nation_1", required = false) String nation_1,

		        @RequestParam(name = "firstName_2", required = false) String firstName_2,
		        @RequestParam(name = "lastName_2", required = false) String lastName_2,
		        @RequestParam(name = "age_2", required = false) Integer age_2,
		        @RequestParam(name = "nation_2", required = false) String nation_2,

		        @RequestParam(name = "firstName_3", required = false) String firstName_3,
		        @RequestParam(name = "lastName_3", required = false) String lastName_3,
		        @RequestParam(name = "age_3", required = false) Integer age_3,
		        @RequestParam(name = "nation_3", required = false) String nation_3,

		        @RequestParam(name = "firstName_4", required = false) String firstName_4,
		        @RequestParam(name = "lastName_4", required = false) String lastName_4,
		        @RequestParam(name = "age_4", required = false) Integer age_4,
		        @RequestParam(name = "nation_4", required = false) String nation_4,


			
			
	        Model model,
			
			RedirectAttributes redirectAttributes,
			 HttpServletRequest request
			
			) 
	{
		
		
		
	   
	    
	    
	    
      
	    
	    if (firstName_1 != null && lastName_1 != null  && age_1 != null && nation_1!=null) {
	        model.addAttribute("firstName_1", firstName_1);
	        model.addAttribute("lastName_1", lastName_1);
	        model.addAttribute("age_1", age_1);
	        model.addAttribute("nation_1", nation_1);
	        
	       
	        
	        
	        
	    }
	    
	    if (firstName_2 != null && lastName_2 != null && age_2 != null && nation_2!=null) {
	    	model.addAttribute("firstName_2", firstName_2);
	        model.addAttribute("lastName_2", lastName_2);
	        model.addAttribute("age_2", age_2);
	        model.addAttribute("nation_2", nation_2);
	        
	        
	        
	    }
	    
	    if (firstName_3 != null && lastName_3 != null  && age_3 != null && nation_3!=null ) {
	        model.addAttribute("firstName_3", firstName_3);
	        model.addAttribute("lastName_3", lastName_3);
	        model.addAttribute("age_3", age_3);
	        model.addAttribute("nation_3", nation_3);
	        
	        
	        
	    }
	    
	    if (firstName_4 != null && lastName_4 != null  && age_4 != null && nation_4!=null) {
	        model.addAttribute("firstName_4", firstName_4);
	        model.addAttribute("lastName_4", lastName_4);
	        model.addAttribute("age_4", age_4);
	        model.addAttribute("nation_4", nation_4);
	        
	        
	       
	    }
	    
	    model.addAttribute("capturedFlightName", capturedFlightName);
	    model.addAttribute("capturedFlightId", capturedFlightId);
	    model.addAttribute("capturedPrice", capturedPrice);

	    model.addAttribute("from", from);
	    model.addAttribute("to", to);
	    model.addAttribute("arrivalTime", arrivalTime);
	    model.addAttribute("departTime", departTime);
	    model.addAttribute("arrivalDate", arrivalDate);
	    model.addAttribute("departDate", departDate);
	    model.addAttribute("travelHours", travelHours);
	    model.addAttribute("travelDays", travelDays);
	    model.addAttribute("boardingTime", boardingTime);
	    model.addAttribute("parsedArrivalDate", parsedArrivalDate);
	    model.addAttribute("parsedDepartDate", parsedDepartDate);
	    model.addAttribute("number", number);
		
		CurrentlyLogged hereUser = logRepo.select();
		
		
		
		String mail=hereUser.getRegisterEmail();
		//delete this data from database
		logRepo.deleteAll();
		
		//updating currentlyLogged
		logRepo.save(new CurrentlyLogged(name,mail));
		
		//updating currentUserWhoBooked
		
		int res = userService.updateEntry(name, mail);
		
		//updating userDetail
		
		userService.updateDetailsEntry(name, mail,dob);
		
		
		hereUser = logRepo.select();

		if(hereUser == null) {
			System.out.println("get request null hai: ");				
			 model.addAttribute("stored_email","Not Defined");
		     model.addAttribute("stored_dob","");
		     model.addAttribute("userName","Not Defined");
		     model.addAttribute("message","Please Log In First!");
		     // Get the referer URL
			

			 // Redirect back to the referer URL
			 return "redirect:/" + page_name;

		}
		
		//now getting all details
	    
	     List<UserDetail> user = repo.findByRegisterEmail(hereUser.getRegisterEmail());
	     
	     System.out.println("ye hai"+ user);
	    
	     
	     model.addAttribute("userName", user.get(0).getRegisterName());
	     model.addAttribute("stored_email", user.get(0).getRegisterEmail());
	     model.addAttribute("stored_dob", user.get(0).getDob());
	     
	    
	     model.addAttribute("message_of_updation","Details Updated Successfully");
		
	    // Redirect back to the referer URL
	    return  page_name;
		
	}
	
	
	@PostMapping("/update_details_of_profile_food_order")
	public String updating_profile(@RequestParam(name="updated_name") String name,
			@RequestParam(name="updated_dob") String dob,
			@RequestParam(name="page_name") String page_name,
			
			
			@RequestParam(name = "capturedFlightName", required = false) String capturedFlightName,
	        @RequestParam(name = "capturedFlightId", required = false) String capturedFlightId,
	        @RequestParam(name = "capturedPrice", required = false) String capturedPrice,
			@RequestParam(name = "from", required = false) String from,
	        @RequestParam(name = "to", required = false) String to,
	        @RequestParam(name = "arrivalTime", required = false) String arrivalTime,
	        @RequestParam(name = "departTime", required = false) String departTime,
	        @RequestParam(name = "arrivalDate", required = false) String arrivalDate,
	        @RequestParam(name = "departDate", required = false) String departDate,
	        @RequestParam(name = "travelHours", required = false) String travelHours,
	        @RequestParam(name = "travelDays", required = false) String travelDays,
	        @RequestParam(name = "boardingTime", required = false) String boardingTime,
	        @RequestParam(name = "parsedArrivalDate", required = false) String parsedArrivalDate,
	        @RequestParam(name = "parsedDepartDate", required = false) String parsedDepartDate,
	        @RequestParam(name = "number", required = false) String number,
	        
	        
	        
	        @RequestParam(name = "firstName_1", required = false) String firstName_1,
	        @RequestParam(name = "lastName_1", required = false) String lastName_1,
	        @RequestParam(name = "age_1", required = false) Integer age_1,
	        @RequestParam(name = "nation_1", required = false) String nation_1,

	        @RequestParam(name = "firstName_2", required = false) String firstName_2,
	        @RequestParam(name = "lastName_2", required = false) String lastName_2,
	        @RequestParam(name = "age_2", required = false) Integer age_2,
	        @RequestParam(name = "nation_2", required = false) String nation_2,

	        @RequestParam(name = "firstName_3", required = false) String firstName_3,
	        @RequestParam(name = "lastName_3", required = false) String lastName_3,
	        @RequestParam(name = "age_3", required = false) Integer age_3,
	        @RequestParam(name = "nation_3", required = false) String nation_3,

	        @RequestParam(name = "firstName_4", required = false) String firstName_4,
	        @RequestParam(name = "lastName_4", required = false) String lastName_4,
	        @RequestParam(name = "age_4", required = false) Integer age_4,
	        @RequestParam(name = "nation_4", required = false) String nation_4,
	        
	     
	        @RequestParam(name = "seatIdName1", required = false) String seatIdName1,
	        @RequestParam(name = "seatIdName2", required = false) String seatIdName2,
	        @RequestParam(name = "seatIdName3", required = false) String seatIdName3,
	        @RequestParam(name = "seatIdName4", required = false) String seatIdName4,
	        
	     // Seat Information
	        @RequestParam(name = "seatPrice", required = false) String seatPrice,
	        @RequestParam(name = "totalPrice", required = false) String totalPrice,
	        
			
	        Model model,
			
			RedirectAttributes redirectAttributes,
			 HttpServletRequest request
			
			) 
	{
		
		
		

		
	    if (firstName_1 != null && lastName_1 != null  && age_1 != null && nation_1!=null) {
	        model.addAttribute("firstName_1", firstName_1);
	        model.addAttribute("lastName_1", lastName_1);
	        model.addAttribute("age_1", age_1);
	        model.addAttribute("nation_1", nation_1);
	        
	     }
	    
	    if (firstName_2 != null && lastName_2 != null && age_2 != null && nation_2!=null) {
	    	model.addAttribute("firstName_2", firstName_2);
	        model.addAttribute("lastName_2", lastName_2);
	        model.addAttribute("age_2", age_2);
	        model.addAttribute("nation_2", nation_2);
	    }
	    
	    if (firstName_3 != null && lastName_3 != null  && age_3 != null && nation_3!=null ) {
	        model.addAttribute("firstName_3", firstName_3);
	        model.addAttribute("lastName_3", lastName_3);
	        model.addAttribute("age_3", age_3);
	        model.addAttribute("nation_3", nation_3);
	        
	    }
	    
	    if (firstName_4 != null && lastName_4 != null  && age_4 != null && nation_4!=null) {
	        model.addAttribute("firstName_4", firstName_4);
	        model.addAttribute("lastName_4", lastName_4);
	        model.addAttribute("age_4", age_4);
	        model.addAttribute("nation_4", nation_4);
	        
	    }
	    
	    
	    model.addAttribute("capturedFlightName", capturedFlightName);
	    model.addAttribute("capturedFlightId", capturedFlightId);
	    model.addAttribute("capturedPrice", capturedPrice);

	    model.addAttribute("from", from);
	    model.addAttribute("to", to);
	    model.addAttribute("arrivalTime", arrivalTime);
	    model.addAttribute("departTime", departTime);
	    model.addAttribute("arrivalDate", arrivalDate);
	    model.addAttribute("departDate", departDate);
	    model.addAttribute("travelHours", travelHours);
	    model.addAttribute("travelDays", travelDays);
	    model.addAttribute("boardingTime", boardingTime);
	    model.addAttribute("parsedArrivalDate", parsedArrivalDate);
	    model.addAttribute("parsedDepartDate", parsedDepartDate);
	    model.addAttribute("number", number);

	    // Adding Seat Information
	    model.addAttribute("seatIdName1", seatIdName1);
	    model.addAttribute("seatIdName2", seatIdName2);
	    model.addAttribute("seatIdName3", seatIdName3);
	    model.addAttribute("seatIdName4", seatIdName4);

	    
        model.addAttribute("travelHours", travelHours);
	    
	    model.addAttribute("travelDays", travelDays);
	    
        model.addAttribute("boardingTime",boardingTime);
        
        
        model.addAttribute("parsedArrivalDate",parsedArrivalDate);
        
        model.addAttribute("parsedDepartDate",parsedDepartDate);
        
        
        model.addAttribute("seatIdName1",seatIdName1);
        model.addAttribute("seatIdName2",seatIdName2);
        model.addAttribute("seatIdName3",seatIdName3);
        model.addAttribute("seatIdName4",seatIdName4);
        
        model.addAttribute("seatPrice",seatPrice);
        model.addAttribute("totalPrice",totalPrice);
        
        
		System.out.println("haa aaya tha");
		CurrentlyLogged hereUser = logRepo.select();
		
		
		
		String mail=hereUser.getRegisterEmail();
		//delete this data from database
		logRepo.deleteAll();
		
		//updating currentlyLogged
		logRepo.save(new CurrentlyLogged(name,mail));
		
		//updating currentUserWhoBooked
		
		int res = userService.updateEntry(name, mail);
		
		//updating userDetail
		
		userService.updateDetailsEntry(name, mail,dob);
		
		
		hereUser = logRepo.select();

		if(hereUser == null) {
			System.out.println("get request null hai: ");				
			 model.addAttribute("stored_email","Not Defined");
		     model.addAttribute("stored_dob","");
		     model.addAttribute("userName","Not Defined");
		     model.addAttribute("message","Please Log In First!");
		     // Get the referer URL
			

			 // Redirect back to the referer URL
			 return "redirect:/" + page_name;

		}
		
		//now getting all details
	    
	     List<UserDetail> user = repo.findByRegisterEmail(hereUser.getRegisterEmail());
	     
	     
	    
	     
	     model.addAttribute("userName", user.get(0).getRegisterName());
	     model.addAttribute("stored_email", user.get(0).getRegisterEmail());
	     model.addAttribute("stored_dob", user.get(0).getDob());
	    
	     model.addAttribute("message_of_updation","Details Updated Successfully");
		
	    // Redirect back to the referer URL
	    return  page_name;
		
	}
	
	
	
	
	
	@PostMapping("/update_details_of_boarding")
	public String updating_profile(@RequestParam(name="updated_name") String name,
			@RequestParam(name="updated_dob") String dob,
			@RequestParam(name="page_name") String page_name,
			
			
			// Captured Flight Details
	        @RequestParam(name = "capturedFlightName", required = false) String capturedFlightName,
	        @RequestParam(name = "capturedFlightId", required = false) String capturedFlightId,
	        @RequestParam(name = "capturedPrice", required = false) String capturedPrice,

	        // Travel Details
	        @RequestParam(name = "from", required = false) String from,
	        @RequestParam(name = "to", required = false) String to,
	        @RequestParam(name = "arrivalTime", required = false) String arrivalTime,
	        @RequestParam(name = "departTime", required = false) String departTime,
	        @RequestParam(name = "arrivalDate", required = false) String arrivalDate,
	        @RequestParam(name = "departDate", required = false) String departDate,
	        @RequestParam(name = "travelHours", required = false) String travelHours,
	        @RequestParam(name = "travelDays", required = false) String travelDays,
	        @RequestParam(name = "boardingTime", required = false) String boardingTime,
	        @RequestParam(name = "parsedArrivalDate", required = false) String parsedArrivalDate,
	        @RequestParam(name = "parsedDepartDate", required = false) String parsedDepartDate,
	        @RequestParam(name = "number", required = false) String number,

		        
		        @RequestParam(name = "firstName_1", required = false) String firstName_1,
		        @RequestParam(name = "lastName_1", required = false) String lastName_1,
		        @RequestParam(name = "age_1", required = false) Integer age_1,
		        @RequestParam(name = "nation_1", required = false) String nation_1,

		        @RequestParam(name = "firstName_2", required = false) String firstName_2,
		        @RequestParam(name = "lastName_2", required = false) String lastName_2,
		        @RequestParam(name = "age_2", required = false) Integer age_2,
		        @RequestParam(name = "nation_2", required = false) String nation_2,

		        @RequestParam(name = "firstName_3", required = false) String firstName_3,
		        @RequestParam(name = "lastName_3", required = false) String lastName_3,
		        @RequestParam(name = "age_3", required = false) Integer age_3,
		        @RequestParam(name = "nation_3", required = false) String nation_3,

		        @RequestParam(name = "firstName_4", required = false) String firstName_4,
		        @RequestParam(name = "lastName_4", required = false) String lastName_4,
		        @RequestParam(name = "age_4", required = false) Integer age_4,
		        @RequestParam(name = "nation_4", required = false) String nation_4,
		        
		        @RequestParam(name = "seat1", required = false) String seatIdName1,
		        @RequestParam(name = "seat2", required = false) String seatIdName2,
		        @RequestParam(name = "seat3", required = false) String seatIdName3,
		        @RequestParam(name = "seat4", required = false) String seatIdName4,
		        
		        @RequestParam(name = "seatPrice", required = false) String seatPrice,
		        @RequestParam(name = "foodPrice", required = false) String foodPrice,
		        @RequestParam(name = "totalPrice", required = false) String totalPrice,
		        
		        
		        
			
			
	        Model model,
			
			RedirectAttributes redirectAttributes,
			 HttpServletRequest request
			
			) 
	{
		
		
		
	   
	    
	    
	    
      
	    
	    if (firstName_1 != null && lastName_1 != null  && age_1 != null && nation_1!=null) {
	        model.addAttribute("firstName_1", firstName_1);
	        model.addAttribute("lastName_1", lastName_1);
	        model.addAttribute("age_1", age_1);
	        model.addAttribute("nation_1", nation_1);
	        
	       
	        
	        
	        
	    }
	    
	    if (firstName_2 != null && lastName_2 != null && age_2 != null && nation_2!=null) {
	    	model.addAttribute("firstName_2", firstName_2);
	        model.addAttribute("lastName_2", lastName_2);
	        model.addAttribute("age_2", age_2);
	        model.addAttribute("nation_2", nation_2);
	        
	        
	        
	    }
	    
	    if (firstName_3 != null && lastName_3 != null  && age_3 != null && nation_3!=null ) {
	        model.addAttribute("firstName_3", firstName_3);
	        model.addAttribute("lastName_3", lastName_3);
	        model.addAttribute("age_3", age_3);
	        model.addAttribute("nation_3", nation_3);
	        
	        
	        
	    }
	    
	    if (firstName_4 != null && lastName_4 != null  && age_4 != null && nation_4!=null) {
	        model.addAttribute("firstName_4", firstName_4);
	        model.addAttribute("lastName_4", lastName_4);
	        model.addAttribute("age_4", age_4);
	        model.addAttribute("nation_4", nation_4);
	        
	        
	       
	    }
	    
	    model.addAttribute("capturedFlightName", capturedFlightName);
	    model.addAttribute("capturedFlightId", capturedFlightId);
	    model.addAttribute("capturedPrice", capturedPrice);

	    model.addAttribute("from", from);
	    model.addAttribute("to", to);
	    model.addAttribute("arrivalTime", arrivalTime);
	    model.addAttribute("departTime", departTime);
	    model.addAttribute("arrivalDate", arrivalDate);
	    model.addAttribute("departDate", departDate);
	    model.addAttribute("travelHours", travelHours);
	    model.addAttribute("travelDays", travelDays);
	    model.addAttribute("boardingTime", boardingTime);
	    model.addAttribute("parsedArrivalDate", parsedArrivalDate);
	    model.addAttribute("parsedDepartDate", parsedDepartDate);
	    model.addAttribute("number", number);
		
		CurrentlyLogged hereUser = logRepo.select();
		
		
		
		String mail=hereUser.getRegisterEmail();
		//delete this data from database
		logRepo.deleteAll();
		
		//updating currentlyLogged
		logRepo.save(new CurrentlyLogged(name,mail));
		
		//updating currentUserWhoBooked
		
		int res = userService.updateEntry(name, mail);
		
		//updating userDetail
		
		userService.updateDetailsEntry(name, mail,dob);
		
		
		hereUser = logRepo.select();

		if(hereUser == null) {
			System.out.println("get request null hai: ");				
			 model.addAttribute("stored_email","Not Defined");
		     model.addAttribute("stored_dob","");
		     model.addAttribute("userName","Not Defined");
		     model.addAttribute("message","Please Log In First!");
		     // Get the referer URL
			

			 // Redirect back to the referer URL
			 return "redirect:/" + page_name;

		}
		
		//now getting all details
	    
	     List<UserDetail> user = repo.findByRegisterEmail(hereUser.getRegisterEmail());
	     
	     System.out.println("ye hai"+ user);
	     
	     
	        model.addAttribute("seatIdName1",seatIdName1);
	        model.addAttribute("seatIdName2",seatIdName2);
	        model.addAttribute("seatIdName3",seatIdName3);
	        model.addAttribute("seatIdName4",seatIdName4);
	        
	        
	        
	        
	        model.addAttribute("seatPrice", seatPrice);
	        
	        
	        model.addAttribute("totalPrice", totalPrice);
	        
	        model.addAttribute("foodPrice", foodPrice);
	        
	    
	     
	     model.addAttribute("userName", user.get(0).getRegisterName());
	     model.addAttribute("stored_email", user.get(0).getRegisterEmail());
	     model.addAttribute("stored_dob", user.get(0).getDob());
	     
	    
	     model.addAttribute("message_of_updation","Details Updated Successfully");
		
	    // Redirect back to the referer URL
	    return  page_name;
		
	}
	
	
	
			
	
	
	@PostMapping("/reset_things")
	public String resetting(
			@RequestParam(name="new_password") String new_password,
			@RequestParam(name="confirm_pass") String confirm_password,
			@RequestParam(name="reset_email") String email,
			
			RedirectAttributes redirectAttributes,
			HttpServletRequest request
			) {
		System.out.println(email+" khaali hai kya");
		
		if(!new_password.equals(confirm_password)) {
			redirectAttributes.addFlashAttribute("message_of_forgot", email);
			redirectAttributes.addFlashAttribute("message_of_password", "Passwords do not match");
            return "redirect:/index_B";
		}
		
		UserDetail user = userService.giveMeAllUsersByEmail(email);
		
		
		boolean isMatch = bp.matches(new_password, user.getRegisterPassword());
		
		if(isMatch == true) {
			redirectAttributes.addFlashAttribute("message_of_forgot", email);
			redirectAttributes.addFlashAttribute("message_of_password", "Please choose different password which you have not used recently");
			
			return "redirect:/index_B";
		}
		
		
		//reset the password
		
		//just updating the things
		int ans = userService.updatingPassword(email, bp.encode(confirm_password));
		
		System.out.println(ans);
		
		redirectAttributes.addFlashAttribute("message", "Password reset successfully");
        
		return "redirect:/index_B";
		
		
	}
	
	@PostMapping("/otpEmail")
	public String otpEmailValidate(
			@RequestParam(name="hidden-otp-mail-reset") String otp,
			@RequestParam(name="mail-reset") String mail,
			RedirectAttributes redirectAttributes,
			HttpServletRequest request
			) 
	{
		
		
		String hereotp=otpStorage.getOTP(mail);
		
		System.out.println(mail);
		System.out.println(otp);
		System.out.println(hereotp);
		
		if(!otp.equals(hereotp)) {
			redirectAttributes.addFlashAttribute("message", "Please Enter Valid OTP");
            return "redirect:/index_B"; 
		}
		
		redirectAttributes.addFlashAttribute("message_of_forgot", mail);
		redirectAttributes.addFlashAttribute("message_of_success", "haan yashi");
		
		System.out.println(mail);
		
		return "redirect:/index_B";
		
	}
	
	@PostMapping("/reset")
	public String resetPassword(@RequestParam(name="hidden-mail") String registerEmail,
			RedirectAttributes redirectAttributes, HttpServletRequest request
			) {
		
		//do it search in all database and find this email
		//if get then => then enter otp
		//if otp valid then redirect to that page
		
		boolean ans = userService.isEmailRegistered(registerEmail);
		
		if(ans==false) {
			redirectAttributes.addFlashAttribute("message","Email is not registered with us");
			return "redirect:/index_B"; 
		}
		
		//enter otp
		redirectAttributes.addFlashAttribute("messagesotp","haaa");
		redirectAttributes.addFlashAttribute("reset_email",registerEmail);
		
		
		
        String genotp = emailOTPHelper.generateOTP();
        
        System.out.println(registerEmail);
	    
	    boolean res = emailOTPHelper.sendEmail(registerEmail,genotp);
	    
	    otpStorage.storeOTP(registerEmail, genotp);
		
		return "redirect:/index_B";
	}
	
	
	@PostMapping("/otp")
	public String finalRegister(
			@RequestParam(name="registerName") String registerName,
			@RequestParam(name="registerEmail") String registerEmail,
			@RequestParam(name="registerPassword") String password,
			@RequestParam(name="dob") String dob,
			@RequestParam(name="otp-hidden") String otp,
			@RequestParam(name="userType") String userType,
			
			
			RedirectAttributes redirectAttributes,
			HttpServletRequest request
			
			) {
		
		String hereotp = otpStorage.getOTP(registerEmail);
		System.out.println(registerEmail);
		System.out.println(otp);
		System.out.println(hereotp);
		
		
		if(!hereotp.equals(otp)) {
			redirectAttributes.addFlashAttribute("message", "Please Enter Valid OTP");
            return "redirect:/index_B";
		}
		
		otpStorage.removeOTP(registerEmail);
		
		
		if(userType.equals("user")) {
		
		    UserDetail user = new UserDetail();
		    user.setRegisterName(registerName);
		    user.setRegisterPassword(bp.encode(password)); // Encode the password before saving
		    user.setDob(dob);
		    user.setRegisterEmail(registerEmail);
		
		    repo.save(user);
		}
		
		else if(userType.equals("admin")) {
			AdminDetail adminLog = new AdminDetail();
			adminLog.setRegisterEmail(registerEmail);
			adminLog.setRegisterName(registerName);
			adminLog.setRegisterPassword(bp.encode(password));
			adminLoginRepo.save(adminLog);
			
			
			AdminAllDetail adminAll = new AdminAllDetail();
			adminAll.setDob(dob);
			adminAll.setRegisterEmail(registerEmail);
			adminAll.setRegisterName(registerName);
			adminAll.setRegisterPassword(bp.encode(password));
			adminAllDetailRepo.save(adminAll);
		}
		
		
		redirectAttributes.addFlashAttribute("message", "User Registered Successfully");
        return "redirect:/index";
	}
	
	

	@PostMapping("/register")
	public String saveUser(@ModelAttribute UserDetail user,
			@RequestParam("userType") String userType,
			HttpSession session,RedirectAttributes redirectAttributes,
			HttpServletRequest request) {
	    
		if(userType.equals("user")) {
	    
		  //check if email is already registered
		  if (userService.isEmailRegistered(user.getRegisterEmail())) {
			redirectAttributes.addFlashAttribute("message", "Email is already registered");
            return "redirect:/index_B";
          }
		}
		
		else if(userType.equals("admin")) {
			
			//checking if adminLog already contain this password
			boolean ans = adminService.isAlreadyRegistered(user.getRegisterEmail());
			
			if(ans == true) {
				redirectAttributes.addFlashAttribute("message", "Email is already registered");
	            return "redirect:/index_B";
			}
			
		}
		
		// Check if the passwords match
	    if (!user.getRegisterPassword().equals(user.getConfirmPassword())) {
	        // If the passwords don't match, set an error message and return to the registration form
	    	redirectAttributes.addFlashAttribute("message", "Passwords do not match");
	        return "redirect:/index_B";
	    }
		
	    
	    // Check the password length
	    if (user.getRegisterPassword().length() <= 4) {
	        // If the passwords don't match, set an error message and return to the registration form
	    	redirectAttributes.addFlashAttribute("message", "Password is too small!");
	        return "redirect:/index_B";
	    }
	    
	    String genotp = emailOTPHelper.generateOTP();
	    
	    boolean res = emailOTPHelper.sendEmail(user.getRegisterEmail(),genotp);
	    
	    otpStorage.storeOTP(user.getRegisterEmail(), genotp);
	    
	    System.out.println(res);
	    
	    

//	    // Save the user if passwords match
//	    user.setRegisterPassword(bp.encode(user.getRegisterPassword())); // Encode the password before saving
//	    repo.save(user);
//	    redirectAttributes.addFlashAttribute("message", "User Registered Successfully");
	    
	    redirectAttributes.addFlashAttribute("registerName",user.getRegisterName());
	    redirectAttributes.addFlashAttribute("registerEmail",user.getRegisterEmail());
	    redirectAttributes.addFlashAttribute("registerPassword",user.getRegisterPassword());
	    redirectAttributes.addFlashAttribute("dob",user.getDob());
	    redirectAttributes.addFlashAttribute("userType",userType);
	    
	    if(res == true)
	          redirectAttributes.addFlashAttribute("messages","haaa");
	    
	    
	    return "redirect:/index_B";
	}
	
	@PostMapping("/login")
	public String login(@RequestParam String loginName, @RequestParam String loginPassword, @RequestParam String action,
			HttpSession session,
			RedirectAttributes redirectAttributes,
			HttpServletRequest request,
			Model model) {
		
		
		if ("user".equals(action)) {
            // Handle login logic
            System.out.println("user button clicked");
            UserDetail result = userService.login(loginName,loginPassword);
    		if(Objects.nonNull(result)) {
    			session.setAttribute("message", "Login successfully....");
    			
    			CurrentlyLogged currentlyLogged = new CurrentlyLogged(result.getRegisterName(),result.getRegisterEmail());
    			
    			logRepo.deleteAll();

    			logRepo.save(currentlyLogged);
    			
    			model.addAttribute("message","Welcome to FlyHighHub.com!!!");  
    			
    			model.addAttribute("userName", loginName);
    			
    			
    			// Create CurrentUserWhoBooked object
    			CurrentlyLogged hereUser = logRepo.select();
    			System.out.print(hereUser);
    			
                // now get the user id of this user by using his mail id currentUserWhoBooked 
			    
			    List<Integer> userIds = userService.findAllCurrentUserId(hereUser.getRegisterEmail());
			    
			    System.out.println(userIds);
			    
			    
			    // now use this userids for fetching all passenger list
			    
			    List<PassengersBooked> allBookingsPassengers = userService.findAllPassengers(userIds);
			    
			    // Group bookings by currentUserWhoBooked.id
			    Map<Integer, List<PassengersBooked>> bookingsByUserId = allBookingsPassengers.stream()
			            .collect(Collectors.groupingBy(passenger -> passenger.getCurrentUserWhoBooked().getId()));

			    System.out.println(bookingsByUserId);
			    
			    model.addAttribute("previousBookings", bookingsByUserId);
			    
			    model.addAttribute("stored_dob",result.getDob());
			    model.addAttribute("stored_email",result.getRegisterEmail());
			    
    			return "flight_booking";
    		}
    		else {
    			
    			redirectAttributes.addFlashAttribute("message","Please Enter valid credentials!!!");
    			System.out.println("Invalid");
    			//session.setAttribute("message", "Please Enter valid credentials!!!");
    			return "redirect:/index_B";

    		}
    		
        } else {
            // Handle admin login logic
        	 System.out.println("admin button clicked");
        	 AdminDetail result = adminService.adminLogin(loginName,loginPassword);
     		if(Objects.nonNull(result)) {
     			session.setAttribute("message", "Login successfully....");
     			
     			// saving details of current admin 
                AdminCurrentlyLogged currentlyLogged = new AdminCurrentlyLogged(result.getRegisterName(),result.getRegisterEmail());
    			
    			adminLogRepo.deleteAll();

    			adminLogRepo.save(currentlyLogged);
     			
			     AdminCurrentlyLogged hereUser = adminLogRepo.findCurrentAdmin().get(0);
			     List<AdminAllDetail> user = adminService.findAllAdminDetails(hereUser.getRegisterEmail());
			     
			     model.addAttribute("stored_email",user.get(0).getRegisterEmail());
			     model.addAttribute("stored_dob",user.get(0).getDob());
			     model.addAttribute("userName",user.get(0).getRegisterName());
			     
			     System.out.println(user.get(0).getRegisterEmail());
			     System.out.println(user.get(0).getDob());
			     System.out.println(user.get(0).getRegisterName());
			     
		
     			return "adminPage";
     		}
     		else {
     			
     			redirectAttributes.addFlashAttribute("message", "Please Enter valid credentials!!!");
     		    return "redirect:/index_B";
     		}
        }
		
		
		
	}
	
	
	
	
	@PostMapping("/cancel_ticket")
	public String cancelTicket(@RequestParam (name="uniqueId") String uid,
			
			RedirectAttributes redirectAttribute,
			Model model) {
		System.out.println("---------------------------");
		System.out.println(uid);
		
		
	    
	    
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
	    
	    List<PassengersBooked> allBookingsPassengers = userService.findAllPassengers(userIds);
	    
	    // Group bookings by currentUserWhoBooked.id
	    Map<Integer, List<PassengersBooked>> bookingsByUserId = allBookingsPassengers.stream()
	            .collect(Collectors.groupingBy(passenger -> passenger.getCurrentUserWhoBooked().getId()));

	    System.out.println(bookingsByUserId);
	    
	    model.addAttribute("isCancelled",true);
	    
	    model.addAttribute("userName",hereUser.getRegisterName());
	    
	    
	    model.addAttribute("previousBookings", bookingsByUserId);
	    
	    //now getting all details
	    
	     List<UserDetail> user = repo.findByRegisterEmail(hereUser.getRegisterEmail());
	     
	     model.addAttribute("stored_email",user.get(0).getRegisterEmail());
	     model.addAttribute("stored_dob",user.get(0).getDob());

	     model.addAttribute("message","Welcome to FlyHighHub.com!!!");  
	     
	     
	     // Now find all the email ids of the users who had done booking on this flight
	     List<String> emails = new ArrayList<>();
	     emails.add(hereUser.getRegisterEmail());

	     System.out.println("emails found => " + emails);

	     // Notify all recipients about the cancellation of their booking
	     emailByAdmin.sendEmail(emails, "Cancellation of Your Booking for Flight " + flightName,
	             "Hi,<br>We have successfully processed the cancellation of your booking for flight " + flightName + "."
	             + "<br>If you have any questions or need further assistance, please contact our support team."
	             + "<br><br>Thank you for choosing FlyhighHub.com.<br>Best regards,<br>FlyhighHub.com");

			
		
		return "flight_booking";
	}
	
	
	
	@PostMapping("/cancel_ticket_with_popup")
	public String cancelTicketWithPopup(@RequestParam (name="uniqueId") String uid,
			
			RedirectAttributes redirectAttribute,
			Model model) {
		System.out.println("---------------------------");
		System.out.println(uid);
		
		
	    
	    
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
	    
	    List<PassengersBooked> allBookingsPassengers = userService.findAllPassengers(userIds);
	    
	    // Group bookings by currentUserWhoBooked.id
	    Map<Integer, List<PassengersBooked>> bookingsByUserId = allBookingsPassengers.stream()
	            .collect(Collectors.groupingBy(passenger -> passenger.getCurrentUserWhoBooked().getId()));

	    System.out.println(bookingsByUserId);
	    
	    model.addAttribute("isCancelled",true);
	    
	    model.addAttribute("userName",hereUser.getRegisterName());
	    
	    
	    model.addAttribute("previousBookings", bookingsByUserId);
	    
	    //now getting all details
	    
	     List<UserDetail> user = repo.findByRegisterEmail(hereUser.getRegisterEmail());
	     
	     model.addAttribute("stored_email",user.get(0).getRegisterEmail());
	     model.addAttribute("stored_dob",user.get(0).getDob());

	     model.addAttribute("message","Welcome to FlyHighHub.com!!!");  
	     
	     
	     // Now find all the email ids of the users who had done booking on this flight
	     List<String> emails = new ArrayList<>();
	     emails.add(hereUser.getRegisterEmail());

	     System.out.println("emails found => " + emails);

	     // Notify all recipients about the cancellation of their booking
	     emailByAdmin.sendEmail(emails, "Cancellation of Your Booking for Flight " + flightName,
	             "Hi,<br>We have successfully processed the cancellation of your booking for flight " + flightName + "."
	             + "<br>If you have any questions or need further assistance, please contact our support team."
	             + "<br><br>Thank you for choosing FlyhighHub.com.<br>Best regards,<br>FlyhighHub.com");

			
		
		return "previous_bookings";
	}
	

	
	
	
	@PostMapping("/email")
	public String toEmail(
			@RequestParam(name="userId") String userId, 
			@RequestParam String capturedFlightName,
	        @RequestParam String capturedFlightId,
	        @RequestParam String capturedPrice,
	        @RequestParam String from,
	        @RequestParam String to,
	        @RequestParam String arrivalTime,
	        @RequestParam String departTime,
	        
	        
	        @RequestParam(name = "travelHours", required = false) Long travelHours,
	        @RequestParam(name = "travelDays", required = false) Long travelDays,
	        @RequestParam(name = "boardingTime", required = false) String boardingTime,
	        @RequestParam(name = "parsedArrivalDate", required = false) String parsedArrivalDate,
	        @RequestParam(name = "parsedDepartDate", required = false) String parsedDepartDate,
	        
	        
	        
	        @RequestParam(name = "firstName_1", required = false) String firstName_1,
	        @RequestParam(name = "lastName_1", required = false) String lastName_1,
	        @RequestParam(name = "age_1", required = false) Integer age_1,
	        @RequestParam(name = "nation_1", required = false) String nation_1,

	        @RequestParam(name = "firstName_2", required = false) String firstName_2,
	        @RequestParam(name = "lastName_2", required = false) String lastName_2,
	        @RequestParam(name = "age_2", required = false) Integer age_2,
	        @RequestParam(name = "nation_2", required = false) String nation_2,

	        @RequestParam(name = "firstName_3", required = false) String firstName_3,
	        @RequestParam(name = "lastName_3", required = false) String lastName_3,
	        @RequestParam(name = "age_3", required = false) Integer age_3,
	        @RequestParam(name = "nation_3", required = false) String nation_3,

	        @RequestParam(name = "firstName_4", required = false) String firstName_4,
	        @RequestParam(name = "lastName_4", required = false) String lastName_4,
	        @RequestParam(name = "age_4", required = false) Integer age_4,
	        @RequestParam(name = "nation_4", required = false) String nation_4,
	        
	        @RequestParam(name = "seat1", required = false) String seatIdName1,
	        @RequestParam(name = "seat2", required = false) String seatIdName2,
	        @RequestParam(name = "seat3", required = false) String seatIdName3,
	        @RequestParam(name = "seat4", required = false) String seatIdName4,
	        
	        @RequestParam(name = "seatPrice", required = false) String seatPrice,
	        @RequestParam(name = "foodPrice", required = false) String foodPrice,
	        @RequestParam(name = "totalPrice", required = false) String totalPrice,
	        
	        
	        
			RedirectAttributes redirectAttributes,
			HttpServletRequest request) {
		//sending boarding pass over email for booking
		// Create CurrentUserWhoBooked object
		CurrentlyLogged hereUser = logRepo.select();
	    emailHelper.sendEmail(hereUser.getRegisterEmail(),userId,firstName_1,lastName_1,age_1,
	    		firstName_2,lastName_2,age_2,
	    		firstName_3,lastName_3,age_3,
	    		firstName_4,lastName_4,age_4,
	    		
	    		capturedFlightName,
		        capturedFlightId,
		        capturedPrice,
		        from,
		        to,
		        arrivalTime,
		        departTime,
		        
		        seatIdName1,
		        seatIdName2,
		        seatIdName3,
		        seatIdName4,
		        
		        
		        travelHours,
		        travelDays,
		        boardingTime,
		        parsedArrivalDate,
		        parsedDepartDate,
		        
		        totalPrice,
		        seatPrice,
		        foodPrice
	    		);
	    redirectAttributes.addFlashAttribute("message","Welcome to FlyHighHub.com!!!"); 
	    redirectAttributes.addFlashAttribute("userName",hereUser.getRegisterName());
	    redirectAttributes.addFlashAttribute("msg_email","Boarding Pass Was Sent to Your E-mail Id Successfully!");
	    return "redirect:/index";
	}
	
	@PostMapping("/boarding")
	public String tillFoodDetails(
			
			@RequestParam String capturedFlightName,
	        @RequestParam String capturedFlightId,
	        @RequestParam String capturedPrice,
	        @RequestParam String from,
	        @RequestParam String to,
	        @RequestParam String arrivalTime,
	        @RequestParam String departTime,
	        
	        
	        @RequestParam Long travelHours,
	        @RequestParam Long travelDays,
	        @RequestParam String boardingTime,
	        @RequestParam String parsedArrivalDate,
	        @RequestParam String parsedDepartDate,
	        
	        
	        
	        @RequestParam(name = "firstName_1", required = false) String firstName_1,
	        @RequestParam(name = "lastName_1", required = false) String lastName_1,
	        @RequestParam(name = "age_1", required = false) Integer age_1,
	        @RequestParam(name = "nation_1", required = false) String nation_1,

	        @RequestParam(name = "firstName_2", required = false) String firstName_2,
	        @RequestParam(name = "lastName_2", required = false) String lastName_2,
	        @RequestParam(name = "age_2", required = false) Integer age_2,
	        @RequestParam(name = "nation_2", required = false) String nation_2,

	        @RequestParam(name = "firstName_3", required = false) String firstName_3,
	        @RequestParam(name = "lastName_3", required = false) String lastName_3,
	        @RequestParam(name = "age_3", required = false) Integer age_3,
	        @RequestParam(name = "nation_3", required = false) String nation_3,

	        @RequestParam(name = "firstName_4", required = false) String firstName_4,
	        @RequestParam(name = "lastName_4", required = false) String lastName_4,
	        @RequestParam(name = "age_4", required = false) Integer age_4,
	        @RequestParam(name = "nation_4", required = false) String nation_4,
	        
	        @RequestParam(name = "seat1", required = false) String seatIdName1,
	        @RequestParam(name = "seat2", required = false) String seatIdName2,
	        @RequestParam(name = "seat3", required = false) String seatIdName3,
	        @RequestParam(name = "seat4", required = false) String seatIdName4,
	        
	        @RequestParam(name = "seatPrice", required = false) String seatPrice,
	        @RequestParam(name = "totalPrice", required = false) String totalPrice,
	        
	        
	        @RequestParam(name = "foodCost", required = false) String foodCost,
			
	        

	        HttpSession session,
	        Model model,
	        RedirectAttributes redirectAttributes
			) 
	{
		
		//process them accordingly
				
			    
			    
			  // Add the form data to the model
			    model.addAttribute("capturedFlightName", capturedFlightName);
			    model.addAttribute("capturedFlightId", capturedFlightId);
			    model.addAttribute("capturedPrice", capturedPrice);
			    model.addAttribute("from", from);
			    model.addAttribute("to", to);
			    model.addAttribute("arrivalTime", arrivalTime);
			    model.addAttribute("departTime", departTime);
			    
			    
			    
			    
		    // Create a list to store PassengersBooked objects
		    List<PassengersBooked> bookings = new ArrayList<>();
			
			// Create CurrentUserWhoBooked object
			CurrentUserWhoBooked currentUser = new CurrentUserWhoBooked();
			CurrentlyLogged hereUser = logRepo.select();
			System.out.print(hereUser);
			currentUser.setRegisterName(hereUser.getRegisterName());
			currentUser.setRegisterEmail(hereUser.getRegisterEmail());
			
			repoBook.save(currentUser);
			
			
			int countOfPassengers = 0;
			    

				
			if (firstName_1 != null && lastName_1 != null  && age_1 != null && nation_1!=null) {
		    	
		    	PassengersBooked passenger1 = new PassengersBooked(firstName_1, lastName_1, age_1, nation_1, seatIdName1, 
		    			capturedFlightName,capturedFlightId,parsedArrivalDate,parsedDepartDate,arrivalTime,departTime,from,to,totalPrice,currentUser);
			        bookings.add(passenger1);
			        repoBookingsStore.save(passenger1);
			        
			        model.addAttribute("firstName_1", firstName_1);
			        model.addAttribute("lastName_1", lastName_1);
			        model.addAttribute("age_1", age_1);
			        model.addAttribute("nation_1", nation_1);
			        countOfPassengers++;
			        
			     }
			    
                  if (firstName_2 != null && lastName_2 != null && age_2 != null && nation_2!=null) {
			    	
			    	PassengersBooked passenger1 = new PassengersBooked(firstName_2, lastName_2, age_2, nation_2, seatIdName2,
			    			capturedFlightName,capturedFlightId,parsedArrivalDate,parsedDepartDate,arrivalTime,departTime,from,to,totalPrice,currentUser);

			    	bookings.add(passenger1);
			        repoBookingsStore.save(passenger1);
			        
			    	model.addAttribute("firstName_2", firstName_2);
			        model.addAttribute("lastName_2", lastName_2);
			        model.addAttribute("age_2", age_2);
			        model.addAttribute("nation_2", nation_2);
			        countOfPassengers++;
			    }
			    
                  if (firstName_3 != null && lastName_3 != null  && age_3 != null && nation_3!=null ) {
  			    	
  			    	PassengersBooked passenger1 = new PassengersBooked(firstName_3, lastName_3, age_3, nation_3,seatIdName3, 
  			    			capturedFlightName,capturedFlightId,parsedArrivalDate,parsedDepartDate,arrivalTime,departTime,from,to,totalPrice,currentUser);
  			        bookings.add(passenger1);
  			        repoBookingsStore.save(passenger1);
			        
			        model.addAttribute("firstName_3", firstName_3);
			        model.addAttribute("lastName_3", lastName_3);
			        model.addAttribute("age_3", age_3);
			        model.addAttribute("nation_3", nation_3);
			        countOfPassengers++;
			        
			    }
			    
                  if (firstName_4 != null && lastName_4 != null  && age_4 != null && nation_4!=null) {
  			    	
  			    	PassengersBooked passenger1 = new PassengersBooked(firstName_4, lastName_4, age_4, nation_4, seatIdName4,
  			    			capturedFlightName,capturedFlightId,parsedArrivalDate,parsedDepartDate,arrivalTime,departTime,from,to,totalPrice,currentUser);
  			        bookings.add(passenger1);
  			        repoBookingsStore.save(passenger1);
			        
			        model.addAttribute("firstName_4", firstName_4);
			        model.addAttribute("lastName_4", lastName_4);
			        model.addAttribute("age_4", age_4);
			        model.addAttribute("nation_4", nation_4);
			        countOfPassengers++;
			        
			    }
			    
		        model.addAttribute("travelHours", travelHours);
			    
			    model.addAttribute("travelDays", travelDays);
			    
		        model.addAttribute("boardingTime",boardingTime);
		        
		        
		        model.addAttribute("parsedArrivalDate",parsedArrivalDate);
		        
		        model.addAttribute("parsedDepartDate",parsedDepartDate);
		        
		        
		        model.addAttribute("seatIdName1",seatIdName1);
		        model.addAttribute("seatIdName2",seatIdName2);
		        model.addAttribute("seatIdName3",seatIdName3);
		        model.addAttribute("seatIdName4",seatIdName4);
		        
		        
		        
		        
		        model.addAttribute("seatPrice", seatPrice);
		        
		        
		        model.addAttribute("totalPrice", totalPrice);
		        
		        model.addAttribute("foodPrice", foodCost);
		        
		        
		        
               //updating seats
			    
			    String fullFlight =  capturedFlightId + " " + capturedFlightName ;
			    
			    boolean found = adminService.isFlightNameFound(fullFlight);
			    
			    
			    
			    
			    
			    //finding the original seat capacity left
			    
			    String originalSeats = adminService.seats(fullFlight);
			    
			    int oc = Integer.parseInt(originalSeats);
			    
			    int ac = oc - countOfPassengers;
			    
			    if(ac < 0) {
			    	model.addAttribute("message","Maximum Capacity of Flight is Reached!");
			    	return "flight_booking";
			    }
			    
			    String updatedSeats = String.valueOf(ac);
			    
			    //reducing the seats by countOfPassengers in table flightAvailable
			    
			    int ans = adminService.updateSeats(fullFlight, updatedSeats);
			    
			    
			    
		        
		        model.addAttribute("passengerForms", bookings);
		
		        // Set the bookings list to the currentUser object
			    currentUser.setBookings(bookings);
			    
			    
			    
			    // now get the user id of this user by using his mail id currentUserWhoBooked 
			    
			    List<Integer> userIds = userService.findAllCurrentUserId(hereUser.getRegisterEmail());
			    
			    System.out.println(userIds);
			    
			    
			    // now use this userids for fetching all passenger list
			    
			    List<PassengersBooked> allBookingsPassengers = userService.findAllPassengers(userIds);
			    
			    
			    // Group bookings by currentUserWhoBooked.id
			    Map<Integer, List<PassengersBooked>> bookingsByUserId = allBookingsPassengers.stream()
			            .collect(Collectors.groupingBy(passenger -> passenger.getCurrentUserWhoBooked().getId()));

			    
			    
			    
			    
			    model.addAttribute("previousBookings", allBookingsPassengers);
			    
			    System.out.println(countOfPassengers);
			    
			    
			    //now getting all details
			    
			     List<UserDetail> user = repo.findByRegisterEmail(hereUser.getRegisterEmail());
			     
			     model.addAttribute("stored_email",user.get(0).getRegisterEmail());
			     model.addAttribute("stored_dob",user.get(0).getDob());
			     model.addAttribute("userName",user.get(0).getRegisterName());
                 
			     model.addAttribute("userId",currentUser.getId());
		
		
		return "boarding_pass"; 
		
	}

	
	
	@PostMapping("/food")
	public String tillSeatDetails(
			@RequestParam String capturedFlightName,
	        @RequestParam String capturedFlightId,
	        @RequestParam String capturedPrice,
	        @RequestParam String from,
	        @RequestParam String to,
	        @RequestParam String arrivalTime,
	        @RequestParam String departTime,
	        
	        
	        @RequestParam Long travelHours,
	        @RequestParam Long travelDays,
	        @RequestParam String boardingTime,
	        @RequestParam String parsedArrivalDate,
	        @RequestParam String parsedDepartDate,
	        
	        
	        
	        @RequestParam(name = "firstName_1", required = false) String firstName_1,
	        @RequestParam(name = "lastName_1", required = false) String lastName_1,
	        @RequestParam(name = "age_1", required = false) Integer age_1,
	        @RequestParam(name = "nation_1", required = false) String nation_1,

	        @RequestParam(name = "firstName_2", required = false) String firstName_2,
	        @RequestParam(name = "lastName_2", required = false) String lastName_2,
	        @RequestParam(name = "age_2", required = false) Integer age_2,
	        @RequestParam(name = "nation_2", required = false) String nation_2,

	        @RequestParam(name = "firstName_3", required = false) String firstName_3,
	        @RequestParam(name = "lastName_3", required = false) String lastName_3,
	        @RequestParam(name = "age_3", required = false) Integer age_3,
	        @RequestParam(name = "nation_3", required = false) String nation_3,

	        @RequestParam(name = "firstName_4", required = false) String firstName_4,
	        @RequestParam(name = "lastName_4", required = false) String lastName_4,
	        @RequestParam(name = "age_4", required = false) Integer age_4,
	        @RequestParam(name = "nation_4", required = false) String nation_4,
	        
	        @RequestParam(name = "seat1", required = false) String seatIdName1,
	        @RequestParam(name = "seat2", required = false) String seatIdName2,
	        @RequestParam(name = "seat3", required = false) String seatIdName3,
	        @RequestParam(name = "seat4", required = false) String seatIdName4,
	        
	        @RequestParam(name = "seatPrice1", required = false) Long seatPrice1,
	        @RequestParam(name = "seatPrice2", required = false) Long seatPrice2,
	        @RequestParam(name = "seatPrice3", required = false) Long seatPrice3,
	        @RequestParam(name = "seatPrice4", required = false) Long seatPrice4,
	        

	        HttpSession session,
	        Model model,
	        RedirectAttributes redirectAttributes
			) 
	{
		
		
	    
	    
	  // Add the form data to the model
	    model.addAttribute("capturedFlightName", capturedFlightName);
	    model.addAttribute("capturedFlightId", capturedFlightId);
	    model.addAttribute("capturedPrice", capturedPrice);
	    model.addAttribute("from", from);
	    model.addAttribute("to", to);
	    model.addAttribute("arrivalTime", arrivalTime);
	    model.addAttribute("departTime", departTime);
	    

		
	    if (firstName_1 != null && lastName_1 != null  && age_1 != null && nation_1!=null) {
	        model.addAttribute("firstName_1", firstName_1);
	        model.addAttribute("lastName_1", lastName_1);
	        model.addAttribute("age_1", age_1);
	        model.addAttribute("nation_1", nation_1);
	        
	     }
	    
	    if (firstName_2 != null && lastName_2 != null && age_2 != null && nation_2!=null) {
	    	model.addAttribute("firstName_2", firstName_2);
	        model.addAttribute("lastName_2", lastName_2);
	        model.addAttribute("age_2", age_2);
	        model.addAttribute("nation_2", nation_2);
	    }
	    
	    if (firstName_3 != null && lastName_3 != null  && age_3 != null && nation_3!=null ) {
	        model.addAttribute("firstName_3", firstName_3);
	        model.addAttribute("lastName_3", lastName_3);
	        model.addAttribute("age_3", age_3);
	        model.addAttribute("nation_3", nation_3);
	        
	    }
	    
	    if (firstName_4 != null && lastName_4 != null  && age_4 != null && nation_4!=null) {
	        model.addAttribute("firstName_4", firstName_4);
	        model.addAttribute("lastName_4", lastName_4);
	        model.addAttribute("age_4", age_4);
	        model.addAttribute("nation_4", nation_4);
	        
	    }
	    
        model.addAttribute("travelHours", travelHours);
	    
	    model.addAttribute("travelDays", travelDays);
	    
        model.addAttribute("boardingTime",boardingTime);
        
        
        model.addAttribute("parsedArrivalDate",parsedArrivalDate);
        
        model.addAttribute("parsedDepartDate",parsedDepartDate);
        
        
        model.addAttribute("seatIdName1",seatIdName1);
        model.addAttribute("seatIdName2",seatIdName2);
        model.addAttribute("seatIdName3",seatIdName3);
        model.addAttribute("seatIdName4",seatIdName4);
        
        long ans = 0;
        
        if(seatPrice1 != null) {
        	ans += seatPrice1; 
        }
        
        if(seatPrice2 != null) {
        	ans += seatPrice2; 
        }
        
        if(seatPrice3 != null) {
        	ans += seatPrice3; 
        }
        
        if(seatPrice4 != null) {
        	ans += seatPrice4; 
        }
        
        
        String seatPrice = String.valueOf(ans);
        model.addAttribute("seatPrice", seatPrice);
        
        long res = Long.valueOf(capturedPrice) + ans;
        String totalPrice = String.valueOf(res);
        model.addAttribute("totalPrice", totalPrice);
        
     // Create CurrentUserWhoBooked object
     CurrentlyLogged hereUser = logRepo.select();
     System.out.print(hereUser);
     model.addAttribute("userName",hereUser.getRegisterName());
        
   //now getting all details
    
     List<UserDetail> user = repo.findByRegisterEmail(hereUser.getRegisterEmail());
     
     model.addAttribute("stored_email",user.get(0).getRegisterEmail());
     model.addAttribute("stored_dob",user.get(0).getDob());
   
		
		System.out.println("Love yourself "+travelHours + " ------>>>>");
		
		return "shoppingBook";
		
	}
	
	
	
	@PostMapping("/passenger")
	public String passengerDetail(
	        @RequestParam String capturedFlightName,
	        @RequestParam String capturedPrice,
	        @RequestParam String capturedArrival,
	        @RequestParam String capturedDeparture,
	        @RequestParam String capturedSeatsAvailable,
	        
	        @RequestParam(name = "firstName_1", required = false) String firstName_1,
	        @RequestParam(name = "lastName_1", required = false) String lastName_1,
	        @RequestParam(name = "age_1", required = false) Integer age_1,
	        @RequestParam(name = "nation_1", required = false) String nation_1,

	        @RequestParam(name = "firstName_2", required = false) String firstName_2,
	        @RequestParam(name = "lastName_2", required = false) String lastName_2,
	        @RequestParam(name = "age_2", required = false) Integer age_2,
	        @RequestParam(name = "nation_2", required = false) String nation_2,

	        @RequestParam(name = "firstName_3", required = false) String firstName_3,
	        @RequestParam(name = "lastName_3", required = false) String lastName_3,
	        @RequestParam(name = "age_3", required = false) Integer age_3,
	        @RequestParam(name = "nation_3", required = false) String nation_3,

	        @RequestParam(name = "firstName_4", required = false) String firstName_4,
	        @RequestParam(name = "lastName_4", required = false) String lastName_4,
	        @RequestParam(name = "age_4", required = false) Integer age_4,
	        @RequestParam(name = "nation_4", required = false) String nation_4,

	        HttpSession session,
	        Model model,
	        RedirectAttributes redirectAttributes) {
		
		
		
	    // Split the flight name to capture flight ID and name
	    String[] f = capturedFlightName.split(" ");
	    String flightId = f[0];
	    String flightName = f[1];

	    // Split the arrival and departure details
	    String[] arrivalArr = capturedArrival.split(" ");
	    String from = arrivalArr[0];
	    String arrivalDate = arrivalArr[1];
	    String arrivalTime = arrivalArr[2];

	    String[] departArr = capturedDeparture.split(" ");
	    String to = departArr[0];
	    String departDate = departArr[1];
	    String departTime = departArr[2];

	    // Add the form data to the model
	    model.addAttribute("capturedFlightName", flightName);
	    model.addAttribute("capturedFlightId", flightId);
	    model.addAttribute("capturedPrice", capturedPrice);
	    model.addAttribute("from", from);
	    model.addAttribute("to", to);
	    model.addAttribute("arrivalTime", arrivalTime);
	    model.addAttribute("departTime", departTime);
	    model.addAttribute("arrivalDate", arrivalDate);
	    model.addAttribute("departDate", departDate);
	    
	    
	    
	    
       int num_of_passengers = 0;
	    
	    
	    
	    if (firstName_1 != null && lastName_1 != null  && age_1 != null && nation_1!=null) {
	        model.addAttribute("firstName_1", firstName_1);
	        model.addAttribute("lastName_1", lastName_1);
	        model.addAttribute("age_1", age_1);
	        model.addAttribute("nation_1", nation_1);
	        
	       
	        num_of_passengers++;
	        
	        
	    }
	    
	    if (firstName_2 != null && lastName_2 != null && age_2 != null && nation_2!=null) {
	    	model.addAttribute("firstName_2", firstName_2);
	        model.addAttribute("lastName_2", lastName_2);
	        model.addAttribute("age_2", age_2);
	        model.addAttribute("nation_2", nation_2);
	        
	        
	        num_of_passengers++;
	    }
	    
	    if (firstName_3 != null && lastName_3 != null  && age_3 != null && nation_3!=null ) {
	        model.addAttribute("firstName_3", firstName_3);
	        model.addAttribute("lastName_3", lastName_3);
	        model.addAttribute("age_3", age_3);
	        model.addAttribute("nation_3", nation_3);
	        
	        
	        num_of_passengers++;
	    }
	    
	    if (firstName_4 != null && lastName_4 != null  && age_4 != null && nation_4!=null) {
	        model.addAttribute("firstName_4", firstName_4);
	        model.addAttribute("lastName_4", lastName_4);
	        model.addAttribute("age_4", age_4);
	        model.addAttribute("nation_4", nation_4);
	        
	        
	        num_of_passengers++;
	    }
	    
	    // Inside your controller method
	    String[] departDateParts = departDate.split("-");
	    String[] arrivalDateParts = arrivalDate.split("-");

	    int departYear = Integer.parseInt(departDateParts[0]);
	    int departMonth = Integer.parseInt(departDateParts[1]);
	    int departDay = Integer.parseInt(departDateParts[2]);

	    int arrivalYear = Integer.parseInt(arrivalDateParts[0]);
	    int arrivalMonth = Integer.parseInt(arrivalDateParts[1]);
	    int arrivalDay = Integer.parseInt(arrivalDateParts[2]);

	    // Parse the dates
	    LocalDate startDate = LocalDate.of(arrivalYear, arrivalMonth, arrivalDay);
	    LocalDate endDate = LocalDate.of(departYear, departMonth, departDay);

	    // Calculate the difference
	    long daysDifference = ChronoUnit.DAYS.between(startDate, endDate);
         
	    //Parsing the time
	    String arrTime[] = arrivalTime.split(":");
	    String depTime[] = departTime.split(":");
	    
	    int ah = Integer.parseInt(arrTime[0]);
	    int am = Integer.parseInt(arrTime[1]);
	    
	    int dh = Integer.parseInt(depTime[0]);
	    int dm = Integer.parseInt(depTime[1]);
	    
	    // Parse the dates
	    LocalDateTime startDateTime = LocalDateTime.of(arrivalYear, arrivalMonth, arrivalDay,ah, am); // Assuming midnight
	    LocalDateTime endDateTime = LocalDateTime.of(departYear, departMonth, departDay, dh, dm); // Assuming midnight

	    // Calculate the difference
	    long hoursDifference = ChronoUnit.HOURS.between(startDateTime, endDateTime);
	    
	 // Calculate the remaining hours after accounting for full days
        long remainingHours = hoursDifference - (daysDifference * 24);
	    
	    model.addAttribute("travelHours", remainingHours);
	    
	    model.addAttribute("travelDays", daysDifference);
	    
	    
	    
	    
	    // Parse the arrival time for boarding time
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime parsedArrivalTime;
        try {
            parsedArrivalTime = LocalTime.parse(arrivalTime, timeFormatter);
        } catch (DateTimeParseException e) {
            // Handle parse exception (e.g., log error, set default time, etc.)
            parsedArrivalTime = LocalTime.of(0, 0);
        }

        // Add 20 minutes to the parsed arrival time
        LocalTime boardingTime = parsedArrivalTime.plusMinutes(20);

        // Format the boarding time back to string
        String boardingTimeString = boardingTime.format(timeFormatter);
        
        
        model.addAttribute("boardingTime",boardingTimeString);
        
        
        //Parsing the Arrival Date for converting in format of 9 Jun
        
        LocalDate date = LocalDate.parse(arrivalDate);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM yyyy");
        String formattedDate = date.format(formatter);
        model.addAttribute("parsedArrivalDate",formattedDate);
        
        LocalDate date2 = LocalDate.parse(departDate);
        String formattedDate2 = date2.format(formatter);
        model.addAttribute("parsedDepartDate",formattedDate2);
        
        //ends here

//	    // Set the bookings list to the currentUser object
//	    currentUser.setBookings(bookings);

	    //return "boarding_pass";
        
      //now getting all details
        CurrentlyLogged hereUser = logRepo.select();
        
        List<UserDetail> user = repo.findByRegisterEmail(hereUser.getRegisterEmail());
        
        model.addAttribute("userName",user.get(0).getRegisterName());
        model.addAttribute("stored_email",user.get(0).getRegisterEmail());
        model.addAttribute("stored_dob",user.get(0).getDob());
        
        System.out.println(num_of_passengers);
        
        model.addAttribute("number",num_of_passengers);
	    return "seatBook";
	}

	
	@PostMapping("/book_2")
	public String cap(@RequestParam String flightName, 
            @RequestParam String price, 
            @RequestParam String arrival, 
            @RequestParam String departure, 
            @RequestParam String seatsAvailable, 
            Model model) {
		
		System.out.println(flightName);
		System.out.println(price);
		System.out.println(arrival);
		System.out.println(departure);
		System.out.println(seatsAvailable);
		
		model.addAttribute("capturedFlightName", flightName);
        model.addAttribute("capturedPrice", price);
        model.addAttribute("capturedArrival", arrival);
        model.addAttribute("capturedDeparture", departure);
        model.addAttribute("capturedSeatsAvailable", seatsAvailable);
        
        
        //now getting all details
        CurrentlyLogged hereUser = logRepo.select();
        
        List<UserDetail> user = repo.findByRegisterEmail(hereUser.getRegisterEmail());
        
        model.addAttribute("userName",user.get(0).getRegisterName());
        model.addAttribute("stored_email",user.get(0).getRegisterEmail());
        model.addAttribute("stored_dob",user.get(0).getDob());
		
		return "passenger_booking";
		
	}
	@PostMapping("/book")
	public String book(@RequestParam String location, @RequestParam String Destlocation, @RequestParam String travel,
			@RequestParam String departure,
			Model model, HttpSession session) {
		System.out.println("Location: " + location);
        System.out.println("Destination Location: " + Destlocation);
        System.out.println("Number of Travelers: " + travel);
        System.out.println("Departure Date: " + departure);
        
        
        

       
        
        
        // fetch from database of admin like perform query operation first
        
        
        int seats_needed=Integer.parseInt(travel);
        List<FlightsAvailable> flights = adminService.fetch(location, Destlocation, departure, seats_needed);
        System.out.println(flights);
        
        
        model.addAttribute("flights", flights);
        
        //now getting all details
        CurrentlyLogged hereUser = logRepo.select();
        
        List<UserDetail> user = repo.findByRegisterEmail(hereUser.getRegisterEmail());
        
        model.addAttribute("userName",user.get(0).getRegisterName());
        model.addAttribute("stored_email",user.get(0).getRegisterEmail());
        model.addAttribute("stored_dob",user.get(0).getDob());
        
        
        
        
		return "flight_booking";
		
	}
	

	
	
}
