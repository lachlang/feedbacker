'use strict';

describe('model service [Model]', function() {
	
	var feedbackService, modelService;
	var deferred, scope;
	
	beforeEach(module('bebuApp'));

	beforeEach(inject(function($q, _Feedback_, _Model_, $rootScope) {
		scope = $rootScope.$new();
		deferred = $q.defer();
		modelService = _Model_;

		feedbackService = _Feedback_;
		spyOn(feedbackService, 'getPendingFeedbackActions').and.returnValue(deferred.promise);

	}));

	it('can get an instantce of itself', function(){
    	expect(modelService).toBeDefined();
    });
    
    it('has defined functions', function() {
    	expect(angular.isFunction(modelService.getPendingFeedbackActions)).toBe(true);
    });

    describe('caches data after the first call to server', function() {
    	var result;
    	
    	it('should call the feedback.getPendingFeedbackActions service only once', function() {
    		var response = {data:{data:[{id:2, element:"thing"}, {id:6, element:"other"}]}};
    		var expectedResult = {id:2, representation:{id:6}, tags:[{id:3}]};
    		modelService.getPendingFeedbackActions().then(function(data) {
    			result = data;
    		});
    		expect(feedbackService.getPendingFeedbackActions).toHaveBeenCalled();
    		expect(feedbackService.getPendingFeedbackActions.calls.count()).toEqual(1);

    		deferred.resolve(response);
    		scope.$digest();
    		expect(result).toEqual(expectedResult);

    		result = {};
    		modelService.getPendingFeedbackActions().then(function(data) {
    			result = data;
    		});
    		expect(feedbackService.getPendingFeedbackActions.calls.count()).toEqual(1);
    		scope.$digest();
    		expect(result).toEqual(expectedResult);
    	});

    });
    
    describe('caches data after the first call to server for multi-tennented data when active representation is set', function() {
    	var result;

    	it('should call the feedback.getPendingFeedbackActions service only once', inject(function($q) {
    		var response = {representationId:10, connections:[{id:2}]}, expectedResult = [{id:2}];

    		modelService.getPendingFeedbackActions().then(function(data) {
    			result = data;
    		});
    		expect(feedbackService.getPendingFeedbackActions).toHaveBeenCalled();
    		expect(feedbackService.getPendingFeedbackActions.calls.count()).toEqual(1);
    		
    		deferred.resolve(response);
    		scope.$digest();
    		expect(result).toEqual(expectedResult);
    		
    		result = {};
    		modelService.getPendingFeedbackActions().then(function(data) {
    			result = data;
    		});
    		expect(feedbackService.getPendingFeedbackActions.calls.count()).toEqual(1);
    		scope.$digest();
    		expect(result).toEqual(expectedResult);
    	}));

    });

    describe('flushes cached data when requestsed', function() {
    	var result;

    	it('should call the feedback.getPendingFeedbackActions service when flushed', function(){
    		var response = {data:{data:{person:{id:2, representation:{}, tags:[{id:3}]}}}};
    		var expectedResult = {id:2, representation:{}, tags:[{id:3}]};
    		modelService.getPendingFeedbackActions().then(function(data) {
    			result = data;
    		});
    		expect(feedbackService.getPendingFeedbackActions).toHaveBeenCalled();
    		expect(feedbackService.getPendingFeedbackActions.calls.count()).toEqual(1);

    		deferred.resolve(response);
    		scope.$digest();
    		expect(result).toEqual(expectedResult);

    		result = {};
    		modelService.getPendingFeedbackActions(true).then(function(data) {
    			result = data;
    		});
    		expect(feedbackService.getPendingFeedbackActions.calls.count()).toEqual(2);
    		scope.$digest();
    		expect(result).toEqual(expectedResult);
    	});

    });

});