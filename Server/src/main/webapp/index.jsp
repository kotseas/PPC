<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
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
        <title>PPC: Welcome!</title>
    </head>
    <body>        
        <div id="top">
            <a id="header" href=<%= url + "/index.jsp"%>><h1>PPC</h1></a>  
            <div id="new">
                <div id="top_letters">New to PPC?</div>
                <div id="button""><a href="signup.jsp" class="to_register">JUST BE PATIENT :)</a></div>
            </div>
        </div>
        <section>      
            <div id="container">
                <div id="welcome">
                    <div id="ppc"><h2>Welcome!</h2>
                        <div id="text1">Ping - Pong Computing:</div>
                        <div id="text">Upload</div>
                        <div id="text">Compile</div>
                        <div id="text">Run</div>
                        <div id="last">your java code on Android devices!</div>
                    </div>
                </div>
                <div id="wrapper_signin">                   
                    <form   action="signin.jsp" autocomplete="on" method="post" >
                        <div id="sign">Sign in</div> 
                        <div id="name_pass"><p><label for="username" class="uname" data-icon="u" >Username </label></p></div>
                        <div id="values"><p><input id="username" name="user" required="required" type="text" placeholder="myusername"/></p></div>
                        <div id="name_pass"> <p><label for="password" class="youpasswd" data-icon="p">Password </label></p></div>
                        <div id="values"><p><input id="password" name="pass" required="required" type="password" placeholder="eg. X8df!90EO"/></p></div>
                        <div id="message"><b><font color='firebrick'>${LoginMessage}</font></b></div>
                        <div id="login"><input type="submit" value="Sign in" /> </div>
                    </form>
                </div>
            </div>
        </section>       
        </div>
    </body>
</html>