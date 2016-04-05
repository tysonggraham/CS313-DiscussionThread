/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Forum;

import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author student
 */
@WebServlet(name = "Forum", urlPatterns = {"/Forum"})
public class Forum extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Folder where I will save files in local environment.
        // String path = getServletConfig().getServletContext().getRealPath("WEB-INF");
        // Folder where you can save files in Openshift.
        String path = System.getenv("OPENSHIFT_DATA_DIR");
        String filename = path + "/comments.txt";
        File file = new File(filename);
        CommentHandler commentHandler = new CommentHandler(file);
        Thread threadCommentHandler = new Thread(commentHandler);
        threadCommentHandler.start();
        List<Comment> comments;
        String page;
        String action = request.getParameter("action");
        String username = (String) request.getSession().getAttribute("username");
        if (username == null) {
            page = "SignIn.jsp";
            request.setAttribute("error", "Please, sign in to access this section of the site.");
            request.getRequestDispatcher(page).forward(request, response);
        } else {
            switch (action) {
                case "post_comment":
                    String content = request.getParameter("comment");
                    content = content.replaceAll("\n", " ").replaceAll("\r", " ");
                    Boolean result = commentHandler.saveComment(username, content);
                    if (result) {
                        request.setAttribute("msg", "You successfully posted your message.");
                    } else {
                        request.setAttribute("error", "Failed writing to file " + filename + ".");
                    }
                case "view_comments":
                    page = "Comments.jsp";
                    try {
                        comments = commentHandler.getComments();
                        if (comments == null) {
                            request.setAttribute("msg", "There isn't any post yet on the forum.");
                        } else {
                            request.setAttribute("comments", comments);
                        }
                    } catch (IOException e) {
                        request.setAttribute("info", "Unable to load past messages.");
                    }
                    request.getRequestDispatcher(page).forward(request, response);
                    break;
                default:
                    break;
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}