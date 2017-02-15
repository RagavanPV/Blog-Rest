package com.ragavan.rest;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ragavan.exception.ServiceException;
import com.ragavan.model.Article;
import com.ragavan.model.Category;
import com.ragavan.model.User;
import com.ragavan.service.CategoryService;
import com.ragavan.service.UserService;

@RestController
@RequestMapping("/category")
public class CategoryRestController {
	CategoryService categoryService = new CategoryService();

	@GetMapping("/list")
	public List<Category> index(@RequestParam("userId") int userId) {
		return categoryService.listByUserIdService(userId);
		
	}

	@GetMapping("/publish")
	public ResponseEntity<Object> publish(@RequestBody Category category,String title) {
		UserService service = new UserService();
		String userName = null;
		Category cate = new Category();
		cate.setName(category.getName());
		User user = new User();
		user.setId(category.getId());
		cate.setUserId(user); 
		Article article = new Article();
		article.setTitle(title);
		article.setUserId(user);
		try {
			userName = service.functionGetUserName(user.getId());
			categoryService.insertCategory(article, cate);
		} catch (ServiceException e) {
			e.printStackTrace();
			return new ResponseEntity<>("Unable to publish",HttpStatus.FORBIDDEN);
		}
		return new ResponseEntity<>("successfully published",HttpStatus.OK);
	}
	
	@PostMapping("/post")
	public String post(@RequestParam("list") String categoryList,HttpSession httpSession) throws JSONException{
		UserService service = new UserService();
		String userName = null;
		User u=(User) httpSession.getAttribute("LOGGED_USER");
		JSONObject category=new JSONObject(categoryList);
		JSONArray tags=category.getJSONArray("category");
		for(int i=0;i<tags.length();i++){
			JSONObject obj=tags.getJSONObject(i);
			Category cate = new Category();
			cate.setName(obj.get("tag").toString());
			
			cate.setUserId(u); 
			Article article = new Article();
			article.setTitle(category.getString("title"));
			article.setUserId(u);
			try {
				userName = service.functionGetUserName(u.getId());
				categoryService.insertCategory(article, cate);
			} catch (ServiceException e) {
				e.printStackTrace();
			
			}
		}
		return "../articles/user?userName=" + userName;
	}
	@PostMapping("/postTest")
	public void postTest(@RequestParam("categoryList") String categoryList) throws JSONException{
		
		JSONObject category=new JSONObject(categoryList);
		JSONArray tags=category.getJSONArray("category");
		for(int i=0;i<tags.length();i++){
			tags.getJSONObject(i);
		}
		
	}

}
