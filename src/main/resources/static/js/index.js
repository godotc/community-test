$(function () {
	$("#publishBtn").click(publish);
});

function publish() {
	$("#publishModal").modal("hide");

	//  Setting CSRF authorization in request Header Before send AJAX request.
//	var token=$("meta[name='_csrf']").attr("content");
//	var header=$("meta[name='_csrf_header']").attr("content");
//	$(document).ajaxSend(function(e,xhr,options){
//	    xhr.setRequestHeader(header,token);
//	});

	// Get title and content
	var title = $("#recipient-name").val();
	var content = $("#message-text").val();

	// Send asynchronous request (POST)
	$.post(
		CONTEXT_PATH + "/discuss/add",
		{ "title": title, "content": content },
		function (data) {
			data = $.parseJSON(data);
			// Display return msg in Prompt Frame

			// update msg
			$("#hintBody").text(data.msg);
			// Display
			$("#hintModal").modal("show");
			// auto hide after 2s
			setTimeout(function () {
				$("#hintModal").modal("hide");
				// Refresh page
				if (data.code == 0) {
					window.location.reload();
				}
			}, 2000);
		}
	)

}