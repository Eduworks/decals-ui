
/***************End From decals-ui.js********************/

function boxedCustomAppJavascript() {       
    $(document).foundation();
    
    $('#modalRegister').bind('close', function() {
    	$('#registrationErrorContainer').hide();
   	});
    
    $('#modalLogin').bind('close', function() {
    	$('#loginErrorContainer').hide();
   	});
    
    //FROM decals-ui.js
    // Show more details when link is clicked
    $('.infoBox').on('click','.link',function() {
        $(this).toggleClass('collapsed expanded');
        $(this).siblings('.moreInfo').slideToggle();
    });

//    $('#searchTypes').on('click','li#interactivTab, li#basicTab', function() {
//        var tab = $(this);
//        if (tab.hasClass("active")) {
//            // do nothing
//        } else {
//            hideHeaderSearch();
//            if (tab.is("#interactivTab")) {
//                showScreen('activeSearch');
//                $('input#interactiveSearchInput').focus();
//            } else {
//                showScreen('basicSearch'); 
//                $('input#basicSearchInput').focus();
//            }
//        }
//    });

    // tabs in header toggle search screens
    $('.tabs').on('click','.tab',function() {
        $(this).siblings('.tab').removeClass('active');
        $(this).addClass('active');
    });

    /* SEARCH */

    // check search input for typing
//    $('input#interactiveSearchHeaderInput, input#interactiveSearchInput').each(function () {
//        var elem = $(this);
//        // Save current value of element
//        elem.data('oldVal', elem.val());
//        // Look for changes in the value
//        elem.bind("propertychange keyup input paste", function (event) {
//            // transfer typing from home search to header search
//            if (elem.attr("id") == "interactiveSearchInput") {
//                showHeaderSearch();
//                $('input#interactiveSearchHeaderInput').val(elem.val());
//                $('input#interactiveSearchHeaderInput').focus();
//            }
//            // If value has changed...
//            if (elem.data('oldVal') != elem.val()) {
//                // Updated stored value
//                elem.data('oldVal', elem.val());
//                
//                // Take action while typing
//                if (elem.val().length > 0) {
//                    showScreen('interactiveSearchTyping');
//                }
//            }
//        });
//    });
    
    // Show interactive search results
//    $('input#interactiveSearchHeaderInput').keydown(function(e) {
//    	var code = e.charCode ? e.charCode : e.keyCode ? e.keyCode : 0;    	
//    	 if(code == 13) { 
//    		 showScreen('interactiveSearchResultsScreen');
//    	     $(this).blur();
//    	     showHeaderSearch();
//    	     $('input#interactiveSearchInput, input#basicSearchInput').val($(this).val());
//    	 }        
//        //$('input#interactiveSearchHeaderInput').on("keydown", function (e) { ac(); });
//    });

    // Show basic scearch results
    //commented out TB 06/23/2014
    //$('input#search-basic').change(function() {
    //	var query = $(this).val();
    //    query=query.replace(" ","%20");
    //    window.open('http://free.ed.gov/?page_id=6&query='+query+'&type=index')
    //});

    
}
