sap.ui.define(["sap/ui/core/UIComponent",
    "sap/ui/core/mvc/Controller",
    "sap/m/MessageBox",
    "sap/ui/model/json/JSONModel"
], function (UIComponent, Controller, MessageBox, JSONModel) {
    "use strict";

    return Controller.extend("com.sap.cloud.c4c.ticket.duplicate.finder.controller.App", {
    	
    	onInit: function(){
    		this.loadApiPath();
    		this.loadAllTickets();
    	},
    	
    	loadApiPath: function(){
    		const controller = this;
    		$.ajax({
    			type: "GET",
    			url: "./api/v1/info/api"
    		}).done(function(data){
    			const modelJson = {
    				apiUrl: data
    			};
    			const model = new JSONModel(modelJson);
    			controller.getView().setModel(model, "info");
    		}).fail(function(error){
    			MessageBox.error("Cound not find api url.");
    		});
    	},
    	
    	loadAllTickets : function(){
    		const controller = this;
    		$.ajax({
    			type: "GET",
    			url: "./api/v1/ticket/all"
    		}).done(function(data){
    			const model = new JSONModel(data);
    			controller.getView().setModel(model, "tickets");
    		}).fail(function(error){
    			MessageBox.error("Loading all tickets failed.");
    		});
    	},
    	
    	fetchTickets: function(){
    		const controller = this;
    		$.ajax({
    			type: "PUT",
    			url: "./api/v1/ticket/fetch"
    		}).done(function(){
    			controller.loadAllTickets();
    		}).fail(function(){
    			MessageBox.error("Triggering fetch tickets failed.");
    		})
    	}
    });
});