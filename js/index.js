/*
 * Fileupload handling
 */
!function(e){var t = function(t, n){this.$element = e(t), this.type = this.$element.data("uploadtype") || (this.$element.find(".thumbnail").length > 0?"image":"file"), this.$input = this.$element.find(":file"); if (this.$input.length === 0)return; this.name = this.$input.attr("name") || n.name, this.$hidden = this.$element.find('input[type=hidden][name="' + this.name + '"]'), this.$hidden.length === 0 && (this.$hidden = e('<input type="hidden" />'), this.$element.prepend(this.$hidden)), this.$preview = this.$element.find(".fileupload-preview"); var r = this.$preview.css("height"); this.$preview.css("display") != "inline" && r != "0px" && r != "none" && this.$preview.css("line-height", r), this.original = {exists:this.$element.hasClass("fileupload-exists"), preview:this.$preview.html(), hiddenVal:this.$hidden.val()}, this.$remove = this.$element.find('[data-dismiss="fileupload"]'), this.$element.find('[data-trigger="fileupload"]').on("click.fileupload", e.proxy(this.trigger, this)), this.listen()}; t.prototype = {listen:function(){this.$input.on("change.fileupload", e.proxy(this.change, this)), e(this.$input[0].form).on("reset.fileupload", e.proxy(this.reset, this)), this.$remove && this.$remove.on("click.fileupload", e.proxy(this.clear, this))}, change:function(e, t){if (t === "clear")return; var n = e.target.files !== undefined?e.target.files[0]:e.target.value?{name:e.target.value.replace(/^.+\\/, "")}:null; if (!n){this.clear(); return}this.$hidden.val(""), this.$hidden.attr("name", ""), this.$input.attr("name", this.name); if (this.type === "image" && this.$preview.length > 0 && (typeof n.type != "undefined"?n.type.match("image.*"):n.name.match(/\.(gif|png|jpe?g)$/i)) && typeof FileReader != "undefined"){var r = new FileReader, i = this.$preview, s = this.$element; r.onload = function(e){i.html('<img src="' + e.target.result + '" ' + (i.css("max-height") != "none"?'style="max-height: ' + i.css("max-height") + ';"':"") + " />"), s.addClass("fileupload-exists").removeClass("fileupload-new")}, r.readAsDataURL(n), console.log("if")} else $("button.btn-success").prop("disabled", false), this.$preview.text(n.name), this.$element.addClass("fileupload-exists").removeClass("fileupload-new")}, clear:function(e){this.$hidden.val(""), $("button.btn-success").prop("disabled", true), this.$hidden.attr("name", this.name), this.$input.attr("name", ""); if (navigator.userAgent.match(/msie/i)){var t = this.$input.clone(!0); this.$input.after(t), this.$input.remove(), this.$input = t} else this.$input.val(""); this.$preview.html(""), this.$element.addClass("fileupload-new").removeClass("fileupload-exists"), e && (this.$input.trigger("change", ["clear"]), e.preventDefault())}, reset:function(e){this.clear(), this.$hidden.val(this.original.hiddenVal), this.$preview.html(this.original.preview), this.original.exists?this.$element.addClass("fileupload-exists").removeClass("fileupload-new"):this.$element.addClass("fileupload-new").removeClass("fileupload-exists")}, trigger:function(e){this.$input.trigger("click"), e.preventDefault()}}, e.fn.fileupload = function(n){return this.each(function(){var r = e(this), i = r.data("fileupload"); i || r.data("fileupload", i = new t(this, n)), typeof n == "string" && i[n]()})}, e.fn.fileupload.Constructor = t, e(document).on("click.fileupload.data-api", '[data-provides="fileupload"]', function(t){var n = e(this); if (n.data("fileupload"))return; n.fileupload(n.data()); var r = e(t.target).closest('[data-dismiss="fileupload"],[data-trigger="fileupload"]'); r.length > 0 && (r.trigger("click.fileupload"), t.preventDefault())})}(window.jQuery)
// Check for the various File API support.
if (window.File && window.FileReader && window.FileList && window.Blob) {
	//do your stuff!
	var reader = new FileReader();
	var processFile = (event) =>{
		//event.preventDefault();
		console.log($("#rdf-file")[0].files[0]);
		var rdfFile = $("#rdf-file")[0].files[0];
		if (!rdfFile) {
			alert("Failed to load file");
		}else{
			reader.onload = (e) => {
				var contents = e.target.result;
				alert( "Got the file.n"
					+"name: " + rdfFile.name + "n"
					+"type: " + rdfFile.type + "n"
					+"size: " + rdfFile.size + " bytesn"
					+ "starts with: " + contents.substr(1, contents.indexOf("n"))
				);
			}
			reader.readAsText(rdfFile);
		}
		/*var rader = new FileReader();
		var parser = N3.Parser(), rdfStream = fs.(rdfFile);
		parser.parse(rdfStream, console.log);*/
	}
	var rdfText = "";
	reader.onloadend = (event) => {
		rdfText = event.target.result,
        error = event.target.error;
		if (error) {
			alert("File could not be read! Code " + error.code);
		} else {
			//console.log("Contents: " + rdfText);
			//do next stuff
			parseAndStore(rdfText);
			findObject('http://mre.zcu.cz/id/8508e58d9d99fdd2b978fe6c22a264cfae737b3d', null, null);
		}
	}
} else {
	alert('The File APIs are not fully supported by your browser.');
}
