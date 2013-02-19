<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%String url = "http://localhost:8084/Server";%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="shortcut icon" href="../ppc.ico" />
        <link href=<%= url + "/main/style.css"%> rel="stylesheet" type="text/css" />
        <title>Upload - <%=session.getAttribute("username")%> -PPC </title>
    </head>
    <body>
        <div id="top">
            <a id="header" href=<%= url + "/XmlParser"%>><h1>PPC</h1></a>
            <nav id="username">
                <ul>
                    <li><a href="#"><%=session.getAttribute("username")%></a>
                        <ul>
                            <li><a href=<%= url + "/Signout"%>>Sign out</a></li>
                        </ul>
                    </li>
                </ul>
            </nav>
        </div>
        <div id="container">
            <form id="contact_form" action=<%= url + "/Upload"%> method="post" enctype="multipart/form-data">
                <div id="details1" >
                    <div id="names1" >Filename:  </div>
                    <div id="values1"><input type="text" name="filename" form="contact_form" placeholder=".java" ><br></div>
                </div>
                <div id="details1" >
                    <div id="names1" >Comment:   </div>
                    <div id="values1"><input type="text" name="comment" form="contact_form"></div>
                </div>                        
                <input type="hidden" name="username" value="<%=session.getAttribute("username")%>" form="contact_form">
                <div id="details1" > 
                    <div id="names1" > Code area:   </div>
                    <div id="values1">
                        <input type="file" name="file"/>
                    </div>
                </div> 
                <div id="details1" > 
                    <div id="values1">
                        <div id="or" > -OR- </div>
                    </div>
                </div> 
                <div id="details1" >
                    <div id="values1"><div id="upload"><textarea  name="myTextarea" id="myText" form="contact_form" placeholder="Enter your code"></textarea></div></div>
                </div>
                <div id="details1" >
                    <div id="values1">
                        <div id="upload"><b><font color='firebrick'>${message}</font></b></div>
                    </div>
                </div>
                <div id="home"> 
                    <div id="button_ok"><input type="submit" value="OK" id="input"/></div>
                </div>
            </form>
            <div id="cancel">
                <form action=<%= url + "/XmlParser"%> method="get" enctype="multipart/form-data">
                    <div id="button_cancel"><input type="submit" value="CANCEL" id="input"/></div>
                </form>
            </div>  

        </div>
    </body>
</html>