package de.codecentric.psd.worblehat.web.controller;

import de.codecentric.psd.worblehat.domain.Book;
import de.codecentric.psd.worblehat.domain.BookService;
import de.codecentric.psd.worblehat.domain.Borrowing;
import de.codecentric.psd.worblehat.web.formdata.BookBorrowFormData;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/** Controller for BorrowingBook */
@RequestMapping("/borrow")
@Controller
public class BorrowBookController {

  private BookService bookService;

  @Autowired
  public BorrowBookController(BookService bookService) {
    this.bookService = bookService;
  }

  @RequestMapping(method = RequestMethod.GET)
  public void setupForm(final ModelMap model) {
    model.put("borrowFormData", new BookBorrowFormData());
  }

  @Transactional
  @RequestMapping(method = RequestMethod.POST)
  public String processSubmit(
      @ModelAttribute("borrowFormData") @Valid BookBorrowFormData borrowFormData,
      BindingResult result) {
    if (result.hasErrors()) {
      return "borrow";
    }
    var borrowings = new LinkedList<Borrowing>();
    for (var isbn : borrowFormData.getIsbn().split(" ")) {
      Set<Book> books = bookService.findBooksByIsbn(isbn);
      if (books.isEmpty()) {
        result.rejectValue("isbn", "noBookExists");
        return "borrow";
      }
      Optional<Borrowing> borrowing = bookService.borrowBook(isbn, borrowFormData.getEmail());
      if (borrowing.isPresent()) {
        borrowings.add(borrowing.get());
      }
    }
    if (!borrowings.isEmpty()) {
      return "home";
    }
    result.rejectValue("isbn", "noBorrowableBooks");
    return "borrow";
  }

  @ExceptionHandler(Exception.class)
  public String handleErrors(Exception ex, HttpServletRequest request) {
    return "home";
  }
}
