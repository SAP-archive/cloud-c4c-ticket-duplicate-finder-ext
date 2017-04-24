sap.ui.define(["sap/ui/core/UIComponent"
], function(UIComponent) {
    "use strict";

    return UIComponent.extend("com.sap.cloud.c4c.ticket.duplicate.finder.Component", {

        metadata: {
            manifest: "json"
        },

        init: function() {
            UIComponent.prototype.init.apply(this, arguments);
            this.getRouter().initialize();
        }
    });
});