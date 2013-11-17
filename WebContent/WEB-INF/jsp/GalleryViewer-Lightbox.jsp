<%@page import="walla.datatypes.auto.*" pageEncoding="UTF-8"%>
<%@page import="walla.datatypes.java.*"%>
<%@page import="walla.business.*"%>
<%@page import="java.util.*"%>


<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>

    <title>FotoWalla - Viewer</title>

    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta http-equiv="content-script-type" content="text/javascript" />
    <meta name='description' content='FotoWalla - your trusted online foto gallery.' />
    <meta name='apple-mobile-web-app-title' content='FotoWalla' />

    <!-- Ensure that on IE the page is rendered to the maximum IE spec. -->
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />

    <!-- Used for viewing on mobile devices.  Ensuring the width is correctly infered -->

    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <!-- Adding javascript to enable CSS style for semantic tags pre IE9 -->
    <!--[if lt IE 9]>
         <script src="../../static/scripts/html5-format-ie8.js"></script>
    <![endif]-->

    <link rel="stylesheet" href="../../../../static/css/blueimp/blueimp-gallery-indicator.css" />
    <link rel="stylesheet" href="../../../../static/css/blueimp/blueimp-gallery.css" />
    <link href="../../../../static/css/custom-${css}/ThemeExt-Lightbox.css" rel="stylesheet" />

    <!-- Browser bar icon (16x16) -->
    <link href="../../static/images/fotowallabrowser.png" rel="shortcut icon" type="image/vnd.microsoft.icon" />

</head>


<body id="galleryBody" data-images-fetchsize="1000" data-groupings-type="${groupingType}" data-total-image-count="${totalImageCount}">
        <header id="pageHeader" class="HeaderStyle">
        <h1><img src="../../../../static/images/fotowallabrowser.png" height="16" width="16" />${name}</h1>
        <span class="HeaderStyle">${desc}</span>
    </header>

    <div style="clear: both;"></div>

    <div id="blueimp-gallery" class="blueimp-gallery">
        <div class="slides"></div>
        <h3 class="title"></h3>
        <a class="prev">‹</a>
        <a class="next">›</a>
        <a class="close">×</a>
        <a class="play-pause"></a>
        <ol class="indicator"></ol>
    </div>

	<div id="links">
	
<% 
	int thumbHeightWidth = 50;
	int mainImageWidth = 1920;
	int mainImageHeight = 1080;

	ImageList imageList = (ImageList)request.getAttribute("imageList");

	if (imageList.getImages() != null)
	{
		if (imageList.getImages().getImageRef().size() > 0)
		{
			//Construct update SQL statements
			for (Iterator<ImageList.Images.ImageRef> imageIterater = imageList.getImages().getImageRef().iterator(); imageIterater.hasNext();)
			{
				ImageList.Images.ImageRef current = (ImageList.Images.ImageRef)imageIterater.next();
				
				String imageMainPath = "../../../ws/" + (String)request.getAttribute("userName") + "/image/" + current.getId() + "/" + mainImageWidth + "/" + mainImageHeight + "\"/";
				String imageThumbPath = "../../../ws/" + (String)request.getAttribute("userName") + "/image/" + current.getId() + "/" + thumbHeightWidth + "/" + thumbHeightWidth + "\"/";
				String output = "<a href=\"" + imageMainPath + "\" title=\"" + current.getName() + "\"><img src=\"" + imageThumbPath + "\" title=\"" + current.getName() + "\"/></a>";
				%><%=output%><%
			}
		}
	}
%>
    </div>

    <script src="//ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>

    <script src="../../../../static/scripts/blueimp/blueimp-gallery.min.js"></script>
    <!-- 
    <script src="../../../../static/scripts/blueimp/blueimp-gallery-fullscreen.js"></script>
    <script src="../../../../static/scripts/blueimp/blueimp-gallery-indicator.js"></script>-->

    <script>
        document.getElementById("links").onclick = function (event) {
            event = event || window.event;
            var target = event.target || event.srcElement,
                link = target.src ? target.parentNode : target,
                options = { index: link, event: event },
                links = this.getElementsByTagName('a');
            blueimp.Gallery(links, options);
        };
    </script>

</body>
   

</html>
