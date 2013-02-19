<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%String url = "http://localhost:8084/Server";%>
<html>
    <head>
        <meta http-equiv="refresh" content="60" />
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <title>Home - <%=session.getAttribute("username")%> - PPC </title>
        <link rel="shortcut icon" href="../ppc.ico" />
        <link href=<%= url + "/main/style.css"%> rel="stylesheet" type="text/css" />
    </head>
    <body>
        <div id="top">
            <a id="header" href=<%= url + "/main/home.jsp"%>><h1>PPC</h1></a> 
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
        <div id="left"> 
            <form action=<%= url + "/main/upload.jsp"%> method="post" enctype="multipart/form-data">
                <div id="button_new"><input type="submit" value="NEW" /></div>
            </form>
        </div>
        <div id="container">
            <%  String[] info = (String[]) session.getAttribute("data");
                String name = null;
                String comment = null;
                String date = null;
                String newflag = null;
                String counter = null;
                String id = null;
                for (int row = 0; row < info.length; row = row + 6) {
                    name = info[row];
                    comment = info[row + 1];
                    date = info[row + 2];
                    newflag = info[row + 3];
                    counter = info[row + 4];
                    id = info[row + 5];
                    if (newflag.equals("true") || (counter.equals("0") == false)) {%>
            <div id="details" style="background-color:whitesmoke;" >
                <a href= <%out.println(url + "/ChangeStatus?row=" + id);%> >
                    <% if (counter.equals("0") == false) {%>
                    <div id="names" style="color:#000000" > <strong><%= name + "(" + counter + ")"%> </strong></div>
                    <% } else {%>
                    <div id="names" style="color:#000000" > <strong><%= name%> </strong></div>
                    <%  }%>
                    <div id="values" style="color:#000000"> <strong><%= comment%> </strong></div>
                    <div id="time" style="color:#000000"> <strong><%= date%> </strong></div>                    
                </a>
            </div>
            <%      } else {%>
            <a href= <%out.println("view.jsp?row=" + id);%> >
                <div id="details" >

                    <div id="names" style="color:#000000"> <%= name%> </div>
                    <div id="values" style="color:#000000"> <%= comment%> </div>
                    <div id="time" style="color:#000000"> <%= date%> </div>

                </div>
            </a>
            <%      }
                }%>
        </div>
    </body>
</html>