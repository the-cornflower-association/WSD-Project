/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cornflower.twf.action;

import cornflower.twf.model.Book;
import cornflower.twf.model.BookCopy;
import cornflower.twf.model.Books;
import cornflower.twf.model.Lister;
import cornflower.twf.model.Reservation;
import cornflower.twf.model.Reservations;
import cornflower.twf.utils.ActionController;
import cornflower.twf.utils.AppMessage;
import cornflower.twf.utils.Validator;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author J-Mo
 */
@WebServlet("/action/copy")
public class CopyServlet extends javax.servlet.http.HttpServlet {
    
    // Fields
    ActionController ac;
    HttpSession session;
    Books books;
    
    private void setFields(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            ac = new ActionController(request.getServletContext());
            session = request.getSession();
            books = ac.getBooks();
        } catch (Exception ex) {
            session.setAttribute("appMessage", new AppMessage("danger", "Something went wrong"));
            response.sendRedirect("../index.jsp");
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        setFields(request, response);
        
        String action = request.getParameter("action");
        
        if (action != null && action.equalsIgnoreCase("delete")) {
            doDestroy(request, response);
        }
        else {
            doCreate(request, response);
        }
    }
    
    private void doDestroy(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Reservations reservations = ac.getReservations();

        String isbn = request.getParameter("isbn");
        int copyId = Integer.parseInt(request.getParameter("copyId"));
        Lister lister = (Lister) session.getAttribute("lister");

        Book book = books.getBook(isbn);
        BookCopy copy = book.getBookCopy(copyId);
        if (copy.getLister().equals(lister.getEmail())) {
            // Remove the book
            book.removeBookCopy(copy);
            books.setBook(isbn, book);
            try {
                ac.commitBookData(books);
            } catch (Exception ex) {
                session.setAttribute("appMessage", new AppMessage("danger", "Something went wrong while committing your data"));
                response.sendRedirect("../index.jsp");
            }

            // Remove any reservations for the book copy
            Reservation reservation = reservations.getReservation(isbn, copyId);
            if (reservation != null) {
                reservations.removeReservation(reservation);
            }
            try {
                ac.commitReservationData(reservations);
            } catch (Exception ex) {
                session.setAttribute("appMessage", new AppMessage("danger", "Something went wrong while committing your data"));
                response.sendRedirect("../index.jsp");
            }
        }
        else {
            session.setAttribute("appMessage", new AppMessage("warning", "You may only delete copies you have listed"));
            response.sendRedirect(request.getHeader("Referer"));
        }

        session.setAttribute("appMessage", new AppMessage("success", "Removed book copy #" + copyId + " for \"" + book.getTitle() + "\""));
        response.sendRedirect("../index.jsp");
    }
    
    private void doCreate(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Validator v = new Validator();

        // Get the book
        String isbn = request.getParameter("isbn");
        Book book = books.getBook(isbn);

        // Get copy attributes
        try {
            String condition = request.getParameter("condition");
            Integer edition = Integer.valueOf(request.getParameter("edition"));
            Integer year = Integer.valueOf(request.getParameter("year"));
            String publisher = request.getParameter("publisher");
            Lister lister = (Lister) session.getAttribute("lister");

            // Validation
            boolean validationsFail = false;

            AppMessage conditionError = v.validText(condition);
            if (conditionError != null) {
                session.setAttribute("appMessage", conditionError);
                validationsFail = true;
            }

            AppMessage editionError = v.validNumber(String.valueOf(edition));
            if (editionError != null) {
                session.setAttribute("appMessage", editionError);
                validationsFail = true;
            }

            AppMessage yearError = v.validYear(String.valueOf(year));
            if (yearError != null) {
                session.setAttribute("appMessage", yearError);
                validationsFail = true;
            }

            AppMessage publisherError = v.validText(publisher);
            if (publisherError != null) {
                session.setAttribute("appMessage", publisherError);
                validationsFail = true;
            }

            if (condition.equals("") || edition == null || year == null || publisher.equals("")) {
                session.setAttribute("appMessage", new AppMessage("danger", "Please fill in all required fields."));
                validationsFail = true;
            }

            if (validationsFail) {
                response.sendRedirect(request.getHeader("Referer"));
            } else {
                // Add the copy
                BookCopy copy = new BookCopy(book.getNewId(), condition, edition, year, publisher, lister.getEmail());
                book.addBookCopy(copy);
                books.setBook(isbn, book);
                ac.commitBookData(books);

                session.setAttribute("appMessage", new AppMessage("success", "Added new book copy for \"" + book.getTitle() + "\""));
                response.sendRedirect("../index.jsp");
            }
        } catch (Exception e) {
            session.setAttribute("appMessage", new AppMessage("danger", "Some validations failed, this is the warning message"));
            response.sendRedirect(request.getHeader("Referer"));
        }
    }
}