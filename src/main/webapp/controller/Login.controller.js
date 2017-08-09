sap.ui.define(["sap/ui/core/UIComponent",
    "sap/ui/core/mvc/Controller"
], function (UIComponent, Controller) {
    "use strict";

    return Controller.extend("com.sap.cloud.c4c.ticket.duplicate.finder.controller.Login", {

    	handleLogOn: function() {
    		window.location.href = "./"
		}
    });
});
