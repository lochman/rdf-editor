var _lock = 1;	
var startLoader = (lock) =>{
	_lock = lock;
	$(".overlay").show();
}
var stopLoader = () =>{
	_lock--;
	if(_lock < 1){
            $(".overlay").hide();
	}	
}
$(document).ready(() =>{    
    $('[data-toggle="tooltip"]').tooltip(); 
})

var processSave = (event) =>{
    startLoader(1);
    return true;
}

var removeInput = (id) =>{
    element = document.getElementById(id);
    element.type = "hidden";
    element.value = "";
    $("button[id='btn"+id+"']").hide();
    return true;
}

var addInput = (id, type, label, guide) =>{
    //workaround
    guideArr = [];
    if (guide != "null" &&  guide != "[]"){
        guideArr = guide.substring(1,guide.length-1).split(", ");
    }
    i = 0;
    while(true){
        divid = "div"+id+"-"+i;
        if ($("div[id='"+divid+"']").length == 0) break;
        console.log(i++);
    }
    parent = $("div[id='par"+id+"']");
    id = id + "-" +i;

    var html = "<div id=\"div"+id+"\">\n";
    html += "<div class=\"col-sm-11\">\n";
    html += "<input list=\"list"+id+"\" id=\"" + id + "\" name=\""+ id +"\"type=\""+type+"\" class=\"form-control\" value=\"\" placeholder=\""+label+"\"/>\n";
    html += "<datalist id=\"list" + id +"\">\n";
    for (x = 0; x < guideArr.length; x++){
        html += "<option value=\""+guideArr[x]+"\"/>\n"
    }
    html +="</datalist>\n";
    html +="</div>\n";
    html +="<div class=\"col-sm-1\">\n";
    html +="<button id=\"btn"+ id +"\" class=\"btn-xs btn-danger\" type=\"button\" onclick=\"removeInput(\'" + id + "\');\"><span class=\"glyphicon glyphicon-minus\"></span></button>\n";
    html +="</div>\n";
    html +="</div>\n";
    parent.append(html);
    return true;
}


                        