function showValue(newValue){
    $("#range-value").html(newValue);
}

function showStatus(value){
    clearStatus();
    var $status = $("<div>").attr("class", "index-process").html(value);
    $("#status").html($status);
}

function clearStatus(){
    $("#status").html("");
}

$(document).on("click", "#view-more", function(event) {
    event.preventDefault();
    searchAjax();
});

function searchAjax() {

    $.ajax({
        type : "POST",
        contentType : "application/json",
        url : "/ajax",
        data : getStart(),
        dataType : 'json',
        timeout : 100000,
        success : function(results) {
            display(results);
            setStart(results.length);
        },
        error : function(e) {
            hideViewMore();
            console.log("AJAX ERROR: ", e);
        }
    });
}

function display(results) {
    var $div = $("<div>").appendTo($("#result-list"));
    $.each(results, function(index, doc) {
        var $h3 = $("<h3>").attr("class","page-title").appendTo($div);
        $("<a>").attr("href",doc.path).text(doc.title).appendTo($h3);
        $("<div>").attr("class","page-path").text(doc.path).appendTo($div);
        $("<div>").attr("class","page-fragment").html(doc.fragment).appendTo($div);
        $("<br>").appendTo($div);
    });
}

function hideViewMore() {
    $("#more-results-form").hide();
}

function getStart() {
    return $("#param-start").val();
}

function setStart(value) {
    var len = parseInt(getStart()) + parseInt(value);
    $("#param-start").val(len);
    if (len >= resultCount) {
        hideViewMore();
    }
}