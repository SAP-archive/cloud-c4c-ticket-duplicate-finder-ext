sap.ui.define(["sap/ui/core/UIComponent",
    "sap/ui/core/mvc/Controller",
    "sap/m/MessageBox",
    "sap/ui/model/json/JSONModel"
], function (UIComponent, Controller, MessageBox, JSONModel) {
    "use strict";

    return Controller.extend("com.sap.cloud.c4c.ticket.duplicate.finder.controller.Admin", {
    	
    	onInit: function(){
    		this.loadApiPath();
    		this.loadAllTickets();
    		this.getCurrentUser();
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
    	
    	getCurrentUser : function(){
    		const controller = this;
    		$.ajax({
    			type: "GET",
    			url: "./api/v1/user/current"
    		}).done(function(data){
    			const model = new JSONModel(data);
    			controller.getView().setModel(model, "currentUser");
    		}).fail(function(error){
    			MessageBox.error("Loading all tickets failed.");
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
    	
    	handleLogoffPress : function(){
    		const controller = this;
    		$.ajax({
    			type: "POST",
    			url: "./api/v1/user/logout"
    		}).done(function(data){
    			window.location.href = "./login.html"
    		}).fail(function(error){
    			MessageBox.error("Logout failed.");
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