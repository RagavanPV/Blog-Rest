package com.ragavan.rest;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ragavan.exception.ServiceException;
import com.ragavan.model.Article;
import com.ragavan.model.Comment;
import com.ragavan.model.User;
import com.ragavan.service.ArticleService;
import com.ragavan.service.CategoryService;
import com.ragavan.service.CommentService;
import com.ragavan.service.UserService;

@RestController
@RequestMapping("/articles")
public class ArticleRestController {
	private Article article = new Article();

	private ArticleService articleService = new ArticleService();

	@GetMapping
	public List<Article> index() {
		List<Article> articleList;
		articleList = articleService.listService();
		return articleList;
	}

	@GetMapping("viewbycategory")
	public List<Article> indexByCategory(@RequestParam("category") String category) {
		CategoryService categoryService = new CategoryService();
		return categoryService.viewByCategoryService(category);
	}

	@GetMapping("comments")
	public List<Comment> indexComments(ModelMap modelMap, @RequestParam("articleId") int articleId) {
			CommentService commentService = new CommentService();
			return commentService.listByArticleIdService(articleId);
	}

	@GetMapping("/other")
	public List<Article> indexOtherUsers(@RequestParam("userId") int userId) {
			List<Article> articleList = null;
			try {
				articleList = articleService.listOtherUserService(userId);
			} catch (ServiceException e) {
				e.printStackTrace();
			}
			return articleList;
	}

	@GetMapping("/user")
	public List<Article> index(@RequestParam("userName") String name) {
			UserService userService = new UserService();
			List<Article> articleList = null;
			try {
				articleList = articleService.listByUserService(userService.functionGetUserId(name));
			} catch (ServiceException e) {
				e.printStackTrace();
			}
			return articleList;
	}

	@GetMapping("/publish")
	public ResponseEntity<Object> publish(@RequestBody Article article) {
			article.setTitle(article.getTitle());
			article.setContent(article.getContent());

			User user = new User();
			user.setId(article.getUserId().getId());
			article.setUserId(user);
			try {

				articleService.publishArticleService(article, user);
			} catch (ServiceException e) {
				return new ResponseEntity<>("Error in publishing :"+e,HttpStatus.OK);
			}
			return new ResponseEntity<>("Successfully published",HttpStatus.OK);
	}

	@GetMapping("/update")
	public String update(@RequestParam("id") int id) {
			article.setId(id);
			article.getId();
			return "../updatearticle.jsp";
	}

	@GetMapping("/updateArticle")
	public String update(@RequestBody Article article) {
			String userName = articleService.getUserIdByArticleId(article.getId());
			try {
				articleService.updateByIdService(article);
			} catch (ServiceException e) {
				e.printStackTrace();
			}
			return "redirect:../articles/user?userName=" + userName;
	}

	@GetMapping("/delete")
	public String delete(@RequestParam("id") int id) {
			article.setId(id);
			String userName = articleService.getUserIdByArticleId(id);
			try {
				articleService.deleteArticleService(article);
			} catch (ServiceException e) {
				e.printStackTrace();
			}
			return "redirect:../articles/user?userName=" + userName;
	}

}
