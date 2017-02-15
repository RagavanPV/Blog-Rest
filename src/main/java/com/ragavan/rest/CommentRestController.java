package com.ragavan.rest;

import org.apache.commons.mail.EmailException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ragavan.exception.ServiceException;
import com.ragavan.model.Article;
import com.ragavan.model.Comment;
import com.ragavan.model.User;
import com.ragavan.service.ArticleService;
import com.ragavan.service.CommentService;
import com.ragavan.service.UserService;
import com.ragavan.util.MailUtil;

@RestController
@RequestMapping("/comment")
public class CommentRestController {
	Comment comment = new Comment();
	CommentService commentService = new CommentService();

	@GetMapping("/save")
	public ResponseEntity<Object> store(@RequestBody Comment comment) {
		Article article = new Article();
		ArticleService articleService=new ArticleService();
		UserService userService = new UserService();
		User user = new User();
		article.setId(comment.getArticleId().getId());
		user.setId(comment.getUserId().getId());
		String authorEmail=articleService.getEmailByArticleId(article.getId());
		user.setEmailId(authorEmail);
		comment.setArticleId(article);
		comment.setCommentText(comment.getCommentText());
		comment.setUserId(user);
		try {
			user.setUserName(userService.functionGetUserName(user.getId()));
			commentService.saveService(comment);
			MailUtil.sendSimpleMail(comment);
		} catch (ServiceException | EmailException e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>("Successfully added comment",HttpStatus.OK);

	}
}
