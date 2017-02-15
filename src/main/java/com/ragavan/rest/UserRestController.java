package com.ragavan.rest;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.mail.EmailException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ragavan.exception.ServiceException;
import com.ragavan.model.Role;
import com.ragavan.model.User;
import com.ragavan.service.RoleService;
import com.ragavan.service.UserService;
import com.ragavan.util.ActivationUtil;
import com.ragavan.util.MailUtil;

@RestController
@RequestMapping("/users")
public class UserRestController {

	private UserService userService = new UserService();
	PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	@GetMapping
	public List<User> index() {
		List<User> userList = userService.listService();
		return userList;
	}

	@GetMapping("/delete/{id}")
	public ResponseEntity<Object> delete(@PathVariable("id") int id,HttpServletResponse response) {
		try {
			userService.deleteService(id);
			response.sendRedirect("localhost:8080/users/");
		} catch (ServiceException | IOException e) {
			return new ResponseEntity<>("Unable to delete",HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>("Successfully deleted",HttpStatus.OK);
	}

	@GetMapping("/activate")
	public ResponseEntity<Object> activate(@RequestParam("code") String code, @RequestParam("userName") String userName) {
		User user = new User();
		user.setActivationCode(code);
		user.setUserName(userName);
		try {
			userService.activateUserService(user);
		} catch (ServiceException e) {
			return new ResponseEntity<>("Unable to activate",HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>("Successfully activated",HttpStatus.OK);

	}

	@GetMapping("/update/{id}")
	public List<Role> update(@PathVariable("id") int id) {
		User user = new User();
		user.setId(id);
		RoleService roleService = new RoleService();
		List<Role> roleList = roleService.listService();
		return roleList;
	}

	@GetMapping("/updateUser")
	public ResponseEntity<Object> update(@RequestParam("userName") String name, @RequestParam("password") String password,
			@RequestParam("emailId") String emailid, @RequestParam("role") int roleId) {
		User user = new User();
		user.setUserName(name);
		user.setPassword(password);
		user.setEmailId(emailid);
		Role role = new Role();
		role.setId(roleId);
		user.setRoleId(role);
		try {
			userService.updateService(user);
		} catch (ServiceException e) {
			return new ResponseEntity<>("Unable to update",HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>("Successfully updated",HttpStatus.OK);
	}

	@GetMapping("/updateRole")
	public ResponseEntity<Object> updateRole(@RequestParam("id") int id, @RequestParam("role") int roleId) {
		User user = new User();
		Role role = new Role();
		role.setId(roleId);
		user.setRoleId(role);
		user.setId(id);
		try {
			userService.updateRoleService(user);
		} catch (ServiceException e) {
			return new ResponseEntity<>("Unable to update",HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>("Successfully updated",HttpStatus.OK);
	}

	@PostMapping("/save")
	public ResponseEntity<Object> store(@RequestBody User u) {
		User user = new User();
		user.setUserName(u.getUserName());
		user.setPassword(passwordEncoder.encode(u.getPassword()));
		user.setActivationCode(ActivationUtil.activateString());
		user.setEmailId(u.getEmailId());
		int result = 0;
		try {
			result = userService.saveService(user);
			MailUtil.sendActivationMail(user);

		} catch (ServiceException e) {
		} catch (EmailException e) {
			e.printStackTrace();
		}
		if (result == 1) {
			return new ResponseEntity<>("Unable to Register",HttpStatus.BAD_REQUEST);
		} else
			return new ResponseEntity<>("Successfully Registered",HttpStatus.OK);
	}

	@PostMapping("/login")
	public ResponseEntity<Object> store(@RequestParam("userName") String name,
			@RequestParam("password") String password) {
		User userLogin = new User();
		userLogin.setUserName(name);
		boolean result = false;
		boolean isactive = false;
		int roleid = 0;
		int userid = 0;
		try {
			if (userService.functionGetUserId(name) != 0) {
				String hashedPassword = userService.getHashedPassword(name);
				roleid = userService.functionGetRoleId(name);
				result = passwordEncoder.matches(password, hashedPassword);
				userid = userService.functionGetUserId(name);
				isactive = userService.functionIsValidUserService(name);
				if (result) {
					Role role = new Role();
					role.setId(roleid);
					userLogin.setRoleId(role);
					userLogin.setId(userid);
					userLogin.setEmailId(userService.functionGetUserEmail(userid));
				}
			}
		} catch (ServiceException e) {
			return new ResponseEntity<>("Unable to login "+e,HttpStatus.BAD_REQUEST);
		}

		if (result) {
			if (isactive) {
				if (roleid == 1) {
					return new ResponseEntity<>("Successfully Logged in as admin",HttpStatus.OK);

				} else {
					return new ResponseEntity<>("Successfully Logged in as user",HttpStatus.OK);
				}
			} else
				return new ResponseEntity<>("Activate your account",HttpStatus.CONFLICT);
		} else
			return new ResponseEntity<>("Invalid account",HttpStatus.BAD_REQUEST);
	}
}
