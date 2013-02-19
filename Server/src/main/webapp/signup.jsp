<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="net.tanesha.recaptcha.ReCaptcha" %>
<%@ page import="net.tanesha.recaptcha.ReCaptchaFactory" %><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%String url = "http://localhost:8084/Server";%>
<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1.0"/> 
        <meta name="description" content="Login and Registration Form with HTML5 and CSS3" /> 
        <meta name="keywords" content="html5, css3, form, switch, animation, :target, pseudo-class" /> 
        <meta name="author" content="Codrops" />
        <link rel="shortcut icon" href="ppc.ico" />
        <link href="style.css" rel="stylesheet" type="text/css" />
        <title>PPC: Create an account</title>
    </head>
    <body>        
        <script type="text/javascript">
            var RecaptchaOptions = { theme : 'white' };
        </script>
        <div id="top">
            <a id="header" href=<%= url + "/index.jsp"%>><h1>PPC</h1></a>
            <div id="button_reg"><a href="index.jsp" class="to_register">Sign in</a></div>
        </div>
        <div id="container">
            <section>
                <div id="welcome">
                    <div id="ppc"><h2>Welcome!</h2>
                        <div id="text1">Ping - Pong Computing:</div>
                        <div id="text">Upload </div>
                        <div id="text">Compile  </div>
                        <div id="text">Run </div>
                        <div id="last">your java code on Android devices!</div>
                    </div>
                </div>
                <div id="wrapper_signup">
                    <form  method="post" action="Signup" autocomplete="on">                      
                        <div id="name_pass"><p><label for="usernamesignup" class="uname">Choose your username</label></p></div>
                        <div id="values"><p><input id="usernamesignup" name="user" required="required" type="text" placeholder="mysuperusername690"/></p></div>
                        <div id="name_pass"><p><label for="emailsignup" class="youmail"> Your email</label></p></div>
                        <div id="values"><p><input id="emailsignup" name="email" required="required" type="email" placeholder="mysupermail@mail.com"/></p></div>
                        <div id="name_pass"><p><label for="passwordsignup" class="youpasswd">Create a password </label></p></div>
                        <div id="values"><p><input id="passwordsignup" name="pass" required="required" type="password" placeholder="eg. X8df!90EO"/></p></div>
                        <div id="name_pass"><p><label for="passwordsignup_confirm" class="youpasswd" data-icon="p">Confirm your password </label></p></div>
                        <div id="values"><p><input id="passwordsignup_confirm" name="pass2" required="required" type="password" placeholder="eg. X8df!90EO"/></p></div>
                        <div id="name_pass"><p>Prove you are not a robot</p></div>                                              
                        <div id="sign_up"><input type="submit" value="Sign up"/> </div>
                        <div id="captcha">
                            <% /*
                                 ReCaptcha c = ReCaptchaFactory.newReCaptcha("6LeehNgSAAAAAHnQE0GJGjLn2w8Hbqx_1450PpkX", "6LeehNgSAAAAAP2nv9YmfNprFns5YX_6_rv-svzs ", false);
                                 out.print(c.createRecaptchaHtml(null, null));*/
                            %>
                        </div>
                        <div id="message_reg"><b><font color='firebrick'>${RegisterMessage}</font></b></div>
                    </form>
                </div> 
            </section> 
        </div>
    </body>
</html>