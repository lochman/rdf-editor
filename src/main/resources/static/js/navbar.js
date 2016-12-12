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
window.onpageshow = (event) => {      
    //stop initial loader	
    stopLoader();
};

var processSearch = (event) =>{
	startLoader(1);
	return true;
}