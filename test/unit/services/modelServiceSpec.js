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
        expect(angular.isFunction(model.getCurrentFeedback)).toBe(true);
        expect(angular.isFunction(model.getFeedbackHistory)).toBe(true);
    });

    describe('caches data after the first call to server', function() {
    	
        var cacheTest = function(modelFunction, cachedService) {
            var result, httpResponse = "dummy response";
            modelFunction().then(function(data) {
                result = data;
            });
            expect(cachedService).toHaveBeenCalled();
            expect(cachedService.calls.count()).toEqual(1);

            deferred.resolve({data: {body: httpResponse}});
            scope.$digest();
            expect(result).toEqual(httpResponse);

            result = undefined;
            modelFunction().then(function(data) {
                result = data;
            });
            expect(cachedService.calls.count()).toEqual(1);
            scope.$digest();
            expect(result).toEqual(httpResponse);
        }
    	
        it('should call the feedback.getPendingFeedbackActions service only once', function() {
            cacheTest(model.getPendingFeedbackActions, feedback.getPendingFeedbackActions);
        });

        it('should call the feedback.getCurrentFeedbackItemsForSelf service only once', function() {
            cacheTest(model.getCurrentFeedback, feedback.getCurrentFeedbackItemsForSelf);
        });

        it('should call the feedback.getFeedbackHistoryForSelf service only once', function() {
            cacheTest(model.getFeedbackHistory, feedback.getFeedbackHistoryForSelf);
        });

    });
    
    describe('flushes cached data when requestsed', function() {

        var flushTest = function(modelFunction, cachedService) {
            var httpResponse = "dummy response", result;
            modelFunction().then(function(data) {
                result = data;
            });
            expect(cachedService).toHaveBeenCalled();
            expect(cachedService.calls.count()).toEqual(1);

            deferred.resolve({data: {body: httpResponse}});
            scope.$digest();
            expect(result).toEqual(httpResponse);

            result = undefined;
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