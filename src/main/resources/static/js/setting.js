$(function () {
    $("#uploadForm").submit(upload);
});

function upload() {
    $.ajax({
        url: "http://upload-z2.qiniup.com",
        method: "post",
        processData: false,
        contentType: false,
        data: new FormData($("#uploadForm")[0]),
        success: function (data) {
            if (data && data.code == 0) {
                // Update header access path
                $.post(
                    CONTENT_PATH + "/user/header/url",
                    { "fileName": $("input[name='key']").val() },
                    function (data) {
                        data = $.parseJSON(data);
                        if (data.code == 0) {
                            window.location.reload();
                        } else {
                            alert(data.msg);
                        }
                    }
                );
            } else {
                alert("Upload Failed!");
            }
        }
    });
    return false;
}