package com.smart.Controller;

import com.smart.DAO.UserRepository;
import com.smart.entities.User;
import com.smart.util.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;

@Controller
public class HomeController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;



    @RequestMapping("/")
    public String home(Model model) {
        model.addAttribute("title","Home - Smart Contact Manager");
        return "home";
    }

    @RequestMapping("/about")
    public String about(Model model) {
        model.addAttribute("title","Home - Smart Contact Manager");
        return "about";
    }

    @RequestMapping("/signup")
    public String signup(Model model) {
        model.addAttribute("title","Home - Smart Contact Manager");
        model.addAttribute("user",new User());
        return "signup";
    }

    @RequestMapping(value="/register", method= RequestMethod.POST)
    public String registerUser(@ModelAttribute("user")User user, BindingResult result,
                               @RequestParam(value="terms",
            defaultValue="false") boolean agreement, Model model,
                               HttpSession session) {
        try {
            if(!agreement) {
                throw new Exception("You have not agreed to Terms and Conditions");
            }
            if(result.hasErrors()) {
                model.addAttribute("user",user);
                return "signup";
            }
            user.setRole("ROLE_USER");
            user.setEnabled(true);
            user.setImageUrl("default.png");
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            User res = userRepository.save(user);
            model.addAttribute("user", new User());
            session.setAttribute("message",new Message("Successfully Registered! ","alert-success"));
            return "signup";

        }catch(Exception e) {
            e.printStackTrace();
            model.addAttribute("user",user);
            session.setAttribute("message",
                    new Message("Something went wrong! "+e.getMessage(),"alert-danger"));
            return "signup";
        }
    }

    @RequestMapping("/signin")
    public String login(Model model) {
        model.addAttribute("title","Login Page");
        return "login";
    }

}
