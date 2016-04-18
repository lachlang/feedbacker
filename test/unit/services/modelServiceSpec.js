'use strict';

describe('service [Model]', function() {
	
	var feedback, model;
	var deferred, scope;
	
	beforeEach(module('feedbacker.services'));

	beforeEach(inject(function($q, _Model_, _Feedback_, $rootScope) {
		scope = $rootScope.$new();
		deferred = $q.defer();
		model = _Model_;

		feedback = _Feedback_;
        spyOn(feedback, 'getPendingFeedbackActions').and.returnValue(deferred.promise);
        spyOn(feedback, 'getCurrentFeedbackItemsForSelf').and.returnValue(deferred.promise);
        spyOn(feedback, 'getFeedbackHistoryForSelf').and.returnValue(deferred.promise);

	}));

	it('can get an instantce of itself', function(){
    	expect(model).toBeDefined();
    });
    
    it('has defined functions', function() {
    	expect(angular.isFunction(model.getPendingFeedbackActions)).toBe(true);
    });

    // the mock doesn't appear to be being passed the service implemention :(
    describe('caches data after the first call to server', function() {
    	var result;
    	
    	it('should call the feedback.getPendingFeedbackActions service only once', function() {
    		var httpResponse = "dummy response";

    		model.getPendingFeedbackActions().then(function(data) {
    			result = data;
    		});
    		expect(feedback.getPendingFeedbackActions).toHaveBeenCalled();
    		expect(feedback.getPendingFeedbackActions.calls.count()).toEqual(1);

    		deferred.resolve({data: {body: httpResponse}});
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
    
    describe('caches data after the first call to server for multi-tennented data when active representation is set', function() {
    	var result;

    	it('should call the feedback.getPendingFeedbackActions service only once', inject(function($q) {
            var httpResponse = "dummy response";

    		model.getPendingFeedbackActions().then(function(data) {
    			result = data;
    		});
    		expect(feedback.getPendingFeedbackActions).toHaveBeenCalled();
    		expect(feedback.getPendingFeedbackActions.calls.count()).toEqual(1);
    		
    		deferred.resolve({data:{body: httpResponse}});
    		scope.$digest();
    		expect(result).toEqual(httpResponse);
    		
    		result = {};
    		model.getPendingFeedbackActions().then(function(data) {
    			result = data;
    		});
    		expect(feedback.getPendingFeedbackActions.calls.count()).toEqual(1);
    		scope.$digest();
    		expect(result).toEqual(httpResponse);
    	}));

    });

    describe('flushes cached data when requestsed', function() {
    	var result;

        var flushTest = function(modelFunction, cachedService) {
            var httpResponse = "dummy response";
            modelFunction().then(function(data) {
                result = data;
            });
            expect(cachedService).toHaveBeenCalled();
            expect(cachedService.calls.count()).toEqual(1);

            deferred.resolve({data: {body: httpResponse}});
            scope.$digest();
            expect(result).toEqual(httpResponse);

            result = {};
            modelFunction(true).then(function(data) {
                result = data;
            });
            expect(cachedService.calls.count()).toEqual(2);
            scope.$digest();
            expect(result).toEqual(httpResponse);
        }

        it('should call the feedback.getPendingFeedbackActions service when flushed', function(){
            flushTest(model.getPendingFeedbackActions, feedback.getPendingFeedbackActions);
        });

        it('should call the feedback.getCurrentFeedbackItemsForSelf service when flushed', function(){
            flushTest(model.getCurrentFeedback, feedback.getCurrentFeedbackItemsForSelf);
        });

        it('should call the feedback.getFeedbackHistoryForSelf service when flushed', function(){
            flushTest(model.getFeedbackHistory, feedback.getFeedbackHistoryForSelf);
        });

    });

});