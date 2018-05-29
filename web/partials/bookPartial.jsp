<%-- 
    Document   : bookPartial
    Created on : 23/04/2018, 12:49:55 PM
    Author     : J-Mo
--%>

<%@page import="cornflower.twf.utils.XmlFetcher"%>
<%@page import="java.io.StringWriter"%>
<%@page import="java.util.Arrays"%>
<%@page import="java.util.ArrayList"%>
<%@page import="cornflower.twf.model.BookCopy"%>
<% 
    Book book = books.getBook(request.getParameter("isbn"));
    
    if (book != null) {
        
        ArrayList<BookCopy> copies;
        
        if (filter != null && filter.equals("myListings")) {
            copies = book.getCopiesByLister(currentUser.getEmail());
        }
        else if (filter != null && filter.equals("unreserved")) {
            copies = book.getUnreservedCopies(reservations);
        }
        else {
            copies = book.getBookCopies();
        }
%>
<div class="card">
    <h4 class="card-header"><%= book.getTitle() %></h4>
    <div class="card-body">
        <h5 class="card-title">
            by <%= book.getAuthor() %> | 
            <%@include file="categoriesPartial.jsp" %>
        </h5>
        <p class="card-text"><b>ISBN:</b> <%= book.getIsbn() %></p>
        <p class="card-text"><b>Description:</b> <%= book.getDescription() %></p>
        <br>
        
        <%@include file="copyListPartial.jsp" %>
        
        <% if (currentUser != null) { %>
        <a class="btn btn-primary float-right" href="form.jsp?form=add_copy&isbn=<%= book.getIsbn() %>">Add Copy</a>
            <% if (copies.size() <= 0) { %>
                <form action="<%= request.getContextPath() %>/action/book" method="post">
                    <input type="hidden" name="action" value="delete"/>
                    <input type="hidden" name="isbn" value="<%= book.getIsbn() %>">
                    <button type="submit" name="submit" class="btn btn-danger">Remove Book</button>
                </form>
            <% }
           } %>
    </div>
</div>
<%
    } else {
%>
<div class="card">
    <div class="card-body">
        <h4 class="card-title text-center">Select a book to view from the list</h4>
    </div>
</div>
<% } %>
