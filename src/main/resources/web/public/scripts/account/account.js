function addUpdatePlugin() {
    var bodyRaw = {
        "name": document.getElementById("plugin-name").value,
        "version": document.getElementById("plugin-version").value,
        "main_page": document.getElementById("plugin-main-page").value,
        "download_link": document.getElementById("plugin-download-link").value
    };

    $.ajax({
        url: "/api/v1/plugin/add",
        headers: {
            "Content-Type": "application/json"
        },
        method: "POST",
        dataType: "json",
        data: JSON.stringify(bodyRaw),
        success: function (jqXHR, textStatus) {
            showSnackbar(jqXHR.responseText);
            document.getElementById("plugin-add-form").reset();
        },
        error: function (jqXHR, textStatus, errorThrown) {
            showSnackbar(jqXHR.responseText);
        }
    });
}