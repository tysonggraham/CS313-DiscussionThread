/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Authentication;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import static javax.servlet.SessionTrackingMode.URL;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author student
 */
@WebServlet(name = "Authenticate", urlPatterns = {"/Authenticate"})
public class Authenticate extends HttpServlet {

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
        String path = "";
        String filename = "";
        String resPath = "";
        URL url;
        if(!("OPENSHIFT_DATA_DIR".equals(null))){
            path = System.getenv("OPENSHIFT_DATA_DIR");
            filename = path + "/forumCredentials.txt";
        } else{
            ServletContext context = request.getSession().getServletContext();
            url = context.getResource("/WEB-INF/forumCredentials.txt");
            resPath = url.getPath();
//            File resFile = new File(url);
            //filename = resPath + "forumCredentials.txt";  
        }
        File file = new File(resPath + "forumCredentials.txt");
        UserHandler userHandler = new UserHandler(file);
        Thread threadUserHandler = new Thread(userHandler);
        threadUserHandler.start();
        String page;
        String action = request.getParameter("action");
        switch (action) {
            case "signin":
                page = "SignIn.jsp";
                String username = request.getParameter("username");
                String password = request.getParameter("password");
                int auth = userHandler.checkCredentials(username, password);
                switch (auth) {
                    case 1:
                        request.getSession().setAttribute("username", username);
                        page = "Welcome.jsp";
                        break;
                    case 0:
                        request.setAttribute("error", "Sorry, either your username, password, or both (is/are) invalid.");
                        break;
                    case -1:
                        request.setAttribute("error", "There is no existing user, please sign up.");
                        page = "SignUp.jsp";
                        break;
                    case -2:
                        // This case should never happen.
                        request.setAttribute("error", "Failed reading file " + filename + ".");
                        break;
                    default:
                        break;
                }

                request.getRequestDispatcher(page).forward(request, response);
                break;
            case "signout":
                request.getSession().invalidate();
                request.setAttribute("msg", "You have successfully logged out. We hope to see you again soon!");
                page = "SignIn.jsp";
                request.getRequestDispatcher(page).forward(request, response);
                break;
            case "signup":
                page = "SignUp.jsp";
                username = request.getParameter("username");
                password = request.getParameter("password");
                String verify = request.getParameter("verify");
                // First check if password and verify match:
                if (password.equals(verify)) {
                    int result = userHandler.saveUser(username, password);
                    switch (result) {
                        case 0:
                            request.setAttribute("error", "This username is already taken.");
                            break;
                        case 1:
                            request.setAttribute("msg", "You successfully signed up, you can now sign in.");
                            page = "SignIn.jsp";
                            break;
                        case -1:
                            // This case should never happen.
                            request.setAttribute("error", "Failed writing to " + filename + ".");
                            break;
                        case -2:
                            // This case should never happen.
                            request.setAttribute("error", "Failed reading file " + filename + ".");
                            break;
                        default:
                            break;
                    }
                }
                else {
                    request.setAttribute("error", "Your password doesn't match.");
                }

                request.getRequestDispatcher(page).forward(request, response);
                break;
            default:
                break;
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