package com.smart.Controller;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.smart.DAO.ContactRepository;
import com.smart.DAO.PayOrderRepository;
import com.smart.DAO.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.PayOrder;
import com.smart.entities.User;
import com.smart.util.Constants;
import com.smart.util.Message;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ContactRepository contactRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private PayOrderRepository payOrderRepository;

    @ModelAttribute
    public void addCommonData(Model model, Principal principal) {
        String userName = principal.getName();
        User user = userRepository.getUserByUserName(userName);
        model.addAttribute("user",user);
    }

    //Show DashBoard to the User
    @RequestMapping("/index")
    public String dashBoard(Model model, Principal principal) {
        model.addAttribute("title", "User Dashboard");
        return "normal/user-dashboard";
    }

    //Show Add Contact Form
    @RequestMapping("/add-contact")
    public String addContactForm(Model model) {
        model.addAttribute("title", "Add Contact");
        model.addAttribute("contact", new Contact());

        return "normal/add-contact-form";
    }
    // Process Add Contact Form and Save to Database
    @PostMapping("/process-contact")
    public String processAddContactForm(@ModelAttribute("contact") Contact contact, BindingResult result,
                                        @RequestParam("profileImage") MultipartFile file, Model model,
                                        Principal principal, HttpSession session) {
        try {
            if (result.hasErrors()) {
                model.addAttribute("contact", contact);
                return "normal/add-contact-form";
            }
            String userName = principal.getName();
            User user = this.userRepository.getUserByUserName(userName);
            if(file.isEmpty()) {
                System.out.println("File is Empty");
                contact.setImage("contact.png");
            } else {
                contact.setImage(file.getOriginalFilename());
                File saveFile = new ClassPathResource("static/images").getFile();
                Path path = Paths.get(saveFile.getAbsolutePath() +
                        File.separator+ file.getOriginalFilename());
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("File is Uploaded");
            }
            contact.setUser(user);
            user.getContacts().add(contact);
            this.userRepository.save(user);
            System.out.println("User Contact is Saved");
            session.setAttribute("message", new Message("Contact Added Successfully! ", "alert-success"));
        } catch(Exception e) {
            session.setAttribute("message", new Message("Failed to Add Contact! ", "alert-danger"));
            e.printStackTrace();

        }
        return "normal/add-contact-form";
    }

    // Show Contacts in Table
    @RequestMapping("/show-contacts/{page}")
    public String showContacts(@PathVariable("page") Integer page, Model model, Principal principal) {
        model.addAttribute("title","View Contacts");
        String userName = principal.getName();
        User user = this.userRepository.getUserByUserName(userName);
        Pageable pageable = PageRequest.of(page, 5);
        Page<Contact> contacts = this.contactRepository.findContactByUser(user.getUserId(), pageable);
        model.addAttribute("contacts",contacts);
        model.addAttribute("currentPage",page);
        model.addAttribute("totalPages",contacts.getTotalPages());
        return "normal/show-contacts";
    }

    //View Details of Single Contact
    @RequestMapping("/{cid}/contact")
    public String showContactDetails(@PathVariable("cid") Integer contactId, Model model, Principal principal) {
        Contact contact = this.contactRepository.findById(contactId).get();
        String userName = principal.getName();
        User user = this.userRepository.getUserByUserName(userName);
        if(user.getUserId()==contact.getUser().getUserId()) {
            model.addAttribute("contact", contact);
            model.addAttribute("title",contact.getName());
        }
        return "normal/contact-details";
    }

    // Delete a Contact
    @GetMapping("/delete-contact/{contactId}")
    public String deleteContact(@PathVariable("contactId") Integer contactId, Principal principal, HttpSession session) {
        Contact contact = this.contactRepository.findById(contactId).get();
        String userName = principal.getName();
        User user = this.userRepository.getUserByUserName(userName);
        if(user.getUserId()==contact.getUser().getUserId()) {
            contact.setUser(null);

            try {
                File saveFile = new ClassPathResource("static/images").getFile();
                Path path = Paths.get(saveFile.getAbsolutePath() +
                        File.separator+contact.getImage());
                Files.delete(path);

                user.getContacts().remove(contact);
                this.userRepository.save(user);

                session.setAttribute("message", new Message("Contact Deleted Successfully...", "success"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "redirect:/user/show-contacts/0";
    }

    // Update Contact
    @PostMapping("/update/{contactId}")
    public String updateContactUserForm(@PathVariable("contactId") Integer contactId, Model model) {
        model.addAttribute("title","Update Form");
        Contact contact = this.contactRepository.findById(contactId).get();
        model.addAttribute("contact", contact);
        return "normal/update-form";
    }

    // Process Update Contact User Form
    @RequestMapping(value = "/process-update",method = RequestMethod.POST)
    public String processUpdateContactForm(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
                                Model model, HttpSession session, Principal principal) {
        try {
            Contact oldContact = this.contactRepository.findById(contact.getContactId()).get();
            if(!file.isEmpty()) {
                //Delete Old Image File
                File deleteFile = new ClassPathResource("static/images").getFile();
                File delFile = new File(deleteFile, oldContact.getImage());
                delFile.delete();

                // Get New Image File and Save
                File saveFile = new ClassPathResource("static/images").getFile();
                Path path = Paths.get(saveFile.getAbsolutePath() +
                        File.separator+ file.getOriginalFilename());
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                contact.setImage(file.getOriginalFilename());
            } else {
                contact.setImage(oldContact.getImage());
            }
            String userName = principal.getName();
            User user = this.userRepository.getUserByUserName(userName);
            contact.setUser(user);
            this.contactRepository.save(contact);
            session.setAttribute("message", new Message("Your Contact is Updated...","success"));
        }catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/user/"+contact.getContactId()+"/contact";
    }

    //View User Profile
    @RequestMapping("/profile")
    public String viewProfile(Model model) {
        model.addAttribute("title","User Profile");
        return "normal/profile";
    }

    @GetMapping("/delete-user/{userId}")
    public String deleteUser(@PathVariable("userId") Integer userId, Principal principal, HttpSession session) {
        User user = this.userRepository.findById(userId).get();
        String userName = principal.getName();
        User userDetail = this.userRepository.getUserByUserName(userName);
        if(userDetail.getUserId()==user.getUserId()) {
        	this.userRepository.delete(user);
        }
        return "redirect:/signin";
    }




    
    @PostMapping("/{userId}/update")
    public String updateUser() {
        return "update-user";
    }

    @RequestMapping("/settings")
    public String setting() {
        return "normal/settings";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam("oldPassword") String oldPassword,
                                 @RequestParam("newPassword")String newPassword, Principal principal,
                                 HttpSession session) {

        String userName = principal.getName();
        User user = this.userRepository.getUserByUserName(userName);
        if(this.bCryptPasswordEncoder.matches(oldPassword, user.getPassword())) {
            user.setPassword(bCryptPasswordEncoder.encode(newPassword));
            this.userRepository.save(user);
            session.setAttribute("message",new Message("Your Password is Successfully Changed","alert-success"));
        } else {
            session.setAttribute("message",new Message("Enter Valid Old Password","alert-danger"));
            return "redirect:/user/settings";
        }
        return "redirect:/user/index";
    }

    @PostMapping("/create-order")
    @ResponseBody
    public String createOrderForPayment(@RequestBody Map<String, Object> data, Principal principal) {

        int amount = Integer.parseInt(data.get("amount").toString());
        if (amount>0) {
            try {
                RazorpayClient razorpayClient = new RazorpayClient(Constants.RAZOR_PAY_KEY, Constants.RAZOR_PAY_SECRET);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("amount",amount);
                jsonObject.put("currency","PKR");
                jsonObject.put("receipt","txn_1513000");
                Order order = razorpayClient.orders.create(jsonObject);
                PayOrder payOrder = new PayOrder();
                payOrder.setAmount(order.get("amount").toString());
                payOrder.setPaymentId(null);
                payOrder.setStatus("created");
                payOrder.setUser(this.userRepository.getUserByUserName(principal.getName()));
                payOrder.setReceipt(order.get("receipt"));
                this.payOrderRepository.save(payOrder);
                return order.toString();
            } catch (RazorpayException | JSONException e) {
                e.printStackTrace();
            }
        }

        return "Done";
    }



    @PostMapping("/update-order")
    public ResponseEntity<?> updatePayment(@RequestBody Map<String, Object> data) {
        PayOrder payOrder = this.payOrderRepository.findPayOrderByOrderId(data.get("orderId").toString());
        payOrder.setPaymentId(data.get("paymentId").toString());
        payOrder.setStatus(data.get("status").toString());
        this.payOrderRepository.save(payOrder);
        return ResponseEntity.ok(Map.of("msg","updated"));
    }
}
