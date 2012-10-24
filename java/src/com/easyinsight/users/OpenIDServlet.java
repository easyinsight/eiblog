package com.easyinsight.users;

import com.easyinsight.database.Database;
import com.easyinsight.database.EIConnection;
import com.easyinsight.html.RedirectUtil;
import com.easyinsight.security.SecurityUtil;
import com.google.step2.AuthRequestHelper;
import com.google.step2.AuthResponseHelper;
import com.google.step2.ConsumerHelper;
import com.google.step2.Step2;
import com.google.step2.discovery.IdpIdentifier;
import com.google.step2.openid.ui.UiMessageRequest;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.openid4java.OpenIDException;
import org.openid4java.consumer.InMemoryConsumerAssociationStore;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.ParameterList;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;

/**
 * User: jamesboe
 * Date: 10/17/12
 * Time: 3:55 PM
 */
public class OpenIDServlet extends HttpServlet {

    protected ConsumerHelper consumerHelper;
    protected String realm;
    protected String returnToPath;
    protected String homePath;

    /**
     * Init the servlet.  For demo purposes, we're just using an in-memory version
     * of OpenID4Java's ConsumerAssociationStore.  Production apps, particularly those
     * in a clustered environment, should consider using an implementation backed by
     * shared storage (memcache, DB, etc.)
     *
     * @param config
     * @throws ServletException
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        returnToPath = getInitParameter("return_to_path", "/openid");
        homePath = getInitParameter("home_path", "/");
        realm = getInitParameter("realm", null);
        ConsumerFactory factory = new ConsumerFactory(new InMemoryConsumerAssociationStore());
        consumerHelper = factory.getConsumerHelper();
    }

    /**
     * Either initiates a login to a given provider or processes a response from an IDP.
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String domain = req.getParameter("hd");
        System.out.println("domain = " + domain);
        System.out.println("callback = " + req.getParameter("callback"));
        if (domain != null) {
            req.getSession().setAttribute("googleDomain", req.getParameter("hd"));
            req.getSession().setAttribute("googleCallbackURL", req.getParameter("callback"));
            // User attempting to login with provided domain, build and OpenID request and redirect
            try {
                AuthRequest authRequest = startAuthentication(domain, req);
                String url = authRequest.getDestinationUrl(true);
                resp.sendRedirect(url);
            } catch (OpenIDException e) {
                throw new ServletException("Error initializing OpenID request", e);
            }
        } else {
            // This is a response from the provider, go ahead and validate
            doPost(req, resp);
        }
    }

    /**
     * Handle the response from the OpenID Provider.
     *
     * @param req Current servlet request
     * @param resp Current servlet response
     * @throws ServletException if unable to process request
     * @throws IOException if unable to process request
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            System.out.println("handling post...");
            UserServiceResponse response = completeAuthentication(req);
            if (response.isGoogleAuth()) {
                System.out.println("redirecting...");
                resp.sendRedirect(RedirectUtil.getURL(req, "/app/googleAppsWelcome.jsp"));
            } else {
                SecurityUtil.populateSession(req.getSession(), response);
                resp.sendRedirect(homePath);
            }
        } catch (OpenIDException e) {
            throw new ServletException("Error processing OpenID response", e);
        }
    }

    /**
     * Builds an auth request for a given OpenID provider.
     *
     * @param op OpenID Provider URL.  In the context of Google Apps, this can be a naked domain
     *           name such as "saasycompany.com".  The length of the domain can exceed 100 chars.
     * @param request Current servlet request
     * @return Auth request
     * @throws org.openid4java.OpenIDException if unable to discover the OpenID endpoint
     */
    AuthRequest startAuthentication(String op, HttpServletRequest request)
            throws OpenIDException {
        IdpIdentifier openId = new IdpIdentifier(op);

        String realm = realm(request);
        String returnToUrl = "https://staging.easy-insight.com/app/openid";

        System.out.println("realm = " + realm);
        System.out.println("return url = " + returnToUrl);

        AuthRequestHelper helper = consumerHelper.getAuthRequestHelper(openId, returnToUrl);
        addAttributes(helper);

        HttpSession session = request.getSession();
        AuthRequest authReq = helper.generateRequest();
        authReq.setRealm(realm);

        UiMessageRequest uiExtension = new UiMessageRequest();
        uiExtension.setIconRequest(true);
        authReq.addExtension(uiExtension);

        session.setAttribute("discovered", helper.getDiscoveryInformation());
        return authReq;
    }

    /**
     * Validates the response to an auth request, returning an authenticated user object if
     * successful.
     *
     * @param request Current servlet request
     * @return User
     * @throws org.openid4java.OpenIDException if unable to verify response
     */

    UserServiceResponse completeAuthentication(HttpServletRequest request)
            throws OpenIDException {
        HttpSession session = request.getSession();
        ParameterList openidResp = Step2.getParameterList(request);
        String receivingUrl = currentUrl(request);
        DiscoveryInformation discovered =
                (DiscoveryInformation) session.getAttribute("discovered");


        AuthResponseHelper authResponse =
                consumerHelper.verify(receivingUrl, openidResp, discovered);
        if (authResponse.getAuthResultType() == AuthResponseHelper.ResultType.AUTH_SUCCESS) {
            return onSuccess(authResponse, request);
        }
        return onFail(authResponse, request);
    }

    /**
     * Adds the requested AX attributes to the request
     *
     * @param helper Request builder
     */
    void addAttributes(AuthRequestHelper helper) {
        helper.requestAxAttribute(Step2.AxSchema.EMAIL, true)
                .requestAxAttribute(Step2.AxSchema.FIRST_NAME, true)
                .requestAxAttribute(Step2.AxSchema.LAST_NAME, true);
    }

    /**
     * Reconstructs the current URL of the request, as sent by the user
     *
     * @param request Current servlet request
     * @return URL as sent by user
     */
    String currentUrl(HttpServletRequest request) {
        return Step2.getUrlWithQueryString(request);
    }

    /**
     * Gets the realm to advertise to the IDP.  If not specified in the servlet configuration.
     * it dynamically constructs the realm based on the current request.
     *
     * @param request Current servlet request
     * @return Realm
     */
    String realm(HttpServletRequest request) {
        if (StringUtils.isNotBlank(realm)) {
            return realm;
        } else {
            return baseUrl(request);
        }
    }

    /**
     * Gets the <code>openid.return_to</code> URL to advertise to the IDP.  Dynamically constructs
     * the URL based on the current request.
     * @param request Current servlet request
     * @return Return to URL
     */
    String returnTo(HttpServletRequest request) {
        return new StringBuffer(baseUrl(request))
                .append(request.getContextPath())
                .append(returnToPath).toString();
    }

    /**
     * Dynamically constructs the base URL for the applicaton based on the current request
     *
     * @param request Current servlet request
     * @return Base URL (path to servlet context)
     */
    String baseUrl(HttpServletRequest request) {
        StringBuffer url = new StringBuffer(request.getScheme())
                .append("://").append("staging.easy-insight.com");

        if ((request.getScheme().equalsIgnoreCase("http")
                && request.getServerPort() != 80)
                || (request.getScheme().equalsIgnoreCase("https")
                && request.getServerPort() != 443)) {
            url.append(":").append(request.getServerPort());
        }

        return url.toString();
    }

    /**
     * Map the OpenID response into a user for our app.
     *
     * @param helper Auth response
     * @param request Current servlet request
     * @return User representation
     */
    UserServiceResponse onSuccess(AuthResponseHelper helper, HttpServletRequest request) {
        String email = helper.getAxFetchAttributeValue(Step2.AxSchema.EMAIL);
        String firstName = helper.getAxFetchAttributeValue(Step2.AxSchema.FIRST_NAME);
        String lastName = helper.getAxFetchAttributeValue(Step2.AxSchema.LAST_NAME);

        EIConnection conn =  Database.instance().getConnection();
        Session session = Database.instance().createSession(conn);
        try {
            List<User> users = session.createQuery("from User where email = ?").setString(0, email).list();
            if(users.size() == 1) {
                User user = users.get(0);
                if (user.getAccount().getGoogleDomainName() == null) {
                    UserServiceResponse userServiceResponse = new UserServiceResponse();
                    userServiceResponse.setGoogleAuth(true);
                    request.getSession().setAttribute("googleAppsSetupEmail", email);
                    request.getSession().setAttribute("googleAppsSetupFirstName", firstName);
                    request.getSession().setAttribute("googleAppsSetupLastName", lastName);
                    return userServiceResponse;
                } else {
                    return UserServiceResponse.createResponse(user, session, conn);
                }
            } else {
                UserServiceResponse userServiceResponse = new UserServiceResponse();
                userServiceResponse.setGoogleAuth(true);
                request.getSession().setAttribute("googleAppsSetupEmail", email);
                request.getSession().setAttribute("googleAppsSetupFirstName", firstName);
                request.getSession().setAttribute("googleAppsSetupLastName", lastName);
                return userServiceResponse;
            }
        } catch(Exception e) {
            throw new RuntimeException(e);
        } finally {
            session.close();
            Database.closeConnection(conn);
        }
    }

    /**
     * Handles the case where authentication failed or was canceled.  Just a no-op
     * here.
     *
     * @param helper Auth response
     * @param request Current servlet request
     * @return User representation
     */
    UserServiceResponse onFail(AuthResponseHelper helper, HttpServletRequest request) {
        return null;
    }

    /**
     * Small helper for fetching init params with default values
     *
     * @param key Parameter to fetch
     * @param defaultValue Default value to use if not set in web.xml
     * @return
     */
    protected String getInitParameter(String key, String defaultValue) {
        String value = getInitParameter(key);
        return StringUtils.isBlank(value) ? defaultValue : value;
    }
}
