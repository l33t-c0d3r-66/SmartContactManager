package com.smart.Controller;

import com.smart.DAO.UserRepository;
import com.smart.entities.User;
import com.smart.service.EmailService;
import com.smart.util.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.Random;

@Controller
public class ForgotController {
    Random random = new Random(1000);

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailService emailService;
    @RequestMapping("/forget-password")
    public String forgetPasswordForm() {
        return "forget-password-form";
    }

    @PostMapping("/send-otp")
    public String sendOTP(@RequestParam("email")String email, HttpSession session) {
        int otp = random.nextInt(999999);
        if(email!=null) {
            String subject = "OTP From Smart Contact Manager";
            String message = "OTP is "+otp;
            String to = email;
            boolean isSent = emailService.sendEmail(subject, message, to);
            if (isSent) {
                session.setAttribute("serverotp",otp);
                session.setAttribute("email",email);
                session.setAttribute("message", new Message("OTP Sent Successfully","alert-success"));
                return "otp-verification";
            }
        }
        session.setAttribute("message",new Message("Invalid OTP Please Check your Email","alert-danger"));
        return "forget-password-form";
    }


    @PostMapping("/verify-otp")
    public String verifyOTP(@RequestParam("otp")Integer otp, HttpSession session) {
        Integer serverOtp = (int) session.getAttribute("serverotp");
        String email  =  (String) session.getAttribute("email");
        if(serverOtp.equals(otp)) {
            User user = this.userRepository.getUserByUserName(email);
            if(user == null) {
                session.setAttribute("message",new Message("User with email "+email+" doesn't exist","alert-danger"));
                return "forget-password-form";
            } else {
                return "change-password";
            }
        }
        session.setAttribute("message",new Message("Wrong OTP!","alert-danger"));
        return "otp-verification";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam("newPassword")String newPassword, HttpSession session) {
        String email = (String) session.getAttribute("email");
        User user = this.userRepository.getUserByUserName(email);
        user.setPassword(bCryptPasswordEncoder.encode(newPassword));
        this.userRepository.save(user);
        session.setAttribute("message",new Message("You password is changed successfully","alert-sucess"));
        return "redirect:/signin?change=Password changed successfully";
    }

}
