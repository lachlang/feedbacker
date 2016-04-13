'use strict';

describe('service [Model]', function() {
	
	var feedback, model;
	var deferred, scope;
	
	beforeEach(module('feedbacker.services'));

	beforeEach(inject(function($q, _Feedback_, _Model_, $rootScope) {
		scope = $rootScope.$new();
		deferred = $q.defer();
		model = _Model_;

		feedback = _Feedback_;
        spyOn(feedback, 'getPendingFeedbackActions').and.returnValue(deferred.promise);

	}));

	it('can get an instantce of itself', function(){
    	expect(model).toBeDefined();
    });
    
    it('has defined functions', function() {
    	expect(angular.isFunction(model.getPendingFeedbackActions)).toBe(true);
    });

    // the mock doesn't appear to be being passed the service implemention :(
    xdescribe('caches data after the first call to server', function() {
    	var result;
    	
    	it('should call the feedback.getPendingFeedbackActions service only once', function() {
    		var httpResponse = "dummy response";

    		model.getPendingFeedbackActions().then(function(data) {
    			result = data;
    		});
    		expect(feedback.getPendingFeedbackActions).toHaveBeenCalled();
    		expect(feedback.getPendingFeedbackActions.calls.count()).toEqual(1);

    		deferred.resolve({data: {data:httpResponse}});
    		scope.$digest();
    		expect(result).toEqual(httpResponse);

    		result = {};
    		model.getPendingFeedbackActions().then(function(data) {
    			result = data;
    		});
    		expect(feedback.getPendingFeedbackActions.calls.count()).toEqual(1);
    		scope.$digest();
    		expect(result).toEqual(httpResponse);
    	});

    });
    
    xdescribe('caches data after the first call to server for multi-tennented data when active representation is set', function() {
    	var result;

    	it('should call the feedback.getPendingFeedbackActions service only once', inject(function($q) {
    		var response = {representationId:10, connections:[{id:2}]}, expectedResult = [{id:2}];

    		model.getPendingFeedbackActions().then(function(data) {
    			result = data;
    		});
    		expect(feedback.getPendingFeedbackActions).toHaveBeenCalled();
    		expect(feedback.getPendingFeedbackActions.calls.count()).toEqual(1);
    		
    		deferred.resolve(response);
    		scope.$digest();
    		expect(result).toEqual(expectedResult);
    		
    		result = {};
    		model.getPendingFeedbackActions().then(function(data) {
    			result = data;
    		});
    		expect(feedback.getPendingFeedbackActions.calls.count()).toEqual(1);
    		scope.$digest();
    		expect(result).toEqual(expectedResult);
    	}));

    });

    xdescribe('flushes cached data when requestsed', function() {
    	var result;

    	it('should call the feedback.getPendingFeedbackActions service when flushed', function(){
    		var response = {data:{data:{person:{id:2, representation:{}, tags:[{id:3}]}}}};
    		var expectedResult = {id:2, representation:{}, tags:[{id:3}]};
    		model.getPendingFeedbackActions().then(function(data) {
    			result = data;
    		});
    		expect(feedback.getPendingFeedbackActions).toHaveBeenCalled();
    		expect(feedback.getPendingFeedbackActions.calls.count()).toEqual(1);

    		deferred.resolve(response);
    		scope.$digest();
    		expect(result).toEqual(expectedResult);

    		result = {};
    		model.getPendingFeedbackActions(true).then(function(data) {
    			result = data;
    		});
    		expect(feedback.getPendingFeedbackActions.calls.count()).toEqual(2);
    		scope.$digest();
    		expect(result).toEqual(expectedResult);
    	});

    });

});